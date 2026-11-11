package com.book.servlet;

import com.book.dao.UserDao;
import com.book.entity.User;
import com.book.util.AppException;
import com.book.util.BCryptUtil;
import com.book.util.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 登录 Servlet。
 *
 * <h3>安全改进</h3>
 * <ul>
 *   <li><b>参数校验</b>：用户名、密码、身份判空与长度校验，非法输入给出友好提示；</li>
 *   <li><b>密码校验</b>：不再用 SQL 比对明文，改为
 *       {@code UserDao.findByUsernameForLogin} 取用户 + {@link BCryptUtil#checkpw} 校验密文；</li>
 *   <li><b>旧密码自动升级</b>：若数据库里仍是历史明文密码，登录成功后<b>立即</b>
 *       把该用户密码改写成 BCrypt 密文，实现无感迁移；</li>
 *   <li><b>会话固定防护</b>：登录成功后使旧 session 失效并新建，防止 session fixation 攻击。</li>
 * </ul>
 */
@WebServlet("/loginServlet")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 已经在登录页了，GET 直接展示表单即可
        String msg = request.getParameter("msg");
        if ("need_login".equals(msg)) {
            request.setAttribute("msg", "请先登录后再访问该页面");
        } else if ("no_permission".equals(msg)) {
            request.setAttribute("msg", "您没有访问该页面的权限，请使用对应身份登录");
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        try {
            // ---------- 1. 参数校验 ----------
            String username = ServletUtils.requireParam(request, "username", "用户名", 20);
            String password = ServletUtils.requireParam(request, "password", "密码", 32);
            String role = ServletUtils.requireParam(request, "role", "登录身份");

            if (!"admin".equals(role) && !"reader".equals(role)) {
                throw new AppException("登录身份不正确");
            }

            // ---------- 2. 密码校验（BCrypt） ----------
            User user = userDao.findByUsernameForLogin(username);
            if (user == null || !BCryptUtil.checkpw(password, user.getPassword())) {
                // 统一提示，不区分"用户不存在"和"密码错误"，防止账号枚举
                fail(request, response, "用户名或密码错误！");
                return;
            }

            // ---------- 3. 角色与状态校验 ----------
            if (!role.equals(user.getRole())) {
                fail(request, response,
                        "身份选择错误！您是" + ("admin".equals(user.getRole()) ? "管理员" : "读者"));
                return;
            }
            if (!"active".equals(user.getStatus())) {
                fail(request, response, "账号已被禁用，请联系管理员！");
                return;
            }

            // ---------- 4. 历史明文密码自动升级为 BCrypt ----------
            upgradeLegacyPasswordIfNeeded(user, password);

            // ---------- 5. 重建 session（防会话固定） ----------
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            user.setPassword(null); // 不把密码（即使是密文）放进会话
            session.setAttribute("loginUser", user);
            session.setAttribute("loginRole", user.getRole());
            session.setMaxInactiveInterval(30 * 60);

            // ---------- 6. 按角色跳转 ----------
            if ("admin".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/admin/indexServlet");
            } else {
                response.sendRedirect(request.getContextPath() + "/reader/indexServlet");
            }

        } catch (AppException | IllegalArgumentException e) {
            fail(request, response, e.getMessage());
        } catch (Exception e) {
            // 未预期异常不把堆栈给用户看
            getServletContext().log("登录处理失败", e);
            fail(request, response, "登录失败，请稍后重试");
        }
    }

    /**
     * 若数据库中的密码不是 BCrypt 密文（历史明文），登录成功后立即升级。
     *
     * <p>升级失败不影响本次登录，只记录日志。</p>
     */
    private void upgradeLegacyPasswordIfNeeded(User user, String plainPassword) {
        if (BCryptUtil.isBcryptHash(user.getPassword())) {
            return;
        }
        try {
            String newHash = BCryptUtil.hashpw(plainPassword);
            userDao.updatePassword(user.getId(), newHash);
            getServletContext().log("已为用户 " + user.getUsername() + " 自动升级密码为 BCrypt 密文");
        } catch (Exception e) {
            getServletContext().log("自动升级密码失败（不影响本次登录）：" + user.getUsername(), e);
        }
    }

    /** 回到登录页并展示提示 */
    private void fail(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("msg", message);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
}