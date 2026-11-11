package com.book.servlet;

import com.book.dao.BookDao;
import com.book.dao.CategoryDao;
import com.book.entity.Book;
import com.book.entity.PageResult;
import com.book.entity.User;
import com.book.util.AppException;
import com.book.util.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 读者图书检索 Servlet。
 *
 * <h3>改造要点</h3>
 * <ul>
 *   <li><b>分页查询</b>：list / search 都走分页，不再一次性查询全部图书；</li>
 *   <li><b>参数校验</b>：bookId、categoryId 做整数校验，keyword 限制长度；</li>
 *   <li><b>身份校验</b>：需要已登录的读者身份；</li>
 *   <li>修正旧版 detail 动作跳转到不存在的 bookDetail.jsp 的问题。</li>
 * </ul>
 */
@WebServlet("/reader/bookSearchServlet")
public class BookSearchServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final BookDao bookDao = new BookDao();
    private final CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        User user = ServletUtils.requireRole(request, response, "reader");
        if (user == null) {
            return;
        }

        String action = ServletUtils.getTrimmed(request, "action");
        try {
            if ("search".equals(action)) {
                searchBooks(request, response);
            } else {
                listBooks(request, response);
            }
        } catch (AppException | IllegalArgumentException e) {
            request.setAttribute("msg", e.getMessage());
            listBooks(request, response);
        } catch (Exception e) {
            getServletContext().log("读者检索图书失败", e);
            request.setAttribute("error", "查询图书失败，请稍后重试");
            request.getRequestDispatcher("/admin/error.jsp").forward(request, response);
        }
    }

    /** 图书列表（分页） */
    private void listBooks(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = ServletUtils.getPageNumber(request);
        int size = ServletUtils.getPageSize(request);
        Integer categoryId = ServletUtils.getPositiveInt(request, "categoryId", Integer.MAX_VALUE);

        PageResult<Book> pageResult = bookDao.findPage(page, size, categoryId);

        request.setAttribute("page", pageResult);
        request.setAttribute("bookList", pageResult.getRecords());
        request.setAttribute("categoryList", categoryDao.findAll());
        request.setAttribute("categoryId", categoryId);
        request.getRequestDispatcher("/reader/bookSearch.jsp").forward(request, response);
    }

    /** 图书搜索（分页） */
    private void searchBooks(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = ServletUtils.getParamWithLimit(request, "keyword", 50);
        Integer categoryId = ServletUtils.getPositiveInt(request, "categoryId", Integer.MAX_VALUE);
        int page = ServletUtils.getPageNumber(request);
        int size = ServletUtils.getPageSize(request);

        PageResult<Book> pageResult = bookDao.searchPage(keyword, categoryId, page, size);

        request.setAttribute("page", pageResult);
        request.setAttribute("bookList", pageResult.getRecords());
        request.setAttribute("categoryList", categoryDao.findAll());
        request.setAttribute("keyword", keyword);
        request.setAttribute("categoryId", categoryId);
        request.setAttribute("msg", pageResult.getTotalRecords() == 0
                ? "未找到符合条件的图书，换个关键词试试"
                : "共找到 " + pageResult.getTotalRecords() + " 本图书");
        request.getRequestDispatcher("/reader/bookSearch.jsp").forward(request, response);
    }
}