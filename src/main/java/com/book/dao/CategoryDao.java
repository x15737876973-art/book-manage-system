package com.book.dao;

import com.book.entity.Category;
import com.book.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 图书分类数据访问层
 */
public class CategoryDao {
    
    /**
     * 添加分类
     */
    public int add(Category category) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "INSERT INTO categories(name, description) VALUES(?,?)";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return row;
    }
    
    /**
     * 更新分类
     */
    public int update(Category category) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE categories SET name=?, description=? WHERE id=?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.setInt(3, category.getId());
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return row;
    }
    
    /**
     * 删除分类
     */
    public int delete(Integer id) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "DELETE FROM categories WHERE id=?";
        
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
     * 根据ID查询分类
     */
    public Category findById(Integer id) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Category category = null;
        String sql = "SELECT * FROM categories WHERE id=?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return category;
    }
    
    /**
     * 根据名称查询分类
     */
    public Category findByName(String name) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Category category = null;
        String sql = "SELECT * FROM categories WHERE name=?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return category;
    }
    
    /**
     * 查询所有分类
     */
    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM categories ORDER BY id";
        
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                list.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 统计分类下的图书数量
     */
    public int getBookCountByCategory(Integer categoryId) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT COUNT(*) FROM books WHERE category_id=?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return 0;
    }
}