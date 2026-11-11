<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.book.dao.FineDao" %>
<%@ page import="com.book.entity.FineRecord" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>检查罚款记录</title>
</head>
<body>
    <h1>罚款记录检查</h1>
    
    <%
        FineDao fineDao = new FineDao();
        List<FineRecord> fineList = fineDao.findAll();
        request.setAttribute("fineList", fineList);
    %>
    
    <p>罚款记录数量: <%= fineList.size() %></p>
    
    <table border="1">
        <tr>
            <th>ID</th>
            <th>读者姓名</th>
            <th>图书名称</th>
            <th>金额</th>
            <th>状态</th>
        </tr>
        <c:forEach items="${fineList}" var="fine">
            <tr>
                <td>${fine.id}</td>
                <td>${fine.userName}</td>
                <td>${fine.bookName}</td>
                <td>${fine.amount}</td>
                <td>${fine.status}</td>
            </tr>
        </c:forEach>
    </table>
</body>
</html>
