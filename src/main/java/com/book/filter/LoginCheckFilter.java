package com.book.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 登录检查过滤器
 * 确保用户已登录才能访问管理员和读者页面
 */
public class LoginCheckFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        // 获取请求路径
        String requestURI = httpRequest.getRequestURI();
        
        // 检查是否是管理员或读者页面
        boolean isAdminPage = requestURI.contains("/admin/");
        boolean isReaderPage = requestURI.contains("/reader/");
        
        // 如果是管理员或读者页面，检查登录状态
        if (isAdminPage || isReaderPage) {
            // 排除登录页面和初始化页面
            if (!requestURI.contains("login.jsp") && 
                !requestURI.contains("initSystem") &&
                !requestURI.contains("test.jsp")) {
                
                // 检查是否已登录
                if (session == null || session.getAttribute("loginUser") == null) {
                    // 未登录，重定向到登录页面
                    httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
                    return;
                }
                
                // 检查角色权限
                String role = (String) session.getAttribute("loginRole");
                if (isAdminPage && !"admin".equals(role)) {
                    // 不是管理员，拒绝访问
                    httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?msg=no_permission");
                    return;
                }
                if (isReaderPage && !"reader".equals(role)) {
                    // 不是读者，拒绝访问
                    httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?msg=no_permission");
                    return;
                }
            }
        }
        
        // 已登录或访问公开页面，继续执行
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        // 清理资源
    }
}