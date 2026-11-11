<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>用户管理 - 图书管理系统</title>
    <link rel="stylesheet" href="../css/style.css">
    <link rel="stylesheet" href="../css/supplement.css">
    <style>
        .message {
            padding: 12px 20px;
            margin-bottom: 20px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 500;
            animation: slideDown 0.3s ease;
        }
        
        @keyframes slideDown {
            from {
                opacity: 0;
                transform: translateY(-10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .message.success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        
        .message.error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
    </style>
</head>
<body>
    <div class="admin-container">
        <!-- 侧边栏 -->
        <div class="sidebar">
            <div class="logo">
                <span class="logo-icon">🏛️📚</span>
                <span class="logo-text">图书管理系统</span>
            </div>
            <ul class="menu">
                <li><a href="indexServlet"><span class="icon">📊</span>系统首页</a></li>
                <li><a href="bookList.jsp"><span class="icon">📚</span>图书管理</a></li>
                <li class="active"><a href="userList.jsp"><span class="icon">👥</span>读者管理</a></li>
                <li><a href="borrowList.jsp"><span class="icon">📖</span>借阅管理</a></li>
                <li><a href="overdueList.jsp"><span class="icon">⚠️</span>逾期管理</a></li>
                <li><a href="statOverview.jsp"><span class="icon">📈</span>数据统计</a></li>
            </ul>
        </div>
        
        <!-- 主内容区 -->
        <div class="main-content">
            <!-- 顶部导航 -->
            <div class="top-nav">
                <h2>用户管理</h2>
                <div class="user-info">
                    <span>欢迎，${sessionScope.user.realName}</span>
                    <a href="../logoutServlet" class="btn-logout">退出</a>
                </div>
            </div>
            
            <!-- 内容卡片 -->
            <div class="content-card">
                <!-- 消息提示 -->
                <c:if test="${not empty msg}">
                    <div class="message ${msgType}">
                        ${msg}
                        <a href="userServlet?action=list" style="margin-left: 15px; color: inherit; text-decoration: underline; font-weight: normal;">返回全部用户</a>
                    </div>
                </c:if>
                
                <div class="card-header">
                    <h3>用户列表</h3>
                    <a href="userServlet?action=toAdd&role=${param.role}" class="btn btn-success">+ 新增用户</a>
                </div>
                
                <!-- 搜索表单 -->
                <form action="userServlet" method="get" class="search-form">
                    <input type="hidden" name="action" value="list">
                    <input type="text" name="keyword" placeholder="搜索用户名" value="${param.keyword}">
                    <select name="role">
                        <option value="">全部身份</option>
                        <option value="admin" ${param.role == 'admin' ? 'selected' : ''}>管理员</option>
                        <option value="reader" ${param.role == 'reader' ? 'selected' : ''}>读者</option>
                    </select>
                    <select name="status">
                        <option value="">全部状态</option>
                        <option value="active" ${param.status == 'active' ? 'selected' : ''}>正常</option>
                        <option value="disabled" ${param.status == 'disabled' ? 'selected' : ''}>禁用</option>
                    </select>
                    <button type="submit" class="btn btn-secondary">查询</button>
                </form>
                
                <!-- 用户列表表格 -->
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>用户名</th>
                            <th>姓名</th>
                            <th>身份</th>
                            <th>电话</th>
                            <th>邮箱</th>
                            <th>状态</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:if test="${empty userList}">
                            <tr>
                                <td colspan="8" class="empty-data">
                                    <div class="empty-placeholder">
                                        <div class="empty-icon">📭</div>
                                        <div class="empty-text">暂无用户数据</div>
                                        <div class="empty-tip">点击"新增用户"按钮添加第一个用户</div>
                                    </div>
                                </td>
                            </tr>
                        </c:if>
                        <c:forEach items="${userList}" var="user">
                            <tr>
                                <td>${user.id}</td>
                                <td>${user.username}</td>
                                <td>${user.realName != null ? user.realName : '-'}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${user.role == 'admin'}">
                                            <span class="badge badge-primary">管理员</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-info">读者</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${user.phone}</td>
                                <td>${user.email}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${user.status == 'active'}">
                                            <span class="badge badge-success">正常</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-danger">禁用</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <a href="userServlet?action=toEdit&id=${user.id}&role=${param.role}" class="btn btn-sm btn-primary">编辑</a>
                                    <c:choose>
                                        <c:when test="${user.status == 'active'}">
                                            <a href="userServlet?action=toggleStatus&id=${user.id}&status=active&role=${param.role}" 
                                               class="btn btn-sm btn-warning" 
                                               onclick="return confirm('确定要禁用该用户吗？')">禁用</a>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="userServlet?action=toggleStatus&id=${user.id}&status=disabled&role=${param.role}" 
                                               class="btn btn-sm btn-success">启用</a>
                                        </c:otherwise>
                                    </c:choose>
                                    <a href="userServlet?action=resetPwd&id=${user.id}&role=${param.role}" 
                                       class="btn btn-sm btn-secondary"
                                       onclick="return confirm('确定要重置该用户密码为123456吗？')">重置密码</a>
                                    <a href="userServlet?action=delete&id=${user.id}&role=${param.role}" 
                                       class="btn btn-sm btn-danger"
                                       onclick="return confirm('确定要删除该用户吗？此操作不可恢复！')">删除</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    
    <script src="../js/common.js"></script>
</body>
</html>