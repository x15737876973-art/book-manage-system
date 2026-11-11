package com.book.servlet;

import com.book.dao.CategoryDao;
import com.book.entity.Category;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 分类管理Servlet
 */
@WebServlet("/admin/categoryServlet")
public class CategoryServlet extends HttpServlet {
    private CategoryDao categoryDao = new CategoryDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        String action = request.getParameter("action");
        
        if ("list".equals(action)) {
            listCategories(request, response);
        } else if ("add".equals(action)) {
            addCategory(request, response);
        } else if ("update".equals(action)) {
            updateCategory(request, response);
        } else if ("delete".equals(action)) {
            deleteCategory(request, response);
        } else {
            listCategories(request, response);
        }
    }
    
    /**
     * 查询分类列表
     */
    private void listCategories(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Category> categories = categoryDao.findAll();
        request.setAttribute("categoryList", categories);
        request.getRequestDispatcher("/admin/categoryList.jsp").forward(request, response);
    }
    
    /**
     * 添加分类
     */
    private void addCategory(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        
        // 检查分类名是否已存在
        if (categoryDao.findByName(name) != null) {
            request.setAttribute("msg", "分类名称已存在！");
            listCategories(request, response);
            return;
        }
        
        Category category = new Category(name, description);
        int row = categoryDao.add(category);
        
        if (row > 0) {
            response.sendRedirect("categoryServlet?action=list");
        } else {
            request.setAttribute("msg", "添加失败！");
            listCategories(request, response);
        }
    }
    
    /**
     * 更新分类
     */
    private void updateCategory(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String idStr = request.getParameter("id");
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        
        Category category = new Category(name, description);
        category.setId(Integer.parseInt(idStr));
        
        int row = categoryDao.update(category);
        
        if (row > 0) {
            response.sendRedirect("categoryServlet?action=list");
        } else {
            request.setAttribute("msg", "更新失败！");
            listCategories(request, response);
        }
    }
    
    /**
     * 删除分类
     */
    private void deleteCategory(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String idStr = request.getParameter("id");
        
        if (idStr != null) {
            int categoryId = Integer.parseInt(idStr);
            // 检查分类下是否有图书
            int bookCount = categoryDao.getBookCountByCategory(categoryId);
            if (bookCount > 0) {
                request.setAttribute("msg", "该分类下还有图书，无法删除！");
                listCategories(request, response);
                return;
            }
            
            categoryDao.delete(categoryId);
        }
        
        response.sendRedirect("categoryServlet?action=list");
    }
}