<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>借阅排行</title>
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
        .table-container { background: white; border-radius: 10px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        table { width: 100%; border-collapse: collapse; }
        table th { background: #667eea; color: white; padding: 12px; text-align: left; }
        table td { padding: 12px; border-bottom: 1px solid #f0f0f0; }
        table tr:hover { background: #f5f5f5; }
        .rank { display: inline-block; width: 30px; height: 30px; line-height: 30px; text-align: center; border-radius: 50%; font-weight: bold; }
        .rank-1 { background: #ffd700; color: white; }
        .rank-2 { background: #c0c0c0; color: white; }
        .rank-3 { background: #cd7f32; color: white; }
        .rank-other { background: #667eea; color: white; }
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
                <li><a href="categoryServlet?action=list">分类管理</a></li>
                <li><a href="borrowServlet?action=list">借阅记录</a></li>
                <li><a href="borrowServlet?action=current">当前借阅</a></li>
                <li><a href="borrowServlet?action=overdue">逾期管理</a></li>
                <li><a href="borrowServlet?action=toBorrow">办理借书</a></li>
                <li><a href="statServlet?action=overview">数据统计</a></li>
            </ul>
        </div>
        
        <div class="main-content">
            <h2 class="page-title">借阅排行榜</h2>
            
            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>排名</th>
                            <th>书名</th>
                            <th>作者</th>
                            <th>借阅次数</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${rankingList}" var="item" varStatus="status">
                            <tr>
                                <td>
                                    <c:choose>
                                        <c:when test="${status.index == 0}">
                                            <span class="rank rank-1">1</span>
                                        </c:when>
                                        <c:when test="${status.index == 1}">
                                            <span class="rank rank-2">2</span>
                                        </c:when>
                                        <c:when test="${status.index == 2}">
                                            <span class="rank rank-3">3</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="rank rank-other">${status.index + 1}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${item[0]}</td>
                                <td>${item[1]}</td>
                                <td>${item[2]}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</body>
</html>