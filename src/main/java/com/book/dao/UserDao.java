package com.book.dao;

import com.book.entity.User;
import com.book.util.DBUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * 用户数据访问层。
 *
 * <p>所有 SQL 均为 PreparedStatement 预编译，资源由 try-with-resources 自动释放。</p>
 *
 * <p><b>密码相关：</b>本类只负责存取，加密与校验由 {@link com.book.util.BCryptUtil} 完成。
 * 数据库中的 {@code password} 字段存储 BCrypt 密文（$2a$10$...）。</p>
 */
public class UserDao {

    private static final String BASE_SELECT = "SELECT * FROM users";

    // ======================================================================
    // 登录与查询
    // ======================================================================

    /**
     * 按用户名查询用户（<b>不再用 SQL 比对密码</b>）。
     *
     * <p>改造说明：旧实现是 {@code WHERE username=? AND password=?}，
     * 意味着密码以可被数据库比较的形式传入。现在密码校验在 Java 层用 BCrypt 完成，
     * 这里只按用户名取出用户，再由调用方调用 {@code BCryptUtil.checkpw} 校验。</p>
     *
     * @return 用户对象；不存在返回 null
     */
    public User findByUsernameForLogin(String username) {
        List<User> list = DBUtil.query(BASE_SELECT + " WHERE username = ?", ps -> ps.setString(1, username), this::mapRow);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 用户登录（兼容旧签名）。
     *
     * <p>旧调用方传入明文密码并与数据库明文比对。为保持行为一致，
     * 本方法内部改用 BCrypt 校验：数据库若已是密文则按 BCrypt 校验，
     * 若仍是历史明文则做兼容比较（详见 {@link com.book.util.BCryptUtil#checkpw}）。</p>
     *
     * <p><b>建议新代码使用 {@link #findByUsernameForLogin} + BCryptUtil.checkpw</b>，
     * 以便在密码为旧明文时顺手升级为密文。</p>
     *
     * @return 用户对象；账号不存在、被禁用或密码错误返回 null
     */
    public User login(String username, String password) {
        User user = findByUsernameForLogin(username);
        if (user == null || !com.book.util.BCryptUtil.checkpw(password, user.getPassword())) {
            return null;
        }
        return "active".equals(user.getStatus()) ? user : null;
    }

    /** 查询所有用户 */
    public List<User> findAll() {
        return DBUtil.query(BASE_SELECT + " ORDER BY id", null, this::mapRow);
    }

    /** 查询所有读者 */
    public List<User> findReaders() {
        return DBUtil.query(BASE_SELECT + " WHERE role = 'reader' ORDER BY id", null, this::mapRow);
    }

    /** 查询所有管理员 */
    public List<User> findAdmins() {
        return DBUtil.query(BASE_SELECT + " WHERE role = 'admin' ORDER BY id", null, this::mapRow);
    }

    /** 根据 id 查询用户 */
    public User findById(Integer id) {
        if (id == null) {
            return null;
        }
        List<User> list = DBUtil.query(BASE_SELECT + " WHERE id = ?", ps -> ps.setInt(1, id), this::mapRow);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 根据用户名查询用户（注册查重、登录使用） */
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        List<User> list = DBUtil.query(BASE_SELECT + " WHERE username = ?",
                ps -> ps.setString(1, username.trim()), this::mapRow);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 用户名是否已被占用（注册查重，比取出整条记录更轻量）。
     */
    public boolean existsByUsername(String username) {
        return DBUtil.queryLong("SELECT COUNT(*) FROM users WHERE username = ?",
                ps -> ps.setString(1, username)) > 0;
    }

    // ======================================================================
    // 写入
    // ======================================================================

    /**
     * 新增用户。
     *
     * <p><b>注意：</b>本方法不会自动加密密码，调用方必须传入已经
     * {@code BCryptUtil.hashpw()} 处理过的密文。</p>
     */
    public int add(User user) {
        String sql = "INSERT INTO users(username, password, role, real_name, phone, email, status) "
                + "VALUES(?,?,?,?,?,?,?)";
        return DBUtil.update(sql, ps -> {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            setNullableString(ps, 4, user.getRealName());
            setNullableString(ps, 5, user.getPhone());
            setNullableString(ps, 6, user.getEmail());
            ps.setString(7, user.getStatus() != null ? user.getStatus() : "active");
        });
    }

    /** 更新用户基本信息（不含密码） */
    public int update(User user) {
        String sql = "UPDATE users SET username=?, real_name=?, phone=?, email=?, status=?, role=? WHERE id=?";
        return DBUtil.update(sql, ps -> {
            ps.setString(1, user.getUsername());
            setNullableString(ps, 2, user.getRealName());
            setNullableString(ps, 3, user.getPhone());
            setNullableString(ps, 4, user.getEmail());
            ps.setString(5, user.getStatus());
            ps.setString(6, user.getRole());
            ps.setInt(7, user.getId());
        });
    }

    /**
     * 修改密码。
     *
     * <p><b>注意：</b>传入的必须是 BCrypt 密文，本方法不做加密，
     * 以免在同一业务里加密两次导致校验失败。</p>
     */
    public int updatePassword(Integer id, String newPasswordHash) {
        return DBUtil.update("UPDATE users SET password = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, newPasswordHash);
                    ps.setInt(2, id);
                });
    }

    /**
     * 重置密码为指定明文（内部会加密后存储）。
     *
     * @param plainPassword 新的明文密码
     */
    public int resetPassword(Integer id, String plainPassword) {
        return updatePassword(id, com.book.util.BCryptUtil.hashpw(plainPassword));
    }

    /**
     * 重置密码为默认值 123456（加密后存储）。
     */
    public int resetPassword(Integer id) {
        return updatePassword(id, com.book.util.BCryptUtil.hashpw(DEFAULT_PASSWORD));
    }

    /** 默认重置密码明文，仅用于提示文案，实际存储的是其 BCrypt 密文 */
    public static final String DEFAULT_PASSWORD = "123456";

    /** 删除用户 */
    public int delete(Integer id) {
        return DBUtil.update("DELETE FROM users WHERE id = ?", ps -> ps.setInt(1, id));
    }

    /** 启用 / 禁用用户 */
    public int updateStatus(Integer id, String status) {
        return DBUtil.update("UPDATE users SET status = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, status);
                    ps.setInt(2, id);
                });
    }

    // ======================================================================
    // 内部工具
    // ======================================================================

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isEmpty()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    /** 结果集 → User 实体 */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setRealName(rs.getString("real_name"));
        user.setPhone(rs.getString("phone"));
        user.setEmail(rs.getString("email"));
        user.setStatus(rs.getString("status"));
        user.setCreateTime(rs.getTimestamp("create_time"));
        return user;
    }
}