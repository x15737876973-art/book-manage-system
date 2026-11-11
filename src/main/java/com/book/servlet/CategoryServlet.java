package com.book.servlet;

import com.book.dao.CategoryDao;
import com.book.entity.Category;
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
 * 分类管理 Servlet（管理员）。
 *
 * <p>参数校验：分类名必填、长度 ≤ 50、描述 ≤ 200；重名校验；删除前检查是否被引用。</p>
 */
@WebServlet("/admin/categoryServlet")
public class CategoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int MAX_NAME = 50;
    private static final int MAX_DESCRIPTION = 200;

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

        User loginUser = ServletUtils.requireRole(request, response, "admin");
        if (loginUser == null) {
            return;
        }

        String action = ServletUtils.getTrimmed(request, "action");
        try {
            if ("add".equals(action)) {
                addCategory(request, response);
            } else if ("update".equals(action)) {
                updateCategory(request, response);
            } else if ("delete".equals(action)) {
                deleteCategory(request, response);
            } else {
                listCategories(request, response);
            }
        } catch (AppException | IllegalArgumentException e) {
            request.setAttribute("msg", e.getMessage());
            request.setAttribute("msgType", "error");
            listCategories(request, response);
        } catch (Exception e) {
            getServletContext().log("分类操作失败", e);
            request.setAttribute("error", "分类操作失败，请稍后重试");
            request.getRequestDispatcher("/admin/error.jsp").forward(request, response);
        }
    }

    private void listCategories(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("categoryList", categoryDao.findAll());
        request.getRequestDispatcher("/admin/categoryList.jsp").forward(request, response);
    }

    private void addCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = ServletUtils.requireParam(request, "name", "分类名称", MAX_NAME);
        String description = ServletUtils.getParamWithLimit(request, "description", MAX_DESCRIPTION);

        if (categoryDao.findByName(name) != null) {
            throw new AppException("分类名称【" + name + "】已存在");
        }
        if (categoryDao.add(new Category(name, description)) <= 0) {
            throw new AppException("添加失败，请稍后重试");
        }
        response.sendRedirect(request.getContextPath() + "/admin/categoryServlet?action=list");
    }

    private void updateCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = ServletUtils.requireInt(request, "id", "分类ID");
        String name = ServletUtils.requireParam(request, "name", "分类名称", MAX_NAME);
        String description = ServletUtils.getParamWithLimit(request, "description", MAX_DESCRIPTION);

        Category existing = categoryDao.findById(id);
        if (existing == null) {
            throw new AppException("分类不存在或已被删除");
        }
        Category sameName = categoryDao.findByName(name);
        if (sameName != null && !sameName.getId().equals(id)) {
            throw new AppException("分类名称【" + name + "】已被其他分类使用");
        }

        Category category = new Category(name, description);
        category.setId(id);
        if (categoryDao.update(category) <= 0) {
            throw new AppException("更新失败，请稍后重试");
        }
        response.sendRedirect(request.getContextPath() + "/admin/categoryServlet?action=list");
    }

    private void deleteCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = ServletUtils.requireInt(request, "id", "分类ID");

        if (categoryDao.findById(id) == null) {
            throw new AppException("分类不存在或已被删除");
        }
        int bookCount = categoryDao.getBookCountByCategory(id);
        if (bookCount > 0) {
            throw new AppException("该分类下还有 " + bookCount + " 本图书，无法删除");
        }
        categoryDao.delete(id);
        response.sendRedirect(request.getContextPath() + "/admin/categoryServlet?action=list");
    }
}