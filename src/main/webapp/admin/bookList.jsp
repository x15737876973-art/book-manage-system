
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>图书管理 - 图书管理系统</title>
    <link rel="stylesheet" href="../css/style.css">
    <style>
        .msg-error {
            background: #f8d7da;
            color: #721c24;
            padding: 10px;
            border-radius: 4px;
            margin-bottom: 10px;
            border: 1px solid #f5c6cb;
        }
        .msg-success {
            background: #d4edda;
            color: #155724;
            padding: 10px;
            border-radius: 4px;
            margin-bottom: 10px;
            border: 1px solid #c3e6cb;
        }
    </style>
    <script src="../js/common.js"></script>
</head>
<body>
    <div class="header">
        <h1>📚 图书管理系统</h1>
        <div class="user-info">
            <span class="username">欢迎，${sessionScope.loginUser.realName}（管理员）</span>
            <a href="../logoutServlet">退出登录</a>
        </div>
    </div>
    
    <div class="container">
        <div class="sidebar">
            <ul>
                <li><a href="indexServlet"><span>📊 首页</span></a></li>
                <li><a href="userServlet?action=list"><span>👥 用户管理</span></a></li>
                <li><a href="bookManageServlet?action=list" class="active"><span>📚 图书管理</span></a></li>
                <li><a href="categoryServlet?action=list"><span>📂 分类管理</span></a></li>
                <li><a href="borrowServlet?action=current"><span>📖 借阅管理</span></a></li>
                <li><a href="borrowServlet?action=overdue"><span>⚠️ 逾期管理</span></a></li>
                <li><a href="statServlet?action=overview"><span>📈 数据统计</span></a></li>
                <li><a href="statServlet?action=ranking"><span>🏆 借阅排行</span></a></li>
            </ul>
        </div>
        
        <div class="main-content">
            <h2 class="page-title">图书管理</h2>

            <c:if test="${not empty msg}">
                <div class="msg ${msgType == 'success' ? 'msg-success' : 'msg-error'}">
                    ${msg}
                    <c:if test="${msgType == 'success' or msgType == 'error'}">
                        <a href="bookManageServlet?action=list" style="margin-left: 10px; color: #666;">返回全部图书</a>
                    </c:if>
                </div>
            </c:if>
            
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">图书列表</h3>
                    <a href="bookManageServlet?action=add" class="btn btn-success">➕ 新增图书</a>
                </div>
                
                <div class="toolbar">
                    <div class="search-box">
                        <form action="bookManageServlet" method="get" style="display: flex; gap: 10px;">
                            <input type="hidden" name="action" value="search">
                            <select name="categoryId">
                                <option value="">全部分类</option>
                                <c:forEach items="${categoryList}" var="cat">
                                    <option value="${cat.id}" ${param.categoryId == cat.id ? 'selected' : ''}>${cat.name}</option>
                                </c:forEach>
                            </select>
                            <input type="text" name="keyword" placeholder="书名/作者" value="${param.keyword}">
                            <button type="submit" class="btn btn-secondary">🔍 查询</button>
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
                                <th>总库存</th>
                                <th>可借</th>
                                <th>状态</th>
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
                                            <td>${book.stock}</td>
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
                                                        <span class="status-badge status-active">可借</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge status-overdue">已借完</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <a href="bookManageServlet?action=toEdit&bid=${book.bid}" class="btn btn-primary btn-sm">编辑</a>
                                                <a href="bookManageServlet?action=delete&bid=${book.bid}" class="btn btn-danger btn-sm" onclick="return confirm('确定删除该图书吗？')">删除</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="10" style="text-align: center; padding: 30px; color: #999;">
                                            📚 暂无图书数据，请先添加图书
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