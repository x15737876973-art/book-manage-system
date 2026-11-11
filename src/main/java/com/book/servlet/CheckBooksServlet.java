package com.book.servlet;

import com.book.util.DBUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

/**
 * 检查数据库图书数据的Servlet
 */
@WebServlet("/admin/checkBooks")
public class CheckBooksServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        StringBuilder result = new StringBuilder();
        result.append("<html><head><title>检查数据库</title></head><body>");
        result.append("<h2>数据库检查结果</h2>");
        
        try {
            Connection conn = DBUtil.getConn();
            Statement stmt = conn.createStatement();
            
            // 检查分类
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
            rs.next();
            int categoryCount = rs.getInt(1);
            rs.close();
            result.append("<p>分类数量: " + categoryCount + "</p>");
            
            // 检查图书
            rs = stmt.executeQuery("SELECT COUNT(*) FROM books");
            rs.next();
            int bookCount = rs.getInt(1);
            rs.close();
            result.append("<p>图书数量: " + bookCount + "</p>");
            
            // 检查读者
            rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role='reader'");
            rs.next();
            int readerCount = rs.getInt(1);
            rs.close();
            result.append("<p>读者数量: " + readerCount + "</p>");
            
            // 显示图书列表
            if (bookCount > 0) {
                result.append("<h3>图书列表（前10本）:</h3>");
                result.append("<table border='1'><tr><th>ID</th><th>编号</th><th>书名</th><th>作者</th><th>库存</th></tr>");
                rs = stmt.executeQuery("SELECT id, bid, bname, author, stock FROM books LIMIT 10");
                while (rs.next()) {
                    result.append("<tr>");
                    result.append("<td>" + rs.getInt("id") + "</td>");
                    result.append("<td>" + rs.getString("bid") + "</td>");
                    result.append("<td>" + rs.getString("bname") + "</td>");
                    result.append("<td>" + rs.getString("author") + "</td>");
                    result.append("<td>" + rs.getInt("stock") + "</td>");
                    result.append("</tr>");
                }
                rs.close();
                result.append("</table>");
            } else {
                result.append("<p style='color:red;'>⚠️ 数据库中没有图书数据！</p>");
                result.append("<p><a href='insertBooksNow'>点击这里插入30本图书数据</a></p>");
            }
            
            conn.close();
            result.append("<p><a href='bookManageServlet?action=list'>返回图书列表</a></p>");
            
        } catch (Exception e) {
            result.append("<p style='color:red;'>错误: " + e.getMessage() + "</p>");
            e.printStackTrace();
        }
        
        result.append("</body></html>");
        response.getWriter().write(result.toString());
    }
}
