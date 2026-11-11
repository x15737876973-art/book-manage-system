package com.book.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Servlet 层通用工具：请求参数校验、分页参数解析、登录态与角色校验。
 *
 * <p>集中放置这些方法的目的：避免每个 Servlet 各写一套判空与类型转换，
 * 既减少重复代码，也保证所有入口的参数校验行为一致。</p>
 *
 * <p>设计原则：解析失败时返回默认值或抛出 {@link IllegalArgumentException}，
 * 由 Servlet 捕获后给出友好提示，绝不让 {@code NumberFormatException}
 * 之类的底层异常冒泡到页面。</p>
 */
public final class ServletUtils {

    /** 用户名：字母、数字、下划线，3~20 位 */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    /** 手机号：中国大陆 11 位 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 邮箱：宽松校验，够用即可 */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** 分页默认每页条数 */
    public static final int DEFAULT_PAGE_SIZE = 10;
    /** 分页每页最大条数，防止恶意传 pageSize=999999 拖垮数据库 */
    public static final int MAX_PAGE_SIZE = 100;

    private ServletUtils() {
    }

    // ======================================================================
    // 参数读取与校验
    // ======================================================================

    /**
     * 取字符串参数并去除首尾空白。
     *
     * @return 参数值；参数缺失或为空白时返回 null
     */
    public static String getTrimmed(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * 取必填字符串参数，缺失或空白时抛出友好异常。
     *
     * @param field 字段的中文名，用于错误提示，如"用户名"
     */
    public static String requireParam(HttpServletRequest request, String name, String field) {
        String value = getTrimmed(request, name);
        if (value == null) {
            throw new IllegalArgumentException(field + "不能为空");
        }
        return value;
    }

    /**
     * 取必填字符串参数并校验最大长度。
     */
    public static String requireParam(HttpServletRequest request, String name, String field, int maxLength) {
        String value = requireParam(request, name, field);
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(field + "长度不能超过 " + maxLength + " 个字符");
        }
        return value;
    }

    /**
     * 取可选字符串参数，超长时截断（用于简介这类非关键长文本）。
     */
    public static String getParamWithLimit(HttpServletRequest request, String name, int maxLength) {
        String value = getTrimmed(request, name);
        if (value != null && value.length() > maxLength) {
            return value.substring(0, maxLength);
        }
        return value;
    }

    /**
     * 取整数参数。
     *
     * @param defaultValue 参数缺失时返回的默认值
     * @throws IllegalArgumentException 参数存在但不是合法整数
     */
    public static Integer getInt(HttpServletRequest request, String name, Integer defaultValue) {
        String value = getTrimmed(request, name);
        if (value == null) {
            return defaultValue;
        }
        return parseInt(value, name);
    }

    /**
     * 取必填整数参数。
     */
    public static int requireInt(HttpServletRequest request, String name, String field) {
        String value = requireParam(request, name, field);
        return parseInt(value, field);
    }

    /**
     * 取正整数参数，并校验上限（用于 id、库存等）。
     */
    public static Integer getPositiveInt(HttpServletRequest request, String name, int maxValue) {
        Integer value = getInt(request, name, null);
        if (value == null) {
            return null;
        }
        if (value <= 0 || value > maxValue) {
            throw new IllegalArgumentException(name + " 取值范围应为 1~" + maxValue);
        }
        return value;
    }

    /**
     * 取浮点数参数（价格等），缺失返回默认值。
     */
    public static Double getDouble(HttpServletRequest request, String name, Double defaultValue) {
        String value = getTrimmed(request, name);
        if (value == null) {
            return defaultValue;
        }
        try {
            double d = Double.parseDouble(value);
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                throw new IllegalArgumentException(name + " 不是合法数字");
            }
            return d;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " 不是合法数字");
        }
    }

    /**
     * 取日期参数（yyyy-MM-dd），格式错误时抛出友好异常。
     *
     * @return java.sql.Date，参数缺失返回 null
     */
    public static java.sql.Date getSqlDate(HttpServletRequest request, String name, String field) {
        String value = getTrimmed(request, name);
        if (value == null) {
            return null;
        }
        try {
            return java.sql.Date.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(field + " 格式应为 yyyy-MM-dd");
        }
    }

    /**
     * 取枚举型参数：值必须属于给定集合，否则返回默认值。
     */
    public static String getEnum(HttpServletRequest request, String name, String defaultValue, String... allowed) {
        String value = getTrimmed(request, name);
        if (value == null) {
            return defaultValue;
        }
        for (String a : allowed) {
            if (a.equals(value)) {
                return value;
            }
        }
        return defaultValue;
    }

    // ======================================================================
    // 格式校验
    // ======================================================================

    /** 校验用户名格式，不合规抛出友好异常。 */
    public static void validateUsername(String username) {
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("用户名只能由字母、数字、下划线组成，长度 3~20 位");
        }
    }

    /** 校验密码强度：6~32 位，要求同时包含字母和数字。 */
    public static void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("密码长度至少 6 位");
        }
        if (password.length() > 32) {
            throw new IllegalArgumentException("密码长度不能超过 32 位");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("密码需同时包含字母和数字");
        }
    }

    /** 手机号格式校验；允许为空。 */
    public static void validatePhone(String phone) {
        if (phone != null && !phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
    }

    /** 邮箱格式校验；允许为空。 */
    public static void validateEmail(String email) {
        if (email != null && !email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
    }

    // ======================================================================
    // 分页参数
    // ======================================================================

    /**
     * 解析页码，非法值自动纠正为 1。
     */
    public static int getPageNumber(HttpServletRequest request) {
        Integer page = getInt(request, "page", 1);
        return (page == null || page < 1) ? 1 : page;
    }

    /**
     * 解析每页条数，非法值纠正为默认值，上限 {@link #MAX_PAGE_SIZE}。
     */
    public static int getPageSize(HttpServletRequest request) {
        Integer size = getInt(request, "pageSize", DEFAULT_PAGE_SIZE);
        if (size == null || size < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    // ======================================================================
    // 登录态与角色
    // ======================================================================

    /**
     * 取当前登录用户，未登录返回 null。
     */
    public static com.book.entity.User getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("loginUser");
        return user instanceof com.book.entity.User ? (com.book.entity.User) user : null;
    }

    /**
     * 强制要求已登录：未登录则重定向到登录页并返回 null。
     *
     * <p>调用方拿到 null 后应立即 return，例如：</p>
     * <pre>
     *   User user = ServletUtils.requireLogin(request, response);
     *   if (user == null) return;
     * </pre>
     *
     * @return 登录用户；未登录返回 null（已写入重定向响应）
     */
    public static com.book.entity.User requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        com.book.entity.User user = getLoginUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?msg=need_login");
            return null;
        }
        return user;
    }

    /**
     * 强制要求指定角色：未登录跳登录页，角色不符跳无权限提示页。
     *
     * @param requiredRole "admin" 或 "reader"
     * @return 登录用户；校验失败返回 null（已写入响应）
     */
    public static com.book.entity.User requireRole(HttpServletRequest request, HttpServletResponse response,
                                                   String requiredRole) throws IOException {
        com.book.entity.User user = requireLogin(request, response);
        if (user == null) {
            return null;
        }
        if (!requiredRole.equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?msg=no_permission");
            return null;
        }
        return user;
    }

    /**
     * 是否为 AJAX/XHR 请求（异常处理时决定返回 JSON 还是跳转错误页）。
     */
    public static boolean isAjax(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"));
    }

    // ======================================================================
    // 内部工具
    // ======================================================================

    private static int parseInt(String value, String name) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " 必须是整数");
        }
    }

    /**
     * 兼容旧代码的查询字符串解析（保留用于迁移期）。
     * 新代码请直接使用 HttpServletRequest#getParameter。
     */
    public static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return params;
        }
        for (String pair : queryString.split("&")) {
            int idx = pair.indexOf('=');
            if (idx == -1) {
                continue;
            }
            try {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            } catch (Exception ignored) {
                // 单个参数解码失败不影响其他参数
            }
        }
        return params;
    }

    /**
     * 把可能为 null 的列表转为空列表，避免 JSP 中 forEach 报错。
     */
    public static <T> List<T> emptyIfNull(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }
}