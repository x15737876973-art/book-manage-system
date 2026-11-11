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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书管理Servlet
 */
@WebServlet("/admin/bookManageServlet")
public class BookManageServlet extends HttpServlet {
    private BookDao bookDao = new BookDao();
    private CategoryDao categoryDao = new CategoryDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 处理GET请求的中文参数
        response.setContentType("text/html;charset=UTF-8");

        // 手动解析查询字符串并正确解码
        String queryString = request.getQueryString();
        if (queryString != null) {
            try {
                // 使用安全解析方法
                Map<String, String> params = safeParseQueryString(queryString);

                // 打印调试信息
                System.out.println("=== GET请求调试信息 ===");
                System.out.println("原始queryString: " + queryString);

                String keyword = params.get("keyword");
                String categoryId = params.get("categoryId");
                String action = params.get("action");

                System.out.println("keyword参数: " + keyword);
                System.out.println("categoryId参数: " + categoryId);
                System.out.println("action参数: " + action);

                // 将参数重新设置到request中
                if (keyword != null) {
                    request.setAttribute("keyword", keyword);
                }
                if (categoryId != null) {
                    request.setAttribute("categoryId", categoryId);
                }
                if (action != null) {
                    request.setAttribute("action", action);
                }
            } catch (Exception e) {
                System.out.println("处理GET参数时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }

        doPost(request, response);
    }

    /**
     * 安全地解析查询字符串
     */
    private Map<String, String> safeParseQueryString(String queryString) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return params;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx == -1) continue;

            String key = pair.substring(0, idx);
            String value = pair.substring(idx + 1);

            // 先尝试UTF-8解码
            key = URLDecoder.decode(key, StandardCharsets.UTF_8);
            value = URLDecoder.decode(value, StandardCharsets.UTF_8);
            params.put(key, value);
        }
        return params;
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
        } else if ("add".equals(action)) {
            addBookPage(request, response);
        } else if ("save".equals(action)) {
            addBook(request, response);
        } else if ("update".equals(action)) {
            updateBook(request, response);
        } else if ("delete".equals(action)) {
            deleteBook(request, response);
        } else if ("toAdd".equals(action)) {
            toAddBook(request, response);
        } else if ("toEdit".equals(action)) {
            toEditBook(request, response);
        } else {
            listBooks(request, response);
        }
    }
    
    /**
     * 查询图书列表
     */
    private void listBooks(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            System.out.println("=== 开始查询图书列表 ===");
            String categoryIdStr = request.getParameter("categoryId");
            List<Book> books;
            
            if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
                books = bookDao.findByCategoryId(Integer.parseInt(categoryIdStr));
            } else {
                books = bookDao.findAll();
            }
            
            System.out.println("查询到的图书数量: " + books.size());
            
            List<Category> categories = categoryDao.findAll();
            System.out.println("查询到的分类数量: " + categories.size());
            
            request.setAttribute("bookList", books);
            request.setAttribute("categoryList", categories);
            request.getRequestDispatcher("/admin/bookList.jsp").forward(request, response);
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
            System.out.println("=== 开始搜索图书 ===");
            String keyword = request.getParameter("keyword");
            String categoryIdStr = request.getParameter("categoryId");
            
            System.out.println("搜索参数 - keyword: " + keyword + ", categoryId: " + categoryIdStr);
            System.out.println("keyword的长度: " + (keyword != null ? keyword.length() : 0));
            System.out.println("keyword的字符编码: " + (keyword != null ? keyword.getBytes("UTF-8").length : 0));
            
            // 如果关键词为空，返回所有图书
            if (keyword == null || keyword.trim().isEmpty()) {
                listBooks(request, response);
                return;
            }
            
            List<Book> books;
            if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
                books = bookDao.searchBooks(keyword.trim(), Integer.parseInt(categoryIdStr));
            } else {
                books = bookDao.searchBooks(keyword.trim());
            }
            
            System.out.println("搜索到的图书数量: " + books.size());
            
            List<Category> categories = categoryDao.findAll();
            request.setAttribute("bookList", books);
            request.setAttribute("categoryList", categories);
            request.setAttribute("keyword", keyword);
            
            // 添加查询结果提示
            if (books.isEmpty()) {
                request.setAttribute("msg", "未找到符合条件的图书");
                request.setAttribute("msgType", "error");
            } else {
                request.setAttribute("msg", "查询成功，共找到 " + books.size() + " 本图书");
                request.setAttribute("msgType", "success");
            }
            
            request.getRequestDispatcher("/admin/bookList.jsp").forward(request, response);
        } catch (Exception e) {
            System.out.println("搜索图书失败: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "搜索图书失败：" + e.getMessage());
            request.getRequestDispatcher("/admin/error.jsp").forward(request, response);
        }
    }
    
    /**
     * 跳转到添加页面
     */
    private void toAddBook(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Category> categories = categoryDao.findAll();
        request.setAttribute("categoryList", categories);
        request.getRequestDispatcher("/admin/bookEdit.jsp").forward(request, response);
    }
    
    /**
     * 添加图书页面
     */
    private void addBookPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Category> categories = categoryDao.findAll();
        request.setAttribute("categoryList", categories);
        request.getRequestDispatcher("/admin/bookEdit.jsp").forward(request, response);
    }
    
    /**
     * 添加图书
     */
    private void addBook(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String bid = request.getParameter("bid");
        String isbn = request.getParameter("isbn");
        String bname = request.getParameter("bname");
        String author = request.getParameter("author");
        String publisher = request.getParameter("publisher");
        String publishDate = request.getParameter("publishDate");
        String priceStr = request.getParameter("price");
        String categoryIdStr = request.getParameter("categoryId");
        String stockStr = request.getParameter("stock");
        String location = request.getParameter("location");
        String description = request.getParameter("description");
        
        // 检查图书编号是否已存在
        if (bookDao.findByBid(bid) != null) {
            request.setAttribute("msg", "图书编号已存在！");
            toAddBook(request, response);
            return;
        }
        
        Book book = new Book();
        book.setBid(bid);
        book.setIsbn(isbn);
        book.setBname(bname);
        book.setAuthor(author);
        book.setPublisher(publisher);
        if (publishDate != null && !publishDate.isEmpty()) {
            book.setPublishDate(java.sql.Date.valueOf(publishDate));
        }
        if (priceStr != null && !priceStr.isEmpty()) {
            book.setPrice(Double.parseDouble(priceStr));
        }
        if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
            book.setCategoryId(Integer.parseInt(categoryIdStr));
        }
        if (stockStr != null && !stockStr.isEmpty()) {
            int stock = Integer.parseInt(stockStr);
            book.setStock(stock);
            book.setAvailableStock(stock);
        }
        book.setLocation(location);
        book.setDescription(description);
        
        int row = bookDao.add(book);
        if (row > 0) {
            response.sendRedirect("bookManageServlet?action=list");
        } else {
            request.setAttribute("msg", "添加失败！");
            toAddBook(request, response);
        }
    }
    
    /**
     * 跳转到编辑页面
     */
    private void toEditBook(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String bid = request.getParameter("bid");
        
        if (bid != null) {
            Book book = bookDao.findByBid(bid);
            request.setAttribute("book", book);
        }
        
        List<Category> categories = categoryDao.findAll();
        request.setAttribute("categoryList", categories);
        request.getRequestDispatcher("/admin/bookEdit.jsp").forward(request, response);
    }
    
    /**
     * 更新图书
     */
    private void updateBook(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String bid = request.getParameter("bid");
        String isbn = request.getParameter("isbn");
        String bname = request.getParameter("bname");
        String author = request.getParameter("author");
        String publisher = request.getParameter("publisher");
        String publishDate = request.getParameter("publishDate");
        String priceStr = request.getParameter("price");
        String categoryIdStr = request.getParameter("categoryId");
        String stockStr = request.getParameter("stock");
        String availableStockStr = request.getParameter("availableStock");
        String location = request.getParameter("location");
        String description = request.getParameter("description");
        
        Book book = new Book();
        book.setBid(bid);
        book.setIsbn(isbn);
        book.setBname(bname);
        book.setAuthor(author);
        book.setPublisher(publisher);
        if (publishDate != null && !publishDate.isEmpty()) {
            book.setPublishDate(java.sql.Date.valueOf(publishDate));
        }
        if (priceStr != null && !priceStr.isEmpty()) {
            book.setPrice(Double.parseDouble(priceStr));
        }
        if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
            book.setCategoryId(Integer.parseInt(categoryIdStr));
        }
        if (stockStr != null && !stockStr.isEmpty()) {
            book.setStock(Integer.parseInt(stockStr));
        }
        if (availableStockStr != null && !availableStockStr.isEmpty()) {
            book.setAvailableStock(Integer.parseInt(availableStockStr));
        }
        book.setLocation(location);
        book.setDescription(description);
        
        int row = bookDao.update(book);
        if (row > 0) {
            response.sendRedirect("bookManageServlet?action=list");
        } else {
            request.setAttribute("msg", "更新失败！");
            toEditBook(request, response);
        }
    }
    
    /**
     * 删除图书
     */
    private void deleteBook(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String bid = request.getParameter("bid");
        
        if (bid != null) {
            bookDao.delete(bid);
        }
        
        response.sendRedirect("bookManageServlet?action=list");
    }
}