package com.book.servlet;

import com.book.dao.BookDao;
import com.book.entity.Book;
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
 * 简化版图书增删改 Servlet（管理员）。
 *
 * <p>旧版本位于根路径 {@code /bookServlet} 且<b>没有任何鉴权</b>，
 * 未登录用户可直接调用 add/del/edit 篡改图书数据。现改为管理员专用，
 * 并做参数校验。</p>
 */
@WebServlet("/bookServlet")
public class BookServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final BookDao bookDao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 该接口会修改图书数据，必须是管理员
        User loginUser = ServletUtils.requireRole(request, response, "admin");
        if (loginUser == null) {
            return;
        }

        String action = ServletUtils.getTrimmed(request, "action");
        try {
            if ("add".equals(action)) {
                addBook(request);
            } else if ("del".equals(action)) {
                deleteBook(request);
            } else if ("edit".equals(action)) {
                editBook(request);
            }
            response.sendRedirect(request.getContextPath() + "/admin/bookManageServlet?action=list");
        } catch (AppException | IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/admin/error.jsp").forward(request, response);
        } catch (Exception e) {
            getServletContext().log("图书操作失败", e);
            request.setAttribute("error", "操作失败，请稍后重试");
            request.getRequestDispatcher("/admin/error.jsp").forward(request, response);
        }
    }

    private void addBook(HttpServletRequest request) {
        String bid = ServletUtils.requireParam(request, "bid", "图书编号", 20);
        String bname = ServletUtils.requireParam(request, "bname", "书名", 200);
        String author = ServletUtils.getParamWithLimit(request, "author", 100);
        Integer stock = ServletUtils.getPositiveInt(request, "stock", 999999);

        if (bookDao.findByBid(bid) != null) {
            throw new AppException("图书编号【" + bid + "】已存在");
        }
        Book book = new Book(bid, bname, author, stock == null ? 0 : stock);
        bookDao.addBook(book);
    }

    private void deleteBook(HttpServletRequest request) {
        String bid = ServletUtils.requireParam(request, "bid", "图书编号", 20);
        if (bookDao.findByBid(bid) == null) {
            throw new AppException("图书不存在或已被删除");
        }
        bookDao.delete(bid);
    }

    private void editBook(HttpServletRequest request) {
        String bid = ServletUtils.requireParam(request, "bid", "图书编号", 20);
        String bname = ServletUtils.requireParam(request, "bname", "书名", 200);
        String author = ServletUtils.getParamWithLimit(request, "author", 100);
        Integer stock = ServletUtils.getPositiveInt(request, "stock", 999999);

        Book existing = bookDao.findByBid(bid);
        if (existing == null) {
            throw new AppException("图书不存在或已被删除");
        }
        int newStock = stock == null ? 0 : stock;
        int oldStock = existing.getStock() == null ? 0 : existing.getStock();
        int oldAvailable = existing.getAvailableStock() == null ? 0 : existing.getAvailableStock();
        int borrowed = oldStock - oldAvailable;

        existing.setBname(bname);
        existing.setAuthor(author);
        existing.setStock(newStock);
        existing.setAvailableStock(Math.max(newStock - borrowed, 0));
        bookDao.update(existing);
    }
}