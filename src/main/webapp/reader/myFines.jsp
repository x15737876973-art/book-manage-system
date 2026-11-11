<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>我的罚款</title>
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
        .summary-box { background: white; border-radius: 10px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); display: flex; justify-content: space-between; align-items: center; }
        .summary-box .amount { font-size: 24px; color: #dc3545; font-weight: bold; }
        .table-container { background: white; border-radius: 10px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        table { width: 100%; border-collapse: collapse; }
        table th { background: #dc3545; color: white; padding: 12px; text-align: left; }
        table td { padding: 12px; border-bottom: 1px solid #f0f0f0; }
        table tr:hover { background: #fff5f5; }
        .status-unpaid { color: #dc3545; font-weight: bold; }
        .status-paid { color: #28a745; }
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
                <li><a href="borrowOpServlet?action=myReservations">我的预约</a></li>
                <li><a href="borrowOpServlet?action=myFines" class="active">我的罚款</a></li>
                <li><a href="profileServlet?action=view">个人中心</a></li>
            </ul>
        </div>
        
        <div class="main-content">
            <h2 class="page-title">我的罚款</h2>
            
            <c:if test="${not empty msg}">
                <div class="msg">${msg}</div>
            </c:if>
            
            <div class="summary-box">
                <span>未支付罚款总额：</span>
                <span class="amount">${unpaidAmount} 元</span>
            </div>
            
            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>图书</th>
                            <th>逾期天数</th>
                            <th>罚款金额</th>
                            <th>状态</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${fineList}" var="fine">
                            <tr>
                                <td>${fine.bookName}</td>
                                <td>${fine.days}天</td>
                                <td>${fine.amount}元</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${fine.status == 'unpaid'}">
                                            <span class="status-unpaid">未支付</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="status-paid">已支付</span>
                                        </c:otherwise>
                                    </c:choose>
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