package com.book.agent;

import com.book.entity.Book;

import java.util.List;

/**
 * Prompt 构造器：把「检索到的图书事实」组装成给大模型的指令。
 *
 * <h3>为什么 Prompt 要写得这么"硬"</h3>
 * <p>大模型的默认行为是"尽可能给出有帮助的回答"，这意味着即使它不知道，
 * 也可能顺着你的问题编一个看起来合理的答案（幻觉）。在图书查询场景下，
 * 编造一本不存在的书会让用户白跑一趟图书馆。</p>
 *
 * <p>因此本类在 System Prompt 中明确约束：</p>
 * <ol>
 *   <li><b>只能</b>依据下方"馆藏资料"作答，不得使用模型自身知识补充书目；</li>
 *   <li>资料中没有的书，必须明确说"本馆暂未收录"，<b>禁止虚构</b>书名、作者、ISBN、库存；</li>
 *   <li>库存 / 价格 / 位置等数字必须与资料完全一致，不得估算；</li>
 *   <li>资料为空时，统一回复固定话术，不再自由发挥。</li>
 * </ol>
 */
public final class PromptBuilder {

    /** System 角色：定义助手身份与硬性规则 */
    private static final String SYSTEM_PROMPT =
            "你是「校园图书管理系统」的智能图书助手。\n"
          + "\n"
          + "【最高优先级规则，必须严格遵守】\n"
          + "1. 你只能依据下文【馆藏资料】中出现的图书信息回答问题。\n"
          + "2. 严禁编造任何馆藏资料中不存在的书籍、作者、ISBN、价格、库存、存放位置。\n"
          + "3. 如果馆藏资料中没有用户要找的书，必须明确回答「本馆暂未收录这本书或相关图书」，"
          + "并可以建议用户更换关键词或查看相近类别。\n"
          + "4. 涉及库存、价格、位置、出版信息时，必须逐字引用馆藏资料中的数值，不得估算或改写。\n"
          + "5. 不要输出你在训练数据中知道的书籍信息，即使你确信它存在。\n"
          + "\n"
          + "【回答风格】\n"
          + "- 使用简体中文，语气友好、简洁。\n"
          + "- 推荐图书时用列表呈现：书名 / 作者 / 可借数量 / 存放位置。\n"
          + "- 最多推荐 5 本，优先推荐可借数量大于 0 的。\n"
          + "- 不建议用户去购买或到其他平台查找，只回答本馆情况。";

    /** 资料为空时的固定话术，避免模型自由发挥 */
    private static final String NO_DATA_INSTRUCTION =
            "【重要】本次检索没有在馆藏数据库中找到任何相关图书。"
          + "请直接回答：「抱歉，本馆暂未收录与您问题相关的图书。"
          + "您可以尝试换一个书名、作者或类别关键词再问一次。」"
          + "不要编造任何书目。";

    private PromptBuilder() {
    }

    /** 返回 System Prompt */
    public static String systemPrompt() {
        return SYSTEM_PROMPT;
    }

    /**
     * 构造 User Prompt：检索到的图书事实 + 用户问题。
     *
     * <p><b>注意：</b>当检索结果为空时，直接把用户问题也一并省略，
     * 避免模型把问题里提到的书名误当成"馆藏中存在的书"，
     * 从而绕过防幻觉约束。此时统一返回固定话术。</p>
     *
     * @param books    检索结果（可为空）
     * @param question 用户原问题
     */
    public static String userPrompt(List<Book> books, String question) {
        StringBuilder sb = new StringBuilder(1024);

        if (books == null || books.isEmpty()) {
            // 检索为空：只给固定指令，不把问题原文传给模型，杜绝"顺着问句编书"
            sb.append(NO_DATA_INSTRUCTION);
            return sb.toString();
        }

        sb.append("【馆藏资料】（以下是从本馆数据库中检索到的真实记录，共 ")
          .append(books.size()).append(" 条）\n");
        int index = 1;
        for (Book b : books) {
            sb.append(index++).append(". ");
            sb.append("书名：").append(nullSafe(b.getBname()));
            sb.append("；作者：").append(nullSafe(b.getAuthor()));
            sb.append("；出版社：").append(nullSafe(b.getPublisher()));
            sb.append("；图书编号：").append(nullSafe(b.getBid()));
            sb.append("；ISBN：").append(nullSafe(b.getIsbn()));
            sb.append("；分类：").append(nullSafe(b.getCategoryName()));
            if (b.getPrice() != null && b.getPrice() > 0) {
                sb.append("；价格：").append(b.getPrice()).append("元");
            }
            sb.append("；总库存：").append(b.getStock() == null ? 0 : b.getStock()).append("本");
            sb.append("；可借：").append(b.getAvailableStock() == null ? 0 : b.getAvailableStock()).append("本");
            if (b.getLocation() != null && !b.getLocation().isEmpty()) {
                sb.append("；存放位置：").append(b.getLocation());
            }
            if (b.getPublishDate() != null) {
                sb.append("；出版日期：").append(formatDate(b));
            }
            sb.append("\n");
        }
        sb.append("\n请仅依据以上馆藏资料回答，不要引入资料之外的任何图书。\n\n");

        sb.append("【用户问题】\n").append(question == null ? "" : question.trim());
        return sb.toString();
    }

    private static String nullSafe(String s) {
        return (s == null || s.isEmpty()) ? "未填写" : s;
    }

    private static String formatDate(Book b) {
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd").format(b.getPublishDate());
        } catch (Exception e) {
            return "";
        }
    }
}