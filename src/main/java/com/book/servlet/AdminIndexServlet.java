package com.book.servlet;

import com.book.dao.BookDao;
import com.book.dao.BorrowDao;
import com.book.dao.UserDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 管理员首页Servlet
 */
@WebServlet("/admin/indexServlet")
public class AdminIndexServlet extends HttpServlet {
    private BookDao bookDao = new BookDao();
    private BorrowDao borrowDao = new BorrowDao();
    private UserDao userDao = new UserDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 获取统计数据
        int totalBooks = bookDao.count();
        int totalReaders = userDao.findReaders().size();
        int currentBorrows = borrowDao.findAllCurrentBorrows().size();
        int overdueCount = borrowDao.findOverdue().size();
        
        request.setAttribute("totalBooks", totalBooks);
        request.setAttribute("totalReaders", totalReaders);
        request.setAttribute("currentBorrows", currentBorrows);
        request.setAttribute("overdueCount", overdueCount);
        
        request.getRequestDispatcher("/admin/index.jsp").forward(request, response);
    }
}