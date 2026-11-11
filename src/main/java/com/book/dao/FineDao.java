package com.book.dao;

import com.book.entity.FineRecord;
import com.book.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 罚款记录数据访问层
 */
public class FineDao {
    
    /**
     * 添加罚款记录
     */
    public int add(FineRecord record) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "INSERT INTO fine_records(user_id, borrow_record_id, amount, days, status) VALUES(?,?,?,?,?)";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getBorrowRecordId());
            pstmt.setDouble(3, record.getAmount());
            pstmt.setInt(4, record.getDays());
            pstmt.setString(5, record.getStatus() != null ? record.getStatus() : "unpaid");
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return row;
    }
    
    /**
     * 更新罚款状态（支付）
     */
    public int pay(Integer id) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE fine_records SET status='paid' WHERE id=?";
        
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
     * 根据ID查询罚款记录
     */
    public FineRecord findById(Integer id) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        FineRecord record = null;
        String sql = "SELECT fr.*, u.username, b.bname as book_name FROM fine_records fr " +
                     "LEFT JOIN users u ON fr.user_id = u.id " +
                     "LEFT JOIN borrow_records br ON fr.borrow_record_id = br.id " +
                     "LEFT JOIN books b ON br.book_id = b.id WHERE fr.id=?";
        
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
     * 查询所有罚款记录
     */
    public List<FineRecord> findAll() {
        List<FineRecord> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT fr.*, u.username, b.bname as book_name FROM fine_records fr " +
                     "LEFT JOIN users u ON fr.user_id = u.id " +
                     "LEFT JOIN borrow_records br ON fr.borrow_record_id = br.id " +
                     "LEFT JOIN books b ON br.book_id = b.id " +
                     "ORDER BY fr.id DESC";
        
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
     * 根据用户ID查询罚款记录
     */
    public List<FineRecord> findByUserId(Integer userId) {
        List<FineRecord> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT fr.*, u.username, b.bname as book_name FROM fine_records fr " +
                     "LEFT JOIN users u ON fr.user_id = u.id " +
                     "LEFT JOIN borrow_records br ON fr.borrow_record_id = br.id " +
                     "LEFT JOIN books b ON br.book_id = b.id " +
                     "WHERE fr.user_id=? ORDER BY fr.id DESC";
        
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
     * 查询用户未支付的罚款记录
     */
    public List<FineRecord> findUnpaidByUserId(Integer userId) {
        List<FineRecord> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT fr.*, u.username, b.bname as book_name FROM fine_records fr " +
                     "LEFT JOIN users u ON fr.user_id = u.id " +
                     "LEFT JOIN borrow_records br ON fr.borrow_record_id = br.id " +
                     "LEFT JOIN books b ON br.book_id = b.id " +
                     "WHERE fr.user_id=? AND fr.status='unpaid' ORDER BY fr.id DESC";
        
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
     * 查询所有未支付的罚款记录
     */
    public List<FineRecord> findAllUnpaid() {
        List<FineRecord> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT fr.*, u.username, b.bname as book_name FROM fine_records fr " +
                     "LEFT JOIN users u ON fr.user_id = u.id " +
                     "LEFT JOIN borrow_records br ON fr.borrow_record_id = br.id " +
                     "LEFT JOIN books b ON br.book_id = b.id " +
                     "WHERE fr.status='unpaid' ORDER BY fr.id DESC";
        
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
     * 计算用户未支付罚款总额
     */
    public double getUnpaidAmountByUserId(Integer userId) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT SUM(amount) FROM fine_records WHERE user_id=? AND status='unpaid'";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return 0;
    }
    
    /**
     * 计算所有未支付罚款总额
     */
    public double getTotalUnpaidAmount() {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT SUM(amount) FROM fine_records WHERE status='unpaid'";
        
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return 0;
    }
    
    /**
     * 结果集映射到对象
     */
    private FineRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        FineRecord record = new FineRecord();
        record.setId(rs.getInt("id"));
        record.setUserId(rs.getInt("user_id"));
        record.setBorrowRecordId(rs.getInt("borrow_record_id"));
        record.setAmount(rs.getDouble("amount"));
        record.setDays(rs.getInt("days"));
        record.setStatus(rs.getString("status"));
        record.setCreateTime(rs.getTimestamp("create_time"));
        record.setUsername(rs.getString("username"));
        // 设置图书名称
        try {
            record.setBookName(rs.getString("book_name"));
        } catch (SQLException e) {
            // 忽略，某些查询可能没有这些字段
        }
        return record;
    }
}