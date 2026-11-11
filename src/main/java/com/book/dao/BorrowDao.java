package com.book.dao;

import com.book.entity.BorrowRecord;
import com.book.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 借阅记录DAO
 */
public class BorrowDao {
    
    /**
     * 查询所有借阅记录
     */
    public List<BorrowRecord> findAll() {
        List<BorrowRecord> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT br.*, u.username, u.real_name, b.bname as title, b.author " +
                     "FROM borrow_records br " +
                     "LEFT JOIN users u ON br.user_id = u.id " +
                     "LEFT JOIN books b ON br.book_id = b.id " +
                     "ORDER BY br.borrow_date DESC";
        
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 根据ID查询借阅记录
     */
    public BorrowRecord findById(Integer id) {
        BorrowRecord record = null;
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT br.*, u.username, u.real_name, b.bname as title, b.author " +
                     "FROM borrow_records br " +
                     "LEFT JOIN users u ON br.user_id = u.id " +
                     "LEFT JOIN books b ON br.book_id = b.id " +
                     "WHERE br.id = ?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                record = mapResultSetToRecord(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return record;
    }
    
    /**
     * 根据用户ID查询借阅记录
     */
    public List<BorrowRecord> findByUserId(Integer userId) {
        List<BorrowRecord> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT br.*, u.username, u.real_name, b.bname as title, b.author " +
                     "FROM borrow_records br " +
                     "LEFT JOIN users u ON br.user_id = u.id " +
                     "LEFT JOIN books b ON br.book_id = b.id " +
                     "WHERE br.user_id = ? " +
                     "ORDER BY br.borrow_date DESC";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 查询所有当前借阅（未还）
     */
    public List<BorrowRecord> findAllCurrentBorrows() {
        List<BorrowRecord> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT br.*, u.username, u.real_name, b.bname as title, b.author " +
                     "FROM borrow_records br " +
                     "LEFT JOIN users u ON br.user_id = u.id " +
                     "LEFT JOIN books b ON br.book_id = b.id " +
                     "WHERE br.status IN ('borrowed', 'overdue') " +
                     "ORDER BY br.borrow_date DESC";
        
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 查询逾期记录
     */
    public List<BorrowRecord> findOverdue() {
        List<BorrowRecord> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT br.*, u.username, u.real_name, b.bname as title, b.author " +
                     "FROM borrow_records br " +
                     "LEFT JOIN users u ON br.user_id = u.id " +
                     "LEFT JOIN books b ON br.book_id = b.id " +
                     "WHERE br.status='overdue' OR (br.status='borrowed' AND br.due_date < CURDATE()) " +
                     "ORDER BY br.borrow_date DESC";
        
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToRecord(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 搜索借阅记录（按读者姓名或图书名称）
     */
    public List<BorrowRecord> search(String keyword) {
        List<BorrowRecord> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT br.*, u.username, u.real_name, b.bname as title, b.author " +
                     "FROM borrow_records br " +
                     "LEFT JOIN users u ON br.user_id = u.id " +
                     "LEFT JOIN books b ON br.book_id = b.id " +
                     "WHERE u.real_name LIKE ? OR b.bname LIKE ? " +
                     "ORDER BY br.borrow_date DESC";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToRecord(rs));
            }
        } catch (SQLException e) {
            System.out.println("搜索借阅记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 添加借阅记录
     */
    public int add(BorrowRecord record) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "INSERT INTO borrow_records(user_id, book_id, borrow_date, due_date, status) VALUES(?, ?, ?, ?, ?)";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getBookId());
            pstmt.setDate(3, new java.sql.Date(record.getBorrowDate().getTime()));
            pstmt.setDate(4, new java.sql.Date(record.getDueDate().getTime()));
            pstmt.setString(5, record.getStatus());
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return row;
    }
    
    /**
     * 更新借阅记录
     */
    public int update(BorrowRecord record) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE borrow_records SET user_id=?, book_id=?, borrow_date=?, due_date=?, return_date=?, status=?, fine_amount=? WHERE id=?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getBookId());
            pstmt.setDate(3, new java.sql.Date(record.getBorrowDate().getTime()));
            pstmt.setDate(4, new java.sql.Date(record.getDueDate().getTime()));
            pstmt.setDate(5, record.getReturnDate() != null ? new java.sql.Date(record.getReturnDate().getTime()) : null);
            pstmt.setString(6, record.getStatus());
            pstmt.setDouble(7, record.getFineAmount());
            pstmt.setInt(8, record.getId());
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return row;
    }
    
    /**
     * 还书
     */
    public int returnBook(Integer recordId, Date returnDate, double fineAmount) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE borrow_records SET return_date=?, status='returned', fine_amount=? WHERE id=?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, returnDate);
            pstmt.setDouble(2, fineAmount);
            pstmt.setInt(3, recordId);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return row;
    }
    
    /**
     * 统计用户当前借阅数量
     */
    public int countCurrentBorrowsByUserId(Integer userId) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id=? AND status IN ('borrowed', 'overdue')";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return count;
    }
    
    /**
     * 统计用户逾期数量
     */
    public int countOverdueByUserId(Integer userId) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id=? AND status='overdue'";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return count;
    }
    
    /**
     * 更新归还日期
     */
    public int updateDueDate(int recordId, Date dueDate) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE borrow_records SET due_date = ? WHERE id = ?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, dueDate);
            pstmt.setInt(2, recordId);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return row;
    }
    
    /**
     * 更新逾期状态
     */
    public void updateOverdueStatus() {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        String sql = "UPDATE borrow_records SET status='overdue' WHERE status='borrowed' AND due_date < CURDATE()";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
    }
    
    /**
     * 检查用户是否借阅过指定图书
     */
    public boolean hasBorrowed(Integer userId, Integer bookId) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id=? AND book_id=? AND status IN ('borrowed', 'overdue')";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return false;
    }
    
    /**
     * 续借图书
     */
    public int renew(Integer recordId, Date newDueDate) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE borrow_records SET due_date=?, renew_count=renew_count+1 WHERE id=?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, newDueDate);
            pstmt.setInt(2, recordId);
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return row;
    }
    
    /**
     * 获取借阅排行榜
     */
    public List<Object[]> getBorrowRanking(int limit) {
        List<Object[]> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT b.bname as title, b.author, COUNT(br.id) as borrow_count " +
                     "FROM borrow_records br " +
                     "LEFT JOIN books b ON br.book_id = b.id " +
                     "GROUP BY br.book_id, b.bname, b.author " +
                     "ORDER BY borrow_count DESC " +
                     "LIMIT ?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = new Object[3];
                row[0] = rs.getString("title");
                row[1] = rs.getString("author");
                row[2] = rs.getInt("borrow_count");
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 映射ResultSet到BorrowRecord对象
     */
    private BorrowRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setId(rs.getInt("id"));
        record.setUserId(rs.getInt("user_id"));
        record.setBookId(rs.getInt("book_id"));
        record.setBorrowDate(rs.getDate("borrow_date"));
        record.setDueDate(rs.getDate("due_date"));
        record.setReturnDate(rs.getDate("return_date"));
        record.setStatus(rs.getString("status"));
        record.setFineAmount(rs.getDouble("fine_amount"));
        record.setRenewCount(rs.getInt("renew_count"));
        
        // 设置关联信息
        try {
            record.setUsername(rs.getString("username"));
            record.setRealName(rs.getString("real_name"));
            // 使用别名 title，实际对应数据库中的 bname
            record.setBookTitle(rs.getString("title"));
            record.setBookAuthor(rs.getString("author"));
        } catch (SQLException e) {
            // 忽略，某些查询可能没有这些字段
        }
        
        // 计算逾期天数和罚款金额
        try {
            java.util.Date dueDate = record.getDueDate();
            if (dueDate != null && ("overdue".equals(record.getStatus()) || "borrowed".equals(record.getStatus()))) {
                long diff = new java.util.Date().getTime() - dueDate.getTime();
                int days = (int) (diff / (1000 * 60 * 60 * 24));
                if (days > 0) {
                    record.setOverdueDays(days);
                    // 如果数据库中的罚款金额为0，则计算罚款金额（0.5元/天）
                    if (record.getFineAmount() == null || record.getFineAmount() == 0) {
                        record.setFineAmount(days * 0.5);
                    }
                }
            }
        } catch (Exception e) {
            // 忽略计算错误
        }
        
        return record;
    }
}