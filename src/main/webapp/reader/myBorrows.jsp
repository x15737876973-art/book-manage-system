<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>我的借阅 - 图书管理系统</title>
    <link rel="stylesheet" href="../css/style.css">
    <link rel="stylesheet" href="../css/supplement.css">
    <script src="../js/common.js"></script>
</head>
<body>
    <div class="header">
        <h1>
            <div class="logo-icon">
                <span class="library-icon">🏛️</span>
                <span class="book-icon">📚</span>
                <span>图书管理系统</span>
            </div>
        </h1>
        <div class="user-info">
            <span class="username">欢迎，${sessionScope.loginUser.realName}（读者）</span>
            <a href="../logoutServlet">退出登录</a>
        </div>
    </div>
    
    <div class="container">
        <div class="sidebar">
            <ul>
                <li><a href="../reader/indexServlet"><span>📊 首页</span></a></li>
                <li><a href="../reader/bookSearchServlet?action=list"><span>🔍 图书检索</span></a></li>
                <li><a href="../reader/borrowOpServlet?action=myBorrows" class="active"><span>📖 我的借阅</span></a></li>
                <li><a href="../reader/borrowOpServlet?action=myReservations"><span>📋 我的预约</span></a></li>
                <li><a href="../reader/borrowOpServlet?action=myFines"><span>💰 我的罚款</span></a></li>
                <li><a href="../reader/profileServlet?action=view"><span>👤 个人中心</span></a></li>
            </ul>
        </div>
        
        <div class="main-content">
            <h2 class="page-title">我的借阅</h2>
            
            <c:if test="${not empty msg}">
                <div class="msg msg-success">${msg}</div>
            </c:if>
            
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">当前借阅</h3>
                </div>
                
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>图书名称</th>
                                <th>借书日期</th>
                                <th>应还日期</th>
                                <th>状态</th>
                                <th>续借次数</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${borrowList}" var="record">
                                <tr>
                                    <td class="text-ellipsis" title="${record.bookName}">${record.bookName}</td>
                                    <td>${record.borrowDate}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${record.status == 'overdue'}">
                                                <span class="important-date">${record.dueDate}（已逾期）</span>
                                            </c:when>
                                            <c:otherwise>
                                                ${record.dueDate}
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${record.status == 'borrowed'}">
                                                <span class="status-badge status-borrowed">借阅中</span>
                                            </c:when>
                                            <c:when test="${record.status == 'overdue'}">
                                                <span class="status-badge status-overdue">已逾期</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge status-active">已归还</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${record.renewCount} / 1</td>
                                    <td>
                                        <c:if test="${record.status == 'borrowed' && record.renewCount < 1}">
                                            <a href="../reader/borrowOpServlet?action=renew&recordId=${record.id}" class="btn btn-info btn-sm">续借</a>
                                        </c:if>
                                        <c:if test="${record.status == 'overdue'}">
                                            <span class="text-danger">请尽快归还</span>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
            
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">借阅历史</h3>
                </div>
                
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>图书名称</th>
                                <th>借书日期</th>
                                <th>应还日期</th>
                                <th>实还日期</th>
                                <th>状态</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${historyList}" var="record">
                                <tr>
                                    <td class="text-ellipsis" title="${record.bookName}">${record.bookName}</td>
                                    <td>${record.borrowDate}</td>
                                    <td>${record.dueDate}</td>
                                    <td>${record.returnDate}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${record.fineAmount > 0}">
                                                <span class="status-badge status-overdue">逾期已还（罚款￥${record.fineAmount}）</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge status-active">正常归还</span>
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
    </div>
</body>
</html>