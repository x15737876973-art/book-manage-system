package com.book.dao;

import com.book.entity.Book;
import com.book.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDao {
    
    /**
     * 查询所有图书（带分类名称）
     */
    public List<Book> findAll() {
        List<Book> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT b.*, c.name as category_name FROM books b LEFT JOIN categories c ON b.category_id = c.id ORDER BY b.id";
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while(rs.next()){
                list.add(mapResultSetToBook(rs));
            }
            System.out.println("查询到图书数量: " + list.size());
        } catch (SQLException e) {
            System.out.println("查询图书失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt,rs);
        }
        return list;
    }
    
    /**
     * 根据ID查询图书
     */
    public Book findById(Integer id) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Book book = null;
        String sql = "SELECT b.*, c.name as category_name FROM books b LEFT JOIN categories c ON b.category_id = c.id WHERE b.id=?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if(rs.next()){
                book = mapResultSetToBook(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt,rs);
        }
        return book;
    }
    
    /**
     * 根据图书编号查询
     */
    public Book findByBid(String bid) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Book book = null;
        String sql = "SELECT b.*, c.name as category_name FROM books b LEFT JOIN categories c ON b.category_id = c.id WHERE b.bid=?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, bid);
            rs = pstmt.executeQuery();
            if(rs.next()){
                book = mapResultSetToBook(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt,rs);
        }
        return book;
    }
    
    /**
     * 根据分类查询图书
     */
    public List<Book> findByCategoryId(Integer categoryId) {
        List<Book> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT b.*, c.name as category_name FROM books b LEFT JOIN categories c ON b.category_id = c.id WHERE b.category_id=? ORDER BY b.id";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            rs = pstmt.executeQuery();
            while(rs.next()){
                list.add(mapResultSetToBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt,rs);
        }
        return list;
    }
    
    /**
     * 搜索图书（按书名、作者）
     */
    public List<Book> searchBooks(String keyword) {
        List<Book> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT b.*, c.name as category_name FROM books b LEFT JOIN categories c ON b.category_id = c.id WHERE b.bname LIKE ? OR b.author LIKE ? ORDER BY b.id";
        try {
            if (conn == null) {
                System.out.println("错误：数据库连接为null");
                return list;
            }
            pstmt = conn.prepareStatement(sql);
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            rs = pstmt.executeQuery();
            while(rs.next()){
                list.add(mapResultSetToBook(rs));
            }
            System.out.println("搜索关键字[" + keyword + "]查询到图书数量: " + list.size());
        } catch (SQLException e) {
            System.out.println("搜索图书失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt,rs);
        }
        return list;
    }
    
    /**
     * 多条件搜索（书名/作者 + 分类）
     */
    public List<Book> searchBooks(String keyword, Integer categoryId) {
        List<Book> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT b.*, c.name as category_name FROM books b LEFT JOIN categories c ON b.category_id = c.id WHERE (b.bname LIKE ? OR b.author LIKE ?) AND b.category_id=? ORDER BY b.id";
        try {
            if (conn == null) {
                System.out.println("错误：数据库连接为null");
                return list;
            }
            pstmt = conn.prepareStatement(sql);
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setInt(3, categoryId);
            rs = pstmt.executeQuery();
            while(rs.next()){
                list.add(mapResultSetToBook(rs));
            }
            System.out.println("搜索关键字[" + keyword + "]分类[" + categoryId + "]查询到图书数量: " + list.size());
        } catch (SQLException e) {
            System.out.println("搜索图书失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt,rs);
        }
        return list;
    }
    
    /**
     * 新增图书（完整字段）
     */
    public int addBook(Book book) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location, description) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, book.getBid());
            pstmt.setString(2, book.getIsbn());
            pstmt.setString(3, book.getBname());
            pstmt.setString(4, book.getAuthor());
            pstmt.setString(5, book.getPublisher());
            pstmt.setDate(6, book.getPublishDate() != null ? new java.sql.Date(book.getPublishDate().getTime()) : null);
            pstmt.setDouble(7, book.getPrice() != null ? book.getPrice() : 0);
            pstmt.setObject(8, book.getCategoryId());
            pstmt.setInt(9, book.getStock() != null ? book.getStock() : 0);
            pstmt.setInt(10, book.getAvailableStock() != null ? book.getAvailableStock() : book.getStock());
            pstmt.setString(11, book.getLocation());
            pstmt.setString(12, book.getDescription());
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt);
        }
        return row;
    }

    // 兼容 bookDao.add()
    public int add(Book book){
        return addBook(book);
    }

    /**
     * 修改图书（完整字段）
     */
    public int update(Book book) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE books SET isbn=?, bname=?, author=?, publisher=?, publish_date=?, price=?, category_id=?, stock=?, available_stock=?, location=?, description=? WHERE bid=?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getBname());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getPublisher());
            pstmt.setDate(5, book.getPublishDate() != null ? new java.sql.Date(book.getPublishDate().getTime()) : null);
            pstmt.setDouble(6, book.getPrice() != null ? book.getPrice() : 0);
            pstmt.setObject(7, book.getCategoryId());
            pstmt.setInt(8, book.getStock() != null ? book.getStock() : 0);
            pstmt.setInt(9, book.getAvailableStock() != null ? book.getAvailableStock() : 0);
            pstmt.setString(10, book.getLocation());
            pstmt.setString(11, book.getDescription());
            pstmt.setString(12, book.getBid());
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt);
        }
        return row;
    }

    // 兼容 bookDao.updateBook()
    public int updateBook(Book book){
        return update(book);
    }

    /**
     * 删除图书
     */
    public int delete(String bid) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "DELETE FROM books WHERE bid=?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,bid);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt);
        }
        return row;
    }

    // 兼容 bookDao.deleteBook()
    public int deleteBook(String bid){
        return delete(bid);
    }
    
    /**
     * 更新库存（借书减少可借数量）
     */
    public int decreaseAvailableStock(Integer id) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE books SET available_stock = available_stock - 1 WHERE id = ? AND available_stock > 0";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt);
        }
        return row;
    }
    
    /**
     * 更新库存（还书增加可借数量）
     */
    public int increaseAvailableStock(Integer id) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE books SET available_stock = available_stock + 1 WHERE id = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt);
        }
        return row;
    }
    
    /**
     * 统计图书总数
     */
    public int count() {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT COUNT(*) FROM books";
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt,rs);
        }
        return 0;
    }
    
    /**
     * 统计总库存
     */
    public int countTotalStock() {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT SUM(stock) FROM books";
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt,rs);
        }
        return 0;
    }
    
    /**
     * 统计可借数量
     */
    public int countAvailableStock() {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT SUM(available_stock) FROM books";
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt,rs);
        }
        return 0;
    }
    
    /**
     * 统计分类下的图书数量
     */
    public int countByCategoryId(Integer categoryId) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT COUNT(*) FROM books WHERE category_id=?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt,rs);
        }
        return 0;
    }
    
    /**
     * 统计分类下的可借数量
     */
    public int countAvailableByCategoryId(Integer categoryId) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT SUM(available_stock) FROM books WHERE category_id=?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn,pstmt,rs);
        }
        return 0;
    }
    
    /**
     * 结果集映射到Book对象
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setBid(rs.getString("bid"));
        b.setIsbn(rs.getString("isbn"));
        b.setBname(rs.getString("bname"));
        b.setAuthor(rs.getString("author"));
        b.setPublisher(rs.getString("publisher"));
        b.setPublishDate(rs.getDate("publish_date"));
        b.setPrice(rs.getDouble("price"));
        b.setCategoryId(rs.getInt("category_id"));
        b.setCategoryName(rs.getString("category_name"));
        b.setStock(rs.getInt("stock"));
        b.setAvailableStock(rs.getInt("available_stock"));
        b.setLocation(rs.getString("location"));
        b.setDescription(rs.getString("description"));
        b.setCreateTime(rs.getTimestamp("create_time"));
        return b;
    }
}