<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List,com.book.entity.Book,com.book.dao.BookDao" %>
<html>
<head>
    <title>图书数据操作</title>
</head>
<body>
<%
    String role = (String)session.getAttribute("loginRole");
    if(role == null){
        response.sendRedirect("login.jsp");
        return;
    }
    BookDao bookDao = new BookDao();
    List<Book> bookList = bookDao.findAll();
%>
<h2>图书列表</h2>
<table border="1">
    <tr>
        <th>书号</th>
        <th>书名</th>
        <th>作者</th>
        <th>库存</th>
        <% if("admin".equals(role)){ %>
        <th>操作</th>
        <% } %>
    </tr>
    <% for(Book b : bookList){ %>
    <tr>
        <td><%=b.getBid()%></td>
        <td><%=b.getBname()%></td>
        <td><%=b.getAuthor()%></td>
        <td><%=b.getStock()%></td>
        <% if("admin".equals(role)){ %>
        <td>
            <a href="editBook.jsp?bid=<%=b.getBid()%>">修改</a>
            <a href="bookServlet?action=del&bid=<%=b.getBid()%>">删除</a>
        </td>
        <% } %>
    </tr>
    <% } %>
</table>

<% if("admin".equals(role)){ %>
<h3>新增图书</h3>
<form action="bookServlet?action=add" method="post">
    书号：<input name="bid" required><br>
    书名：<input name="bname" required><br>
    作者：<input name="author"><br>
    库存：<input name="stock" type="number" value="0"><br>
    <input type="submit" value="新增">
</form>
<% } %>
<br>
<a href="index.jsp">返回首页</a>
<a href="login.jsp">退出登录</a>
</body>
</html>