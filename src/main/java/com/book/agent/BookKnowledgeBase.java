package com.book.agent;

import com.book.dao.BookDao;
import com.book.entity.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * 轻量 RAG 的「检索」环节（Retrieval）。
 *
 * <h3>RAG 是什么</h3>
 * <p>RAG = Retrieval-Augmented Generation（检索增强生成）。
 * 大模型本身并不知道你数据库里有哪些书，如果直接问它"你们图书馆有《三体》吗"，
 * 它很可能编造一个答案。RAG 的做法是：</p>
 * <ol>
 *   <li><b>Retrieve</b>：先从你自己的数据源（这里是 MySQL 的 books 表）检索出相关记录；</li>
 *   <li><b>Augment</b>：把这些记录拼进 Prompt，作为"事实依据"交给大模型；</li>
 *   <li><b>Generate</b>：要求大模型<b>只能</b>基于这些事实回答，查不到就如实告知。</li>
 * </ol>
 *
 * <h3>为什么不用向量检索</h3>
 * <p>真正的 RAG 通常包含"文本向量化 + 向量库相似度检索"。本项目是轻量实现：
 * 用<b>关键词切分 + SQL LIKE 多字段匹配</b>来近似"语义检索"。
 * 好处是零额外依赖、零额外成本、结果可解释；缺点是同义词泛化能力弱
 * （搜"三体"能命中，搜"科幻小说"未必命中）。
 * 对图书条数在几千本以内的项目完全够用。</p>
 *
 * <h3>检索策略</h3>
 * <ol>
 *   <li><b>关键词提取</b>：把用户问题按标点与停用词切分，得到候选关键词；</li>
 *   <li><b>多字段 OR 匹配</b>：书名 / 作者 / 出版社 / 分类名 / ISBN / 图书编号
 *       任一包含关键词即命中；</li>
 *   <li><b>排序</b>：书名精确匹配优先，其次按可借库存降序；</li>
 *   <li><b>截断</b>：最多取 {@link AgentConfig#getTopK()} 条，控制 Prompt 长度与成本。</li>
 * </ol>
 */
public class BookKnowledgeBase {

    /** 常见无意义词，切词后丢弃，避免污染检索条件。
     *
     * <p><b>只有多字停用词</b>才用于切分片段——因为单字（如"在""不""有"）
     * 出现在正常词汇内部时切分会破坏词义，例如"不存在"会被切成空、"图书馆"会被切断。
     * 单字停用词只在 {@link #stripEdges} 中做词首/词尾剥离。</p>
     */
    private static final String[] STOP_WORDS = {
            // 多字停用词（可安全作为切分边界）
            "什么", "怎么", "怎样", "如何", "请问", "帮我", "我想", "想看", "想找",
            "有没有", "是不是", "能不能", "可不可以", "有哪些", "哪些", "哪个", "哪里",
            "推荐", "介绍", "关于", "相关", "类似", "最好", "比较", "一下", "一些",
            "几本", "本馆", "图书馆", "馆内", "馆藏", "还有", "现有", "现藏", "是否",
            "可以", "能够", "我们", "你们", "他们", "这个", "那个", "这些", "那些",
            "请问一下", "麻烦", "辛苦"
    };

    /**
     * 单字停用词：<b>只在片段首尾剥离</b>，不参与内部切分。
     *
     * <p>这里刻意<b>不包含</b>「不、有、是、在、和」等字：它们出现在词首时
     * 往往是正常词汇的一部分（"不存在"、"有机化学"、"物理学"、"在线"），
     * 剥掉会破坏词义。只保留纯语气 / 助词。 </p>
     */
    private static final String EDGE_STOP_CHARS = "的了吗呢吧啊我你他她它们这那哪";

    /**
     * 中缀分隔字：这些字在中文里永远不作为词的一部分，出现在哪里都可以安全切开。
     *
     * <p>例如"计算机类的图书" → ["计算机", "图"]、"刘慈欣的书" → ["刘慈欣"]、
     * "不存在的书" → ["不存在"]。</p>
     */
    private static final String INFIX_SEPARATORS = "的了吗呢吧啊";

    /** 单个关键词最大长度，过长的按子串截断 */
    private static final int MAX_KEYWORD_LENGTH = 20;
    /** 最多保留几个关键词，避免 SQL 条件过多 */
    private static final int MAX_KEYWORDS = 5;
    /** 有效关键词的最短长度：2 个汉字（"三体"、"史记"） */
    private static final int MIN_KEYWORD_LENGTH = 2;

    private final BookDao bookDao = new BookDao();

    /**
     * 根据用户问题检索相关图书。
     *
     * @param question 用户原始问题
     * @param topK     最多返回条数
     * @return 相关图书列表，可能为空（表示库里没有相关图书）
     */
    public List<Book> retrieve(String question, int topK) {
        if (question == null || question.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> keywords = extractKeywords(question);
        int limit = topK <= 0 ? 8 : Math.min(topK, 20);

        if (keywords.isEmpty()) {
            // 没有有效关键词（如只问了"你好"），给出有库存的图书作为选读建议。
            // 注意：这是"推荐"而非"检索命中"，上层据此决定话术。
            return bookDao.recommendBest(limit);
        }
        return bookDao.searchForAgent(keywords, limit);
    }

    /**
     * 判断问题是否包含可用于检索的关键词。
     *
     * <p>用于区分两种"结果为空"：</p>
     * <ul>
     *   <li>没有关键词（"你好"）→ 不应报"未收录"，而应给推荐或引导；</li>
     *   <li>有关键词但无命中（"有《不存在的书》吗"）→ 明确告知未收录。</li>
     * </ul>
     */
    public boolean hasKeywords(String question) {
        return !extractKeywords(question).isEmpty();
    }

    /**
     * 从问题中提取检索关键词。
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>把标点 / 空白统一替换为空格，得到若干片段；</li>
     *   <li>对每个片段，反复剥离其内部的停用词（"刘慈欣的书有哪些" → "刘慈欣"）；</li>
     *   <li>丢弃长度小于 2 的残留片段（如单个"书"、"的"）；</li>
     *   <li>去重并截断到 {@link #MAX_KEYWORDS} 个。</li>
     * </ol>
     *
     * <p>示例：</p>
     * <pre>
     *   有《三体》吗？        → [三体]
     *   推荐几本计算机类的图书 → [计算机]
     *   刘慈欣的书有哪些？     → [刘慈欣]
     *   你好                 → []            （无有效关键词，走兜底推荐）
     * </pre>
     */
    public static List<String> extractKeywords(String question) {
        List<String> result = new ArrayList<>();
        if (question == null) {
            return result;
        }

        // 1. 标点 / 空白统一成分隔符
        String normalized = question.replaceAll("[\\p{Punct}\\s，。！？、；：“”‘’（）《》【】…—～]+", " ");

        // 2. 把每个片段按"停用词"切成若干块：停用词当作分隔符而不是删除
        //    这样"刘慈欣的书有哪些" → ["刘慈欣"] + []（"的书有哪些"全是停用词）
        //        "推荐几本计算机类的图书" → ["计算机"]
        for (String segment : normalized.split("\\s+")) {
            for (String chunk : splitByStopWords(segment)) {
                // 再按"中缀分隔字"切开："计算机类的图书" → ["计算机", "图"]
                for (String piece : splitByInfix(chunk)) {
                    String word = stripEdges(piece.trim());
                    if (word.length() < MIN_KEYWORD_LENGTH) {
                        continue;
                    }
                    if (word.length() > MAX_KEYWORD_LENGTH) {
                        word = word.substring(0, MAX_KEYWORD_LENGTH);
                    }
                    if (!result.contains(word)) {
                        result.add(word);
                    }
                    if (result.size() >= MAX_KEYWORDS) {
                        return result;
                    }
                }
            }
        }
        return result;
    }

    /** 按中缀分隔字切开片段，丢弃空串 */
    private static List<String> splitByInfix(String chunk) {
        List<String> pieces = new ArrayList<>();
        if (chunk == null || chunk.isEmpty()) {
            return pieces;
        }
        int from = 0;
        for (int i = 0; i < chunk.length(); i++) {
            if (INFIX_SEPARATORS.indexOf(chunk.charAt(i)) >= 0) {
                if (i > from) {
                    pieces.add(chunk.substring(from, i));
                }
                from = i + 1;
            }
        }
        if (from < chunk.length()) {
            pieces.add(chunk.substring(from));
        }
        return pieces;
    }

    /**
     * 剥离片段首尾的单字停用词。
     *
     * <p>只在首尾剥离，<b>不动中间</b>——这样才能保住"不存在""图书馆""计算机"
     * 这类正常词汇的完整性。</p>
     */
    private static String stripEdges(String word) {
        if (word == null) {
            return "";
        }
        int begin = 0;
        int end = word.length();
        while (begin < end && EDGE_STOP_CHARS.indexOf(word.charAt(begin)) >= 0) {
            begin++;
        }
        while (end > begin && EDGE_STOP_CHARS.indexOf(word.charAt(end - 1)) >= 0) {
            end--;
        }
        return word.substring(begin, end);
    }

    /**
     * 以停用词为边界切分片段。
     *
     * <p>实现方式：在片段中所有停用词出现的位置插入分隔符，再按分隔符切开。
     * 停用词按长度从长到短处理，避免"有没有"被"有"抢先切开。</p>
     */
    private static List<String> splitByStopWords(String segment) {
        List<String> chunks = new ArrayList<>();
        if (segment == null || segment.isEmpty()) {
            return chunks;
        }

        // 构造正则：把所有停用词用 | 连接，长词在前
        StringBuilder regex = new StringBuilder();
        List<String> sorted = new ArrayList<>();
        for (String s : STOP_WORDS) {
            if (s != null && !s.isEmpty()) {
                sorted.add(s);
            }
        }
        // 长词优先
        sorted.sort((a, b) -> b.length() - a.length());
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) {
                regex.append('|');
            }
            regex.append(java.util.regex.Pattern.quote(sorted.get(i)));
        }

        String[] parts = segment.split(regex.toString());
        for (String part : parts) {
            String p = part.trim();
            if (!p.isEmpty()) {
                chunks.add(p);
            }
        }
        return chunks;
    }
}