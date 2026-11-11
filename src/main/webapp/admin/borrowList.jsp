<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>借阅管理 - 图书管理系统</title>
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
            <span class="username">欢迎，${sessionScope.loginUser.realName}（管理员）</span>
            <a href="../logoutServlet">退出登录</a>
        </div>
    </div>
    
    <div class="container">
        <div class="sidebar">
            <ul>
                <li><a href="indexServlet"><span class="menu-icon">📊</span><span>首页</span></a></li>
                <li><a href="userServlet?action=list"><span class="menu-icon">👥</span><span>用户管理</span></a></li>
                <li><a href="bookManageServlet?action=list"><span class="menu-icon">📚</span><span>图书管理</span></a></li>
                <li><a href="categoryServlet?action=list"><span class="menu-icon">📂</span><span>分类管理</span></a></li>
                <li><a href="borrowServlet?action=list" class="active"><span class="menu-icon">📖</span><span>借阅管理</span></a></li>
                <li><a href="borrowServlet?action=overdue"><span class="menu-icon">⚠️</span><span>逾期管理</span></a></li>
                <li><a href="statServlet?action=overview"><span class="menu-icon">📈</span><span>数据统计</span></a></li>
                <li><a href="statServlet?action=ranking"><span class="menu-icon">🏆</span><span>借阅排行</span></a></li>
            </ul>
        </div>
        
        <div class="main-content">
            <h2 class="page-title">当前借阅</h2>
            
            <c:if test="${not empty msg}">
                <div class="msg msg-success">${msg}</div>
            </c:if>
            
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">借阅列表</h3>
                    <a href="borrowServlet?action=toBorrow" class="btn btn-success">📖 办理借书</a>
                </div>
                
                <div class="toolbar">
                    <div class="search-box">
                        <form action="borrowServlet" method="get" style="display: flex; gap: 10px;">
                            <input type="hidden" name="action" value="search">
                            <input type="text" name="keyword" placeholder="读者姓名/图书名称" value="${param.keyword}">
                            <button type="submit" class="btn btn-secondary">🔍 查询</button>
                        </form>
                    </div>
                </div>
                
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>读者姓名</th>
                                <th>图书名称</th>
                                <th>借书日期</th>
                                <th>应还日期</th>
                                <th>状态</th>
                                <th>续借次数</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty borrowList}">
                                    <c:forEach items="${borrowList}" var="record">
                                        <tr>
                                            <td>${record.id}</td>
                                            <td>${record.realName}</td>
                                            <td class="text-ellipsis" title="${record.bookTitle}">${record.bookTitle}</td>
                                            <td>${record.borrowDate}</td>
                                            <td>${record.dueDate}</td>
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
                                            <td>${record.renewCount}</td>
                                            <td>
                                                <c:if test="${record.status == 'borrowed' || record.status == 'overdue'}">
                                                    <a href="borrowServlet?action=return&recordId=${record.id}" class="btn btn-primary btn-sm" onclick="return confirm('确定要还书吗？')">还书</a>
                                                    <a href="borrowServlet?action=toEditDueDate&recordId=${record.id}" class="btn btn-info btn-sm" style="margin-left: 5px;">编辑归还日期</a>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="8" style="text-align: center; padding: 30px; color: #999;">
                                            📖 暂无借阅记录
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</body>
</html>