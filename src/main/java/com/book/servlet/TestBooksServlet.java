package com.book.servlet;

import com.book.dao.BookDao;
import com.book.entity.Book;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 测试图书数据的Servlet
 */
@WebServlet("/admin/testBooksServlet")
public class TestBooksServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        BookDao bookDao = new BookDao();
        
        try {
            List<Book> books = bookDao.findAll();
            int count = bookDao.count();
            
            out.println("<html><head><title>测试图书数据</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; padding: 20px; }");
            out.println("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
            out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            out.println("th { background-color: #4CAF50; color: white; }");
            out.println("tr:nth-child(even) { background-color: #f2f2f2; }");
            out.println(".info { background: #e7f3ff; padding: 15px; border-radius: 5px; margin-bottom: 20px; }");
            out.println("</style>");
            out.println("</head><body>");
            
            out.println("<h2>📊 图书数据测试结果</h2>");
            out.println("<div class='info'>");
            out.println("<p><strong>图书总数：</strong>" + count + " 本</p>");
            out.println("<p><strong>查询结果：</strong>" + books.size() + " 条记录</p>");
            out.println("</div>");
            
            if (books.isEmpty()) {
                out.println("<div style='color: red; padding: 20px; background: #fff3cd; border-radius: 5px;'>");
                out.println("<h3>⚠️ 数据库中没有图书数据！</h3>");
                out.println("<p>请先导入图书数据：</p>");
                out.println("<ol>");
                out.println("<li>访问 <a href='importBooksServlet'>导入示例图书</a></li>");
                out.println("<li>或手动添加图书</li>");
                out.println("</ol>");
                out.println("</div>");
            } else {
                out.println("<table>");
                out.println("<tr><th>ID</th><th>图书编号</th><th>书名</th><th>作者</th><th>分类</th><th>库存</th><th>可借</th></tr>");
                
                for (Book book : books) {
                    out.println("<tr>");
                    out.println("<td>" + book.getId() + "</td>");
                    out.println("<td>" + book.getBid() + "</td>");
                    out.println("<td>" + book.getBname() + "</td>");
                    out.println("<td>" + book.getAuthor() + "</td>");
                    out.println("<td>" + (book.getCategoryName() != null ? book.getCategoryName() : "未分类") + "</td>");
                    out.println("<td>" + book.getStock() + "</td>");
                    out.println("<td>" + book.getAvailableStock() + "</td>");
                    out.println("</tr>");
                }
                
                out.println("</table>");
            }
            
            out.println("<br><a href='bookManageServlet?action=list' style='padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 4px;'>返回图书列表</a>");
            out.println("</body></html>");
            
        } catch (Exception e) {
            out.println("<h3 style='color: red;'>❌ 错误：" + e.getMessage() + "</h3>");
            e.printStackTrace(new PrintWriter(out));
        }
    }
}