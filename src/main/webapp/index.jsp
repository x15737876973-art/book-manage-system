<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>图书管理系统</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; display: flex; justify-content: center; align-items: center; }
        .welcome-container { text-align: center; color: white; }
        .welcome-container h1 { font-size: 48px; margin-bottom: 20px; }
        .welcome-container p { font-size: 18px; margin-bottom: 30px; opacity: 0.9; }
        .btn-group { display: flex; gap: 20px; justify-content: center; }
        .btn { padding: 15px 40px; border: 2px solid white; border-radius: 30px; color: white; text-decoration: none; font-size: 16px; font-weight: 600; transition: all 0.3s; }
        .btn:hover { background: white; color: #667eea; }
        .btn-primary { background: white; color: #667eea; }
        .btn-primary:hover { background: transparent; color: white; }
    </style>
</head>
<body>
    <div class="welcome-container">
        <h1>📚 图书管理系统</h1>
        <p>欢迎使用图书管理系统，请登录或注册以使用系统功能</p>
        <div class="btn-group">
            <a href="login.jsp" class="btn btn-primary">登录</a>
            <a href="register.jsp" class="btn">注册</a>
        </div>
    </div>
</body>
</html>
