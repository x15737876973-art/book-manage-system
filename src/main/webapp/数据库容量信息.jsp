<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.book.util.DBUtil" %>
<%@ page import="java.sql.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>数据库容量信息</title>
    <style>
        body {
            font-family: 'Microsoft YaHei', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
            margin: 0;
        }
        .container {
            max-width: 1000px;
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
        .warning-box {
            background: #fff3cd;
            border-radius: 8px;
            padding: 20px;
            margin: 15px 0;
            border-left: 4px solid #ffc107;
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
        .metric {
            display: inline-block;
            padding: 10px 20px;
            background: #667eea;
            color: white;
            border-radius: 8px;
            margin: 5px;
        }
        .metric-value {
            font-size: 24px;
            font-weight: bold;
        }
        .metric-label {
            font-size: 12px;
            opacity: 0.9;
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
    <div class="container">
        <h1>📊 数据库容量信息</h1>
        
        <%
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            
            try {
                conn = DBUtil.getConn();
                stmt = conn.createStatement();
        %>
        
        <h2>💾 SQLite 数据库特性</h2>
        <div class="info-box">
            <h3>数据库类型：SQLite</h3>
            <p><strong>SQLite 是一个轻量级的嵌入式数据库，具有以下特点：</strong></p>
            <ul>
                <li>✅ 无需独立服务器进程，零配置</li>
                <li>✅ 单个磁盘文件存储整个数据库</li>
                <li>✅ 跨平台，易于备份和迁移</li>
                <li>✅ 适合中小型应用和开发测试</li>
            </ul>
        </div>
        
        <h2>📈 理论容量限制</h2>
        <table>
            <tr>
                <th>项目</th>
                <th>限制值</th>
                <th>说明</th>
            </tr>
            <tr>
                <td><strong>数据库文件大小</strong></td>
                <td>281 TB</td>
                <td>理论上支持的最大文件大小</td>
            </tr>
            <tr>
                <td><strong>单表行数</strong></td>
                <td>18亿行</td>
                <td>受主键ID限制（2^61-1）</td>
            </tr>
            <tr>
                <td><strong>单行数据大小</strong></td>
                <td>1 GB</td>
                <td>单行数据的最大大小</td>
            </tr>
            <tr>
                <td><strong>BLOB数据大小</strong></td>
                <td>1 GB</td>
                <td>二进制大对象的最大大小</td>
            </tr>
            <tr>
                <td><strong>字符串长度</strong></td>
                <td>10亿字符</td>
                <td>TEXT字段的最大长度</td>
            </tr>
            <tr>
                <td><strong>表数量</strong></td>
                <td>无限制</td>
                <td>受磁盘空间限制</td>
            </tr>
            <tr>
                <td><strong>索引数量</strong></td>
                <td>无限制</td>
                <td>受磁盘空间限制</td>
            </tr>
        </table>
        
        <h2>📊 当前数据库状态</h2>
        <%
            // 获取数据库文件大小
            String dbPath = application.getRealPath("/") + "../book_manage.db";
            java.io.File dbFile = new java.io.File("book_manage.db");
            long fileSize = dbFile.exists() ? dbFile.length() : 0;
            double fileSizeKB = fileSize / 1024.0;
            double fileSizeMB = fileSizeKB / 1024.0;
        %>
        
        <div style="text-align: center; margin: 20px 0;">
            <div class="metric">
                <div class="metric-value"><%= String.format("%.2f", fileSizeKB) %> KB</div>
                <div class="metric-label">数据库文件大小</div>
            </div>
            <div class="metric">
                <div class="metric-value"><%= String.format("%.4f", fileSizeMB) %> MB</div>
                <div class="metric-label">数据库文件大小(MB)</div>
            </div>
        </div>
        
        <table>
            <tr>
                <th>数据表</th>
                <th>记录数</th>
                <th>说明</th>
            </tr>
            <%
                // 查询各表记录数
                String[] tables = {"users", "books", "categories", "borrows", "reservations", "fines"};
                String[] tableNames = {"用户表", "图书表", "分类表", "借阅记录表", "预约记录表", "罚款记录表"};
                
                for (int i = 0; i < tables.length; i++) {
                    try {
                        rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tables[i]);
                        rs.next();
                        int count = rs.getInt(1);
                        rs.close();
            %>
            <tr>
                <td><strong><%= tableNames[i] %></strong></td>
                <td><%= count %> 条</td>
                <td><%= tables[i] %></td>
            </tr>
            <%
                    } catch (Exception e) {
            %>
            <tr>
                <td><strong><%= tableNames[i] %></strong></td>
                <td>0 条</td>
                <td>表不存在或为空</td>
            </tr>
            <%
                    }
                }
            %>
        </table>
        
        <h2>💡 实际使用建议</h2>
        <div class="success-box">
            <h3>✅ SQLite 适合场景：</h3>
            <ul>
                <li>📚 <strong>图书管理系统</strong>：可轻松支持数万到数十万本图书</li>
                <li>👥 <strong>用户管理</strong>：可支持数千到数万用户</li>
                <li>📖 <strong>借阅记录</strong>：可支持数十万到数百万条记录</li>
                <li>💻 <strong>单机应用</strong>：适合单服务器部署的应用</li>
                <li>🔧 <strong>开发测试</strong>：快速开发和原型验证</li>
            </ul>
        </div>
        
        <div class="warning-box">
            <h3>⚠️ 如果需要更大规模：</h3>
            <p>如果您的系统需要支持：</p>
            <ul>
                <li>百万级以上的图书数据</li>
                <li>高并发访问（每秒数百次以上）</li>
                <li>分布式部署（多台服务器）</li>
                <li>复杂的查询和事务处理</li>
            </ul>
            <p><strong>建议升级到 MySQL 或 PostgreSQL 数据库</strong></p>
        </div>
        
        <h2>📈 容量估算</h2>
        <div class="info-box">
            <h3>基于当前数据库大小估算：</h3>
            <p>当前数据库大小：<strong><%= String.format("%.2f", fileSizeKB) %> KB</strong></p>
            <p>假设每条图书记录约占 <strong>1-2 KB</strong>，则：</p>
            <ul>
                <li>📊 <strong>10,000 本图书</strong> ≈ 10-20 MB</li>
                <li>📊 <strong>100,000 本图书</strong> ≈ 100-200 MB</li>
                <li>📊 <strong>1,000,000 本图书</strong> ≈ 1-2 GB</li>
            </ul>
            <p><strong>结论：</strong>对于大多数图书馆管理系统，SQLite 完全够用！</p>
        </div>
        
        <h2>🔧 性能优化建议</h2>
        <div class="info-box">
            <h3>保持数据库高性能：</h3>
            <ol>
                <li>✅ 定期清理无用数据（已归还很久的借阅记录）</li>
                <li>✅ 为常用查询字段创建索引（已自动创建）</li>
                <li>✅ 定期执行 VACUUM 命令优化数据库文件</li>
                <li>✅ 避免存储大文件（如图片）在数据库中</li>
                <li>✅ 定期备份数据库文件</li>
            </ol>
        </div>
        
        <div style="margin-top: 30px; text-align: center;">
            <a href="login.jsp" class="btn">🔐 返回登录</a>
            <a href="admin/index.jsp" class="btn">👨‍💼 管理员首页</a>
            <a href="快速访问.html" class="btn" style="background: #6c757d;">🏠 快速访问</a>
        </div>
        
        <%
            } catch (Exception e) {
        %>
        <div class="warning-box">
            <h3>❌ 获取数据库信息失败</h3>
            <p><%= e.getMessage() %></p>
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