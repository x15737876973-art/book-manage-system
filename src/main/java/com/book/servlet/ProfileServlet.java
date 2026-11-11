package com.book.servlet;

import com.book.dao.UserDao;
import com.book.entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 读者个人信息管理Servlet
 */
@WebServlet("/reader/profileServlet")
public class ProfileServlet extends HttpServlet {
    private UserDao userDao = new UserDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        String action = request.getParameter("action");
        
        if ("view".equals(action)) {
            viewProfile(request, response);
        } else if ("updatePwd".equals(action)) {
            updatePassword(request, response);
        } else {
            viewProfile(request, response);
        }
    }
    
    /**
     * 查看个人信息
     */
    private void viewProfile(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        
        if (user == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        // 重新获取最新用户信息
        User latestUser = userDao.findById(user.getId());
        request.setAttribute("user", latestUser);
        request.getRequestDispatcher("/reader/profile.jsp").forward(request, response);
    }
    
    /**
     * 修改密码
     */
    private void updatePassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        
        if (user == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // 验证原密码
        if (!user.getPassword().equals(oldPassword)) {
            request.setAttribute("msg", "原密码错误！");
            viewProfile(request, response);
            return;
        }
        
        // 验证新密码
        if (newPassword == null || newPassword.trim().isEmpty()) {
            request.setAttribute("msg", "新密码不能为空！");
            viewProfile(request, response);
            return;
        }
        
        // 验证确认密码
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("msg", "两次输入的密码不一致！");
            viewProfile(request, response);
            return;
        }
        
        // 更新密码
        int row = userDao.updatePassword(user.getId(), newPassword);
        if (row > 0) {
            // 更新session中的用户信息
            user.setPassword(newPassword);
            session.setAttribute("loginUser", user);
            request.setAttribute("msg", "密码修改成功！");
        } else {
            request.setAttribute("msg", "密码修改失败！");
        }
        
        viewProfile(request, response);
    }
}