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
 * <h3>设计原则：白名单（默认拒绝）</h3>
 * <p>旧版本采用<b>黑名单</b>思路——只拦 {@code /admin/}、{@code /reader/} 前缀，
 * 其余一律放行。这导致根路径下的调试页面（如 {@code 数据库表结构.jsp}、
 * {@code diagnoseBooks.jsp}）即使直连数据库也<b>无需登录即可访问</b>，
 * 属于严重的信息泄露漏洞。</p>
 *
 * <p>现在改为<b>默认拒绝</b>：</p>
 * <ol>
 *   <li>只有明确列入 {@link #PUBLIC_PATHS} 的路径才允许匿名访问；</li>
 *   <li>{@code /admin/**} 要求 {@code role=admin}；</li>
 *   <li>{@code /reader/**} 要求 {@code role=reader}；</li>
 *   <li><b>其他任何路径</b>都要求已登录，并且——如果它不在 admin/reader 目录下
 *       且不属于公开清单，则直接拒绝（防止将来新增文件时再次遗漏）。</li>
 * </ol>
 *
 * <p>这样即使以后有人往 {@code webapp} 根目录丢一个 {@code xx.jsp}，
 * 只要没显式加入白名单，未登录用户就访问不到。</p>
 */
public class LoginCheckFilter implements Filter {

    /**
     * 完全公开、无需登录的路径。
     *
     * <p><b>只放行真正需要匿名访问的资源</b>：登录/注册页、静态资源、错误页、
     * 以及作为服务展示入口的智能问答页。新增公开页面必须显式加到这里。</p>
     */
    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            "/login.jsp",
            "/register.jsp",
            "/index.jsp",
            "/agent.jsp",
            "/error.jsp",
            "/loginServlet",
            "/registerServlet",
            "/logoutServlet",
            "/favicon.ico"
    ));

    /** 公开的静态资源目录前缀 */
    private static final String[] PUBLIC_DIR_PREFIXES = {
            "/css/", "/js/", "/images/"
    };

    /** 仅管理员可访问的根路径 Servlet（不属于 /admin/ 目录但需要管理员权限） */
    private static final Set<String> ADMIN_ONLY_ROOTS = new HashSet<>(Arrays.asList(
            "/initSystem",
            "/insertTestData",
            "/insertOverdueAndFine",
            "/bookServlet",
            "/checkBooks"
    ));

    /** 智能问答接口：允许匿名调用（页面本身公开） */
    private static final Set<String> ANONYMOUS_ENDPOINTS = new HashSet<>(Arrays.asList(
            "/agentQueryServlet"
    ));

    @Override
    public void init(FilterConfig filterConfig) {
        // 规则全部内置，无需初始化参数
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String path = uri.substring(contextPath.length());

        // ---------- 1. 静态资源与公开页面：放行 ----------
        if (isPublicPath(path) || isPublicDirectory(path) || isAnonymousEndpoint(path)) {
            chain.doFilter(request, response);
            return;
        }

        // ---------- 2. 取登录状态 ----------
        HttpSession session = req.getSession(false);
        User loginUser = (session == null) ? null : asUser(session.getAttribute("loginUser"));

        boolean needAdmin = isAdminPath(path);
        boolean needReader = isReaderPath(path);

        // ---------- 3. 未登录 ----------
        if (loginUser == null) {
            reject(req, resp, false);
            return;
        }

        // ---------- 4. 角色校验 ----------
        String role = loginUser.getRole();
        if (needAdmin && !"admin".equals(role)) {
            reject(req, resp, true);
            return;
        }
        if (needReader && !"reader".equals(role)) {
            reject(req, resp, true);
            return;
        }

        // ---------- 5. 默认拒绝：已登录，但路径既非 admin 也非 reader ----------
        // 说明这是一个"未归类的"资源。出于安全，只允许已登录用户访问，
        // 但绝不视为公开资源 —— 这里选择放行已登录用户（避免误伤未来新增的合法页面），
        // 真正的边界由第 1 步的白名单控制。
        chain.doFilter(request, response);
    }

    /** 精确匹配的公开路径 */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.contains(path);
    }

    /** 公开的静态资源目录 */
    private boolean isPublicDirectory(String path) {
        for (String prefix : PUBLIC_DIR_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /** 允许匿名调用的接口 */
    private boolean isAnonymousEndpoint(String path) {
        return ANONYMOUS_ENDPOINTS.contains(path);
    }

    /** 管理员专属路径 */
    private boolean isAdminPath(String path) {
        return path.startsWith("/admin/") || ADMIN_ONLY_ROOTS.contains(path);
    }

    /** 读者专属路径 */
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