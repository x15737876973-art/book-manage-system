<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>个人信息</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: #f5f5f5; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 30px; }
        .header h1 { font-size: 24px; }
        .container { padding: 30px; }
        .profile-container { display: flex; gap: 20px; max-width: 1000px; margin: 0 auto; }
        .info-box { flex: 1; background: white; border-radius: 10px; padding: 25px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .pwd-box { flex: 1; background: white; border-radius: 10px; padding: 25px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .box-title { font-size: 18px; color: #667eea; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #667eea; }
        .info-item { display: flex; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
        .info-item .label { width: 100px; color: #666; }
        .info-item .value { flex: 1; color: #333; font-weight: bold; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 8px; color: #333; font-weight: bold; }
        .form-group input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; }
        .btn { padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; font-size: 14px; background: #667eea; color: white; }
        .msg { padding: 10px; margin-bottom: 15px; border-radius: 5px; background: #fff3cd; color: #856404; }
    </style>
</head>
<body>
    <div class="header">
        <h1>📚 图书管理系统</h1>
    </div>
    
    <div class="container">
        <div class="profile-container">
            <div class="info-box">
                <h3 class="box-title">个人信息</h3>
                <div class="info-item">
                    <span class="label">用户名：</span>
                    <span class="value">${user.username}</span>
                </div>
                <div class="info-item">
                    <span class="label">姓名：</span>
                    <span class="value">${user.realName}</span>
                </div>
                <div class="info-item">
                    <span class="label">角色：</span>
                    <span class="value">
                        <c:choose>
                            <c:when test="${user.role == 'admin'}">管理员</c:when>
                            <c:otherwise>读者</c:otherwise>
                        </c:choose>
                    </span>
                </div>
                <div class="info-item">
                    <span class="label">电话：</span>
                    <span class="value">${user.phone}</span>
                </div>
                <div class="info-item">
                    <span class="label">邮箱：</span>
                    <span class="value">${user.email}</span>
                </div>
            </div>
            
            <div class="pwd-box">
                <h3 class="box-title">修改密码</h3>
                
                <c:if test="${not empty msg}">
                    <div class="msg">${msg}</div>
                </c:if>
                
                <form action="adminProfileServlet" method="post">
                    <input type="hidden" name="action" value="updatePwd">
                    
                    <div class="form-group">
                        <label>原密码：</label>
                        <input type="password" name="oldPassword" required>
                    </div>
                    
                    <div class="form-group">
                        <label>新密码：</label>
                        <input type="password" name="newPassword" required>
                    </div>
                    
                    <div class="form-group">
                        <label>确认密码：</label>
                        <input type="password" name="confirmPassword" required>
                    </div>
                    
                    <button type="submit" class="btn">修改密码</button>
                </form>
            </div>
        </div>
    </div>
</body>
</html>