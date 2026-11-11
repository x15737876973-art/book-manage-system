package com.book.servlet;

import com.book.dao.BookDao;
import com.book.dao.CategoryDao;
import com.book.entity.Book;
import com.book.entity.Category;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 读者图书检索Servlet
 */
@WebServlet("/reader/bookSearchServlet")
public class BookSearchServlet extends HttpServlet {
    private BookDao bookDao = new BookDao();
    private CategoryDao categoryDao = new CategoryDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=UTF-8");
        String action = request.getParameter("action");
        
        if ("list".equals(action)) {
            listBooks(request, response);
        } else if ("search".equals(action)) {
            searchBooks(request, response);
        } else if ("detail".equals(action)) {
            showDetail(request, response);
        } else {
            listBooks(request, response);
        }
    }
    
    /**
     * 查询图书列表
     */
    private void listBooks(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            System.out.println("=== 读者端开始查询图书列表 ===");
            String categoryIdStr = request.getParameter("categoryId");
            List<Book> books;
            
            if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
                books = bookDao.findByCategoryId(Integer.parseInt(categoryIdStr));
            } else {
                books = bookDao.findAll();
            }
            
            System.out.println("查询到的图书数量: " + books.size());
            
            List<Category> categories = categoryDao.findAll();
            request.setAttribute("bookList", books);
            request.setAttribute("categoryList", categories);
            request.getRequestDispatcher("/reader/bookSearch.jsp").forward(request, response);
        } catch (Exception e) {
            System.out.println("查询图书列表失败: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "查询图书列表失败：" + e.getMessage());
            request.getRequestDispatcher("/admin/error.jsp").forward(request, response);
        }
    }
    
    /**
     * 搜索图书
     */
    private void searchBooks(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            System.out.println("=== 读者端开始搜索图书 ===");
            String keyword = request.getParameter("keyword");
            String categoryIdStr = request.getParameter("categoryId");
            
            System.out.println("搜索参数 - keyword: " + keyword + ", categoryId: " + categoryIdStr);
            
            // 如果keyword为空，则查询所有图书
            if (keyword == null || keyword.trim().isEmpty()) {
                listBooks(request, response);
                return;
            }
            
            List<Book> books;
            if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
                books = bookDao.searchBooks(keyword, Integer.parseInt(categoryIdStr));
            } else {
                books = bookDao.searchBooks(keyword);
            }
            
            System.out.println("搜索到的图书数量: " + books.size());
            
            List<Category> categories = categoryDao.findAll();
            request.setAttribute("bookList", books);
            request.setAttribute("categoryList", categories);
            request.setAttribute("keyword", keyword);
            
            // 添加查询结果提示
            if (books.isEmpty()) {
                request.setAttribute("msg", "未找到符合条件的图书");
            } else {
                request.setAttribute("msg", "查询成功，共找到 " + books.size() + " 本图书");
            }
            
            request.getRequestDispatcher("/reader/bookSearch.jsp").forward(request, response);
        } catch (Exception e) {
            System.out.println("搜索图书失败: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "搜索图书失败：" + e.getMessage());
            request.getRequestDispatcher("/admin/error.jsp").forward(request, response);
        }
    }
    
    /**
     * 显示图书详情
     */
    private void showDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String bookIdStr = request.getParameter("bookId");
        
        if (bookIdStr != null) {
            Book book = bookDao.findById(Integer.parseInt(bookIdStr));
            request.setAttribute("book", book);
        }
        
        request.getRequestDispatcher("/reader/bookDetail.jsp").forward(request, response);
    }
}