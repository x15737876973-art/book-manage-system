package com.book.servlet;

import com.book.dao.BookDao;
import com.book.dao.BorrowDao;
import com.book.dao.UserDao;
import com.book.entity.User;
import com.book.util.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 管理员首页 Servlet：汇总看板数据。
 *
 * <p>改造点：加入身份校验与统一异常处理，避免数据库异常时页面出现 500 堆栈。</p>
 */
@WebServlet("/admin/indexServlet")
public class AdminIndexServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final BookDao bookDao = new BookDao();
    private final BorrowDao borrowDao = new BorrowDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User loginUser = ServletUtils.requireRole(request, response, "admin");
        if (loginUser == null) {
            return;
        }

        try {
            request.setAttribute("totalBooks", bookDao.count());
            request.setAttribute("totalReaders", userDao.findReaders().size());
            request.setAttribute("currentBorrows", borrowDao.findAllCurrentBorrows().size());
            request.setAttribute("overdueCount", borrowDao.findOverdue().size());
            request.getRequestDispatcher("/admin/index.jsp").forward(request, response);
        } catch (Exception e) {
            getServletContext().log("加载管理员首页失败", e);
            request.setAttribute("error", "加载首页数据失败，请检查数据库连接后重试");
            request.getRequestDispatcher("/admin/error.jsp").forward(request, response);
        }
    }
}