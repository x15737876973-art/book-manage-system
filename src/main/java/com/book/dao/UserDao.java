package com.book.dao;

import com.book.entity.User;
import com.book.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问层
 */
public class UserDao {
    
    /**
     * 用户登录验证
     * @return 用户对象，null代表登录失败
     */
    public User login(String username, String password) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;
        String sql = "SELECT * FROM users WHERE username=? AND password=? AND status='active'";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setRealName(rs.getString("real_name"));
                user.setPhone(rs.getString("phone"));
                user.setEmail(rs.getString("email"));
                user.setStatus(rs.getString("status"));
                user.setCreateTime(rs.getTimestamp("create_time"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return user;
    }
    
    /**
     * 查询所有用户
     */
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM users ORDER BY id";
        
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
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
                list.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 查询所有读者
     */
    public List<User> findReaders() {
        List<User> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM users WHERE role='reader' ORDER BY id";
        
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
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
                list.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 查询所有管理员
     */
    public List<User> findAdmins() {
        List<User> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM users WHERE role='admin' ORDER BY id";
        
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
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
                list.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 根据ID查询用户
     */
    public User findById(Integer id) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;
        String sql = "SELECT * FROM users WHERE id=?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setRealName(rs.getString("real_name"));
                user.setPhone(rs.getString("phone"));
                user.setEmail(rs.getString("email"));
                user.setStatus(rs.getString("status"));
                user.setCreateTime(rs.getTimestamp("create_time"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return user;
    }
    
    /**
     * 根据用户名查询用户
     */
    public User findByUsername(String username) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;
        String sql = "SELECT * FROM users WHERE username=?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setRealName(rs.getString("real_name"));
                user.setPhone(rs.getString("phone"));
                user.setEmail(rs.getString("email"));
                user.setStatus(rs.getString("status"));
                user.setCreateTime(rs.getTimestamp("create_time"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return user;
    }
    
    /**
     * 添加用户
     */
    public int add(User user) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "INSERT INTO users(username, password, role, real_name, phone, email, status) VALUES(?,?,?,?,?,?,?)";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getRealName());
            pstmt.setString(5, user.getPhone());
            pstmt.setString(6, user.getEmail());
            pstmt.setString(7, user.getStatus() != null ? user.getStatus() : "active");
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return row;
    }
    
    /**
     * 更新用户信息
     */
    public int update(User user) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE users SET username=?, real_name=?, phone=?, email=?, status=?, role=? WHERE id=?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getRealName());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getStatus());
            pstmt.setString(6, user.getRole());
            pstmt.setInt(7, user.getId());
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return row;
    }
    
    /**
     * 修改密码
     */
    public int updatePassword(Integer id, String newPassword) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE users SET password=? WHERE id=?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, id);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return row;
    }
    
    /**
     * 重置密码为123456
     */
    public int resetPassword(Integer id) {
        return updatePassword(id, "123456");
    }
    
    /**
     * 删除用户
     */
    public int delete(Integer id) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "DELETE FROM users WHERE id=?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return row;
    }
    
    /**
     * 启用/禁用用户
     */
    public int updateStatus(Integer id, String status) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE users SET status=? WHERE id=?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return row;
    }
}
