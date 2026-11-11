package com.book.servlet;

import javax.servlet.annotation.WebServlet;

/**
 * 读者个人中心。
 *
 * <p>查看资料与修改密码的逻辑见父类 {@link AbstractProfileServlet}，
 * 密码以 BCrypt 校验与存储。</p>
 */
@WebServlet("/reader/profileServlet")
public class ProfileServlet extends AbstractProfileServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getRequiredRole() {
        return "reader";
    }

    @Override
    protected String getProfilePage() {
        return "/reader/profile.jsp";
    }
}