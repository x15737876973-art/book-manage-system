<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.book.dao.BookDao" %>
<%@ page import="com.book.entity.Book" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>图书数据诊断</title>
    <style>
        body { font-family: Arial, sans-serif; padding: 20px; background: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .info-box { background: #e7f3ff; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
        .error-box { background: #fff3cd; padding: 15px; border-radius: 5px; margin-bottom: 20px; border-left: 4px solid #ffc107; }
        .success-box { background: #d4edda; padding: 15px; border-radius: 5px; margin-bottom: 20px; border-left: 4px solid #28a745; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:nth-child(even) { background-color: #f2f2f2; }
        .btn { display: inline-block; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 4px; margin: 5px; }
        .btn:hover { background: #0056b3; }
    </style>
</head>
<body>
    <div class="container">
        <h2>📊 图书数据诊断页面</h2>
        
        <%
            try {
                BookDao bookDao = new BookDao();
                List<Book> books = bookDao.findAll();
                int count = bookDao.count();
        %>
        
        <div class="info-box">
            <p><strong>图书总数：</strong><%= count %> 本</p>
            <p><strong>查询结果：</strong><%= books.size() %> 条记录</p>
        </div>
        
        <%
                if (books.isEmpty()) {
        %>
        <div class="error-box">
            <h3>⚠️ 数据库中没有图书数据！</h3>
            <p>请先导入图书数据：</p>
            <ol>
                <li>点击下方"导入示例图书"按钮</li>
                <li>或通过"新增图书"手动添加</li>
            </ol>
            <a href="importBooksServlet" class="btn" onclick="return confirm('确定要导入20本示例图书数据吗？')">📚 导入示例图书</a>
        </div>
        <%
                } else {
        %>
        <div class="success-box">
            <h3>✅ 数据库中有图书数据</h3>
            <p>如果图书列表页面不显示，可能是JSP标签库问题。</p>
        </div>
        
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>图书编号</th>
                    <th>书名</th>
                    <th>作者</th>
                    <th>分类</th>
                    <th>库存</th>
                    <th>可借</th>
                </tr>
            </thead>
            <tbody>
                <%
                    for (Book book : books) {
                %>
                <tr>
                    <td><%= book.getId() %></td>
                    <td><%= book.getBid() %></td>
                    <td><%= book.getBname() %></td>
                    <td><%= book.getAuthor() %></td>
                    <td><%= book.getCategoryName() != null ? book.getCategoryName() : "未分类" %></td>
                    <td><%= book.getStock() %></td>
                    <td><%= book.getAvailableStock() %></td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>
        <%
                }
            } catch (Exception e) {
        %>
        <div class="error-box">
            <h3>❌ 发生错误</h3>
            <p><%= e.getMessage() %></p>
            <pre><%= e.getClass().getName() %></pre>
        </div>
        <%
            }
        %>
        
        <div style="margin-top: 20px;">
            <a href="bookManageServlet?action=list" class="btn">📖 返回图书列表</a>
            <a href="indexServlet" class="btn" style="background: #6c757d;">🏠 返回首页</a>
        </div>
    </div>
</body>
</html>