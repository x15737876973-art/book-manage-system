<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>编辑归还日期</title>
    <link rel="stylesheet" href="../css/style.css">
    <link rel="stylesheet" href="../css/supplement.css">
    <style>
        .form-container {
            max-width: 600px;
            margin: 0 auto;
            padding: 30px;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #333;
        }
        
        .form-group input, .form-group select {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 14px;
        }
        
        .form-group input:read-only {
            background: #f5f5f5;
            color: #666;
        }
        
        .btn-group {
            display: flex;
            gap: 10px;
            margin-top: 30px;
        }
        
        .info-box {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        
        .info-box p {
            margin: 5px 0;
            color: #666;
        }
        
        .info-box strong {
            color: #333;
        }
    </style>
</head>
<body>
    <div class="admin-container">
        <div class="sidebar">
            <div class="logo">
                <span class="logo-icon">🏛️📚</span>
                <span class="logo-text">图书管理系统</span>
            </div>
            <ul class="menu">
                <li><a href="indexServlet"><span class="icon">📊</span>系统首页</a></li>
                <li><a href="bookList.jsp"><span class="icon">📚</span>图书管理</a></li>
                <li><a href="userList.jsp"><span class="icon">👥</span>读者管理</a></li>
                <li class="active"><a href="borrowServlet?action=list"><span class="icon">📖</span>借阅管理</a></li>
                <li><a href="overdueList.jsp"><span class="icon">⚠️</span>逾期管理</a></li>
                <li><a href="statOverview.jsp"><span class="icon">📈</span>数据统计</a></li>
            </ul>
        </div>
        
        <div class="main-content">
            <h2 class="page-title">编辑归还日期</h2>
            
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">借阅信息</h3>
                </div>
                
                <div class="form-container">
                    <c:if test="${not empty record}">
                        <div class="info-box">
                            <p><strong>读者姓名：</strong>${record.realName}</p>
                            <p><strong>图书名称：</strong>${record.bookTitle}</p>
                            <p><strong>借阅日期：</strong>${record.borrowDate}</p>
                            <p><strong>当前状态：</strong>
                                <c:choose>
                                    <c:when test="${record.status == 'borrowed'}">
                                        <span class="status-badge status-borrowed">借阅中</span>
                                    </c:when>
                                    <c:when test="${record.status == 'overdue'}">
                                        <span class="status-badge status-overdue">已逾期</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-badge">${record.status}</span>
                                    </c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                        
                        <form action="borrowServlet" method="post">
                            <input type="hidden" name="action" value="updateDueDate">
                            <input type="hidden" name="recordId" value="${record.id}">
                            
                            <div class="form-group">
                                <label>归还日期</label>
                                <input type="date" name="dueDate" value="${record.dueDate}" required>
                            </div>
                            
                            <div class="btn-group">
                                <button type="submit" class="btn btn-primary">保存修改</button>
                                <a href="borrowServlet?action=list" class="btn btn-secondary">取消</a>
                            </div>
                        </form>
                    </c:if>
                    
                    <c:if test="${empty record}">
                        <div class="empty-data">
                            <p>未找到借阅记录</p>
                            <a href="borrowServlet?action=list" class="btn btn-secondary">返回列表</a>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</body>
</html>