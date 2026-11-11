<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>分类管理</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: #f5f5f5; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .header h1 { font-size: 24px; }
        .header .user-info a { color: white; text-decoration: none; padding: 8px 15px; border: 1px solid white; border-radius: 5px; }
        .container { display: flex; min-height: calc(100vh - 60px); }
        .sidebar { width: 220px; background: white; box-shadow: 2px 0 10px rgba(0,0,0,0.05); }
        .sidebar ul { list-style: none; }
        .sidebar ul li a { display: block; padding: 15px 25px; color: #333; text-decoration: none; }
        .sidebar ul li a:hover { background: #f5f5f5; color: #667eea; }
        .sidebar ul li a.active { background: #667eea; color: white; }
        .main-content { flex: 1; padding: 20px; }
        .page-title { font-size: 20px; color: #333; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #667eea; }
        .content-wrapper { display: flex; gap: 20px; }
        .form-box { flex: 1; background: white; border-radius: 10px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .list-box { flex: 2; background: white; border-radius: 10px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .form-title { font-size: 16px; color: #667eea; margin-bottom: 15px; font-weight: bold; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 8px; color: #333; }
        .form-group input, .form-group textarea { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
        .btn { padding: 8px 16px; border: none; border-radius: 5px; cursor: pointer; font-size: 14px; }
        .btn-primary { background: #667eea; color: white; }
        .btn-warning { background: #ffc107; color: #333; }
        .btn-danger { background: #dc3545; color: white; }
        .btn-sm { padding: 5px 10px; font-size: 12px; }
        table { width: 100%; border-collapse: collapse; }
        table th { background: #667eea; color: white; padding: 10px; text-align: left; }
        table td { padding: 10px; border-bottom: 1px solid #f0f0f0; }
        .msg { padding: 10px; margin-bottom: 15px; border-radius: 5px; background: #fff3cd; color: #856404; }
    </style>
</head>
<body>
    <div class="header">
        <h1>📚 图书管理系统</h1>
        <div class="user-info">
            <a href="../logoutServlet">退出登录</a>
        </div>
    </div>
    
    <div class="container">
        <div class="sidebar">
            <ul>
                <li><a href="indexServlet">首页</a></li>
                <li><a href="userServlet?action=list&role=reader">读者管理</a></li>
                <li><a href="bookManageServlet?action=list">图书管理</a></li>
                <li><a href="categoryServlet?action=list" class="active">分类管理</a></li>
                <li><a href="borrowServlet?action=list">借阅记录</a></li>
                <li><a href="borrowServlet?action=current">当前借阅</a></li>
                <li><a href="borrowServlet?action=overdue">逾期管理</a></li>
                <li><a href="borrowServlet?action=toBorrow">办理借书</a></li>
                <li><a href="statServlet?action=overview">数据统计</a></li>
            </ul>
        </div>
        
        <div class="main-content">
            <h2 class="page-title">分类管理</h2>
            
            <c:if test="${not empty msg}">
                <div class="msg">${msg}</div>
            </c:if>
            
            <div class="content-wrapper">
                <div class="form-box">
                    <h3 class="form-title">添加/编辑分类</h3>
                    <form action="categoryServlet" method="post">
                        <input type="hidden" name="action" value="add">
                        <div class="form-group">
                            <label>分类名称：</label>
                            <input type="text" name="name" required>
                        </div>
                        <div class="form-group">
                            <label>描述：</label>
                            <textarea name="description" rows="3"></textarea>
                        </div>
                        <button type="submit" class="btn btn-primary">添加</button>
                    </form>
                </div>
                
                <div class="list-box">
                    <h3 class="form-title">分类列表</h3>
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>分类名称</th>
                                <th>描述</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${categoryList}" var="cat">
                                <tr>
                                    <td>${cat.id}</td>
                                    <td>${cat.name}</td>
                                    <td>${cat.description}</td>
                                    <td>
                                        <a href="categoryServlet?action=delete&id=${cat.id}" class="btn btn-danger btn-sm" onclick="return confirm('确定删除？')">删除</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</body>
</html>