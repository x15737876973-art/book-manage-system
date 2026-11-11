<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.book.util.DBUtil" %>
<%@ page import="java.sql.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>图书数据诊断</title>
    <style>
        body { font-family: Arial, sans-serif; padding: 20px; background: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; }
        h1 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 10px; }
        h2 { color: #007bff; margin-top: 30px; }
        table { width: 100%; border-collapse: collapse; margin: 15px 0; }
        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
        th { background-color: #007bff; color: white; }
        tr:nth-child(even) { background-color: #f8f9fa; }
        .success { color: #28a745; font-weight: bold; }
        .error { color: #dc3545; font-weight: bold; }
        .info { background: #e7f3ff; padding: 15px; border-radius: 5px; margin: 10px 0; }
        .btn { display: inline-block; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 4px; margin: 5px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>📊 图书数据诊断</h1>
        
        <%
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            
            try {
                conn = DBUtil.getConn();
                stmt = conn.createStatement();
        %>
        
        <h2>1. 数据库连接状态</h2>
        <p class="success">✅ 数据库连接成功</p>
        
        <h2>2. 图书表结构</h2>
        <%
            rs = stmt.executeQuery("PRAGMA table_info(books)");
        %>
        <table>
            <tr>
                <th>字段名</th>
                <th>数据类型</th>
                <th>是否可空</th>
                <th>默认值</th>
            </tr>
            <%
                while (rs.next()) {
            %>
            <tr>
                <td><%= rs.getString("name") %></td>
                <td><%= rs.getString("type") %></td>
                <td><%= rs.getString("notnull").equals("1") ? "否" : "是" %></td>
                <td><%= rs.getString("dflt_value") %></td>
            </tr>
            <%
                }
                rs.close();
            %>
        </table>
        
        <h2>3. 图书数据统计</h2>
        <%
            rs = stmt.executeQuery("SELECT COUNT(*) FROM books");
            rs.next();
            int bookCount = rs.getInt(1);
            rs.close();
        %>
        <p class="info"><strong>图书总数：</strong><%= bookCount %> 本</p>
        
        <h2>4. 分类数据统计</h2>
        <%
            rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
            rs.next();
            int categoryCount = rs.getInt(1);
            rs.close();
        %>
        <p class="info"><strong>分类总数：</strong><%= categoryCount %> 个</p>
        
        <h2>5. 图书列表（前20本）</h2>
        <%
            rs = stmt.executeQuery("SELECT b.*, c.name as category_name FROM books b LEFT JOIN categories c ON b.category_id = c.id ORDER BY b.id LIMIT 20");
        %>
        <table>
            <tr>
                <th>ID</th>
                <th>编号</th>
                <th>书名</th>
                <th>作者</th>
                <th>分类</th>
                <th>库存</th>
                <th>可借</th>
            </tr>
            <%
                int count = 0;
                while (rs.next()) {
                    count++;
            %>
            <tr>
                <td><%= rs.getInt("id") %></td>
                <td><%= rs.getString("bid") %></td>
                <td><%= rs.getString("bname") %></td>
                <td><%= rs.getString("author") %></td>
                <td><%= rs.getString("category_name") %></td>
                <td><%= rs.getInt("stock") %></td>
                <td><%= rs.getInt("available_stock") %></td>
            </tr>
            <%
                }
                rs.close();
                
                if (count == 0) {
            %>
            <tr>
                <td colspan="7" style="text-align: center; color: #dc3545;">
                    <strong>❌ 没有找到图书数据！</strong>
                </td>
            </tr>
            <%
                }
            %>
        </table>
        
        <h2>6. 分类列表</h2>
        <%
            rs = stmt.executeQuery("SELECT * FROM categories ORDER BY id");
        %>
        <table>
            <tr>
                <th>ID</th>
                <th>分类名称</th>
                <th>描述</th>
                <th>图书数量</th>
            </tr>
            <%
                while (rs.next()) {
                    int catId = rs.getInt("id");
                    Statement stmt2 = conn.createStatement();
                    ResultSet rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM books WHERE category_id = " + catId);
                    rs2.next();
                    int bookNum = rs2.getInt(1);
                    rs2.close();
                    stmt2.close();
            %>
            <tr>
                <td><%= catId %></td>
                <td><%= rs.getString("name") %></td>
                <td><%= rs.getString("description") %></td>
                <td><%= bookNum %> 本</td>
            </tr>
            <%
                }
                rs.close();
            %>
        </table>
        
        <h2>7. 借阅记录统计</h2>
        <%
            rs = stmt.executeQuery("SELECT COUNT(*) FROM borrow_records");
            rs.next();
            int borrowCount = rs.getInt(1);
            rs.close();
        %>
        <p class="info"><strong>借阅记录总数：</strong><%= borrowCount %> 条</p>
        
        <h2>8. 诊断结果</h2>
        <%
            if (bookCount == 0) {
        %>
        <p class="error">❌ 数据库中没有图书数据！请运行插入数据的程序。</p>
        <p><a href="insertTestData.jsp" class="btn">📊 插入测试数据</a></p>
        <%
            } else if (categoryCount == 0) {
        %>
        <p class="error">❌ 数据库中没有分类数据！请先创建分类。</p>
        <%
            } else {
        %>
        <p class="success">✅ 数据库中有 <%= bookCount %> 本图书，<%= categoryCount %> 个分类</p>
        <p><strong>如果图书管理页面仍然没有显示图书，可能是以下原因：</strong></p>
        <ul>
            <li>1. JSTL 标签库未正确加载</li>
            <li>2. Servlet 查询逻辑有问题</li>
            <li>3. 浏览器缓存问题</li>
            <li>4. 服务器未重启</li>
        </ul>
        <p><strong>建议操作：</strong></p>
        <ol>
            <li>重启服务器</li>
            <li>清除浏览器缓存（Ctrl+F5）</li>
            <li>检查 Tomcat 控制台是否有错误信息</li>
        </ol>
        <%
            }
        %>
        
        <div style="margin-top: 30px;">
            <a href="login.jsp" class="btn">🔐 登录系统</a>
            <a href="admin/bookManageServlet?action=list" class="btn">📚 图书管理</a>
            <a href="admin/index.jsp" class="btn">👨‍💼 管理员首页</a>
        </div>
        
        <%
            } catch (Exception e) {
        %>
        <p class="error">❌ 错误：<%= e.getMessage() %></p>
        <pre><%= e.getClass().getName() %>: <%= e.getMessage() %></pre>
        <%
                e.printStackTrace();
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        %>
    </div>
</body>
</html>