package com.book.servlet;

import com.book.dao.BookDao;
import com.book.entity.Book;
import com.book.util.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 图书数据诊断 Servlet（仅管理员）。
 *
 * <p>改造点：管理员鉴权、HTML 转义防 XSS、统一异常处理。</p>
 */
@WebServlet("/admin/testBooksServlet")
public class TestBooksServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final BookDao bookDao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        if (ServletUtils.requireRole(request, response, "admin") == null) {
            return;
        }

        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>图书数据诊断</title>");
        out.println("<style>body{font-family:system-ui,'Microsoft YaHei',sans-serif;padding:20px}"
                + "table{border-collapse:collapse;width:100%}th,td{border:1px solid #ddd;padding:8px;text-align:left}"
                + "th{background:#4a7cf7;color:#fff}tr:nth-child(even){background:#f7f8fa}"
                + ".info{background:#e7f3ff;padding:14px;border-radius:6px;margin-bottom:16px}</style></head><body>");

        try {
            int total = bookDao.count();
            List<Book> books = bookDao.findAll();

            out.println("<h2>📊 图书数据诊断</h2>");
            out.println("<div class='info'><p><strong>图书总数：</strong>" + total + " 本</p>"
                    + "<p><strong>查询返回：</strong>" + books.size() + " 条</p></div>");

            if (books.isEmpty()) {
                out.println("<p style='color:#b26a00'>⚠️ 没有查询到图书数据，请先执行系统初始化</p>");
            } else {
                out.println("<table><tr><th>ID</th><th>编号</th><th>书名</th><th>作者</th>"
                        + "<th>分类</th><th>库存</th><th>可借</th></tr>");
                for (Book b : books) {
                    out.println("<tr><td>" + b.getId() + "</td>"
                            + "<td>" + esc(b.getBid()) + "</td>"
                            + "<td>" + esc(b.getBname()) + "</td>"
                            + "<td>" + esc(b.getAuthor()) + "</td>"
                            + "<td>" + esc(b.getCategoryName() == null ? "未分类" : b.getCategoryName()) + "</td>"
                            + "<td>" + b.getStock() + "</td>"
                            + "<td>" + b.getAvailableStock() + "</td></tr>");
                }
                out.println("</table>");
            }
            out.println("<p><a href='" + request.getContextPath()
                    + "/admin/bookManageServlet?action=list'>返回图书管理</a></p>");

        } catch (Exception e) {
            getServletContext().log("图书诊断失败", e);
            out.println("<p style='color:red'>诊断失败：" + esc(e.getMessage()) + "</p>");
        }

        out.println("</body></html>");
    }

    private String esc(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}