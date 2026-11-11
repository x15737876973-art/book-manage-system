<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>我的预约</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: #f5f5f5; }
        .header { background: linear-gradient(135deg, #28a745 0%, #20c997 100%); color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .header h1 { font-size: 24px; }
        .header .user-info a { color: white; text-decoration: none; padding: 8px 15px; border: 1px solid white; border-radius: 5px; }
        .container { display: flex; min-height: calc(100vh - 60px); }
        .sidebar { width: 220px; background: white; box-shadow: 2px 0 10px rgba(0,0,0,0.05); }
        .sidebar ul { list-style: none; }
        .sidebar ul li a { display: block; padding: 15px 25px; color: #333; text-decoration: none; }
        .sidebar ul li a:hover { background: #f5f5f5; color: #28a745; }
        .sidebar ul li a.active { background: #28a745; color: white; }
        .main-content { flex: 1; padding: 20px; }
        .page-title { font-size: 20px; color: #333; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #28a745; }
        .table-container { background: white; border-radius: 10px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        table { width: 100%; border-collapse: collapse; }
        table th { background: #28a745; color: white; padding: 12px; text-align: left; }
        table td { padding: 12px; border-bottom: 1px solid #f0f0f0; }
        table tr:hover { background: #f5f5f5; }
        .btn { padding: 5px 10px; border: none; border-radius: 5px; cursor: pointer; font-size: 12px; }
        .btn-success { background: #28a745; color: white; }
        .btn-danger { background: #dc3545; color: white; }
        .status-waiting { color: #ffc107; }
        .status-completed { color: #28a745; }
        .status-cancelled { color: #6c757d; }
        .msg { padding: 10px; margin-bottom: 20px; border-radius: 5px; background: #fff3cd; color: #856404; }
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
                <li><a href="../reader/indexServlet">首页</a></li>
                <li><a href="../reader/bookSearchServlet?action=list">图书检索</a></li>
                <li><a href="borrowOpServlet?action=myBorrows">我的借阅</a></li>
                <li><a href="borrowOpServlet?action=myReservations" class="active">我的预约</a></li>
                <li><a href="borrowOpServlet?action=myFines">我的罚款</a></li>
                <li><a href="profileServlet?action=view">个人中心</a></li>
            </ul>
        </div>
        
        <div class="main-content">
            <h2 class="page-title">我的预约</h2>
            
            <c:if test="${not empty msg}">
                <div class="msg">${msg}</div>
            </c:if>
            
            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>图书</th>
                            <th>预约时间</th>
                            <th>状态</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${reservationList}" var="res">
                            <tr>
                                <td>${res.bookName}</td>
                                <td>${res.reserveDate}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${res.status == 'waiting'}">
                                            <span class="status-waiting">等待中</span>
                                        </c:when>
                                        <c:when test="${res.status == 'completed'}">
                                            <span class="status-completed">已完成</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="status-cancelled">已取消</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:if test="${res.status == 'waiting'}">
                                        <a href="borrowOpServlet?action=borrow&bookId=${res.bookId}" class="btn btn-success">借阅</a>
                                        <a href="borrowOpServlet?action=cancelReservation&reservationId=${res.id}" class="btn btn-danger">取消</a>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</body>
</html>