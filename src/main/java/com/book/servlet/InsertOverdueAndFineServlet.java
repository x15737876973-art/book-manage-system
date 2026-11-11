package com.book.servlet;

import com.book.entity.User;
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
import java.sql.Types;

/**
 * 生成逾期记录与罚款记录（仅管理员，演示用）。
 *
 * <h3>本次修复</h3>
 * <ul>
 *   <li>旧版本用 {@code String.format} 拼接 INSERT，并且
 *       {@code "... LIMIT " + needInsert} 直接拼接数字；现全部改为占位符；</li>
 *   <li>try-with-resources 管理资源；</li>
 *   <li>新增管理员身份校验。</li>
 * </ul>
 */
@WebServlet("/insertOverdueAndFine")
public class InsertOverdueAndFineServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** 目标逾期记录数 */
    private static final int TARGET_OVERDUE = 30;
    /** 目标罚款记录数 */
    private static final int TARGET_FINES = 30;
    /** 每日罚款（元） */
    private static final double FINE_PER_DAY = 0.5d;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        User loginUser = ServletUtils.requireRole(request, response, "admin");
        if (loginUser == null) {
            return;
        }

        PrintWriter out = response.getWriter();
        printHeader(out, "生成逾期与罚款数据");

        try (Connection conn = DBUtil.requireConnection()) {
            int userCount = count(conn, "users");
            int bookCount = count(conn, "books");

            out.println("<p class='info'>当前用户 " + userCount + " 个，图书 " + bookCount + " 本</p>");

            if (userCount < 2 || bookCount < 1) {
                out.println("<div class='error'>基础数据不足，请先执行系统初始化或生成测试数据</div>");
                printFooter(out);
                return;
            }

            int overdueAdded = fillOverdue(conn, userCount, bookCount, out);
            int fineAdded = fillFines(conn, out);

            out.println("<h3>📊 结果统计</h3>");
            out.println("<table><tr><th>数据</th><th>数量</th></tr>");
            out.println("<tr><td>逾期借阅记录</td><td>" + count(conn, "borrow_records", "status = 'overdue'")
                    + " 条</td></tr>");
            out.println("<tr><td>罚款记录</td><td>" + count(conn, "fine_records") + " 条</td></tr>");
            out.println("<tr><td>未支付罚款</td><td>" + count(conn, "fine_records", "status = 'unpaid'")
                    + " 条</td></tr>");
            out.println("</table>");
            out.println("<p class='info'>本次新增逾期 " + overdueAdded + " 条，罚款 " + fineAdded + " 条</p>");

            out.println("<p><a class='btn' href='" + request.getContextPath()
                    + "/admin/borrowServlet?action=overdue'>⚠️ 查看逾期列表</a></p>");

        } catch (SQLException e) {
            getServletContext().log("生成逾期数据失败", e);
            out.println("<div class='error'>生成失败：" + escapeHtml(e.getMessage()) + "</div>");
        } catch (Exception e) {
            getServletContext().log("生成逾期数据失败", e);
            out.println("<div class='error'>生成失败，请检查数据库连接</div>");
        }

        printFooter(out);
    }

    // ======================================================================
    // 逾期记录
    // ======================================================================

    private int fillOverdue(Connection conn, int userCount, int bookCount, PrintWriter out) throws SQLException {
        int existing = count(conn, "borrow_records", "status = 'overdue'");
        out.println("<h3>1. 逾期借阅记录</h3>");
        out.println("<p class='info'>现有逾期记录 " + existing + " 条</p>");

        if (existing >= TARGET_OVERDUE) {
            out.println("<p class='info'>数量已足够，跳过</p>");
            return 0;
        }

        int need = TARGET_OVERDUE - existing;
        // ★ 占位符 + 显式 null，不再拼日期字符串
        String sql = "INSERT INTO borrow_records(user_id, book_id, borrow_date, due_date, return_date, "
                + "status, renew_count, fine_amount) VALUES(?,?,?,?,?,?,?,?)";
        long now = System.currentTimeMillis();
        long dayMs = 24L * 60 * 60 * 1000;
        int inserted = 0;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < need; i++) {
                int userId = (i % Math.max(userCount - 1, 1)) + 2;
                int bookId = (i % bookCount) + 1;
                long borrowDate = now - (45L + i % 16) * dayMs;
                long dueDate = borrowDate + 30L * dayMs;

                ps.setInt(1, userId);
                ps.setInt(2, bookId);
                ps.setDate(3, new java.sql.Date(borrowDate));
                ps.setDate(4, new java.sql.Date(dueDate));
                ps.setNull(5, Types.DATE);
                ps.setString(6, "overdue");
                ps.setInt(7, i % 3);
                ps.setDouble(8, 0d);
                inserted += ps.executeUpdate();
            }
        }
        out.println("<p class='success'>✅ 新增逾期记录 " + inserted + " 条</p>");
        return inserted;
    }

    // ======================================================================
    // 罚款记录
    // ======================================================================

    private int fillFines(Connection conn, PrintWriter out) throws SQLException {
        int existing = count(conn, "fine_records");
        out.println("<h3>2. 罚款记录</h3>");
        out.println("<p class='info'>现有罚款记录 " + existing + " 条</p>");

        if (existing >= TARGET_FINES) {
            out.println("<p class='info'>数量已足够，跳过</p>");
            return 0;
        }

        int need = TARGET_FINES - existing;
        String insertSql = "INSERT INTO fine_records(user_id, borrow_record_id, amount, days, status) "
                + "VALUES(?,?,?,?,?)";
        // LIMIT 也用占位符，杜绝 "LIMIT " + n 这类拼接
        String selectSql = "SELECT id, user_id FROM borrow_records WHERE status = 'overdue' ORDER BY id LIMIT ?";

        int inserted = 0;
        try (PreparedStatement select = conn.prepareStatement(selectSql)) {
            select.setInt(1, need);
            try (ResultSet rs = select.executeQuery();
                 PreparedStatement insert = conn.prepareStatement(insertSql)) {
                int i = 0;
                while (rs.next() && i < need) {
                    int days = (i % 15) + 1;
                    insert.setInt(1, rs.getInt("user_id"));
                    insert.setInt(2, rs.getInt("id"));
                    insert.setDouble(3, days * FINE_PER_DAY);
                    insert.setInt(4, days);
                    insert.setString(5, i < 20 ? "paid" : "unpaid");
                    inserted += insert.executeUpdate();
                    i++;
                }
            }
        }
        out.println("<p class='success'>✅ 新增罚款记录 " + inserted + " 条</p>");
        return inserted;
    }

    // ======================================================================
    // 工具
    // ======================================================================

    private int count(Connection conn, String table) throws SQLException {
        return count(conn, table, null);
    }

    /**
     * 统计行数。表名与条件均为代码内置常量（非用户输入），
     * 且表名做了白名单校验，安全无注入风险。
     */
    private int count(Connection conn, String table, String where) throws SQLException {
        if (!java.util.Arrays.asList("users", "books", "borrow_records", "fine_records", "reservations")
                .contains(table)) {
            throw new SQLException("非法的表名: " + table);
        }
        String sql = "SELECT COUNT(*) FROM " + table + (where == null ? "" : " WHERE " + where);
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private String escapeHtml(String raw) {
        return raw == null ? "" : raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void printHeader(PrintWriter out, String title) {
        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + title + "</title>");
        out.println("<style>body{font-family:system-ui,'Microsoft YaHei',sans-serif;padding:20px;background:#f5f6fa}"
                + ".container{max-width:860px;margin:0 auto;background:#fff;padding:28px;border-radius:10px;"
                + "box-shadow:0 2px 12px rgba(0,0,0,.08)}"
                + ".success{color:#155724;background:#d4edda;padding:10px;border-radius:6px;margin:6px 0}"
                + ".error{color:#721c24;background:#f8d7da;padding:10px;border-radius:6px;margin:6px 0}"
                + ".info{color:#0c5460;background:#d1ecf1;padding:10px;border-radius:6px;margin:6px 0}"
                + ".btn{display:inline-block;padding:10px 18px;background:#4a7cf7;color:#fff;"
                + "text-decoration:none;border-radius:6px;margin-top:12px}"
                + "h2{color:#333}h3{color:#4a7cf7;margin-top:22px}"
                + "table{border-collapse:collapse;width:100%}th,td{border:1px solid #ddd;padding:8px;text-align:left}"
                + "th{background:#4a7cf7;color:#fff}</style></head><body><div class='container'>");
        out.println("<h2>" + title + "</h2>");
    }

    private void printFooter(PrintWriter out) {
        out.println("</div></body></html>");
    }
}