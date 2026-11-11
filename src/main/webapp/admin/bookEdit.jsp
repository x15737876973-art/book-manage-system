<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>编辑图书</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: #f5f5f5; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 30px; }
        .header h1 { font-size: 24px; }
        .container { padding: 30px; }
        .form-container { background: white; border-radius: 10px; padding: 30px; max-width: 800px; margin: 0 auto; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .form-title { font-size: 20px; color: #333; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #667eea; }
        .form-row { display: flex; gap: 20px; margin-bottom: 20px; }
        .form-group { flex: 1; }
        .form-group label { display: block; margin-bottom: 8px; color: #333; font-weight: bold; }
        .form-group input, .form-group select, .form-group textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 14px; }
        .form-group textarea { height: 100px; resize: vertical; }
        .btn-group { display: flex; gap: 10px; margin-top: 20px; }
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
            <h2 class="form-title">
                <c:choose>
                    <c:when test="${not empty book}">编辑图书</c:when>
                    <c:otherwise>添加图书</c:otherwise>
                </c:choose>
            </h2>
            
            <c:if test="${not empty msg}">
                <div class="msg">${msg}</div>
            </c:if>
            
            <form action="bookManageServlet" method="post">
                <input type="hidden" name="action" value="${not empty book ? 'update' : 'save'}">
                
                <div class="form-row">
                    <div class="form-group">
                        <label>图书编号：</label>
                        <input type="text" name="bid" value="${book.bid}" required>
                    </div>
                    <div class="form-group">
                        <label>ISBN：</label>
                        <input type="text" name="isbn" value="${book.isbn}">
                    </div>
                </div>
                
                <div class="form-row">
                    <div class="form-group">
                        <label>书名：</label>
                        <input type="text" name="bname" value="${book.bname}" required>
                    </div>
                    <div class="form-group">
                        <label>作者：</label>
                        <input type="text" name="author" value="${book.author}">
                    </div>
                </div>
                
                <div class="form-row">
                    <div class="form-group">
                        <label>出版社：</label>
                        <input type="text" name="publisher" value="${book.publisher}">
                    </div>
                    <div class="form-group">
                        <label>出版日期：</label>
                        <input type="date" name="publishDate" value="${book.publishDate}">
                    </div>
                </div>
                
                <div class="form-row">
                    <div class="form-group">
                        <label>价格：</label>
                        <input type="number" name="price" value="${book.price}" step="0.01">
                    </div>
                    <div class="form-group">
                        <label>分类：</label>
                        <select name="categoryId">
                            <option value="">请选择</option>
                            <c:forEach items="${categoryList}" var="cat">
                                <option value="${cat.id}" ${book.categoryId == cat.id ? 'selected' : ''}>${cat.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                
                <div class="form-row">
                    <div class="form-group">
                        <label>总库存：</label>
                        <input type="number" name="stock" value="${book.stock}" min="0">
                    </div>
                    <div class="form-group">
                        <label>可借数量：</label>
                        <input type="number" name="availableStock" value="${book.availableStock}" min="0">
                    </div>
                </div>
                
                <div class="form-group">
                    <label>存放位置：</label>
                    <input type="text" name="location" value="${book.location}">
                </div>
                
                <div class="form-group">
                    <label>简介：</label>
                    <textarea name="description">${book.description}</textarea>
                </div>
                
                <div class="btn-group">
                    <button type="submit" class="btn btn-primary">保存</button>
                    <a href="bookManageServlet?action=list" class="btn btn-secondary">返回</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>