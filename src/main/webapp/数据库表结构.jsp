<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.book.util.DBUtil" %>
<%@ page import="java.sql.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>数据库表结构</title>
    <style>
        body {
            font-family: 'Microsoft YaHei', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
            margin: 0;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 16px;
            padding: 40px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
        }
        h1 {
            text-align: center;
            color: #333;
            margin-bottom: 30px;
        }
        h2 {
            color: #667eea;
            border-bottom: 2px solid #667eea;
            padding-bottom: 10px;
            margin-top: 30px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 15px 0;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 12px;
            text-align: left;
        }
        th {
            background-color: #667eea;
            color: white;
        }
        tr:nth-child(even) {
            background-color: #f8f9fa;
        }
        tr:hover {
            background-color: #e7f3ff;
        }
        .info-box {
            background: #e7f3ff;
            border-radius: 8px;
            padding: 20px;
            margin: 15px 0;
            border-left: 4px solid #007bff;
        }
        .success-box {
            background: #d4edda;
            border-radius: 8px;
            padding: 20px;
            margin: 15px 0;
            border-left: 4px solid #28a745;
        }
        .table-name {
            font-weight: bold;
            color: #667eea;
            font-size: 18px;
        }
        .data-type {
            color: #6c757d;
            font-size: 12px;
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
        .badge {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 3px;
            font-size: 11px;
            font-weight: bold;
        }
        .badge-primary {
            background: #007bff;
            color: white;
        }
        .badge-success {
            background: #28a745;
            color: white;
        }
        .badge-warning {
            background: #ffc107;
            color: #333;
        }
        .badge-info {
            background: #17a2b8;
            color: white;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>📊 数据库表结构</h1>
        
        <%
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            
            try {
                conn = DBUtil.getConn();
                stmt = conn.createStatement();
        %>
        
        <div class="info-box">
            <h3>📋 数据库信息</h3>
            <p><strong>数据库类型：</strong>SQLite</p>
            <p><strong>数据库路径：</strong>C:/java/untitled6/book_manage.db</p>
            <%
                // 获取数据库文件大小
                java.io.File dbFile = new java.io.File("C:/java/untitled6/book_manage.db");
                if (dbFile.exists()) {
                    double fileSizeKB = dbFile.length() / 1024.0;
                    double fileSizeMB = fileSizeKB / 1024.0;
            %>
            <p><strong>文件大小：</strong><%= String.format("%.2f", fileSizeKB) %> KB (<%= String.format("%.4f", fileSizeMB) %> MB)</p>
            <% } %>
        </div>
        
        <h2>📑 数据表列表</h2>
        
        <%
            // 获取所有表
            rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
            
            int tableCount = 0;
            while (rs.next()) {
                tableCount++;
            }
            rs.close();
        %>
        
        <div class="success-box">
            <p><strong>共有 <%= tableCount %> 个数据表</strong></p>
        </div>
        
        <%
            // 重新获取表列表
            rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
            
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
        %>
        
        <h2 class="table-name">📋 表：<%= tableName %></h2>
        
        <%
            // 获取表结构
            ResultSet columns = conn.getMetaData().getColumns(null, null, tableName, null);
        %>
        
        <table>
            <tr>
                <th>序号</th>
                <th>字段名</th>
                <th>数据类型</th>
                <th>是否可空</th>
                <th>默认值</th>
                <th>说明</th>
            </tr>
            <%
                int colIndex = 1;
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String typeName = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    String nullable = columns.getString("IS_NULLABLE");
                    String defaultValue = columns.getString("COLUMN_DEF");
                    String remarks = columns.getString("REMARKS");
                    
                    // 判断是否是主键
                    ResultSet pkRs = conn.getMetaData().getPrimaryKeys(null, null, tableName);
                    boolean isPrimaryKey = false;
                    while (pkRs.next()) {
                        if (pkRs.getString("COLUMN_NAME").equals(columnName)) {
                            isPrimaryKey = true;
                            break;
                        }
                    }
                    pkRs.close();
            %>
            <tr>
                <td><%= colIndex %></td>
                <td>
                    <%= columnName %>
                    <% if (isPrimaryKey) { %>
                        <span class="badge badge-warning">主键</span>
                    <% } %>
                </td>
                <td>
                    <%= typeName %>
                    <% if (columnSize > 0) { %>
                        <span class="data-type">(<%= columnSize %>)</span>
                    <% } %>
                </td>
                <td>
                    <% if ("YES".equals(nullable)) { %>
                        <span class="badge badge-info">可空</span>
                    <% } else { %>
                        <span class="badge badge-primary">非空</span>
                    <% } %>
                </td>
                <td><%= defaultValue != null ? defaultValue : "-" %></td>
                <td><%= remarks != null ? remarks : "-" %></td>
            </tr>
            <%
                    colIndex++;
                }
                columns.close();
            %>
        </table>
        
        <%
            // 获取表记录数
            try {
                ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
                countRs.next();
                int recordCount = countRs.getInt(1);
                countRs.close();
        %>
        
        <div class="info-box">
            <p><strong>记录数：</strong><%= recordCount %> 条</p>
        </div>
        
        <%
            } catch (Exception e) {
                // 忽略错误
            }
        %>
        
        <%
            }
            rs.close();
        %>
        
        <h2>📊 数据统计</h2>
        <table>
            <tr>
                <th>表名</th>
                <th>记录数</th>
                <th>状态</th>
            </tr>
            <%
                // 重新获取表列表统计
                rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
                
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    
                    try {
                        ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
                        countRs.next();
                        int recordCount = countRs.getInt(1);
                        countRs.close();
            %>
            <tr>
                <td><strong><%= tableName %></strong></td>
                <td><%= recordCount %> 条</td>
                <td>
                    <% if (recordCount > 0) { %>
                        <span class="badge badge-success">有数据</span>
                    <% } else { %>
                        <span class="badge badge-warning">空表</span>
                    <% } %>
                </td>
            </tr>
            <%
                    } catch (Exception e) {
            %>
            <tr>
                <td><strong><%= tableName %></strong></td>
                <td>-</td>
                <td><span class="badge badge-info">无法统计</span></td>
            </tr>
            <%
                    }
                }
                rs.close();
            %>
        </table>
        
        <div style="margin-top: 30px; text-align: center;">
            <a href="login.jsp" class="btn">🔐 返回登录</a>
            <a href="admin/index.jsp" class="btn">👨‍💼 管理员首页</a>
            <a href="数据库容量信息.jsp" class="btn" style="background: #28a745;">📊 数据库容量</a>
            <a href="快速访问.html" class="btn" style="background: #6c757d;">🏠 快速访问</a>
        </div>
        
        <%
            } catch (Exception e) {
        %>
        <div class="info-box" style="background: #f8d7da; border-left-color: #dc3545;">
            <h3 style="color: #dc3545;">❌ 获取数据库信息失败</h3>
            <p><%= e.getMessage() %></p>
            <pre><%= e.getClass().getName() %></pre>
        </div>
        <%
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        %>
    </div>
</body>
</html>