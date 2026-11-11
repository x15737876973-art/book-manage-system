package com.book.servlet;

import com.book.dao.UserDao;
import com.book.entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 注册Servlet
 */
@WebServlet("/registerServlet")
public class RegisterServlet extends HttpServlet {
    private UserDao userDao = new UserDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String password2 = request.getParameter("password2");
        String realName = request.getParameter("realName");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        
        // 验证用户名
        if (username == null || username.trim().isEmpty()) {
            request.setAttribute("msg", "用户名不能为空！");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }
        
        // 检查用户名是否已存在
        if (userDao.findByUsername(username) != null) {
            request.setAttribute("msg", "用户名已存在！");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }
        
        // 验证密码
        if (password == null || password.trim().isEmpty()) {
            request.setAttribute("msg", "密码不能为空！");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }
        
        // 验证密码长度
        if (password.length() < 6) {
            request.setAttribute("msg", "密码长度必须至少6位！");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }
        
        // 验证确认密码
        if (password2 == null || password2.trim().isEmpty()) {
            request.setAttribute("msg", "请输入确认密码！");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }
        
        // 验证两次密码是否一致
        if (!password.equals(password2)) {
            request.setAttribute("msg", "两次输入的密码不一致！");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("reader"); // 注册默认为读者
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus("active");
        
        int row = userDao.add(user);
        if (row > 0) {
            request.setAttribute("msg", "注册成功，请登录！");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        } else {
            request.setAttribute("msg", "注册失败！");
            request.getRequestDispatcher("register.jsp").forward(request, response);
        }
    }
}