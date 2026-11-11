package com.book.servlet;

import com.book.util.DBUtil;
import com.book.util.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库健康检查 Servlet（仅管理员）。
 *
 * <h3>本次修复</h3>
 * <ul>
 *   <li>旧版本直接 {@code conn.createStatement()} 且没有 try-with-resources，
 *       异常路径下连接会泄漏；现使用 try-with-resources；</li>
 *   <li>输出内容做 HTML 转义，避免图书名中的 {@code <} 破坏页面（XSS）；</li>
 *   <li>新增管理员身份校验。</li>
 * </ul>
 */
@WebServlet("/admin/checkBooks")
public class CheckBooksServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

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
        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>数据库检查</title>");
        out.println("<style>body{font-family:system-ui,'Microsoft YaHei',sans-serif;padding:20px}"
                + "table{border-collapse:collapse}th,td{border:1px solid #ddd;padding:6px 10px}"
                + "th{background:#4a7cf7;color:#fff}</style></head><body>");
        out.println("<h2>数据库检查结果</h2>");

        try (Connection conn = DBUtil.requireConnection()) {
            int categoryCount = count(conn, "SELECT COUNT(*) FROM categories");
            int bookCount = count(conn, "SELECT COUNT(*) FROM books");
            int readerCount = count(conn, "SELECT COUNT(*) FROM users WHERE role = 'reader'");

            out.println("<p>分类数量：" + categoryCount + "</p>");
            out.println("<p>图书数量：" + bookCount + "</p>");
            out.println("<p>读者数量：" + readerCount + "</p>");

            if (bookCount > 0) {
                out.println("<h3>图书列表（前 10 本）</h3>");
                out.println("<table><tr><th>ID</th><th>编号</th><th>书名</th><th>作者</th><th>库存</th></tr>");
                String sql = "SELECT id, bid, bname, author, stock FROM books ORDER BY id LIMIT ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, 10);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            out.println("<tr><td>" + rs.getInt("id") + "</td>"
                                    + "<td>" + esc(rs.getString("bid")) + "</td>"
                                    + "<td>" + esc(rs.getString("bname")) + "</td>"
                                    + "<td>" + esc(rs.getString("author")) + "</td>"
                                    + "<td>" + rs.getInt("stock") + "</td></tr>");
                        }
                    }
                }
                out.println("</table>");
            } else {
                out.println("<p style='color:red'>⚠️ 数据库中没有图书数据</p>");
            }

            out.println("<p><a href='" + request.getContextPath()
                    + "/admin/bookManageServlet?action=list'>返回图书列表</a></p>");

        } catch (SQLException e) {
            getServletContext().log("数据库检查失败", e);
            out.println("<p style='color:red'>检查失败：" + esc(e.getMessage()) + "</p>");
        } catch (Exception e) {
            getServletContext().log("数据库检查失败", e);
            out.println("<p style='color:red'>检查失败，请确认数据库已启动</p>");
        }

        out.println("</body></html>");
    }

    private int count(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** HTML 转义，防止 XSS */
    private String esc(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}