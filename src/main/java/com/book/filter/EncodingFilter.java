package com.book.filter;

import javax.servlet.*;
import java.io.IOException;

/**
 * 字符编码过滤器
 * 确保所有请求和响应使用 UTF-8 编码
 */
public class EncodingFilter implements Filter {
    
    private String encoding = "UTF-8";
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String encodingParam = filterConfig.getInitParameter("encoding");
        if (encodingParam != null && !encodingParam.isEmpty()) {
            this.encoding = encodingParam;
        }
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // 设置请求编码
        request.setCharacterEncoding(encoding);
        
        // 设置响应编码
        response.setCharacterEncoding(encoding);
        response.setContentType("text/html;charset=" + encoding);
        
        // 继续执行过滤器链
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        // 清理资源
    }
}