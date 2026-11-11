<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.book.dao.UserDao" %>
<%@ page import="com.book.entity.User" %>
<%@ page import="java.util.List" %>
<%
    // 更新用户的姓名字段
    UserDao userDao = new UserDao();
    List<User> users = userDao.findAll();
    
    int updated = 0;
    for (User user : users) {
        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            String defaultName = "admin".equals(user.getRole()) ? "管理员" : "读者";
            user.setRealName(defaultName + user.getId());
            user.setStatus("active");
            userDao.update(user);
            updated++;
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>更新用户姓名</title>
    <style>
        body { font-family: Arial, sans-serif; padding: 50px; background: #f5f5f5; }
        .container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { color: #333; }
        .success { color: #28a745; font-size: 18px; margin: 20px 0; }
        .info { color: #666; margin: 10px 0; }
        a { display: inline-block; margin-top: 20px; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 5px; }
        a:hover { background: #0056b3; }
    </style>
</head>
<body>
    <div class="container">
        <h1>更新用户姓名完成</h1>
        <div class="success">✓ 成功更新 <%= updated %> 个用户的姓名字段</div>
        <div class="info">总用户数: <%= users.size() %></div>
        <div class="info">现在可以在用户管理页面搜索姓名了</div>
        <a href="admin/userList.jsp">返回用户管理</a>
    </div>
</body>
</html>