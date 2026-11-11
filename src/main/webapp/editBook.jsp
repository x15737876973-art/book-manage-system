<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.book.dao.BookDao,com.book.entity.Book" %>
<html>
<head>
    <title>修改图书</title>
</head>
<body>
<%
    String bid = request.getParameter("bid");
    BookDao dao = new BookDao();
    Book target = null;
    for(Book b : dao.findAll()){
        if(b.getBid().equals(bid)){
            target = b;
            break;
        }
    }
%>
<h2>修改图书信息</h2>
<form action="bookServlet?action=edit" method="post">
    书号：<input name="bid" value="<%=target.getBid()%>" readonly><br>
    书名：<input name="bname" value="<%=target.getBname()%>" required><br>
    作者：<input name="author" value="<%=target.getAuthor()%>"><br>
    库存：<input name="stock" type="number" value="<%=target.getStock()%>"><br>
    <input type="submit" value="保存修改">
</form>
<a href="bookList.jsp">返回列表</a>
</body>
</html>