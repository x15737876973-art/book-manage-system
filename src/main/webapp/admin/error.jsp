<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>错误 - 图书管理系统</title>
    <link rel="stylesheet" href="../css/style.css">
    <style>
        .error-container {
            max-width: 800px;
            margin: 50px auto;
            padding: 30px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .error-title {
            color: #dc3545;
            font-size: 24px;
            margin-bottom: 20px;
        }
        .error-message {
            background: #f8d7da;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
            border-left: 4px solid #dc3545;
        }
        .btn {
            display: inline-block;
            padding: 10px 20px;
            background: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            margin: 5px;
        }
        .btn:hover {
            background: #0056b3;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <h2 class="error-title">❌ 操作失败</h2>
        
        <div class="error-message">
            <strong>错误信息：</strong><br>
            <%= request.getAttribute("error") != null ? request.getAttribute("error") : (exception != null ? exception.getMessage() : "未知错误") %>
        </div>
        
        <div style="margin-top: 20px;">
            <a href="indexServlet" class="btn">🏠 返回首页</a>
            <a href="bookManageServlet?action=list" class="btn">📖 图书列表</a>
            <a href="checkBooks" class="btn" style="background: #28a745;">🔍 数据库检查</a>
        </div>
        
        <div style="margin-top: 30px; padding: 15px; background: #e7f3ff; border-radius: 5px;">
            <h4>💡 可能的解决方案：</h4>
            <ol>
                <li>检查数据库连接是否正常（MySQL 服务是否启动）</li>
                <li>确认已执行 <code>database_init.sql</code> 初始化数据</li>
                <li>访问 <a href="checkBooks" style="color: #007bff;">数据库检查</a> 查看当前数据量</li>
                <li>重启服务器后重试</li>
            </ol>
        </div>
    </div>
</body>
</html>