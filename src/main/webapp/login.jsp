<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>登录 - 图书管理系统</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/supplement.css">
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }
        
        .login-container {
            background: white;
            border-radius: 16px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
            width: 420px;
            padding: 40px;
            animation: slideUp 0.5s ease;


        }
        
        @keyframes slideUp {
            from {
                transform: translateY(30px);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
            }
        }
        
        .login-header {
            text-align: center;
            margin-bottom: 30px;
        }
        
        .login-header h1 {
            font-size: 28px;
            color: #333;
            margin-bottom: 10px;
        }
        
        .login-header p {
            color: #999;
            font-size: 14px;
        }
        
        .login-form .form-group {
            margin-bottom: 20px;
        }
        
        .login-form .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #555;
            font-weight: 500;
        }
        
        .login-form .form-group input,
        .login-form .form-group select {
            width: 100%;
            padding: 12px 15px;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 14px;
            transition: all 0.3s;
        }
        
        .login-form .form-group input:focus,
        .login-form .form-group select:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }
        
        .login-form .btn-login {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            margin-top: 10px;
        }
        
        .login-form .btn-login:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }
        
        .login-footer {
            text-align: center;
            margin-top: 25px;
            padding-top: 20px;
            border-top: 1px solid #f0f0f0;
        }
        
        .login-footer p {
            color: #999;
            font-size: 13px;
            margin-bottom: 10px;
        }
        
        .login-footer a {
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
        }
        
        .login-footer a:hover {
            text-decoration: underline;
        }
        
        .error-msg {
            background: #f8d7da;
            color: #721c24;
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 20px;
            text-align: center;
            font-size: 14px;
        }
        
        .demo-account {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            margin-top: 15px;
            font-size: 12px;
            color: #666;
        }
        
        .demo-account strong {
            color: #333;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="login-header">
            <h1>🏛️📚 图书管理系统</h1>
            <p>欢迎回来，请登录您的账号</p>
        </div>
        
        <c:if test="${not empty msg}">
            <div class="error-msg">${msg}</div>
        </c:if>
        
        <form class="login-form" action="loginServlet" method="post">
            <div class="form-group">
                <label>用户名</label>
                <input type="text" name="username" placeholder="请输入用户名" required>
            </div>
            
            <div class="form-group">
                <label>密码</label>
                <input type="password" name="password" placeholder="请输入密码" required>
            </div>
            
            <div class="form-group">
                <label>登录身份</label>
                <select name="role" required>
                    <option value="">请选择身份</option>
                    <option value="admin">管理员</option>
                    <option value="reader">读者</option>
                </select>
            </div>
            
            <button type="submit" class="btn-login">登 录</button>
        </form>
        
        <div class="login-footer">
            <p>还没有账号？<a href="register.jsp">立即注册</a></p>
            
            <div class="demo-account">
                <strong>演示账号：</strong><br>
                管理员：admin / 123456<br>
                读者：reader01 / 123456
            </div>
        </div>
    </div>
</body>
</html>