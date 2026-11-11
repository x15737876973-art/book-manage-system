package com.book.agent;

import com.book.dao.BookDao;
import com.book.entity.Book;
import com.book.util.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 智能图书问答 Servlet（RAG 流程入口）。
 *
 * <h3>完整链路</h3>
 * <pre>
 *   用户提问
 *      ↓
 *   ① 参数校验（长度、非空）
 *      ↓
 *   ② 检索：BookKnowledgeBase 切关键词 → JDBC 查 MySQL books 表  ← 轻量 RAG 的 R
 *      ↓
 *   ③ 组装：PromptBuilder 把图书事实写进 System/User Prompt      ← 轻量 RAG 的 A
 *      ↓
 *   ④ 生成：DeepSeekClient 调用 DeepSeek API                     ← 轻量 RAG 的 G
 *      ↓
 *   ⑤ 返回：JSON（含回答 + 命中的图书列表，供页面渲染书目卡片）
 * </pre>
 *
 * <h3>容错设计</h3>
 * <ul>
 *   <li><b>未配置 API Key</b>：自动降级为"本地检索模式"，
 *       直接把数据库命中结果整理成回答，保证功能可演示；</li>
 *   <li><b>调用失败</b>（超时、限流、余额不足等）：同样降级为本地检索，
 *       并在返回中标注降级原因，不向用户抛 500；</li>
 *   <li><b>检索为空</b>：明确告知"本馆暂未收录"，不调用模型编造。</li>
 * </ul>
 *
 * <p>请求方式：POST，参数 {@code question}。
 * 返回 {@code application/json}，结构见 {@link #writeJson}。</p>
 */
@WebServlet("/agentQueryServlet")
public class AgentQueryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** 问题最大长度，防止恶意超长输入消耗 token */
    private static final int MAX_QUESTION_LENGTH = 200;

    private final BookDao bookDao = new BookDao();
    private final BookKnowledgeBase knowledgeBase = new BookKnowledgeBase();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 直接访问 GET 时跳回问答页面
        response.sendRedirect(request.getContextPath() + "/agent.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        try {
            // ---------- ① 参数校验 ----------
            String question = ServletUtils.getTrimmed(request, "question");
            if (question == null) {
                writeJson(response, false, "请输入您的问题", null, null, null);
                return;
            }
            if (question.length() > MAX_QUESTION_LENGTH) {
                writeJson(response, false,
                        "问题太长了，请控制在 " + MAX_QUESTION_LENGTH + " 字以内", null, null, null);
                return;
            }

            // ---------- ② 检索 MySQL 图书表 ----------
            AgentConfig cfg = AgentConfig.getInstance();
            List<Book> books = knowledgeBase.retrieve(question, cfg.getTopK());

            // 检索为空：直接如实告知，不调用模型（避免模型编造）
            if (books.isEmpty()) {
                String answer = "抱歉，本馆暂未收录与「" + question + "」相关的图书。"
                        + "您可以尝试换一个书名、作者或类别关键词再问一次。";
                writeJson(response, true, answer, books, "local", "no_result");
                return;
            }

            // ---------- ③④ 组装 Prompt 并调用大模型 ----------
            String answer;
            String mode;
            String note = null;

            DeepSeekClient client = new DeepSeekClient(cfg);
            if (!client.isAvailable()) {
                // 未配置 Key：降级为本地检索模式
                answer = buildLocalAnswer(question, books);
                mode = "local";
                note = "未配置 DeepSeek API Key，当前为本地检索模式（仅展示数据库命中的图书）";
            } else {
                try {
                    answer = client.askWithBooks(books, question);
                    mode = "deepseek";
                } catch (DeepSeekClient.AgentException e) {
                    // 调用失败：降级，但仍给出可用结果
                    getServletContext().log("调用 DeepSeek 失败，降级为本地检索：" + e.getMessage(), e);
                    answer = buildLocalAnswer(question, books);
                    mode = "local";
                    note = "调用大模型失败（" + e.getMessage() + "），已为您展示数据库检索结果";
                }
            }

            // ---------- ⑤ 返回 ----------
            writeJson(response, true, answer, books, mode, note);

        } catch (Exception e) {
            getServletContext().log("智能问答处理失败", e);
            writeJson(response, false, "抱歉，服务暂时不可用，请稍后重试", null, null, null);
        }
    }

    // ======================================================================
    // 本地检索模式的回答构造
    // ======================================================================

    /**
     * 不依赖大模型时，把检索结果整理成可读回答。
     *
     * <p>这样即使没配 Key（或 Key 失效、断网），页面也能给出有用的结果。</p>
     */
    private String buildLocalAnswer(String question, List<Book> books) {
        StringBuilder sb = new StringBuilder();
        sb.append("根据馆藏数据库检索，与「").append(question).append("」相关的图书共 ")
          .append(books.size()).append(" 本：\n\n");

        int index = 1;
        for (Book b : books) {
            if (index > 5) {
                break;
            }
            int available = b.getAvailableStock() == null ? 0 : b.getAvailableStock();
            sb.append(index++).append(". 《").append(b.getBname()).append("》");
            sb.append("　作者：").append(b.getAuthor() == null ? "未填写" : b.getAuthor());
            sb.append("　可借：").append(available).append(" 本");
            if (b.getLocation() != null && !b.getLocation().isEmpty()) {
                sb.append("　位置：").append(b.getLocation());
            }
            if (available <= 0) {
                sb.append("（当前已借完，可预约）");
            }
            sb.append("\n");
        }
        if (books.size() > 5) {
            sb.append("\n还有 ").append(books.size() - 5).append(" 本相关图书，可到「图书检索」页查看完整列表。");
        }
        return sb.toString();
    }

    // ======================================================================
    // JSON 输出
    // ======================================================================

    /**
     * 输出 JSON。
     *
     * <pre>
     *   {
     *     "success": true,
     *     "answer":  "回答正文（纯文本，前端用 textContent 渲染防 XSS）",
     *     "mode":    "deepseek" | "local",
     *     "note":    "降级原因说明，可为 null",
     *     "books":   [ {"bid":"B001","bname":"红楼梦","author":"曹雪芹",
     *                   "available":5,"location":"B区-1排-1架"} ]
     *   }
     * </pre>
     */
    private void writeJson(HttpServletResponse response, boolean success, String answer,
                           List<Book> books, String mode, String note) throws IOException {
        StringBuilder sb = new StringBuilder(512);
        sb.append("{\"success\":").append(success);
        sb.append(",\"answer\":").append(jsonString(answer));
        sb.append(",\"mode\":").append(jsonString(mode));
        sb.append(",\"note\":").append(jsonString(note));
        sb.append(",\"books\":[");
        if (books != null) {
            for (int i = 0; i < books.size(); i++) {
                Book b = books.get(i);
                if (i > 0) {
                    sb.append(',');
                }
                sb.append("{\"bid\":").append(jsonString(b.getBid()));
                sb.append(",\"bname\":").append(jsonString(b.getBname()));
                sb.append(",\"author\":").append(jsonString(b.getAuthor()));
                sb.append(",\"available\":").append(b.getAvailableStock() == null ? 0 : b.getAvailableStock());
                sb.append(",\"location\":").append(jsonString(b.getLocation()));
                sb.append('}');
            }
        }
        sb.append("]}");
        response.getWriter().write(sb.toString());
    }

    /** JSON 字符串转义 */
    private static String jsonString(String raw) {
        if (raw == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(raw.length() + 16);
        sb.append('"');
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}