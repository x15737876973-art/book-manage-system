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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 专门插入逾期记录和罚款记录的Servlet
 * 确保逾期列表和罚款记录都有至少30条数据
 */
@WebServlet("/insertOverdueAndFine")
public class InsertOverdueAndFineServlet extends HttpServlet {
    
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
            
            out.println("<html><head><title>插入逾期和罚款记录</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; padding: 20px; background: #f5f5f5; }");
            out.println(".container { max-width: 900px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; }");
            out.println(".success { color: #28a745; padding: 8px; background: #d4edda; margin: 3px 0; border-radius: 4px; }");
            out.println(".error { color: #dc3545; padding: 8px; background: #f8d7da; margin: 3px 0; border-radius: 4px; }");
            out.println(".info { background: #e7f3ff; padding: 15px; border-radius: 5px; margin: 10px 0; }");
            out.println(".btn { display: inline-block; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 4px; margin: 5px; }");
            out.println("h2 { color: #333; } h3 { color: #667eea; margin-top: 20px; }");
            out.println("</style>");
            out.println("</head><body>");
            out.println("<div class='container'>");
            out.println("<h2>📊 插入逾期记录和罚款记录</h2>");
            
            // 1. 检查用户和图书数据
            out.println("<h3>0. 检查基础数据</h3>");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            int userCount = rs.getInt(1);
            rs.close();
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM books");
            rs.next();
            int bookCount = rs.getInt(1);
            rs.close();
            
            out.println("<p class='info'>用户数量: " + userCount + " 个</p>");
            out.println("<p class='info'>图书数量: " + bookCount + " 本</p>");
            
            if (userCount < 5 || bookCount < 5) {
                out.println("<p class='error'>❌ 基础数据不足，请先插入用户和图书数据！</p>");
                out.println("<a href='insertTestData' class='btn'>📊 插入基础数据</a>");
                return;
            }
            
            // 2. 插入逾期借阅记录
            out.println("<h3>1. 插入逾期借阅记录</h3>");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM borrow_records WHERE status='overdue'");
            rs.next();
            int overdueCount = rs.getInt(1);
            rs.close();
            
            out.println("<p class='info'>当前逾期记录数: " + overdueCount + " 条</p>");
            
            if (overdueCount < 30) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                long now = System.currentTimeMillis();
                long dayMs = 24 * 60 * 60 * 1000L;
                
                int inserted = 0;
                int needInsert = 30 - overdueCount;
                
                out.println("<p class='info'>需要插入 " + needInsert + " 条逾期记录</p>");
                
                for (int i = 0; i < needInsert; i++) {
                    try {
                        int userId = (i % (userCount - 1)) + 2; // 用户ID从2开始
                        int bookId = (i % bookCount) + 1; // 图书ID从1开始
                        
                        // 创建逾期记录：借出时间为45-60天前，应还时间为30天前，现已逾期
                        long borrowDate = now - (45 + i % 16) * dayMs; // 45-60天前借出
                        long dueDate = borrowDate + 30 * dayMs; // 30天后应还（已逾期）
                        
                        String sql = String.format(
                            "INSERT INTO borrow_records(user_id, book_id, borrow_date, due_date, return_date, status, renew_count, fine_amount) " +
                            "VALUES(%d, %d, '%s', '%s', NULL, 'overdue', %d, 0.0)",
                            userId, bookId, 
                            sdf.format(new Date(borrowDate)), 
                            sdf.format(new Date(dueDate)),
                            i % 3
                        );
                        stmt.executeUpdate(sql);
                        inserted++;
                        out.println("<p class='success'>✅ 已插入逾期记录 #" + (overdueCount + inserted) + "</p>");
                    } catch (Exception e) {
                        out.println("<p class='error'>❌ 插入失败 #" + (i+1) + " - " + e.getMessage() + "</p>");
                    }
                }
                out.println("<p class='success'>✅ 成功插入 " + inserted + " 条逾期记录</p>");
            } else {
                out.println("<p class='info'>逾期记录已足够：" + overdueCount + " 条</p>");
            }
            
            // 3. 插入罚款记录
            out.println("<h3>2. 插入罚款记录</h3>");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM fine_records");
            rs.next();
            int fineCount = rs.getInt(1);
            rs.close();
            
            out.println("<p class='info'>当前罚款记录数: " + fineCount + " 条</p>");
            
            if (fineCount < 30) {
                int inserted = 0;
                int needInsert = 30 - fineCount;
                
                out.println("<p class='info'>需要插入 " + needInsert + " 条罚款记录</p>");
                
                // 获取逾期借阅记录ID
                rs = stmt.executeQuery("SELECT id, user_id FROM borrow_records WHERE status='overdue' LIMIT " + needInsert);
                int[] borrowIds = new int[needInsert];
                int[] userIds = new int[needInsert];
                int idx = 0;
                while (rs.next() && idx < needInsert) {
                    borrowIds[idx] = rs.getInt("id");
                    userIds[idx] = rs.getInt("user_id");
                    idx++;
                }
                rs.close();
                
                for (int i = 0; i < idx; i++) {
                    try {
                        // 计算逾期天数和罚款金额
                        int days = (i % 15) + 1; // 1-15天
                        double amount = days * 0.5; // 每天0.5元
                        
                        // 前20条已支付，后10条未支付
                        String status = i < 20 ? "paid" : "unpaid";
                        
                        stmt.executeUpdate(String.format(
                            "INSERT INTO fine_records(user_id, borrow_record_id, amount, days, status) " +
                            "VALUES(%d, %d, %.2f, %d, '%s')",
                            userIds[i], borrowIds[i], amount, days, status
                        ));
                        inserted++;
                        
                        if (status.equals("unpaid")) {
                            out.println("<p class='success'>✅ 已插入未支付罚款记录 #" + (fineCount + inserted) + " - 金额: ¥" + String.format("%.2f", amount) + "</p>");
                        } else {
                            out.println("<p class='success'>✅ 已插入已支付罚款记录 #" + (fineCount + inserted) + " - 金额: ¥" + String.format("%.2f", amount) + "</p>");
                        }
                    } catch (Exception e) {
                        out.println("<p class='error'>❌ 插入失败 #" + (i+1) + " - " + e.getMessage() + "</p>");
                    }
                }
                out.println("<p class='success'>✅ 成功插入 " + inserted + " 条罚款记录</p>");
            } else {
                out.println("<p class='info'>罚款记录已足够：" + fineCount + " 条</p>");
            }
            
            // 4. 最终统计
            out.println("<h2>📊 最终数据统计</h2>");
            
            out.println("<table border='1' style='width:100%; border-collapse: collapse; margin-bottom: 20px;'>");
            out.println("<tr style='background:#667eea; color:white;'><th>数据类型</th><th>记录数</th></tr>");
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM borrow_records WHERE status='overdue'");
            rs.next();
            out.println("<tr><td>逾期借阅记录</td><td>" + rs.getInt(1) + " 条</td></tr>");
            rs.close();
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM fine_records");
            rs.next();
            out.println("<tr><td>罚款记录</td><td>" + rs.getInt(1) + " 条</td></tr>");
            rs.close();
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM fine_records WHERE status='unpaid'");
            rs.next();
            out.println("<tr><td>未支付罚款</td><td>" + rs.getInt(1) + " 条</td></tr>");
            rs.close();
            
            out.println("</table>");
            
            out.println("<div style='margin-top: 20px;'>");
            out.println("<a href='login.jsp' class='btn' style='background: #28a745;'>🔐 登录系统</a>");
            out.println("<a href='admin/borrowServlet?action=overdue' class='btn' style='background: #dc3545;'>⚠️ 查看逾期列表</a>");
            out.println("<a href='admin/index.jsp' class='btn'>👨‍💼 管理员首页</a>");
            out.println("</div>");
            
            out.println("</div></body></html>");
            
        } catch (Exception e) {
            out.println("<h3 class='error'>❌ 错误：" + e.getMessage() + "</h3>");
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
}
