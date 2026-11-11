package com.book.servlet;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 系统初始化 Servlet（仅管理员）。
 *
 * <h3>本次修复</h3>
 * <ul>
 *   <li><b>SQL 注入</b>：旧版本用 {@code String.format} 拼出 INSERT 语句，
 *       靠手工把单引号替换成两个单引号来"防注入"，一旦漏掉某个字段就会中招。
 *       现全部改为 {@link PreparedStatement} + 占位符；</li>
 *   <li><b>资源泄漏</b>：使用 try-with-resources，连接与语句必然关闭；</li>
 *   <li><b>越权</b>：该接口会写数据库，现要求管理员身份；</li>
 *   <li><b>密码加密</b>：初始化出来的账号密码以 BCrypt 密文存储。</li>
 * </ul>
 */
@WebServlet("/initSystem")
public class InitSystemServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** 初始化账号（密码统一走 BCrypt 加密） */
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "123456";
    private static final String READER_USERNAME = "reader01";
    private static final String READER_PASSWORD = "123456";

    /** 示例图书：{bid, isbn, 书名, 作者, 出版社, 出版日期, 价格, 分类id, 库存, 位置} */
    private static final String[][] SAMPLE_BOOKS = {
            {"B001", "978-7-02-013757-5", "红楼梦", "曹雪芹", "人民文学出版社", "2008-07-01", "59.70", "1", "5", "B区-1排-1架"},
            {"B002", "978-7-02-013758-2", "三国演义", "罗贯中", "人民文学出版社", "2008-07-01", "49.00", "1", "5", "B区-1排-1架"},
            {"B003", "978-7-5327-7241-0", "百年孤独", "加西亚·马尔克斯", "上海译文出版社", "2017-08-01", "55.00", "1", "8", "B区-1排-2架"},
            {"B004", "978-7-5442-7153-6", "活着", "余华", "作家出版社", "2012-08-01", "35.00", "1", "20", "B区-1排-2架"},
            {"B005", "978-7-04-039661-2", "高等数学", "同济大学数学系", "高等教育出版社", "2014-07-01", "49.00", "2", "30", "C区-1排-1架"},
            {"B006", "978-7-04-028582-7", "大学物理", "程守洙", "高等教育出版社", "2016-12-01", "45.00", "2", "20", "C区-1排-1架"},
            {"B007", "978-7-04-021481-6", "线性代数", "同济大学数学系", "高等教育出版社", "2014-06-01", "28.00", "2", "25", "C区-1排-2架"},
            {"B008", "978-7-04-031234-9", "概率论与数理统计", "浙江大学", "高等教育出版社", "2015-03-01", "35.00", "2", "22", "C区-1排-2架"},
            {"B009", "978-7-5399-5436-9", "明朝那些事儿", "当年明月", "浙江人民出版社", "2011-12-01", "188.00", "3", "25", "D区-1排-1架"},
            {"B010", "978-7-101-05452-6", "万历十五年", "黄仁宇", "中华书局", "2007-01-01", "29.00", "3", "15", "D区-1排-1架"},
            {"B011", "978-7-101-00325-8", "史记", "司马迁", "中华书局", "2013-08-01", "128.00", "3", "10", "D区-1排-2架"},
            {"B012", "978-7-101-09876-5", "中国通史", "吕思勉", "中华书局", "2015-06-01", "98.00", "3", "12", "D区-1排-2架"},
            {"B013", "978-7-111-21382-6", "Java编程思想", "Bruce Eckel", "机械工业出版社", "2007-06-01", "108.00", "4", "10", "A区-1排-3架"},
            {"B014", "978-7-111-55842-2", "Effective Java", "Joshua Bloch", "机械工业出版社", "2018-01-01", "89.00", "4", "8", "A区-1排-3架"},
            {"B015", "978-7-111-40701-0", "算法导论", "Thomas H.Cormen", "机械工业出版社", "2013-01-01", "128.00", "4", "8", "A区-3排-1架"},
            {"B016", "978-7-115-46812-5", "Linux命令行与Shell脚本编程", "Richard Blum", "人民邮电出版社", "2019-05-01", "79.00", "4", "10", "A区-3排-1架"},
            {"B017", "978-7-301-25649-8", "经济学原理", "N.Gregory Mankiw", "北京大学出版社", "2015-05-01", "98.00", "5", "10", "E区-1排-1架"},
            {"B018", "978-7-300-23456-7", "管理学", "Stephen P.Robbins", "中国人民大学出版社", "2017-01-01", "65.00", "5", "8", "E区-1排-1架"},
            {"B019", "978-7-5432-2789-4", "营销管理", "Philip Kotler", "格致出版社", "2016-04-01", "88.00", "5", "6", "E区-1排-2架"},
            {"B020", "978-7-5086-3456-2", "投资学", "Zvi Bodie", "中信出版社", "2018-09-01", "128.00", "5", "9", "E区-1排-2架"}
    };

    /** 示例分类 */
    private static final String[][] SAMPLE_CATEGORIES = {
            {"文学", "文学类图书"},
            {"理工", "理工科类图书"},
            {"历史", "历史类图书"},
            {"计算机", "计算机类图书"},
            {"经济管理", "经济管理类图书"}
    };

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        // 会写数据，必须是管理员
        User loginUser = ServletUtils.requireRole(request, response, "admin");
        if (loginUser == null) {
            return;
        }

        PrintWriter out = response.getWriter();
        printHeader(out, "系统初始化");

        try (Connection conn = DBUtil.requireConnection()) {
            int categoryCount = initCategories(conn, out);
            int userCount = initUsers(conn, out);
            int bookCount = initBooks(conn, out);

            out.println("<h3>📊 初始化结果</h3>");
            out.println("<p class='info'>分类：" + categoryCount + " 个　用户：" + userCount
                    + " 个　图书：" + bookCount + " 本</p>");
            out.println("<div class='success'>🎉 系统初始化完成</div>");
            out.println("<p><strong>登录账号：</strong>管理员 " + ADMIN_USERNAME + " / " + ADMIN_PASSWORD
                    + "　读者 " + READER_USERNAME + " / " + READER_PASSWORD + "</p>");
            out.println("<p><a class='btn' href='" + request.getContextPath()
                    + "/login.jsp'>🔐 立即登录</a></p>");

        } catch (SQLException e) {
            getServletContext().log("系统初始化失败", e);
            out.println("<div class='error'>初始化失败：" + escapeHtml(e.getMessage()) + "</div>");
        } catch (Exception e) {
            getServletContext().log("系统初始化失败", e);
            out.println("<div class='error'>初始化失败，请检查数据库连接</div>");
        }

        printFooter(out);
    }

    // ======================================================================
    // 分类
    // ======================================================================

    private int initCategories(Connection conn, PrintWriter out) throws SQLException {
        out.println("<h3>1. 分类数据</h3>");
        int existing = countRows(conn, "categories");
        if (existing > 0) {
            out.println("<p class='info'>分类已存在 " + existing + " 个，跳过创建</p>");
            return existing;
        }
        String sql = "INSERT INTO categories(name, description) VALUES(?, ?)";
        int inserted = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] cat : SAMPLE_CATEGORIES) {
                ps.setString(1, cat[0]);
                ps.setString(2, cat[1]);
                inserted += ps.executeUpdate();
            }
        }
        out.println("<p class='success'>✅ 创建了 " + inserted + " 个分类</p>");
        return inserted;
    }

    // ======================================================================
    // 用户
    // ======================================================================

    private int initUsers(Connection conn, PrintWriter out) throws SQLException {
        out.println("<h3>2. 用户数据</h3>");
        int existing = countRows(conn, "users");
        if (existing > 0) {
            out.println("<p class='info'>用户已存在 " + existing + " 个，跳过创建</p>");
            return existing;
        }

        String sql = "INSERT INTO users(username, password, role, real_name, status) VALUES(?,?,?,?,?)";
        int inserted = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // 管理员：密码 BCrypt 加密后存储
            ps.setString(1, ADMIN_USERNAME);
            ps.setString(2, BCryptUtil.hashpw(ADMIN_PASSWORD));
            ps.setString(3, "admin");
            ps.setString(4, "系统管理员");
            ps.setString(5, "active");
            inserted += ps.executeUpdate();

            // 读者
            ps.setString(1, READER_USERNAME);
            ps.setString(2, BCryptUtil.hashpw(READER_PASSWORD));
            ps.setString(3, "reader");
            ps.setString(4, "测试读者");
            ps.setString(5, "active");
            inserted += ps.executeUpdate();
        }
        out.println("<p class='success'>✅ 创建了 " + inserted + " 个用户（密码已加密存储）</p>");
        return inserted;
    }

    // ======================================================================
    // 图书
    // ======================================================================

    private int initBooks(Connection conn, PrintWriter out) throws SQLException {
        out.println("<h3>3. 图书数据</h3>");
        int existing = countRows(conn, "books");
        if (existing >= 20) {
            out.println("<p class='info'>图书已存在 " + existing + " 本，跳过创建</p>");
            return existing;
        }

        // ★ 全部使用占位符，不再拼接字符串
        String sql = "INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, "
                + "category_id, stock, available_stock, location) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int inserted = 0;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] b : SAMPLE_BOOKS) {
                ps.setString(1, b[0]);
                ps.setString(2, b[1]);
                ps.setString(3, b[2]);
                ps.setString(4, b[3]);
                ps.setString(5, b[4]);
                ps.setDate(6, new java.sql.Date(parseDate(sdf, b[5]).getTime()));
                ps.setDouble(7, Double.parseDouble(b[6]));
                ps.setInt(8, Integer.parseInt(b[7]));
                ps.setInt(9, Integer.parseInt(b[8]));
                ps.setInt(10, Integer.parseInt(b[8]));   // 初始可借 = 总库存
                ps.setString(11, b[9]);
                inserted += ps.executeUpdate();
            }
        }
        out.println("<p class='success'>✅ 创建了 " + inserted + " 本图书</p>");
        return inserted;
    }

    // ======================================================================
    // 工具
    // ======================================================================

    /**
     * 统计表行数。
     *
     * <p>表名来自代码内置常量，不是用户输入，因此用字符串拼接是安全的；
     * 但为清晰起见，这里用白名单校验一次。</p>
     */
    private int countRows(Connection conn, String table) throws SQLException {
        List<String> allowed = new ArrayList<>(java.util.Arrays.asList(
                "categories", "users", "books", "borrow_records", "fine_records", "reservations"));
        if (!allowed.contains(table)) {
            throw new SQLException("非法的表名: " + table);
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM " + table);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Date parseDate(SimpleDateFormat sdf, String text) {
        try {
            return sdf.parse(text);
        } catch (Exception e) {
            return new Date();
        }
    }

    private String escapeHtml(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
                + "text-decoration:none;border-radius:6px;margin-top:10px}"
                + "h2{color:#333}h3{color:#4a7cf7;margin-top:22px}</style></head><body><div class='container'>");
        out.println("<h2>" + title + "</h2>");
    }

    private void printFooter(PrintWriter out) {
        out.println("</div></body></html>");
    }
}