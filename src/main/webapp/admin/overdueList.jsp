<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>逾期管理 - 图书管理系统</title>
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
                <li><a href="indexServlet"><span>📊 首页</span></a></li>
                <li><a href="userServlet?action=list"><span>👥 用户管理</span></a></li>
                <li><a href="bookManageServlet?action=list"><span>📚 图书管理</span></a></li>
                <li><a href="categoryServlet?action=list"><span>📂 分类管理</span></a></li>
                <li><a href="borrowServlet?action=current"><span>📖 借阅管理</span></a></li>
                <li><a href="borrowServlet?action=overdue" class="active"><span>⚠️ 逾期管理</span></a></li>
                <li><a href="statServlet?action=overview"><span>📈 数据统计</span></a></li>
                <li><a href="statServlet?action=ranking"><span>🏆 借阅排行</span></a></li>
            </ul>
        </div>
        
        <div class="main-content">
            <h2 class="page-title">逾期管理</h2>
            
            <c:if test="${not empty msg}">
                <div class="msg msg-success">${msg}</div>
            </c:if>
            
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">逾期列表</h3>
                </div>
                
                <div class="msg msg-warning" style="margin-bottom: 20px;">
                    <strong>⚠️ 提醒：</strong>以下图书已逾期，请及时联系读者归还！逾期罚款按 0.5元/天 计算。
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
                                <th>逾期天数</th>
                                <th>罚款金额</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${overdueList}" var="record">
                                <tr>
                                    <td>${record.id}</td>
                                    <td>${record.userName}</td>
                                    <td class="text-ellipsis" title="${record.bookName}">${record.bookName}</td>
                                    <td>${record.borrowDate}</td>
                                    <td><span class="important-date">${record.dueDate}</span></td>
                                    <td><span class="status-badge status-overdue">${record.overdueDays} 天</span></td>
                                    <td><span class="important-money">￥${record.fineAmount}</span></td>
                                    <td>
                                        <a href="borrowServlet?action=return&id=${record.id}" class="btn btn-primary btn-sm">还书</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
            
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">罚款记录</h3>
                </div>
                
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>读者姓名</th>
                                <th>图书名称</th>
                                <th>逾期天数</th>
                                <th>罚款金额</th>
                                <th>状态</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${fineList}" var="fine">
                                <tr>
                                    <td>${fine.id}</td>
                                    <td>${fine.userName != null ? fine.userName : '未知'}</td>
                                    <td class="text-ellipsis" title="${fine.bookName}">${fine.bookName}</td>
                                    <td>${fine.days} 天</td>
                                    <td><span class="text-danger font-bold">￥${fine.amount}</span></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${fine.status == 'unpaid'}">
                                                <span class="status-badge status-overdue">未支付</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge status-active">已支付</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:if test="${fine.status == 'unpaid'}">
                                            <a href="borrowServlet?action=payFine&fineId=${fine.id}" class="btn btn-success btn-sm">确认支付</a>
                                        </c:if>
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