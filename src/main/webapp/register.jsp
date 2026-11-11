<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>注册 - 图书管理系统</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 20px;
        }
        
        .register-container {
            background: white;
            border-radius: 16px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
            width: 500px;
            max-width: 100%;
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
        
        .register-header {
            text-align: center;
            margin-bottom: 30px;
        }
        
        .register-header h1 {
            font-size: 28px;
            color: #333;
            margin-bottom: 10px;
        }
        
        .register-header p {
            color: #999;
            font-size: 14px;
        }
        
        .register-form .form-group {
            margin-bottom: 20px;
        }
        
        .register-form .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #555;
            font-weight: 500;
        }
        
        .register-form .form-group input {
            width: 100%;
            padding: 12px 15px;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 14px;
            transition: all 0.3s;
        }
        
        .register-form .form-group input:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }
        
        .register-form .btn-register {
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
        
        .register-form .btn-register:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }
        
        .register-footer {
            text-align: center;
            margin-top: 25px;
            padding-top: 20px;
            border-top: 1px solid #f0f0f0;
        }
        
        .register-footer p {
            color: #999;
            font-size: 13px;
        }
        
        .register-footer a {
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
        }
        
        .register-footer a:hover {
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
        
        .form-row {
            display: flex;
            gap: 15px;
        }
        
        .form-row .form-group {
            flex: 1;
        }
        
        .password-hint {
            font-size: 12px;
            color: #999;
            margin-top: 5px;
        }
        
        .password-hint.error {
            color: #dc3545;
        }
        
        .password-hint.success {
            color: #28a745;
        }
    </style>
</head>
<body>
    <div class="register-container">
        <div class="register-header">
            <h1>📚 读者注册</h1>
            <p>创建您的账号，开始借阅之旅</p>
        </div>
        
        <c:if test="${not empty msg}">
            <div class="error-msg">${msg}</div>
        </c:if>
        
        <form class="register-form" action="registerServlet" method="post" onsubmit="return validateForm()">
            <div class="form-group">
                <label>用户名 *</label>
                <input type="text" name="username" placeholder="请输入用户名（用于登录）" required>
            </div>
            
            <div class="form-group">
                <label>真实姓名 *</label>
                <input type="text" name="realName" placeholder="请输入您的真实姓名" required>
            </div>
            
            <div class="form-row">
                <div class="form-group">
                    <label>密码 *（至少6位）</label>
                    <input type="password" name="password" id="password" placeholder="请输入密码（至少6位）" required minlength="6" oninput="checkPassword()">
                    <div id="passwordHint" class="password-hint"></div>
                </div>
                
                <div class="form-group">
                    <label>确认密码 *</label>
                    <input type="password" name="password2" id="password2" placeholder="请再次输入密码" required minlength="6" oninput="checkPassword()">
                    <div id="password2Hint" class="password-hint"></div>
                </div>
            </div>
            
            <div class="form-row">
                <div class="form-group">
                    <label>电话</label>
                    <input type="text" name="phone" placeholder="请输入联系电话">
                </div>
                
                <div class="form-group">
                    <label>邮箱</label>
                    <input type="email" name="email" placeholder="请输入邮箱地址">
                </div>
            </div>
            
            <button type="submit" class="btn-register">立即注册</button>
        </form>
        
        <div class="register-footer">
            <p>已有账号？<a href="login.jsp">立即登录</a></p>
        </div>
    </div>
    
    <script>
        function checkPassword() {
            var password = document.getElementById('password').value;
            var password2 = document.getElementById('password2').value;
            var passwordHint = document.getElementById('passwordHint');
            var password2Hint = document.getElementById('password2Hint');
            
            // 检查密码长度
            if (password.length > 0 && password.length < 6) {
                passwordHint.textContent = '密码长度必须至少6位';
                passwordHint.className = 'password-hint error';
            } else if (password.length >= 6) {
                passwordHint.textContent = '✓ 密码长度符合要求';
                passwordHint.className = 'password-hint success';
            } else {
                passwordHint.textContent = '';
                passwordHint.className = 'password-hint';
            }
            
            // 检查两次密码是否一致
            if (password2.length > 0) {
                if (password === password2) {
                    password2Hint.textContent = '✓ 两次密码一致';
                    password2Hint.className = 'password-hint success';
                } else {
                    password2Hint.textContent = '✗ 两次密码不一致';
                    password2Hint.className = 'password-hint error';
                }
            } else {
                password2Hint.textContent = '';
                password2Hint.className = 'password-hint';
            }
        }
        
        function validateForm() {
            var password = document.getElementById('password').value;
            var password2 = document.getElementById('password2').value;
            
            // 验证密码长度
            if (password.length < 6) {
                alert('密码长度必须至少6位！');
                return false;
            }
            
            // 验证两次密码是否一致
            if (password !== password2) {
                alert('两次输入的密码不一致！');
                return false;
            }
            
            return true;
        }
    </script>
</body>
</html>