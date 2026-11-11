<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>图书管理系统 - 读者</title>
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
            <span class="username">欢迎，${sessionScope.loginUser.realName}（读者）</span>
            <a href="../reader/profileServlet?action=view">个人信息</a>
            <a href="../logoutServlet">退出登录</a>
        </div>
    </div>
    
    <div class="container">
        <div class="sidebar">
            <ul>
                <li><a href="../reader/indexServlet" class="active"><span class="menu-icon">📊</span><span>首页</span></a></li>
                <li><a href="../reader/bookSearchServlet?action=list"><span class="menu-icon">🔍</span><span>图书检索</span></a></li>
                <li><a href="../reader/borrowOpServlet?action=myBorrows"><span class="menu-icon">📖</span><span>我的借阅</span></a></li>
                <li><a href="../reader/borrowOpServlet?action=myReservations"><span class="menu-icon">📋</span><span>我的预约</span></a></li>
                <li><a href="../reader/borrowOpServlet?action=myFines"><span class="menu-icon">💰</span><span>我的罚款</span></a></li>
                <li><a href="../reader/profileServlet?action=view"><span class="menu-icon">👤</span><span>个人中心</span></a></li>
            </ul>
        </div>
        
        <div class="main-content">
            <h2 class="page-title">读者首页</h2>
            
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="icon primary">📖</div>
                    <div class="info">
                        <h3>${myStat.borrowedCount}</h3>
                        <p>当前借阅</p>
                    </div>
                </div>
                
                <div class="stat-card">
                    <div class="icon warning">⚠️</div>
                    <div class="info">
                        <h3>${myStat.overdueCount}</h3>
                        <p>逾期图书</p>
                    </div>
                </div>
                
                <div class="stat-card">
                    <div class="icon danger">💰</div>
                    <div class="info">
                        <h3>￥${myStat.unpaidFine}</h3>
                        <p>未缴罚款</p>
                    </div>
                </div>
                
                <div class="stat-card">
                    <div class="icon success">📋</div>
                    <div class="info">
                        <h3>${myStat.reservationCount}</h3>
                        <p>预约图书</p>
                    </div>
                </div>
            </div>
            
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">快捷操作</h3>
                </div>
                <div style="display: flex; gap: 15px; flex-wrap: wrap;">
                    <a href="../reader/bookSearchServlet?action=list" class="btn btn-primary">🔍 图书检索</a>
                    <a href="../reader/borrowOpServlet?action=myBorrows" class="btn btn-info">📖 我的借阅</a>
                    <a href="../reader/profileServlet?action=view" class="btn btn-secondary">👤 个人中心</a>
                </div>
            </div>
            
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">借阅须知</h3>
                </div>
                <div class="msg msg-info">
                    <strong>📢 借阅规则：</strong><br>
                    1. 每位读者最多可借阅 5 本图书<br>
                    2. 借阅期限为 30 天，可续借 1 次<br>
                    3. 逾期罚款按 0.5元/天 计算<br>
                    4. 请按时归还图书，保持良好信用
                </div>
            </div>
        </div>
    </div>
</body>
</html>