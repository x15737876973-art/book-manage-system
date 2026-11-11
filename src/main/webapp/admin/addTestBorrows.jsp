<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.book.dao.*" %>
<%@ page import="com.book.entity.*" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>添加测试借阅数据</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
        }
        .success {
            color: green;
            padding: 10px;
            background: #e8f5e9;
            border-radius: 5px;
            margin: 10px 0;
        }
        .info {
            color: #666;
            padding: 10px;
            background: #f5f5f5;
            border-radius: 5px;
            margin: 10px 0;
        }
        .btn {
            display: inline-block;
            padding: 10px 20px;
            background: #4CAF50;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin: 10px 0;
        }
    </style>
</head>
<body>
    <h1>添加测试借阅数据</h1>
    
    <%
    BorrowDao borrowDao = new BorrowDao();
    UserDao userDao = new UserDao();
    BookDao bookDao = new BookDao();
    
    // 获取所有读者
    List<User> readers = userDao.findReaders();
    // 获取所有图书
    List<Book> books = bookDao.findAll();
    
    if (readers.isEmpty()) {
        out.println("<p class='info'>没有读者数据，请先添加读者</p>");
    } else if (books.isEmpty()) {
        out.println("<p class='info'>没有图书数据，请先添加图书</p>");
    } else {
        // 检查是否已有借阅记录
        List<BorrowRecord> existingRecords = borrowDao.findAll();
        
        if (existingRecords.size() < 5) {
            // 添加一些测试借阅记录
            int addedCount = 0;
            
            for (int i = 0; i < Math.min(5, readers.size()); i++) {
                User reader = readers.get(i);
                
                for (int j = 0; j < Math.min(2, books.size()); j++) {
                    Book book = books.get(j);
                    
                    // 检查是否已经借阅过
                    if (!borrowDao.hasBorrowed(reader.getId(), book.getId())) {
                        BorrowRecord record = new BorrowRecord();
                        record.setUserId(reader.getId());
                        record.setBookId(book.getId());
                        record.setBorrowDate(new java.sql.Date(System.currentTimeMillis()));
                        
                        // 设置归还日期为30天后
                        long dueTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000);
                        record.setDueDate(new java.sql.Date(dueTime));
                        
                        // 设置状态：部分借阅中，部分逾期
                        if (addedCount % 3 == 0) {
                            // 设置逾期状态（归还日期为昨天）
                            long overdueTime = System.currentTimeMillis() - (24L * 60 * 60 * 1000);
                            record.setDueDate(new java.sql.Date(overdueTime));
                            record.setStatus("overdue");
                        } else {
                            record.setStatus("borrowed");
                        }
                        
                        int row = borrowDao.add(record);
                        if (row > 0) {
                            addedCount++;
                            out.println("<p class='success'>✅ 添加借阅记录：读者 " + reader.getUsername() + 
                                       " 借阅图书 " + book.getTitle() + "，状态：" + record.getStatus() + "</p>");
                        }
                    }
                }
            }
            
            if (addedCount == 0) {
                out.println("<p class='info'>已存在足够的借阅记录，无需添加</p>");
            } else {
                out.println("<p class='success'>成功添加 " + addedCount + " 条借阅记录</p>");
            }
        } else {
            out.println("<p class='info'>已存在 " + existingRecords.size() + " 条借阅记录</p>");
        }
        
        // 显示当前借阅记录
        List<BorrowRecord> currentRecords = borrowDao.findAllCurrentBorrows();
        out.println("<h2>当前借阅记录（" + currentRecords.size() + " 条）</h2>");
        out.println("<table border='1' style='width:100%; border-collapse: collapse;'>");
        out.println("<tr style='background:#f0f0f0;'><th>ID</th><th>读者</th><th>图书</th><th>借阅日期</th><th>归还日期</th><th>状态</th></tr>");
        
        for (BorrowRecord record : currentRecords) {
            out.println("<tr>");
            out.println("<td>" + record.getId() + "</td>");
            out.println("<td>" + (record.getRealName() != null ? record.getRealName() : record.getUsername()) + "</td>");
            out.println("<td>" + record.getBookTitle() + "</td>");
            out.println("<td>" + record.getBorrowDate() + "</td>");
            out.println("<td>" + record.getDueDate() + "</td>");
            out.println("<td>" + record.getStatus() + "</td>");
            out.println("</tr>");
        }
        out.println("</table>");
    }
    %>
    
    <br>
    <a href="borrowServlet?action=list" class="btn">返回借阅管理</a>
</body>
</html>