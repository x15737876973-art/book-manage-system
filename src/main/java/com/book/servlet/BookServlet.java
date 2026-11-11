package com.book.servlet;

import com.book.dao.BookDao;
import com.book.entity.Book;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/bookServlet")
public class BookServlet extends HttpServlet {
    private BookDao bookDao = new BookDao();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        String action = request.getParameter("action");
        if("add".equals(action)){
            String bid = request.getParameter("bid");
            String bname = request.getParameter("bname");
            String author = request.getParameter("author");
            String stockStr = request.getParameter("stock");
            int stock = 0;
            if(stockStr != null && !stockStr.trim().isEmpty()){
                try {
                    stock = Integer.parseInt(stockStr.trim());
                } catch (NumberFormatException e) {
                    stock = 0;
                }
            }
            bookDao.add(new Book(bid,bname,author,stock));
        }else if("del".equals(action)){
            String bid = request.getParameter("bid");
            bookDao.delete(bid);
        }else if("edit".equals(action)){
            String bid = request.getParameter("bid");
            String bname = request.getParameter("bname");
            String author = request.getParameter("author");
            String stockStr = request.getParameter("stock");
            int stock = 0;
            if(stockStr != null && !stockStr.trim().isEmpty()){
                try {
                    stock = Integer.parseInt(stockStr.trim());
                } catch (NumberFormatException e) {
                    stock = 0;
                }
            }
            Book existing = bookDao.findByBid(bid);
            if (existing != null) {
                int oldStock = existing.getStock() != null ? existing.getStock() : 0;
                int oldAvailable = existing.getAvailableStock() != null ? existing.getAvailableStock() : 0;
                int borrowed = oldStock - oldAvailable;
                existing.setBname(bname);
                existing.setAuthor(author);
                existing.setStock(stock);
                existing.setAvailableStock(Math.max(stock - borrowed, 0));
                bookDao.update(existing);
            }
        }
        response.sendRedirect("bookList.jsp");
    }
}