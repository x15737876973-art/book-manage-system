<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>编辑用户</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: #f5f5f5; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 30px; }
        .header h1 { font-size: 24px; }
        .container { padding: 30px; }
        .form-container { background: white; border-radius: 10px; padding: 30px; max-width: 600px; margin: 0 auto; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .form-title { font-size: 20px; color: #333; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #667eea; }
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; margin-bottom: 8px; color: #333; font-weight: bold; }
        .form-group input, .form-group select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; }
        .form-group input:focus, .form-group select:focus { border-color: #667eea; outline: none; }
        .btn-group { display: flex; gap: 10px; }
        .btn { padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; font-size: 14px; }
        .btn-primary { background: #667eea; color: white; }
        .btn-secondary { background: #6c757d; color: white; }
        .msg { padding: 10px; margin-bottom: 20px; border-radius: 5px; background: #fff3cd; color: #856404; }
    </style>
</head>
<body>
    <div class="header">
        <h1>📚 图书管理系统</h1>
    </div>
    
    <div class="container">
        <div class="form-container">
            <h2 class="form-title">
                <c:choose>
                    <c:when test="${not empty user}">编辑用户</c:when>
                    <c:otherwise>添加用户</c:otherwise>
                </c:choose>
            </h2>
            
            <c:if test="${not empty msg}">
                <div class="msg">${msg}</div>
            </c:if>
            
            <form action="userServlet" method="post">
                <input type="hidden" name="action" value="${not empty user ? 'update' : 'add'}">
                <c:if test="${not empty user}">
                    <input type="hidden" name="id" value="${user.id}">
                </c:if>
                
                <div class="form-group">
                    <label>用户名：</label>
                    <input type="text" name="username" value="${user.username}" required>
                </div>
                
                <c:if test="${empty user}">
                    <div class="form-group">
                        <label>密码：</label>
                        <input type="text" name="password" placeholder="默认密码123">
                    </div>
                </c:if>
                
                <div class="form-group">
                    <label>角色：</label>
                    <select name="role">
                        <option value="reader" ${user.role == 'reader' ? 'selected' : ''}>读者</option>
                        <option value="admin" ${user.role == 'admin' ? 'selected' : ''}>管理员</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label>姓名：</label>
                    <input type="text" name="realName" value="${user.realName}">
                </div>
                
                <div class="form-group">
                    <label>电话：</label>
                    <input type="text" name="phone" value="${user.phone}">
                </div>
                
                <div class="form-group">
                    <label>邮箱：</label>
                    <input type="email" name="email" value="${user.email}">
                </div>
                
                <div class="btn-group">
                    <button type="submit" class="btn btn-primary">保存</button>
                    <a href="userServlet?action=list&role=${role}" class="btn btn-secondary">返回</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>