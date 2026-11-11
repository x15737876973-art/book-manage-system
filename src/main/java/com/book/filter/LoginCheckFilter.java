package com.book.filter;

import com.book.entity.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 统一登录与角色校验过滤器。
 *
 * <h3>修复的越权问题</h3>
 * <p>旧实现只按 URL 里是否含 {@code /admin/}、{@code /reader/} 做判断，
 * 而 {@code /initSystem}、{@code /insertTestData}、{@code /bookServlet}、
 * {@code /logoutServlet} 这些<b>根路径下的敏感接口完全没有被保护</b>，
 * 未登录用户可以直接调用，属于典型的越权访问漏洞。</p>
 *
 * <h3>新的判定规则</h3>
 * <ol>
 *   <li><b>公开路径</b>（登录、注册、静态资源、错误页、智能问答页）直接放行；</li>
 *   <li><b>管理员路径</b>：{@code /admin/**} 及初始化/测试数据类接口，
 *       必须已登录且 {@code role=admin}；</li>
 *   <li><b>读者路径</b>：{@code /reader/**}，必须已登录且 {@code role=reader}；</li>
 *   <li>其余路径默认放行（首页、图书列表等公开页面）。</li>
 * </ol>
 *
 * <p>未登录跳转 {@code login.jsp?msg=need_login}；角色不符跳转
 * {@code login.jsp?msg=no_permission}。AJAX 请求返回 401/403 状态码，
 * 便于前端处理而不跳页面。</p>
 */
public class LoginCheckFilter implements Filter {

    /** 完全公开、无需登录即可访问的路径前缀 */
    private static final Set<String> PUBLIC_PREFIXES = new HashSet<>(Arrays.asList(
            "/login.jsp",
            "/register.jsp",
            "/index.jsp",
            "/agent.jsp",
            "/loginServlet",
            "/registerServlet",
            "/logoutServlet",
            "/error.jsp",
            "/css/",
            "/js/",
            "/images/",
            "/favicon.ico",
            "/quickAccess"
    ));

    /** 仅管理员可访问的根路径接口（旧实现遗漏的部分） */
    private static final Set<String> ADMIN_ONLY_ROOTS = new HashSet<>(Arrays.asList(
            "/initSystem",
            "/insertTestData",
            "/insertOverdueAndFine",
            "/bookServlet",
            "/checkBooks"
    ));

    @Override
    public void init(FilterConfig filterConfig) {
        // 无需初始化参数，规则全部内置
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String path = uri.substring(contextPath.length());

        // 1. 公开路径直接放行
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User loginUser = (session == null) ? null : asUser(session.getAttribute("loginUser"));

        boolean needAdmin = isAdminPath(path);
        boolean needReader = isReaderPath(path);

        if (!needAdmin && !needReader) {
            // 其余公开页面（系统首页等）
            chain.doFilter(request, response);
            return;
        }

        // 2. 未登录
        if (loginUser == null) {
            reject(req, resp, false);
            return;
        }

        // 3. 角色校验
        String role = loginUser.getRole();
        if (needAdmin && !"admin".equals(role)) {
            reject(req, resp, true);
            return;
        }
        if (needReader && !"reader".equals(role)) {
            reject(req, resp, true);
            return;
        }

        chain.doFilter(request, response);
    }

    /** 是否为公开路径 */
    private boolean isPublicPath(String path) {
        for (String p : PUBLIC_PREFIXES) {
            if (path.equals(p) || path.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    /** 是否为管理员专属路径 */
    private boolean isAdminPath(String path) {
        if (path.startsWith("/admin/")) {
            return true;
        }
        return ADMIN_ONLY_ROOTS.contains(path);
    }

    /** 是否为读者专属路径（读取类页面允许管理员查看，写操作在 Servlet 内再校验） */
    private boolean isReaderPath(String path) {
        return path.startsWith("/reader/");
    }

    /** 拒绝访问：AJAX 返回状态码，普通请求跳转登录页 */
    private void reject(HttpServletRequest req, HttpServletResponse resp, boolean permissionDenied)
            throws IOException {
        if (isAjax(req)) {
            resp.setStatus(permissionDenied
                    ? HttpServletResponse.SC_FORBIDDEN
                    : HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(permissionDenied
                    ? "{\"success\":false,\"message\":\"无访问权限\"}"
                    : "{\"success\":false,\"message\":\"请先登录\"}");
            return;
        }
        String target = permissionDenied ? "/login.jsp?msg=no_permission" : "/login.jsp?msg=need_login";
        resp.sendRedirect(req.getContextPath() + target);
    }

    private boolean isAjax(HttpServletRequest req) {
        String xrw = req.getHeader("X-Requested-With");
        String accept = req.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(xrw)
                || (accept != null && accept.contains("application/json"));
    }

    private User asUser(Object obj) {
        return obj instanceof User ? (User) obj : null;
    }

    @Override
    public void destroy() {
        // 无资源需要释放
    }
}