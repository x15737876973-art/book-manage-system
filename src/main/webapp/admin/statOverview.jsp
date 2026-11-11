<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>数据统计 - 图书管理系统</title>
    <link rel="stylesheet" href="../css/style.css">
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
                <li><a href="bookManageServlet?action=list"><span>📚 图书管理</span></a></li>
                <li><a href="categoryServlet?action=list"><span>📂 分类管理</span></a></li>
                <li><a href="borrowServlet?action=current"><span>📖 借阅管理</span></a></li>
                <li><a href="borrowServlet?action=overdue"><span>⚠️ 逾期管理</span></a></li>
                <li><a href="statServlet?action=overview" class="active"><span>📈 数据统计</span></a></li>
                <li><a href="statServlet?action=ranking"><span>🏆 借阅排行</span></a></li>
            </ul>
        </div>
        
        <div class="main-content">
            <h2 class="page-title">数据统计</h2>
            
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="icon primary">📚</div>
                    <div class="info">
                        <h3>${stat.totalBooks}</h3>
                        <p>图书种类</p>
                    </div>
                </div>
                
                <div class="stat-card">
                    <div class="icon success">👥</div>
                    <div class="info">
                        <h3>${stat.totalUsers}</h3>
                        <p>注册读者</p>
                    </div>
                </div>
                
                <div class="stat-card">
                    <div class="icon warning">📖</div>
                    <div class="info">
                        <h3>${stat.currentBorrows}</h3>
                        <p>当前借阅</p>
                    </div>
                </div>
                
                <div class="stat-card">
                    <div class="icon danger">⚠️</div>
                    <div class="info">
                        <h3>${stat.overdueCount}</h3>
                        <p>逾期未还</p>
                    </div>
                </div>
            </div>
            
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">分类统计</h3>
                </div>
                
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>分类名称</th>
                                <th>图书数量</th>
                                <th>可借数量</th>
                                <th>借出数量</th>
                                <th>占比</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${categoryStats}" var="stat">
                                <tr>
                                    <td>${stat.categoryName}</td>
                                    <td>${stat.totalCount}</td>
                                    <td><span class="status-badge status-active">${stat.availableCount}</span></td>
                                    <td><span class="status-badge status-borrowed">${stat.borrowedCount}</span></td>
                                    <td>
                                        <div style="background: #f0f0f0; border-radius: 10px; height: 20px; overflow: hidden;">
                                            <div style="background: linear-gradient(90deg, #667eea, #764ba2); height: 100%; width: ${stat.percentage}%;"></div>
                                        </div>
                                        <small class="text-muted">${stat.percentage}%</small>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
            
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">库存统计</h3>
                </div>
                
                <div class="stats-grid" style="grid-template-columns: repeat(3, 1fr);">
                    <div class="stat-card" style="background: #f8f9fa;">
                        <div class="info">
                            <h3>${stat.totalStock}</h3>
                            <p>总库存（册）</p>
                        </div>
                    </div>
                    
                    <div class="stat-card" style="background: #f8f9fa;">
                        <div class="info">
                            <h3>${stat.availableStock}</h3>
                            <p>可借数量（册）</p>
                        </div>
                    </div>
                    
                    <div class="stat-card" style="background: #f8f9fa;">
                        <div class="info">
                            <h3>${stat.borrowedStock}</h3>
                            <p>已借出（册）</p>
                        </div>
                    </div>
                </div>
                
                <div style="padding: 15px; color: #666; font-size: 14px;">
                    <p>📊 说明：总库存 = 可借数量 + 已借出</p>
                    <p>💡 提示：已借出数量 = 总库存 - 可借数量</p>
                </div>
            </div>
        </div>
    </div>
</body>
</html>