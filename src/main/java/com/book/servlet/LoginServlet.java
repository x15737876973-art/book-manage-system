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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录Servlet
 */
@WebServlet("/loginServlet")
public class LoginServlet extends HttpServlet {
    private UserDao userDao = new UserDao();
    
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

                String username = params.get("username");
                String password = params.get("password");
                String role = params.get("role");

                System.out.println("username参数: " + username);
                System.out.println("password参数: " + password);
                System.out.println("role参数: " + role);

                // 将参数重新设置到request中
                if (username != null) {
                    request.setAttribute("username", username);
                }
                if (password != null) {
                    request.setAttribute("password", password);
                }
                if (role != null) {
                    request.setAttribute("role", role);
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

            try {
                // 先尝试UTF-8解码
                key = URLDecoder.decode(key, StandardCharsets.UTF_8);
                value = URLDecoder.decode(value, StandardCharsets.UTF_8);
            } catch (Exception e) {
                try {
                    key = URLDecoder.decode(key, "GBK");
                    value = URLDecoder.decode(value, "GBK");
                } catch (UnsupportedEncodingException ex) {
                    key = URLDecoder.decode(key, StandardCharsets.ISO_8859_1);
                    value = URLDecoder.decode(value, StandardCharsets.ISO_8859_1);
                }
            }
            params.put(key, value);
        }
        return params;
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        
        User user = userDao.login(username, password);
        
        if (user == null) {
            request.setAttribute("msg", "用户名或密码错误！");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }
        
        // 验证角色
        if (!user.getRole().equals(role)) {
            request.setAttribute("msg", "身份选择错误！您是" + (user.getRole().equals("admin") ? "管理员" : "读者"));
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }
        
        // 验证状态
        if (!"active".equals(user.getStatus())) {
            request.setAttribute("msg", "账号已被禁用，请联系管理员！");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }
        
        // 登录成功
        HttpSession session = request.getSession();
        session.setAttribute("loginUser", user);
        session.setAttribute("loginRole", user.getRole());
        
        // 根据角色跳转
        if ("admin".equals(role)) {
            response.sendRedirect("admin/indexServlet");
        } else {
            response.sendRedirect("reader/indexServlet");
        }
    }
}