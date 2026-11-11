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
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理 Servlet（管理员）。
 *
 * <h3>安全改进</h3>
 * <ul>
 *   <li>新增用户与重置密码时，密码一律 BCrypt 加密后入库；</li>
 *   <li>参数校验：用户名格式、密码强度、手机/邮箱格式、id 合法性；</li>
 *   <li>不能删除或禁用自己，避免把自己锁在系统外。</li>
 * </ul>
 */
@WebServlet("/admin/userServlet")
public class UserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        User loginUser = ServletUtils.requireRole(request, response, "admin");
        if (loginUser == null) {
            return;
        }

        String action = ServletUtils.getTrimmed(request, "action");
        try {
            if ("add".equals(action)) {
                addUser(request, response);
            } else if ("toAdd".equals(action)) {
                toAddUser(request, response);
            } else if ("update".equals(action)) {
                updateUser(request, response, loginUser);
            } else if ("delete".equals(action)) {
                deleteUser(request, response, loginUser);
            } else if ("resetPwd".equals(action)) {
                resetPassword(request, response);
            } else if ("toggleStatus".equals(action)) {
                toggleStatus(request, response, loginUser);
            } else if ("toEdit".equals(action)) {
                toEditUser(request, response);
            } else {
                listUsers(request, response);
            }
        } catch (AppException | IllegalArgumentException e) {
            request.setAttribute("msg", e.getMessage());
            request.setAttribute("msgType", "error");
            listUsers(request, response);
        } catch (Exception e) {
            getServletContext().log("用户管理操作失败", e);
            request.setAttribute("error", "用户管理操作失败，请稍后重试");
            request.getRequestDispatcher("/admin/error.jsp").forward(request, response);
        }
    }

    // ======================================================================
    // 查询
    // ======================================================================

    private void listUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String role = ServletUtils.getEnum(request, "role", null, "admin", "reader");
        String keyword = ServletUtils.getParamWithLimit(request, "keyword", 50);
        String status = ServletUtils.getEnum(request, "status", null, "active", "disabled");

        boolean hasCondition = keyword != null || role != null || status != null;

        List<User> users;
        if ("reader".equals(role)) {
            users = userDao.findReaders();
        } else if ("admin".equals(role)) {
            users = userDao.findAdmins();
        } else {
            users = userDao.findAll();
        }

        if (hasCondition) {
            List<User> filtered = new ArrayList<>();
            String kw = keyword == null ? null : keyword.toLowerCase();
            for (User u : users) {
                boolean match = true;
                if (kw != null) {
                    String name = u.getUsername() == null ? "" : u.getUsername().toLowerCase();
                    String realName = u.getRealName() == null ? "" : u.getRealName().toLowerCase();
                    match = name.contains(kw) || realName.contains(kw);
                }
                if (match && status != null) {
                    String s = u.getStatus() != null ? u.getStatus() : "active";
                    match = status.equals(s);
                }
                if (match) {
                    filtered.add(u);
                }
            }
            users = filtered;
        }

        request.setAttribute("userList", users);
        request.setAttribute("role", role);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.getRequestDispatcher("/admin/userList.jsp").forward(request, response);
    }

    private void toEditUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = ServletUtils.requireInt(request, "id", "用户ID");
        User user = userDao.findById(id);
        if (user == null) {
            throw new AppException("用户不存在或已被删除");
        }
        request.setAttribute("user", user);
        request.setAttribute("role", ServletUtils.getTrimmed(request, "role"));
        request.getRequestDispatcher("/admin/userEdit.jsp").forward(request, response);
    }

    private void toAddUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("role", ServletUtils.getEnum(request, "role", "reader", "admin", "reader"));
        request.getRequestDispatcher("/admin/userAdd.jsp").forward(request, response);
    }

    // ======================================================================
    // 写入
    // ======================================================================

    /** 新增用户：密码 BCrypt 加密后入库 */
    private void addUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = ServletUtils.requireParam(request, "username", "用户名", 20);
        String password = ServletUtils.getTrimmed(request, "password");
        String role = ServletUtils.getEnum(request, "role", "reader", "admin", "reader");
        String realName = ServletUtils.getParamWithLimit(request, "realName", 50);
        String phone = ServletUtils.getTrimmed(request, "phone");
        String email = ServletUtils.getTrimmed(request, "email");

        ServletUtils.validateUsername(username);
        ServletUtils.validatePhone(phone);
        ServletUtils.validateEmail(email);

        // 未填密码时使用默认密码
        if (password == null) {
            password = UserDao.DEFAULT_PASSWORD;
        } else {
            ServletUtils.validatePassword(password);
        }

        if (userDao.existsByUsername(username)) {
            throw new AppException("用户名【" + username + "】已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(BCryptUtil.hashpw(password));   // ★ 加密存储
        user.setRole(role);
        user.setRealName(realName != null ? realName : username);
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus("active");

        if (userDao.add(user) <= 0) {
            throw new AppException("添加失败，请稍后重试");
        }
        response.sendRedirect(request.getContextPath() + "/admin/userServlet?action=list&role=" + role);
    }

    /** 更新用户基本信息（不含密码） */
    private void updateUser(HttpServletRequest request, HttpServletResponse response, User loginUser)
            throws ServletException, IOException {
        int id = ServletUtils.requireInt(request, "id", "用户ID");
        String username = ServletUtils.requireParam(request, "username", "用户名", 20);
        String role = ServletUtils.getEnum(request, "role", "reader", "admin", "reader");
        String realName = ServletUtils.getParamWithLimit(request, "realName", 50);
        String phone = ServletUtils.getTrimmed(request, "phone");
        String email = ServletUtils.getTrimmed(request, "email");

        ServletUtils.validateUsername(username);
        ServletUtils.validatePhone(phone);
        ServletUtils.validateEmail(email);

        User existing = userDao.findById(id);
        if (existing == null) {
            throw new AppException("用户不存在或已被删除");
        }
        User sameName = userDao.findByUsername(username);
        if (sameName != null && !sameName.getId().equals(id)) {
            throw new AppException("用户名【" + username + "】已被占用");
        }
        // 不允许把自己改成读者（否则会失去管理权限）
        Integer selfId = loginUser.getId();
        if (selfId != null && selfId.intValue() == id && !"admin".equals(role)) {
            throw new AppException("不能修改自己的身份为读者");
        }

        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus(existing.getStatus());

        if (userDao.update(user) <= 0) {
            throw new AppException("更新失败，请稍后重试");
        }
        response.sendRedirect(request.getContextPath() + "/admin/userServlet?action=list&role=" + role);
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response, User loginUser)
            throws ServletException, IOException {
        int id = ServletUtils.requireInt(request, "id", "用户ID");
        String role = ServletUtils.getTrimmed(request, "role");

        Integer selfId2 = loginUser.getId();
        if (selfId2 != null && selfId2.intValue() == id) {
            throw new AppException("不能删除当前登录的账号");
        }
        User target = userDao.findById(id);
        if (target == null) {
            throw new AppException("用户不存在或已被删除");
        }
        userDao.delete(id);

        request.setAttribute("msg", "用户【" + target.getUsername() + "】已删除");
        request.setAttribute("msgType", "success");
        request.setAttribute("role", role);
        listUsers(request, response);
    }

    /**
     * 重置密码为默认值 123456（BCrypt 加密后存储）。
     */
    private void resetPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = ServletUtils.requireInt(request, "id", "用户ID");
        String role = ServletUtils.getTrimmed(request, "role");

        User target = userDao.findById(id);
        if (target == null) {
            throw new AppException("用户不存在或已被删除");
        }
        userDao.resetPassword(id);   // 内部 BCrypt 加密

        request.setAttribute("msg", "密码已重置为 " + UserDao.DEFAULT_PASSWORD);
        request.setAttribute("msgType", "success");
        request.setAttribute("role", role);
        listUsers(request, response);
    }

    private void toggleStatus(HttpServletRequest request, HttpServletResponse response, User loginUser)
            throws ServletException, IOException {
        int id = ServletUtils.requireInt(request, "id", "用户ID");
        String status = ServletUtils.requireParam(request, "status", "状态");
        String role = ServletUtils.getTrimmed(request, "role");

        Integer selfId2 = loginUser.getId();
        if (selfId2 != null && selfId2.intValue() == id) {
            throw new AppException("不能禁用当前登录的账号");
        }
        User target = userDao.findById(id);
        if (target == null) {
            throw new AppException("用户不存在或已被删除");
        }

        String newStatus = "active".equals(status) ? "disabled" : "active";
        userDao.updateStatus(id, newStatus);

        request.setAttribute("msg", "用户已" + ("active".equals(newStatus) ? "启用" : "禁用"));
        request.setAttribute("msgType", "success");
        request.setAttribute("role", role);
        listUsers(request, response);
    }
}