package com.book.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.book.entity.User;
import com.book.util.BCryptUtil;
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
import java.text.SimpleDateFormat;

/**
 * 测试数据生成 Servlet（仅管理员）——用于演示/答辩时快速铺满数据。
 *
 * <h3>本次修复</h3>
 * <ul>
 *   <li>旧版本用 {@code String.format} 拼 INSERT 与 {@code "LIMIT " + n}，
 *       属于典型的 SQL 注入面；现全部改为 {@link PreparedStatement} 占位符；</li>
 *   <li>表名出现处改为白名单校验；</li>
 *   <li>try-with-resources 释放资源；</li>
 *   <li>新增用户时密码以 BCrypt 密文存储。</li>
 * </ul>
 *
 * <p><b>注意：</b>这是演示辅助接口，生产环境建议在 {@code web.xml}
 * 或网关层禁用它。</p>
 */
@WebServlet("/insertTestData")
public class InsertTestDataServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(InsertTestDataServlet.class);

    private static final long serialVersionUID = 1L;

    /** 目标数据量 */
    private static final int TARGET_USERS = 30;
    private static final int TARGET_BOOKS = 30;
    private static final int TARGET_BORROWS = 30;
    private static final int TARGET_FINES = 30;
    private static final int TARGET_RESERVATIONS = 30;

    /** 允许统计的表名白名单 */
    private static final String[] TABLES = {"categories", "users", "books", "borrow_records", "fine_records", "reservations"};
    private static final String[] TABLE_LABELS = {"分类", "用户", "图书", "借阅记录", "罚款记录", "预约记录"};

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
        printHeader(out, "生成测试数据");

        try (Connection conn = DBUtil.requireConnection()) {
            String[] results = new String[6];
            int[] added = new int[10];

            added[0] = fillCategories(conn);
            added[1] = fillUsers(conn);
            added[2] = fillBooks(conn);
            added[3] = fillBorrowRecords(conn);
            added[4] = fillFines(conn);
            added[5] = fillReservations(conn);

            out.println("<h3>📊 最终统计</h3><table border='1' style='border-collapse:collapse;width:100%'>");
            out.println("<tr style='background:#4a7cf7;color:#fff'><th>数据表</th><th>记录数</th></tr>");
            for (int i = 0; i < TABLES.length; i++) {
                out.println("<tr><td>" + TABLE_LABELS[i] + "</td><td>" + countRows(conn, TABLES[i]) + " 条</td></tr>");
            }
            out.println("</table>");
            out.println("<p><a class='btn' href='" + request.getContextPath()
                    + "/admin/indexServlet'>👨‍💼 返回管理员首页</a></p>");

        } catch (SQLException e) {
            getServletContext().log("生成测试数据失败", e);
            out.println("<div class='error'>生成失败：" + escapeHtml(e.getMessage()) + "</div>");
        } catch (Exception e) {
            getServletContext().log("生成测试数据失败", e);
            out.println("<div class='error'>生成失败，请检查数据库连接</div>");
        }

        printFooter(out);
    }

    // ======================================================================
    // 各类数据
    // ======================================================================

    private int fillCategories(Connection conn) throws SQLException {
        String[][] cats = {
                {"文学", "文学类图书"}, {"理工", "理工科类图书"}, {"历史", "历史类图书"},
                {"计算机", "计算机类图书"}, {"经济管理", "经济管理类图书"}
        };
        String sql = "INSERT INTO categories(name, description) VALUES(?, ?)";
        int inserted = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] c : cats) {
                if (exists(conn, "SELECT COUNT(*) FROM categories WHERE name = ?", c[0])) {
                    continue;
                }
                ps.setString(1, c[0]);
                ps.setString(2, c[1]);
                inserted += ps.executeUpdate();
            }
        }
        log.debug("分类本次新增 {} 条", inserted);
        return inserted;
    }

    private int fillUsers(Connection conn) throws SQLException {
        int existing = countRows(conn, "users");
        if (existing >= TARGET_USERS) {
            return 0;
        }
        String sql = "INSERT INTO users(username, password, role, real_name, phone, email, status) "
                + "VALUES(?,?,?,?,?,?,?)";
        int inserted = 0;
        // 统一的 BCrypt 密文只需算一次（同一明文 + 同一密文可复用，但每次 salt 不同更安全；
        // 此处为 30 个测试账号，用相同哈希可显著加快生成速度）
        String defaultHash = BCryptUtil.hashpw("123456");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = existing; i < TARGET_USERS; i++) {
                String username = String.format("reader%02d", i);
                if (exists(conn, "SELECT COUNT(*) FROM users WHERE username = ?", username)) {
                    continue;
                }
                ps.setString(1, username);
                ps.setString(2, defaultHash);
                ps.setString(3, "reader");
                ps.setString(4, "读者" + i);
                ps.setString(5, String.format("138%08d", i));
                ps.setString(6, username + "@library.com");
                ps.setString(7, "active");
                inserted += ps.executeUpdate();
            }
        }
        return inserted;
    }

    private int fillBooks(Connection conn) throws SQLException {
        return 0;   // 图书数据请用 /initSystem 初始化，避免重复
    }

    private int fillBorrowRecords(Connection conn) throws SQLException {
        int existing = countRows(conn, "borrow_records");
        if (existing >= TARGET_BORROWS) {
            return 0;
        }
        // ★ 占位符写法，不再拼接日期字符串
        String sql = "INSERT INTO borrow_records(user_id, book_id, borrow_date, due_date, return_date, "
                + "status, renew_count) VALUES(?,?,?,?,?,?,?)";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long now = System.currentTimeMillis();
        long dayMs = 24L * 60 * 60 * 1000;
        int userCount = Math.max(countRows(conn, "users") - 1, 1);
        int bookCount = Math.max(countRows(conn, "books"), 1);

        int inserted = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = existing; i < TARGET_BORROWS; i++) {
                int userId = (i % userCount) + 2;
                int bookId = (i % bookCount) + 1;
                long borrowDate = now - (45L + i % 16) * dayMs;
                long dueDate = borrowDate + 30L * dayMs;
                String status;
                java.sql.Date returnDate = null;

                if (i < 10) {
                    status = "returned";
                    returnDate = new java.sql.Date(borrowDate + (25L + i % 5) * dayMs);
                } else if (i < 20) {
                    status = "borrowed";
                } else {
                    status = "overdue";
                }

                ps.setInt(1, userId);
                ps.setInt(2, bookId);
                ps.setDate(3, new java.sql.Date(borrowDate));
                ps.setDate(4, new java.sql.Date(dueDate));
                if (returnDate == null) {
                    ps.setNull(5, java.sql.Types.DATE);
                } else {
                    ps.setDate(5, returnDate);
                }
                ps.setString(6, status);
                ps.setInt(7, i % 3);
                inserted += ps.executeUpdate();
            }
        }
        return inserted;
    }

    private int fillFines(Connection conn) throws SQLException {
        int existing = countRows(conn, "fine_records");
        if (existing >= TARGET_FINES) {
            return 0;
        }
        String sql = "INSERT INTO fine_records(user_id, borrow_record_id, amount, days, status) VALUES(?,?,?,?,?)";
        int inserted = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             PreparedStatement select = conn.prepareStatement(
                     "SELECT id, user_id FROM borrow_records WHERE status = 'overdue' LIMIT ?")) {
            select.setInt(1, TARGET_FINES - existing);
            try (ResultSet rs = select.executeQuery()) {
                int i = 0;
                while (rs.next()) {
                    int days = (i % 15) + 1;
                    ps.setInt(1, rs.getInt("user_id"));
                    ps.setInt(2, rs.getInt("id"));
                    ps.setDouble(3, days * 0.5d);
                    ps.setInt(4, days);
                    ps.setString(5, i < 20 ? "paid" : "unpaid");
                    inserted += ps.executeUpdate();
                    i++;
                }
            }
        }
        return inserted;
    }

    private int fillReservations(Connection conn) throws SQLException {
        int existing = countRows(conn, "reservations");
        if (existing >= TARGET_RESERVATIONS) {
            return 0;
        }
        String sql = "INSERT INTO reservations(user_id, book_id, status) VALUES(?,?,?)";
        String[] statuses = {"waiting", "notified", "completed", "cancelled"};
        int userCount = Math.max(countRows(conn, "users") - 1, 1);
        int bookCount = Math.max(countRows(conn, "books"), 1);

        int inserted = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = existing; i < TARGET_RESERVATIONS; i++) {
                ps.setInt(1, (i % userCount) + 2);
                ps.setInt(2, (i % bookCount) + 1);
                ps.setString(3, statuses[i % statuses.length]);
                try {
                    inserted += ps.executeUpdate();
                } catch (SQLException ignored) {
                    // 外键冲突等异常跳过该条，不影响整体生成
                }
            }
        }
        return inserted;
    }

    // ======================================================================
    // 工具
    // ======================================================================

    private boolean exists(Connection conn, String sql, String param) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** 统计表行数，表名已白名单校验 */
    private int countRows(Connection conn, String table) throws SQLException {
        boolean allowed = false;
        for (String t : TABLES) {
            if (t.equals(table)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new SQLException("非法的表名: " + table);
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM " + table);
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
                + ".error{color:#721c24;background:#f8d7da;padding:10px;border-radius:6px}"
                + ".btn{display:inline-block;padding:10px 18px;background:#4a7cf7;color:#fff;"
                + "text-decoration:none;border-radius:6px;margin-top:12px}"
                + "h2{color:#333}h3{color:#4a7cf7;margin-top:22px}"
                + "th,td{border:1px solid #ddd;padding:8px;text-align:left}</style></head><body><div class='container'>");
        out.println("<h2>" + title + "</h2>");
    }

    private void printFooter(PrintWriter out) {
        out.println("</div></body></html>");
    }
}