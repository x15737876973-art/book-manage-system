<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>图书检索 - 图书管理系统</title>
    <link rel="stylesheet" href="../css/style.css">
    <script src="../js/common.js"></script>
</head>
<body>
    <div class="header">
        <h1>📚 图书管理系统</h1>
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
                <li><a href="../reader/borrowOpServlet?action=myBorrows"><span>📖 我的借阅</span></a></li>
                <li><a href="../reader/borrowOpServlet?action=myReservations"><span>📋 我的预约</span></a></li>
                <li><a href="../reader/borrowOpServlet?action=myFines"><span>💰 我的罚款</span></a></li>
                <li><a href="../reader/profileServlet?action=view"><span>👤 个人中心</span></a></li>
            </ul>
        </div>
        
        <div class="main-content">
            <h2 class="page-title">图书检索</h2>
            
            <c:if test="${not empty msg}">
                <div class="msg msg-success">${msg}</div>
            </c:if>
            
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">搜索图书</h3>
                </div>
                
                <div class="toolbar">
                    <div class="search-box">
                        <form action="../reader/bookSearchServlet" method="get" style="display: flex; gap: 10px; flex-wrap: wrap;">
                            <input type="hidden" name="action" value="search">
                            <select name="categoryId">
                                <option value="">全部分类</option>
                                <c:forEach items="${categoryList}" var="cat">
                                    <option value="${cat.id}" ${param.categoryId == cat.id ? 'selected' : ''}>${cat.name}</option>
                                </c:forEach>
                            </select>
                            <input type="text" name="keyword" placeholder="书名/作者" value="${param.keyword}" style="width: 300px;">
                            <button type="submit" class="btn btn-primary">🔍 搜索</button>
                        </form>
                    </div>
                </div>
                
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>图书编号</th>
                                <th>书名</th>
                                <th>作者</th>
                                <th>分类</th>
                                <th>出版社</th>
                                <th>价格</th>
                                <th>可借</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty bookList}">
                                    <c:forEach items="${bookList}" var="book">
                                        <tr>
                                            <td>${book.bid}</td>
                                            <td class="text-ellipsis" title="${book.bname}">${book.bname}</td>
                                            <td>${book.author}</td>
                                            <td>${book.categoryName}</td>
                                            <td class="text-ellipsis" title="${book.publisher}">${book.publisher}</td>
                                            <td>￥${book.price}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${book.availableStock > 0}">
                                                        <span class="status-badge status-active">${book.availableStock}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge status-overdue">0</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${book.availableStock > 0}">
                                                        <a href="../reader/borrowOpServlet?action=borrow&bookId=${book.id}" class="btn btn-success btn-sm" onclick="return confirm('确定借阅《${book.bname}》吗？')">借阅</a>
                                                        <a href="../reader/borrowOpServlet?action=reserve&bookId=${book.id}" class="btn btn-info btn-sm">预约</a>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted">已借完</span>
                                                        <a href="../reader/borrowOpServlet?action=reserve&bookId=${book.id}" class="btn btn-warning btn-sm">预约</a>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="8" style="text-align: center; padding: 30px; color: #999;">
                                            📚 暂无符合条件的图书数据
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