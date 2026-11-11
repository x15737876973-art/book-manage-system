<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.book.util.DBUtil" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>插入测试数据</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
            margin: 0;
        }
        .container {
            max-width: 900px;
            margin: 0 auto;
            background: white;
            border-radius: 16px;
            padding: 40px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
        }
        h1 { text-align: center; color: #333; margin-bottom: 30px; }
        h2 { color: #667eea; border-bottom: 2px solid #667eea; padding-bottom: 10px; margin-top: 30px; }
        h3 { color: #667eea; margin-top: 20px; }
        .success { color: #28a745; padding: 8px; background: #d4edda; margin: 3px 0; border-radius: 4px; }
        .error { color: #dc3545; padding: 8px; background: #f8d7da; margin: 3px 0; border-radius: 4px; }
        .info { background: #e7f3ff; padding: 15px; border-radius: 5px; margin: 10px 0; border-left: 4px solid #007bff; }
        .btn { display: inline-block; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 4px; margin: 5px; }
        .btn:hover { background: #0056b3; }
        table { width: 100%; border-collapse: collapse; margin: 15px 0; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #667eea; color: white; }
        tr:nth-child(even) { background-color: #f8f9fa; }
    </style>
</head>
<body>
    <div class="container">
        <h1>📊 插入测试数据</h1>
        
        <%
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            
            try {
                conn = DBUtil.getConn();
                stmt = conn.createStatement();
        %>
        
        <%
            // 1. 插入分类数据
            out.println("<h3>1. 插入分类数据</h3>");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
            rs.next();
            int categoryCount = rs.getInt(1);
            rs.close();
            
            if (categoryCount < 30) {
                String[] categories = {
                    "文学", "理工", "历史", "计算机", "经济管理", "哲学", "艺术", "法律", "教育", "医学",
                    "农业", "工业技术", "交通运输", "航空航天", "环境科学", "心理学", "社会学", "政治", "军事", "体育",
                    "语言文字", "新闻传播", "旅游", "烹饪", "家居", "育儿", "成功励志", "传记", "科普", "工具书"
                };
                String[] descriptions = {
                    "文学类图书", "理工科类图书", "历史类图书", "计算机类图书", "经济管理类图书",
                    "哲学类图书", "艺术类图书", "法律类图书", "教育类图书", "医学类图书",
                    "农业类图书", "工业技术类图书", "交通运输类图书", "航空航天类图书", "环境科学类图书",
                    "心理学类图书", "社会学类图书", "政治类图书", "军事类图书", "体育类图书",
                    "语言文字类图书", "新闻传播类图书", "旅游类图书", "烹饪类图书", "家居类图书",
                    "育儿类图书", "成功励志类图书", "传记类图书", "科普类图书", "工具书类图书"
                };
                
                int inserted = 0;
                for (int i = categoryCount; i < categories.length; i++) {
                    try {
                        stmt.executeUpdate(String.format(
                            "INSERT INTO categories(name, description) VALUES('%s', '%s')",
                            categories[i], descriptions[i]
                        ));
                        inserted++;
                    } catch (Exception e) {
                        // 忽略重复
                    }
                }
                out.println("<p class='success'>✅ 成功插入 " + inserted + " 个分类，总计 " + (categoryCount + inserted) + " 个</p>");
            } else {
                out.println("<p class='info'>分类数据已存在：" + categoryCount + " 个</p>");
            }
        %>
        
        <%
            // 2. 插入用户数据
            out.println("<h3>2. 插入用户数据</h3>");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            int userCount = rs.getInt(1);
            rs.close();
            
            if (userCount < 30) {
                String[] names = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十", "郑十一", "王小明",
                                "李小红", "张小芳", "刘大伟", "陈晓华", "杨丽丽", "黄强", "周杰", "吴敏", "赵磊", "孙悦",
                                "马云飞", "朱婷婷", "秦风", "许文强", "韩雪", "唐僧", "孙悟空", "猪八戒", "沙僧", "白龙马"};
                
                int inserted = 0;
                for (int i = userCount; i < 30; i++) {
                    try {
                        String username = "user" + (i + 1);
                        String realName = names[i % names.length];
                        String role = (i == 0) ? "admin" : "reader";
                        stmt.executeUpdate(String.format(
                            "INSERT INTO users(username, password, role, real_name, phone, email, status) " +
                            "VALUES('%s', '123456', '%s', '%s', '138%08d', '%s@test.com', 'active')",
                            username, role, realName, i, username
                        ));
                        inserted++;
                    } catch (Exception e) {
                        // 忽略错误
                    }
                }
                out.println("<p class='success'>✅ 成功插入 " + inserted + " 个用户，总计 " + (userCount + inserted) + " 个</p>");
            } else {
                out.println("<p class='info'>用户数据已存在：" + userCount + " 个</p>");
            }
        %>
        
        <%
            // 3. 插入图书数据
            out.println("<h3>3. 插入图书数据</h3>");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM books");
            rs.next();
            int bookCount = rs.getInt(1);
            rs.close();
            
            if (bookCount < 30) {
                String[][] books = {
                    {"B001", "978-7-02-013757-5", "红楼梦", "曹雪芹", "人民文学出版社", "2008-07-01", "59.70", "1"},
                    {"B002", "978-7-02-013758-2", "三国演义", "罗贯中", "人民文学出版社", "2008-07-01", "49.00", "1"},
                    {"B003", "978-7-5327-7241-0", "百年孤独", "加西亚·马尔克斯", "上海译文出版社", "2017-08-01", "55.00", "1"},
                    {"B004", "978-7-5442-7153-6", "活着", "余华", "作家出版社", "2012-08-01", "35.00", "1"},
                    {"B005", "978-7-04-039661-2", "高等数学", "同济大学数学系", "高等教育出版社", "2014-07-01", "49.00", "2"},
                    {"B006", "978-7-04-028582-7", "大学物理", "程守洙", "高等教育出版社", "2016-12-01", "45.00", "2"},
                    {"B007", "978-7-04-021481-6", "线性代数", "同济大学数学系", "高等教育出版社", "2014-06-01", "28.00", "2"},
                    {"B008", "978-7-04-031234-9", "概率论与数理统计", "浙江大学", "高等教育出版社", "2015-03-01", "35.00", "2"},
                    {"B009", "978-7-5399-5436-9", "明朝那些事儿", "当年明月", "浙江人民出版社", "2011-12-01", "188.00", "3"},
                    {"B010", "978-7-101-05452-6", "万历十五年", "黄仁宇", "中华书局", "2007-01-01", "29.00", "3"},
                    {"B011", "978-7-111-21382-6", "Java编程思想", "Bruce Eckel", "机械工业出版社", "2007-06-01", "108.00", "4"},
                    {"B012", "978-7-111-55842-2", "Effective Java", "Joshua Bloch", "机械工业出版社", "2018-01-01", "89.00", "4"},
                    {"B013", "978-7-111-40701-0", "算法导论", "Thomas H.Cormen", "机械工业出版社", "2013-01-01", "128.00", "4"},
                    {"B014", "978-7-115-46812-5", "Linux命令行与Shell脚本编程", "Richard Blum", "人民邮电出版社", "2019-05-01", "79.00", "4"},
                    {"B015", "978-7-301-25649-8", "经济学原理", "N.Gregory Mankiw", "北京大学出版社", "2015-05-01", "98.00", "5"},
                    {"B016", "978-7-300-23456-7", "管理学", "Stephen P.Robbins", "中国人民大学出版社", "2017-01-01", "65.00", "5"},
                    {"B017", "978-7-5442-7154-3", "围城", "钱钟书", "人民文学出版社", "1991-02-01", "39.00", "1"},
                    {"B018", "978-7-5442-7155-0", "平凡的世界", "路遥", "北京十月文艺出版社", "2017-06-01", "99.00", "1"},
                    {"B019", "978-7-5442-7156-7", "三体", "刘慈欣", "重庆出版社", "2008-01-01", "23.00", "1"},
                    {"B020", "978-7-5442-7157-4", "白夜行", "东野圭吾", "南海出版公司", "2017-08-01", "59.80", "1"},
                    {"B021", "978-7-5442-7158-1", "解忧杂货店", "东野圭吾", "南海出版公司", "2014-05-01", "39.50", "1"},
                    {"B022", "978-7-5442-7159-8", "小王子", "圣埃克苏佩里", "人民文学出版社", "2003-08-01", "22.00", "1"},
                    {"B023", "978-7-5442-7160-4", "追风筝的人", "卡勒德·胡赛尼", "上海人民出版社", "2006-05-01", "29.00", "1"},
                    {"B024", "978-7-5442-7161-1", "人类简史", "尤瓦尔·赫拉利", "中信出版社", "2017-02-01", "68.00", "3"},
                    {"B025", "978-7-5442-7162-8", "时间简史", "史蒂芬·霍金", "湖南科学技术出版社", "2010-04-01", "45.00", "15"},
                    {"B026", "978-7-5442-7163-5", "Python编程从入门到实践", "Eric Matthes", "人民邮电出版社", "2020-01-01", "89.00", "4"},
                    {"B027", "978-7-5442-7164-2", "深入理解计算机系统", "Randal E.Bryant", "机械工业出版社", "2016-07-01", "139.00", "4"},
                    {"B028", "978-7-5442-7165-9", "设计模式", "Erich Gamma", "机械工业出版社", "2010-07-01", "55.00", "4"},
                    {"B029", "978-7-5442-7166-6", "数据结构与算法分析", "Mark Allen Weiss", "机械工业出版社", "2013-05-01", "69.00", "4"},
                    {"B030", "978-7-5442-7167-3", "数据库系统概论", "王珊", "高等教育出版社", "2014-09-01", "49.00", "4"}
                };
                
                int inserted = 0;
                for (int i = bookCount; i < books.length && i < 30; i++) {
                    try {
                        stmt.executeUpdate(String.format(
                            "INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                            "VALUES('%s', '%s', '%s', '%s', '%s', '%s', %s, %s, %d, %d, 'A区-%d排-%d架')",
                            books[i][0], books[i][1], books[i][2], books[i][3], books[i][4], 
                            books[i][5], books[i][6], books[i][7],
                            10 + i, 10 + i, (i/5) + 1, (i%5) + 1
                        ));
                        inserted++;
                        out.println("<p class='success'>✅ 已插入：" + books[i][2] + "</p>");
                    } catch (Exception e) {
                        out.println("<p class='error'>❌ 插入失败：" + books[i][2] + " - " + e.getMessage() + "</p>");
                    }
                }
                out.println("<p class='success'>✅ 成功插入 " + inserted + " 本图书</p>");
            } else {
                out.println("<p class='info'>图书数据已存在：" + bookCount + " 本</p>");
            }
        %>
        
        <%
            // 4. 插入借阅记录
            out.println("<h3>4. 插入借阅记录</h3>");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM borrow_records");
            rs.next();
            int borrowCount = rs.getInt(1);
            rs.close();
            
            if (borrowCount < 30) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                long now = System.currentTimeMillis();
                long dayMs = 24 * 60 * 60 * 1000L;
                
                int inserted = 0;
                for (int i = borrowCount; i < 30; i++) {
                    try {
                        int userId = (i % 28) + 2;
                        int bookId = (i % 30) + 1;
                        long borrowDate = now - (i + 5) * dayMs;
                        long dueDate = borrowDate + 30 * dayMs;
                        String status = i < 20 ? "returned" : (i < 25 ? "borrowed" : "overdue");
                        String returnDate = "returned".equals(status) ? 
                            sdf.format(new Date(borrowDate + (i % 20 + 5) * dayMs)) : null;
                        
                        String sql = String.format(
                            "INSERT INTO borrow_records(user_id, book_id, borrow_date, due_date, return_date, status, renew_count) " +
                            "VALUES(%d, %d, '%s', '%s', %s, '%s', %d)",
                            userId, bookId, sdf.format(new Date(borrowDate)), sdf.format(new Date(dueDate)),
                            returnDate != null ? "'" + returnDate + "'" : "NULL",
                            status, i % 3
                        );
                        stmt.executeUpdate(sql);
                        inserted++;
                    } catch (Exception e) {
                        // 忽略错误
                    }
                }
                out.println("<p class='success'>✅ 成功插入 " + inserted + " 条借阅记录</p>");
            } else {
                out.println("<p class='info'>借阅记录已存在：" + borrowCount + " 条</p>");
            }
        %>
        
        <%
            // 5. 插入罚款记录
            out.println("<h3>5. 插入罚款记录</h3>");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM fine_records");
            rs.next();
            int fineCount = rs.getInt(1);
            rs.close();
            
            if (fineCount < 30) {
                int inserted = 0;
                for (int i = fineCount; i < 30; i++) {
                    try {
                        int userId = (i % 28) + 2;
                        int borrowId = i + 1;
                        int days = (i % 10) + 1;
                        double amount = days * 0.5;
                        String status = i < 20 ? "paid" : "unpaid";
                        
                        stmt.executeUpdate(String.format(
                            "INSERT INTO fine_records(user_id, borrow_record_id, amount, days, status) " +
                            "VALUES(%d, %d, %.2f, %d, '%s')",
                            userId, borrowId, amount, days, status
                        ));
                        inserted++;
                    } catch (Exception e) {
                        // 忽略错误
                    }
                }
                out.println("<p class='success'>✅ 成功插入 " + inserted + " 条罚款记录</p>");
            } else {
                out.println("<p class='info'>罚款记录已存在：" + fineCount + " 条</p>");
            }
        %>
        
        <%
            // 6. 插入预约记录
            out.println("<h3>6. 插入预约记录</h3>");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM reservations");
            rs.next();
            int reservationCount = rs.getInt(1);
            rs.close();
            
            if (reservationCount < 30) {
                int inserted = 0;
                String[] statuses = {"waiting", "notified", "completed", "cancelled"};
                for (int i = reservationCount; i < 30; i++) {
                    try {
                        int userId = (i % 28) + 2;
                        int bookId = (i % 30) + 1;
                        String status = statuses[i % 4];
                        
                        stmt.executeUpdate(String.format(
                            "INSERT INTO reservations(user_id, book_id, status) VALUES(%d, %d, '%s')",
                            userId, bookId, status
                        ));
                        inserted++;
                    } catch (Exception e) {
                        // 忽略错误
                    }
                }
                out.println("<p class='success'>✅ 成功插入 " + inserted + " 条预约记录</p>");
            } else {
                out.println("<p class='info'>预约记录已存在：" + reservationCount + " 条</p>");
            }
        %>
        
        <h2>📊 最终数据统计</h2>
        <table>
            <tr>
                <th>表名</th>
                <th>记录数</th>
            </tr>
            <%
                String[] tables = {"categories", "users", "books", "borrow_records", "fine_records", "reservations"};
                String[] tableNames = {"分类", "用户", "图书", "借阅记录", "罚款记录", "预约记录"};
                
                for (int i = 0; i < tables.length; i++) {
                    rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tables[i]);
                    rs.next();
                    out.println("<tr><td>" + tableNames[i] + "</td><td>" + rs.getInt(1) + " 条</td></tr>");
                    rs.close();
                }
            %>
        </table>
        
        <div style="margin-top: 20px; text-align: center;">
            <a href="login.jsp" class="btn" style="background: #28a745;">🔐 登录系统</a>
            <a href="databaseTables.jsp" class="btn">📊 查看表结构</a>
            <a href="admin/index.jsp" class="btn">👨‍💼 管理员首页</a>
        </div>
        
        <%
            } catch (Exception e) {
                out.println("<h3 class='error'>❌ 错误：" + e.getMessage() + "</h3>");
                e.printStackTrace();
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