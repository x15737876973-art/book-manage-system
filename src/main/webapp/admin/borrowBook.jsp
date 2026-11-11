<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>办理借书</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: #f5f5f5; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 30px; }
        .header h1 { font-size: 24px; }
        .container { padding: 30px; }
        .form-container { background: white; border-radius: 10px; padding: 30px; max-width: 600px; margin: 0 auto; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .form-title { font-size: 20px; color: #333; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #667eea; }
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; margin-bottom: 8px; color: #333; font-weight: bold; }
        .form-group select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; }
        .btn-group { display: flex; gap: 10px; }
        .btn { padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; font-size: 14px; }
        .btn-primary { background: #667eea; color: white; }
        .btn-secondary { background: #6c757d; color: white; }
        .msg { padding: 10px; margin-bottom: 20px; border-radius: 5px; background: #fff3cd; color: #856404; }
    </style>
</head>
<body>
    <div class="header">
        <h1>📚 图书管理系统</h1>
    </div>
    
    <div class="container">
        <div class="form-container">
            <h2 class="form-title">办理借书</h2>
            
            <c:if test="${not empty msg}">
                <div class="msg">${msg}</div>
            </c:if>
            
            <form action="borrowServlet" method="post">
                <input type="hidden" name="action" value="borrow">
                
                <div class="form-group">
                    <label>选择读者：</label>
                    <select name="userId" required>
                        <option value="">请选择读者</option>
                        <c:forEach items="${readerList}" var="reader">
                            <option value="${reader.id}">${reader.realName} (${reader.username})</option>
                        </c:forEach>
                    </select>
                </div>
                
                <div class="form-group">
                    <label>选择图书：</label>
                    <select name="bookId" required>
                        <option value="">请选择图书</option>
                        <c:forEach items="${bookList}" var="book">
                            <c:if test="${book.availableStock > 0}">
                                <option value="${book.id}">${book.bname} - ${book.author} (可借: ${book.availableStock})</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>
                
                <div class="btn-group">
                    <button type="submit" class="btn btn-primary">确认借书</button>
                    <a href="borrowServlet?action=list" class="btn btn-secondary">返回</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>