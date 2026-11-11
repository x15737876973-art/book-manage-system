package com.book.servlet;

import com.book.dao.UserDao;
import com.book.entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理Servlet
 */
@WebServlet("/admin/userServlet")
public class UserServlet extends HttpServlet {
    private UserDao userDao = new UserDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        String action = request.getParameter("action");
        
        if ("list".equals(action)) {
            listUsers(request, response);
        } else if ("add".equals(action)) {
            addUser(request, response);
        } else if ("toAdd".equals(action)) {
            toAddUser(request, response);
        } else if ("update".equals(action)) {
            updateUser(request, response);
        } else if ("delete".equals(action)) {
            deleteUser(request, response);
        } else if ("resetPwd".equals(action)) {
            resetPassword(request, response);
        } else if ("toggleStatus".equals(action)) {
            toggleStatus(request, response);
        } else if ("toEdit".equals(action)) {
            toEditUser(request, response);
        } else {
            listUsers(request, response);
        }
    }
    
    /**
     * 查询用户列表
     */
    private void listUsers(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String role = request.getParameter("role");
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        List<User> users;
        
        // 判断是否有有效的查询条件
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasRole = role != null && !role.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty();
        boolean hasSearchCondition = hasKeyword || hasRole || hasStatus;
        
        // 先获取基础用户列表
        if (hasRole) {
            // 如果选择了身份筛选，先按身份获取
            if ("reader".equals(role)) {
                users = userDao.findReaders();
            } else if ("admin".equals(role)) {
                users = userDao.findAdmins();
            } else {
                users = userDao.findAll();
            }
        } else {
            // 如果没有选择身份，获取所有用户
            users = userDao.findAll();
        }
        
        // 如果有查询条件，进行过滤
        if (hasSearchCondition && users != null) {
            List<User> filteredUsers = new ArrayList<>();
            
            for (User user : users) {
                boolean match = true;
                
                // 按关键词过滤（只搜索用户名）
                if (hasKeyword) {
                    String keywordLower = keyword.trim().toLowerCase();
                    String username = user.getUsername() != null ? user.getUsername().toLowerCase() : "";
                    
                    // 不区分大小写的模糊匹配
                    boolean keywordMatch = username.contains(keywordLower);
                    if (!keywordMatch) {
                        match = false;
                    }
                }
                
                // 按状态过滤
                if (hasStatus) {
                    String userStatus = user.getStatus() != null ? user.getStatus() : "active";
                    if (!status.equals(userStatus)) {
                        match = false;
                    }
                }
                
                if (match) {
                    filteredUsers.add(user);
                }
            }
            
            users = filteredUsers;
            
            // 只有在有查询条件时才设置提示消息
            if (!users.isEmpty()) {
                request.setAttribute("msg", "查询成功，共找到 " + users.size() + " 条记录");
                request.setAttribute("msgType", "success");
            } else {
                request.setAttribute("msg", "未找到符合条件的记录，请检查搜索条件");
                request.setAttribute("msgType", "error");
            }
        }
        
        request.setAttribute("userList", users);
        request.setAttribute("role", role);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.getRequestDispatcher("/admin/userList.jsp").forward(request, response);
    }
    
    /**
     * 添加用户
     */
    private void addUser(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        String realName = request.getParameter("realName");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        
        // 检查用户名是否已存在
        if (userDao.findByUsername(username) != null) {
            request.setAttribute("msg", "用户名已存在！");
            listUsers(request, response);
            return;
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(password != null && !password.trim().isEmpty() ? password : "123");
        user.setRole(role);
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus("active");
        
        int row = userDao.add(user);
        if (row > 0) {
            response.sendRedirect("userServlet?action=list&role=" + role);
        } else {
            request.setAttribute("msg", "添加失败！");
            listUsers(request, response);
        }
    }
    
    /**
     * 更新用户信息
     */
    private void updateUser(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String idStr = request.getParameter("id");
        String username = request.getParameter("username");
        String role = request.getParameter("role");
        String realName = request.getParameter("realName");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        
        // 获取原用户信息，保持status不变
        User originalUser = userDao.findById(Integer.parseInt(idStr));
        
        User user = new User();
        user.setId(Integer.parseInt(idStr));
        user.setUsername(username);
        user.setRole(role);
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus(originalUser.getStatus()); // 保持原状态
        
        int row = userDao.update(user);
        if (row > 0) {
            response.sendRedirect("userServlet?action=list&role=" + role);
        } else {
            request.setAttribute("msg", "更新失败！");
            listUsers(request, response);
        }
    }
    
    /**
     * 删除用户
     */
    private void deleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String idStr = request.getParameter("id");
        String role = request.getParameter("role");
        
        if (idStr != null) {
            int row = userDao.delete(Integer.parseInt(idStr));
            if (row > 0) {
                request.setAttribute("msg", "用户已删除");
                request.setAttribute("msgType", "success");
            } else {
                request.setAttribute("msg", "删除失败");
                request.setAttribute("msgType", "error");
            }
        }
        
        // 保持查询条件
        request.setAttribute("role", role);
        listUsers(request, response);
    }
    
    /**
     * 重置密码
     */
    private void resetPassword(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String idStr = request.getParameter("id");
        String role = request.getParameter("role");
        
        if (idStr != null) {
            int row = userDao.resetPassword(Integer.parseInt(idStr));
            if (row > 0) {
                request.setAttribute("msg", "密码已重置为123456");
                request.setAttribute("msgType", "success");
            } else {
                request.setAttribute("msg", "重置密码失败");
                request.setAttribute("msgType", "error");
            }
        }
        
        // 保持查询条件
        request.setAttribute("role", role);
        listUsers(request, response);
    }
    
    /**
     * 切换用户状态（启用/禁用）
     */
    private void toggleStatus(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String idStr = request.getParameter("id");
        String status = request.getParameter("status");
        String role = request.getParameter("role");
        
        if (idStr != null && status != null) {
            String newStatus = "active".equals(status) ? "disabled" : "active";
            int row = userDao.updateStatus(Integer.parseInt(idStr), newStatus);
            if (row > 0) {
                String statusText = "active".equals(newStatus) ? "已启用" : "已禁用";
                request.setAttribute("msg", "用户状态已更新：" + statusText);
                request.setAttribute("msgType", "success");
            } else {
                request.setAttribute("msg", "状态更新失败");
                request.setAttribute("msgType", "error");
            }
        }
        
        // 保持查询条件
        request.setAttribute("role", role);
        listUsers(request, response);
    }
    
    /**
     * 跳转到编辑页面
     */
    private void toEditUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String role = request.getParameter("role");
        
        if (idStr != null) {
            User user = userDao.findById(Integer.parseInt(idStr));
            request.setAttribute("user", user);
        }
        
        request.setAttribute("role", role);
        request.getRequestDispatcher("/admin/userEdit.jsp").forward(request, response);
    }
    
    /**
     * 跳转到新增页面
     */
    private void toAddUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String role = request.getParameter("role");
        request.setAttribute("role", role);
        request.getRequestDispatcher("/admin/userAdd.jsp").forward(request, response);
    }
}