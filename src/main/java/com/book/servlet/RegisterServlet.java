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
import java.io.IOException;

/**
 * 读者注册 Servlet。
 *
 * <h3>安全改进</h3>
 * <ul>
 *   <li><b>密码加密存储</b>：使用 {@link BCryptUtil#hashpw} 加密后再入库，数据库里没有明文；</li>
 *   <li><b>参数校验</b>：用户名格式、密码强度、两次密码一致、手机/邮箱格式、长度上限；</li>
 *   <li><b>统一异常</b>：任何异常都转成友好提示，不会把堆栈打到页面。</li>
 * </ul>
 */
@WebServlet("/registerServlet")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            // ---------- 1. 取参数并做格式校验 ----------
            String username = ServletUtils.requireParam(request, "username", "用户名", 20);
            String password = ServletUtils.requireParam(request, "password", "密码", 32);
            String password2 = ServletUtils.requireParam(request, "password2", "确认密码", 32);
            String realName = ServletUtils.getParamWithLimit(request, "realName", 50);
            String phone = ServletUtils.getTrimmed(request, "phone");
            String email = ServletUtils.getTrimmed(request, "email");

            ServletUtils.validateUsername(username);
            ServletUtils.validatePassword(password);
            ServletUtils.validatePhone(phone);
            ServletUtils.validateEmail(email);

            if (!password.equals(password2)) {
                throw new AppException("两次输入的密码不一致！");
            }

            // ---------- 2. 用户名查重 ----------
            if (userDao.existsByUsername(username)) {
                throw new AppException("用户名已存在，请换一个");
            }

            // ---------- 3. 密码加密后入库 ----------
            User user = new User();
            user.setUsername(username);
            user.setPassword(BCryptUtil.hashpw(password));   // ★ 存密文，不存明文
            user.setRole("reader");                          // 注册一律为读者，防止提权
            user.setRealName(realName != null ? realName : username);
            user.setPhone(phone);
            user.setEmail(email);
            user.setStatus("active");

            int row = userDao.add(user);
            if (row > 0) {
                request.setAttribute("msg", "注册成功，请登录！");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            } else {
                throw new AppException("注册失败，请稍后重试");
            }

        } catch (AppException | IllegalArgumentException e) {
            backToForm(request, response, e.getMessage());
        } catch (Exception e) {
            getServletContext().log("注册失败", e);
            backToForm(request, response, "注册失败，请稍后重试");
        }
    }

    /** 回到注册页并回显提示 */
    private void backToForm(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("msg", message);
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }
}