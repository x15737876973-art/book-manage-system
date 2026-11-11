package com.book.servlet;

import com.book.util.DBUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 完整初始化系统的Servlet
 */
@WebServlet("/initSystem")
public class InitSystemServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConn();
            stmt = conn.createStatement();
            
            out.println("<html><head><title>系统初始化</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; padding: 20px; background: #f5f5f5; }");
            out.println(".container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; }");
            out.println(".success { color: #28a745; padding: 10px; background: #d4edda; margin: 5px 0; border-radius: 4px; }");
            out.println(".error { color: #dc3545; padding: 10px; background: #f8d7da; margin: 5px 0; border-radius: 4px; }");
            out.println(".info { background: #e7f3ff; padding: 15px; border-radius: 5px; margin: 10px 0; }");
            out.println(".btn { display: inline-block; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 4px; margin: 5px; }");
            out.println("h2 { color: #333; }");
            out.println("</style>");
            out.println("</head><body>");
            out.println("<div class='container'>");
            out.println("<h2>🔧 系统完整初始化</h2>");
            
            // 1. 检查并创建分类
            out.println("<h3>1. 检查分类数据</h3>");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
            rs.next();
            int categoryCount = rs.getInt(1);
            rs.close();
            
            if (categoryCount == 0) {
                out.println("<p class='info'>正在创建分类数据...</p>");
                stmt.executeUpdate("INSERT INTO categories(name, description) VALUES('文学', '文学类图书')");
                stmt.executeUpdate("INSERT INTO categories(name, description) VALUES('理工', '理工科类图书')");
                stmt.executeUpdate("INSERT INTO categories(name, description) VALUES('历史', '历史类图书')");
                stmt.executeUpdate("INSERT INTO categories(name, description) VALUES('计算机', '计算机类图书')");
                stmt.executeUpdate("INSERT INTO categories(name, description) VALUES('经济管理', '经济管理类图书')");
                out.println("<p class='success'>✅ 成功创建5个分类</p>");
            } else {
                out.println("<p class='success'>✅ 分类数据已存在：" + categoryCount + " 个</p>");
            }
            
            // 2. 检查并创建用户
            out.println("<h3>2. 检查用户数据</h3>");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            int userCount = rs.getInt(1);
            rs.close();
            
            if (userCount == 0) {
                out.println("<p class='info'>正在创建用户数据...</p>");
                stmt.executeUpdate("INSERT INTO users(username, password, role, real_name, status) VALUES('a', '123456', 'admin', '系统管理员', 'active')");
                stmt.executeUpdate("INSERT INTO users(username, password, role, real_name, status) VALUES('d', '123456', 'reader', '测试读者', 'active')");
                out.println("<p class='success'>✅ 成功创建2个用户</p>");
                out.println("<p class='info'>管理员：a / 123456<br>读者：d / 123456</p>");
            } else {
                out.println("<p class='success'>✅ 用户数据已存在：" + userCount + " 个</p>");
                // 显示现有用户
                rs = stmt.executeQuery("SELECT username, role, real_name FROM users");
                out.println("<p class='info'>现有用户：<br>");
                while (rs.next()) {
                    out.println("  - " + rs.getString("username") + " (" + rs.getString("role") + ") - " + rs.getString("real_name") + "<br>");
                }
                rs.close();
                out.println("</p>");
            }
            
            // 3. 检查并创建图书
            out.println("<h3>3. 检查图书数据</h3>");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM books");
            rs.next();
            int bookCount = rs.getInt(1);
            rs.close();
            
            if (bookCount < 20) {
                out.println("<p class='info'>正在创建图书数据...</p>");
                
                int inserted = 0;
                // 文学类 (category_id = 1)
                inserted += insertBook(stmt, out, "B001", "978-7-02-013757-5", "红楼梦", "曹雪芹", "人民文学出版社", "2008-07-01", 59.70, 1, 5, "B区-1排-1架");
                inserted += insertBook(stmt, out, "B002", "978-7-02-013758-2", "三国演义", "罗贯中", "人民文学出版社", "2008-07-01", 49.00, 1, 5, "B区-1排-1架");
                inserted += insertBook(stmt, out, "B003", "978-7-5327-7241-0", "百年孤独", "加西亚·马尔克斯", "上海译文出版社", "2017-08-01", 55.00, 1, 8, "B区-1排-2架");
                inserted += insertBook(stmt, out, "B004", "978-7-5442-7153-6", "活着", "余华", "作家出版社", "2012-08-01", 35.00, 1, 20, "B区-1排-2架");
                
                // 理工类 (category_id = 2)
                inserted += insertBook(stmt, out, "B005", "978-7-04-039661-2", "高等数学", "同济大学数学系", "高等教育出版社", "2014-07-01", 49.00, 2, 30, "C区-1排-1架");
                inserted += insertBook(stmt, out, "B006", "978-7-04-028582-7", "大学物理", "程守洙", "高等教育出版社", "2016-12-01", 45.00, 2, 20, "C区-1排-1架");
                inserted += insertBook(stmt, out, "B007", "978-7-04-021481-6", "线性代数", "同济大学数学系", "高等教育出版社", "2014-06-01", 28.00, 2, 25, "C区-1排-2架");
                inserted += insertBook(stmt, out, "B008", "978-7-04-031234-9", "概率论与数理统计", "浙江大学", "高等教育出版社", "2015-03-01", 35.00, 2, 22, "C区-1排-2架");
                
                // 历史类 (category_id = 3)
                inserted += insertBook(stmt, out, "B009", "978-7-5399-5436-9", "明朝那些事儿", "当年明月", "浙江人民出版社", "2011-12-01", 188.00, 3, 25, "D区-1排-1架");
                inserted += insertBook(stmt, out, "B010", "978-7-101-05452-6", "万历十五年", "黄仁宇", "中华书局", "2007-01-01", 29.00, 3, 15, "D区-1排-1架");
                inserted += insertBook(stmt, out, "B011", "978-7-101-00325-8", "史记", "司马迁", "中华书局", "2013-08-01", 128.00, 3, 10, "D区-1排-2架");
                inserted += insertBook(stmt, out, "B012", "978-7-101-09876-5", "中国通史", "吕思勉", "中华书局", "2015-06-01", 98.00, 3, 12, "D区-1排-2架");
                
                // 计算机类 (category_id = 4)
                inserted += insertBook(stmt, out, "B013", "978-7-111-21382-6", "Java编程思想", "Bruce Eckel", "机械工业出版社", "2007-06-01", 108.00, 4, 10, "A区-1排-3架");
                inserted += insertBook(stmt, out, "B014", "978-7-111-55842-2", "Effective Java", "Joshua Bloch", "机械工业出版社", "2018-01-01", 89.00, 4, 8, "A区-1排-3架");
                inserted += insertBook(stmt, out, "B015", "978-7-111-40701-0", "算法导论", "Thomas H.Cormen", "机械工业出版社", "2013-01-01", 128.00, 4, 8, "A区-3排-1架");
                inserted += insertBook(stmt, out, "B016", "978-7-115-46812-5", "Linux命令行与Shell脚本编程", "Richard Blum", "人民邮电出版社", "2019-05-01", 79.00, 4, 10, "A区-3排-1架");
                
                // 经济管理类 (category_id = 5)
                inserted += insertBook(stmt, out, "B017", "978-7-301-25649-8", "经济学原理", "N.Gregory Mankiw", "北京大学出版社", "2015-05-01", 98.00, 5, 10, "E区-1排-1架");
                inserted += insertBook(stmt, out, "B018", "978-7-300-23456-7", "管理学", "Stephen P.Robbins", "中国人民大学出版社", "2017-01-01", 65.00, 5, 8, "E区-1排-1架");
                inserted += insertBook(stmt, out, "B019", "978-7-5432-2789-4", "营销管理", "Philip Kotler", "格致出版社", "2016-04-01", 88.00, 5, 6, "E区-1排-2架");
                inserted += insertBook(stmt, out, "B020", "978-7-5086-3456-2", "投资学", "Zvi Bodie", "中信出版社", "2018-09-01", 128.00, 5, 9, "E区-1排-2架");
                
                out.println("<p class='success'>✅ 成功插入 " + inserted + " 本图书</p>");
            } else {
                out.println("<p class='success'>✅ 图书数据已存在：" + bookCount + " 本</p>");
            }
            
            // 最终统计
            out.println("<h3>📊 最终数据统计</h3>");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
            rs.next();
            out.println("<p class='info'>分类数量：" + rs.getInt(1) + " 个</p>");
            rs.close();
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            out.println("<p class='info'>用户数量：" + rs.getInt(1) + " 个</p>");
            rs.close();
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM books");
            rs.next();
            out.println("<p class='info'>图书数量：" + rs.getInt(1) + " 本</p>");
            rs.close();
            
            out.println("<div style='margin-top: 30px; padding: 20px; background: #d4edda; border-radius: 8px;'>");
            out.println("<h3 style='color: #28a745;'>🎉 系统初始化完成！</h3>");
            out.println("<p><strong>登录账号：</strong></p>");
            out.println("<p>管理员：a / 123456</p>");
            out.println("<p>读者：d / 123456</p>");
            out.println("</div>");
            
            out.println("<div style='margin-top: 20px;'>");
            out.println("<a href='login.jsp' class='btn' style='background: #28a745;'>🔐 立即登录</a>");
            out.println("<a href='admin/index.jsp' class='btn'>👨‍💼 管理员首页</a>");
            out.println("<a href='快速访问.html' class='btn' style='background: #6c757d;'>🏠 快速访问</a>");
            out.println("</div>");
            
            out.println("</div></body></html>");
            
        } catch (Exception e) {
            out.println("<h3 class='error'>❌ 初始化失败：" + e.getMessage() + "</h3>");
            e.printStackTrace(new PrintWriter(out));
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private int insertBook(Statement stmt, PrintWriter out, String bid, String isbn, String bname, 
                          String author, String publisher, String publishDate, 
                          double price, int categoryId, int stock, String location) {
        try {
            String safeBname = bname.replace("'", "''");
            String safeAuthor = author.replace("'", "''");
            String safePublisher = publisher.replace("'", "''");
            String safeLocation = location.replace("'", "''");
            String safeIsbn = isbn.replace("'", "''");
            String sql = String.format(
                "INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('%s', '%s', '%s', '%s', '%s', '%s', %.2f, %d, %d, %d, '%s')",
                bid, safeIsbn, safeBname, safeAuthor, safePublisher, publishDate, price, categoryId, stock, stock, safeLocation
            );
            stmt.executeUpdate(sql);
            out.println("<p class='success'>✅ " + bname + "</p>");
            return 1;
        } catch (Exception e) {
            out.println("<p class='error'>❌ " + bname + " - " + e.getMessage() + "</p>");
            return 0;
        }
    }
}