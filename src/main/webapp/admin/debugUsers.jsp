<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.book.dao.*" %>
<%@ page import="com.book.entity.*" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>用户数据调试</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 1200px;
            margin: 20px auto;
            padding: 20px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: left;
        }
        th {
            background: #4CAF50;
            color: white;
        }
        tr:nth-child(even) {
            background: #f2f2f2;
        }
        .info {
            background: #e3f2fd;
            padding: 10px;
            border-radius: 5px;
            margin: 10px 0;
        }
        .warning {
            background: #fff3e0;
            padding: 10px;
            border-radius: 5px;
            margin: 10px 0;
            color: #e65100;
        }
        .btn {
            display: inline-block;
            padding: 10px 20px;
            background: #4CAF50;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin: 10px 5px;
        }
        .btn-secondary {
            background: #6c757d;
        }
        form {
            margin: 20px 0;
            padding: 20px;
            background: #f5f5f5;
            border-radius: 5px;
        }
        input[type="text"] {
            padding: 8px;
            width: 200px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }
        button {
            padding: 8px 16px;
            background: #4CAF50;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        button:hover {
            background: #45a049;
        }
    </style>
</head>
<body>
    <h1>用户数据调试页面</h1>
    
    <%
    UserDao userDao = new UserDao();
    
    // 处理更新姓名的请求
    String action = request.getParameter("action");
    if ("updateName".equals(action)) {
        String userId = request.getParameter("userId");
        String newName = request.getParameter("newName");
        if (userId != null && newName != null && !newName.trim().isEmpty()) {
            try {
                User user = userDao.findById(Integer.parseInt(userId));
                if (user != null) {
                    user.setRealName(newName.trim());
                    userDao.update(user);
                    out.println("<p class='info' style='color:green;'>✅ 成功更新用户 " + user.getUsername() + " 的姓名为: " + newName + "</p>");
                }
            } catch (Exception e) {
                out.println("<p class='warning'>❌ 更新失败: " + e.getMessage() + "</p>");
            }
        }
    }
    
    // 获取所有用户
    List<User> allUsers = userDao.findAll();
    List<User> readers = userDao.findReaders();
    List<User> admins = userDao.findAdmins();
    %>
    
    <div class="info">
        <p><strong>用户统计：</strong></p>
        <ul>
            <li>总用户数: <%= allUsers.size() %></li>
            <li>读者数: <%= readers.size() %></li>
            <li>管理员数: <%= admins.size() %></li>
        </ul>
    </div>
    
    <h2>所有用户列表</h2>
    <table>
        <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>姓名 (real_name)</th>
            <th>角色</th>
            <th>状态</th>
            <th>操作</th>
        </tr>
        <% for (User user : allUsers) { %>
        <tr>
            <td><%= user.getId() %></td>
            <td><%= user.getUsername() %></td>
            <td style="color: <%= user.getRealName() == null || user.getRealName().trim().isEmpty() ? "red" : "green" %>;">
                <%= user.getRealName() != null && !user.getRealName().trim().isEmpty() ? user.getRealName() : "(空)" %>
            </td>
            <td><%= user.getRole() %></td>
            <td><%= user.getStatus() %></td>
            <td>
                <form style="display:inline; padding:0; background:none;" method="post">
                    <input type="hidden" name="action" value="updateName">
                    <input type="hidden" name="userId" value="<%= user.getId() %>">
                    <input type="text" name="newName" placeholder="输入姓名" value="<%= user.getRealName() != null ? user.getRealName() : "" %>" style="width:100px;">
                    <button type="submit">更新</button>
                </form>
            </td>
        </tr>
        <% } %>
    </table>
    
    <h2>测试搜索功能</h2>
    <form method="get" action="userList.jsp">
        <label>关键词（用户名或姓名）：</label>
        <input type="text" name="keyword" placeholder="输入搜索关键词">
        <br><br>
        <label>身份：</label>
        <select name="role">
            <option value="">全部</option>
            <option value="reader">读者</option>
            <option value="admin">管理员</option>
        </select>
        <br><br>
        <label>状态：</label>
        <select name="status">
            <option value="">全部</option>
            <option value="active">正常</option>
            <option value="disabled">禁用</option>
        </select>
        <br><br>
        <button type="submit">搜索</button>
        <a href="userList.jsp" class="btn btn-secondary" style="display:inline-block; line-height:20px;">重置</a>
    </form>
    
    <br>
    <a href="userList.jsp" class="btn">返回用户管理</a>
</body>
</html>