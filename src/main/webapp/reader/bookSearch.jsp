<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>图书检索 - 图书管理系统</title>
    <link rel="stylesheet" href="../css/style.css">
    <script src="../js/common.js"></script>
    <style>
        .pager {
            display: flex; align-items: center; justify-content: space-between;
            flex-wrap: wrap; gap: 12px; padding: 14px 4px 4px;
            border-top: 1px solid #eef0f5; margin-top: 4px;
        }
        .pager-info { color: #7b8494; font-size: 13.5px; }
        .pager-btns { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
        .pager-btns a, .pager-btns span {
            min-width: 34px; height: 34px; padding: 0 10px;
            display: inline-flex; align-items: center; justify-content: center;
            border-radius: 7px; border: 1px solid #dfe3ee; background: #fff;
            color: #5a6683; font-size: 13.5px; text-decoration: none; transition: all .18s;
        }
        .pager-btns a:hover { border-color: #667eea; color: #667eea; }
        .pager-btns .current {
            background: linear-gradient(135deg, #667eea, #764ba2);
            border-color: transparent; color: #fff; font-weight: 600;
        }
        .pager-btns .disabled { color: #c3c9d6; background: #f7f8fb; cursor: not-allowed; }
        .pager-btns .ellipsis { border: none; background: transparent; color: #a9b2c9; }
        .pager-size { display: flex; align-items: center; gap: 6px; font-size: 13.5px; color: #7b8494; }
        .pager-size select {
            height: 32px; padding: 0 8px; border-radius: 6px;
            border: 1px solid #dfe3ee; background: #fff; color: #5a6683;
        }
    </style>
</head>
<body>
<div class="header">
    <h1>📚 图书管理系统</h1>
    <div class="user-info">
        <span class="username">欢迎，${sessionScope.loginUser.realName}（读者）</span>
        <a href="../agent.jsp">🤖 智能助手</a>
        <a href="../logoutServlet">退出登录</a>
    </div>
</div>

<div class="container">
    <div class="sidebar">
        <ul>
            <li><a href="../reader/indexServlet"><span>📊 首页</span></a></li>
            <li><a href="../reader/bookSearchServlet?action=list" class="active"><span>🔍 图书检索</span></a></li>
            <li><a href="../reader/borrowOpServlet?action=myBorrows"><span>📖 我的借阅</span></a></li>
            <li><a href="../reader/borrowOpServlet?action=myReservations"><span>📋 我的预约</span></a></li>
            <li><a href="../reader/borrowOpServlet?action=myFines"><span>💰 我的罚款</span></a></li>
            <li><a href="../reader/profileServlet?action=view"><span>👤 个人中心</span></a></li>
        </ul>
    </div>

    <div class="main-content">
        <h2 class="page-title">图书检索</h2>

        <c:if test="${not empty msg}">
            <div class="msg msg-success">${msg}</div>
        </c:if>

        <div class="card">
            <div class="card-header">
                <h3 class="card-title">搜索图书</h3>
            </div>

            <div class="toolbar">
                <div class="search-box">
                    <form action="../reader/bookSearchServlet" method="get"
                          style="display:flex;gap:10px;flex-wrap:wrap;">
                        <input type="hidden" name="action" value="search">
                        <select name="categoryId">
                            <option value="">全部分类</option>
                            <c:forEach items="${categoryList}" var="cat">
                                <option value="${cat.id}" ${categoryId == cat.id ? 'selected' : ''}>${cat.name}</option>
                            </c:forEach>
                        </select>
                        <input type="text" name="keyword" placeholder="书名 / 作者 / ISBN"
                               value="${keyword}" maxlength="50" style="width:280px;">
                        <button type="submit" class="btn btn-primary">🔍 搜索</button>
                        <a href="../reader/bookSearchServlet?action=list" class="btn btn-secondary">重置</a>
                    </form>
                </div>
            </div>

            <div class="table-container">
                <table>
                    <thead>
                    <tr>
                        <th>图书编号</th>
                        <th>书名</th>
                        <th>作者</th>
                        <th>分类</th>
                        <th>出版社</th>
                        <th>价格</th>
                        <th>可借</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${not empty bookList}">
                            <c:forEach items="${bookList}" var="book">
                                <tr>
                                    <td>${book.bid}</td>
                                    <td class="text-ellipsis" title="${book.bname}">${book.bname}</td>
                                    <td>${book.author}</td>
                                    <td>${book.categoryName}</td>
                                    <td class="text-ellipsis" title="${book.publisher}">${book.publisher}</td>
                                    <td>￥<fmt:formatNumber value="${book.price}" pattern="0.00"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${book.availableStock > 0}">
                                                <span class="status-badge status-active">${book.availableStock}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge status-overdue">0</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${book.availableStock > 0}">
                                                <a href="../reader/borrowOpServlet?action=borrow&bookId=${book.id}"
                                                   class="btn btn-success btn-sm"
                                                   onclick="return confirm('确定借阅《${book.bname}》吗？');">借阅</a>
                                                <a href="../reader/borrowOpServlet?action=reserve&bookId=${book.id}"
                                                   class="btn btn-info btn-sm">预约</a>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">已借完</span>
                                                <a href="../reader/borrowOpServlet?action=reserve&bookId=${book.id}"
                                                   class="btn btn-warning btn-sm">预约</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8" style="text-align:center;padding:36px;color:#999;">
                                    📚 暂无符合条件的图书
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>

            <%-- ===================== 分页控件 ===================== --%>
            <c:if test="${page != null and page.totalRecords > 0}">
                <div class="pager">
                    <div class="pager-info">
                        共 <strong>${page.totalRecords}</strong> 本，
                        第 ${page.startIndex}~${page.endIndex} 条，
                        第 <strong>${page.pageNumber}</strong> / ${page.totalPages} 页
                    </div>

                    <div class="pager-btns">
                        <c:choose>
                            <c:when test="${page.hasPrevious}">
                                <a href="javascript:goPage(${page.previousPage})">‹ 上一页</a>
                            </c:when>
                            <c:otherwise>
                                <span class="disabled">‹ 上一页</span>
                            </c:otherwise>
                        </c:choose>

                        <c:set var="startPage" value="${page.pageNumber - 2}"/>
                        <c:if test="${startPage < 1}"><c:set var="startPage" value="1"/></c:if>
                        <c:set var="endPage" value="${page.pageNumber + 2}"/>
                        <c:if test="${endPage > page.totalPages}"><c:set var="endPage" value="${page.totalPages}"/></c:if>

                        <c:if test="${startPage > 1}">
                            <a href="javascript:goPage(1)">1</a>
                            <c:if test="${startPage > 2}"><span class="ellipsis">…</span></c:if>
                        </c:if>

                        <c:forEach var="p" begin="${startPage}" end="${endPage}">
                            <c:choose>
                                <c:when test="${p == page.pageNumber}">
                                    <span class="current">${p}</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="javascript:goPage(${p})">${p}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>

                        <c:if test="${endPage < page.totalPages}">
                            <c:if test="${endPage < page.totalPages - 1}"><span class="ellipsis">…</span></c:if>
                            <a href="javascript:goPage(${page.totalPages})">${page.totalPages}</a>
                        </c:if>

                        <c:choose>
                            <c:when test="${page.hasNext}">
                                <a href="javascript:goPage(${page.nextPage})">下一页 ›</a>
                            </c:when>
                            <c:otherwise>
                                <span class="disabled">下一页 ›</span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="pager-size">
                        <label>每页</label>
                        <select onchange="changeSize(this.value)">
                            <option value="10" ${page.pageSize == 10 ? 'selected' : ''}>10</option>
                            <option value="20" ${page.pageSize == 20 ? 'selected' : ''}>20</option>
                            <option value="50" ${page.pageSize == 50 ? 'selected' : ''}>50</option>
                        </select>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
</div>

<script>
    (function () {
        var params = new URLSearchParams(window.location.search);
        var keyword = params.get('keyword');
        var action = (keyword !== null && keyword !== '') ? 'search' : 'list';

        window.goPage = function (pageNo) {
            var url = new URL(window.location.href);
            url.searchParams.set('action', action);
            url.searchParams.set('page', pageNo);
            window.location.href = url.toString();
        };

        window.changeSize = function (size) {
            var url = new URL(window.location.href);
            url.searchParams.set('action', action);
            url.searchParams.set('pageSize', size);
            url.searchParams.set('page', 1);
            window.location.href = url.toString();
        };
    })();
</script>
</body>
</html>