package com.book.servlet;

import com.book.dao.UserDao;
import com.book.entity.User;
import com.book.util.AppException;
import com.book.util.BCryptUtil;
import com.book.util.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 个人中心基类：管理员与读者的"查看资料 / 修改密码"逻辑完全一致，抽到此处复用。
 *
 * <p>子类只需提供：</p>
 * <ul>
 *   <li>{@link #getRequiredRole()} —— 允许访问的角色；</li>
 *   <li>{@link #getProfilePage()} —— 对应的 JSP 页面路径。</li>
 * </ul>
 *
 * <h3>密码修改的安全要点</h3>
 * <ul>
 *   <li>校验原密码使用 {@link BCryptUtil#checkpw}，不再是明文 {@code equals}；</li>
 *   <li>新密码做强度校验，并以 BCrypt 密文写入数据库；</li>
 *   <li>修改成功后同步更新会话中的用户对象，避免同一会话内校验旧密码失败。</li>
 * </ul>
 */
public abstract class AbstractProfileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected final UserDao userDao = new UserDao();

    /** 允许访问本页的角色：admin 或 reader */
    protected abstract String getRequiredRole();

    /** 个人中心页面路径，如 /admin/profile.jsp */
    protected abstract String getProfilePage();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        User loginUser = ServletUtils.requireRole(request, response, getRequiredRole());
        if (loginUser == null) {
            return;
        }

        String action = ServletUtils.getTrimmed(request, "action");
        if ("updatePwd".equals(action)) {
            updatePassword(request, response, loginUser);
        } else {
            viewProfile(request, response, loginUser);
        }
    }

    /** 展示个人资料 */
    private void viewProfile(HttpServletRequest request, HttpServletResponse response, User loginUser)
            throws ServletException, IOException {
        User latest = userDao.findById(loginUser.getId());
        request.setAttribute("user", latest != null ? latest : loginUser);
        request.getRequestDispatcher(getProfilePage()).forward(request, response);
    }

    /** 修改密码 */
    private void updatePassword(HttpServletRequest request, HttpServletResponse response, User loginUser)
            throws ServletException, IOException {
        try {
            String oldPassword = ServletUtils.requireParam(request, "oldPassword", "原密码", 32);
            String newPassword = ServletUtils.requireParam(request, "newPassword", "新密码", 32);
            String confirmPassword = ServletUtils.requireParam(request, "confirmPassword", "确认密码", 32);

            // 以数据库中的最新密文为准校验，而不是会话里的旧对象
            User latest = userDao.findById(loginUser.getId());
            if (latest == null) {
                throw new AppException("用户不存在，请重新登录");
            }

            if (!BCryptUtil.checkpw(oldPassword, latest.getPassword())) {
                throw new AppException("原密码错误！");
            }
            if (!newPassword.equals(confirmPassword)) {
                throw new AppException("两次输入的新密码不一致！");
            }
            if (newPassword.equals(oldPassword)) {
                throw new AppException("新密码不能与原密码相同");
            }
            ServletUtils.validatePassword(newPassword);

            String newHash = BCryptUtil.hashpw(newPassword);
            int row = userDao.updatePassword(latest.getId(), newHash);
            if (row <= 0) {
                throw new AppException("密码修改失败，请稍后重试");
            }

            // 同步会话中的用户对象（只放密文，不放明文）
            HttpSession session = request.getSession(false);
            if (session != null) {
                latest.setPassword(newHash);
                session.setAttribute("loginUser", latest);
            }
            request.setAttribute("msg", "密码修改成功！");

        } catch (AppException | IllegalArgumentException e) {
            request.setAttribute("msg", e.getMessage());
        } catch (Exception e) {
            getServletContext().log("修改密码失败", e);
            request.setAttribute("msg", "密码修改失败，请稍后重试");
        }

        viewProfile(request, response, loginUser);
    }
}