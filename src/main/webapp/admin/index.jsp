.<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>图书管理系统 - 管理员</title>
    <link rel="stylesheet" href="../css/style.css">
    <link rel="stylesheet" href="../css/supplement.css">
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
            <span class="username">欢迎，${sessionScope.loginUser != null ? sessionScope.loginUser.realName : '管理员'}（管理员）</span>
            <a href="adminProfileServlet?action=view">个人信息</a>
            <a href="../logoutServlet">退出登录</a>
        </div>
    </div>
    
    <div class="container">
        <div class="sidebar">
            <ul>
                <li><a href="indexServlet" class="active"><span class="menu-icon">📊</span><span>首页</span></a></li>
                <li><a href="userServlet?action=list"><span class="menu-icon">👥</span><span>用户管理</span></a></li>
                <li><a href="bookManageServlet?action=list"><span class="menu-icon">📚</span><span>图书管理</span></a></li>
                <li><a href="categoryServlet?action=list"><span class="menu-icon">📂</span><span>分类管理</span></a></li>
                <li><a href="borrowServlet?action=list"><span class="menu-icon">📖</span><span>借阅管理</span></a></li>
                <li><a href="borrowServlet?action=overdue"><span class="menu-icon">⚠️</span><span>逾期管理</span></a></li>
                <li><a href="statServlet?action=overview"><span class="menu-icon">📈</span><span>数据统计</span></a></li>
                <li><a href="statServlet?action=ranking"><span class="menu-icon">🏆</span><span>借阅排行</span></a></li>
            </ul>
        </div>
        
        <div class="main-content">
            <h2 class="page-title">系统概览</h2>
            
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="icon primary">📚</div>
                    <div class="info">
                        <h3>${totalBooks}</h3>
                        <p>图书总数</p>
                    </div>
                </div>
                
                <div class="stat-card">
                    <div class="icon success">👥</div>
                    <div class="info">
                        <h3>${totalReaders}</h3>
                        <p>注册读者</p>
                    </div>
                </div>
                
                <div class="stat-card">
                    <div class="icon warning">📖</div>
                    <div class="info">
                        <h3>${currentBorrows}</h3>
                        <p>当前借阅</p>
                    </div>
                </div>
                
                <div class="stat-card">
                    <div class="icon danger">⚠️</div>
                    <div class="info">
                        <h3>${overdueCount}</h3>
                        <p>逾期未还</p>
                    </div>
                </div>
            </div>
            
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">快捷操作</h3>
                </div>
                <div style="display: flex; gap: 15px; flex-wrap: wrap;">
                    <a href="userServlet?action=toAdd" class="btn btn-success">➕ 新增读者</a>
                    <a href="bookManageServlet?action=add" class="btn btn-success">➕ 新增图书</a>
                    <a href="borrowServlet?action=list" class="btn btn-primary">📖 办理借书</a>
                    <a href="borrowServlet?action=overdue" class="btn btn-warning">⚠️ 逾期管理</a>
                    <a href="statServlet?action=overview" class="btn btn-info">📈 查看统计</a>
                </div>
            </div>
            
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">系统公告</h3>
                </div>
                <div class="msg msg-info">
                    <strong>📢 公告：</strong>欢迎使用图书管理系统！请及时处理逾期图书，确保图书流通顺畅。
                </div>
            </div>
        </div>
    </div>
</body>
</html>