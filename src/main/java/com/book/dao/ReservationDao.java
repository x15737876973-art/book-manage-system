package com.book.dao;

import com.book.entity.Reservation;
import com.book.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 预约记录数据访问层
 */
public class ReservationDao {
    
    /**
     * 添加预约
     */
    public int add(Reservation reservation) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "INSERT INTO reservations(user_id, book_id, status) VALUES(?,?,?)";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reservation.getUserId());
            pstmt.setInt(2, reservation.getBookId());
            pstmt.setString(3, reservation.getStatus() != null ? reservation.getStatus() : "waiting");
            row = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }
        return row;
    }
    
    /**
     * 取消预约
     */
    public int cancel(Integer id) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE reservations SET status='cancelled' WHERE id=?";
        
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
     * 完成预约
     */
    public int complete(Integer id) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE reservations SET status='completed' WHERE id=?";
        
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
     * 通知预约
     */
    public int notify(Integer id) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        int row = 0;
        String sql = "UPDATE reservations SET status='notified' WHERE id=?";
        
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
     * 根据ID查询预约
     */
    public Reservation findById(Integer id) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Reservation reservation = null;
        String sql = "SELECT r.*, u.username, b.bname FROM reservations r " +
                     "LEFT JOIN users u ON r.user_id = u.id " +
                     "LEFT JOIN books b ON r.book_id = b.id WHERE r.id=?";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                reservation = mapResultSetToReservation(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return reservation;
    }
    
    /**
     * 查询所有预约
     */
    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT r.*, u.username, b.bname FROM reservations r " +
                     "LEFT JOIN users u ON r.user_id = u.id " +
                     "LEFT JOIN books b ON r.book_id = b.id ORDER BY r.id DESC";
        
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 根据用户ID查询预约
     */
    public List<Reservation> findByUserId(Integer userId) {
        List<Reservation> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT r.*, u.username, b.bname FROM reservations r " +
                     "LEFT JOIN users u ON r.user_id = u.id " +
                     "LEFT JOIN books b ON r.book_id = b.id WHERE r.user_id=? ORDER BY r.id DESC";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 查询用户对某书的等待中预约
     */
    public Reservation findWaitingByUserAndBook(Integer userId, Integer bookId) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Reservation reservation = null;
        String sql = "SELECT r.*, u.username, b.bname FROM reservations r " +
                     "LEFT JOIN users u ON r.user_id = u.id " +
                     "LEFT JOIN books b ON r.book_id = b.id WHERE r.user_id=? AND r.book_id=? AND r.status='waiting'";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                reservation = mapResultSetToReservation(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return reservation;
    }
    
    /**
     * 查询某书的等待中预约列表
     */
    public List<Reservation> findWaitingByBook(Integer bookId) {
        List<Reservation> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT r.*, u.username, b.bname FROM reservations r " +
                     "LEFT JOIN users u ON r.user_id = u.id " +
                     "LEFT JOIN books b ON r.book_id = b.id WHERE r.book_id=? AND r.status='waiting' ORDER BY r.id ASC";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 查询用户的有效预约（等待中或已通知）
     */
    public List<Reservation> findActiveByUserId(Integer userId) {
        List<Reservation> list = new ArrayList<>();
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT r.*, u.username, b.bname FROM reservations r " +
                     "LEFT JOIN users u ON r.user_id = u.id " +
                     "LEFT JOIN books b ON r.book_id = b.id WHERE r.user_id=? AND r.status IN ('waiting', 'notified') ORDER BY r.id DESC";
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
    
    /**
     * 结果集映射到对象
     */
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getInt("id"));
        reservation.setUserId(rs.getInt("user_id"));
        reservation.setBookId(rs.getInt("book_id"));
        reservation.setReserveDate(rs.getTimestamp("reserve_date"));
        reservation.setStatus(rs.getString("status"));
        reservation.setUsername(rs.getString("username"));
        reservation.setBookName(rs.getString("bname"));
        return reservation;
    }
    
    /**
     * 统计用户等待中的预约数量
     */
    public int countWaitingByUserId(Integer userId) {
        Connection conn = DBUtil.getConn();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;
        String sql = "SELECT COUNT(*) FROM reservations WHERE user_id=? AND status='waiting'";
        
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
}