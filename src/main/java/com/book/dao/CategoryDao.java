package com.book.dao;

import com.book.entity.Category;
import com.book.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 图书分类数据访问层。
 *
 * <p>所有 SQL 使用 PreparedStatement 预编译，资源由 try-with-resources 自动释放。</p>
 */
public class CategoryDao {

    private static final String BASE_SELECT = "SELECT * FROM categories";

    /** 新增分类 */
    public int add(Category category) {
        return DBUtil.update("INSERT INTO categories(name, description) VALUES(?,?)",
                ps -> {
                    ps.setString(1, category.getName());
                    ps.setString(2, category.getDescription());
                });
    }

    /** 更新分类 */
    public int update(Category category) {
        return DBUtil.update("UPDATE categories SET name=?, description=? WHERE id=?",
                ps -> {
                    ps.setString(1, category.getName());
                    ps.setString(2, category.getDescription());
                    ps.setInt(3, category.getId());
                });
    }

    /** 删除分类 */
    public int delete(Integer id) {
        return DBUtil.update("DELETE FROM categories WHERE id = ?", ps -> ps.setInt(1, id));
    }

    /** 根据 id 查询分类 */
    public Category findById(Integer id) {
        if (id == null) {
            return null;
        }
        List<Category> list = DBUtil.query(BASE_SELECT + " WHERE id = ?", ps -> ps.setInt(1, id), this::mapRow);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 根据名称查询分类（用于重名校验） */
    public Category findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        List<Category> list = DBUtil.query(BASE_SELECT + " WHERE name = ?",
                ps -> ps.setString(1, name.trim()), this::mapRow);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 查询所有分类 */
    public List<Category> findAll() {
        return DBUtil.query(BASE_SELECT + " ORDER BY id", null, this::mapRow);
    }

    /** 统计分类下的图书数量（删除前校验，防止误删） */
    public int getBookCountByCategory(Integer categoryId) {
        return (int) DBUtil.queryLong("SELECT COUNT(*) FROM books WHERE category_id = ?",
                ps -> ps.setInt(1, categoryId));
    }

    /** 结果集 → Category 实体 */
    private Category mapRow(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        return category;
    }
}