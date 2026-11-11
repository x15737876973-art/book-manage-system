package com.book.dao;

import com.book.entity.Reservation;
import com.book.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 预约记录数据访问层。
 *
 * <p>所有 SQL 使用 PreparedStatement 预编译，资源由 try-with-resources 自动释放。</p>
 */
public class ReservationDao {

    private static final String BASE_SELECT =
            "SELECT r.*, u.username, b.bname "
                    + "FROM reservations r "
                    + "LEFT JOIN users u ON r.user_id = u.id "
                    + "LEFT JOIN books b ON r.book_id = b.id ";

    // ======================================================================
    // 写入
    // ======================================================================

    /** 新增预约 */
    public int add(Reservation reservation) {
        return DBUtil.update("INSERT INTO reservations(user_id, book_id, status) VALUES(?,?,?)",
                ps -> {
                    ps.setInt(1, reservation.getUserId());
                    ps.setInt(2, reservation.getBookId());
                    ps.setString(3, reservation.getStatus() != null ? reservation.getStatus() : "waiting");
                });
    }

    /** 取消预约 */
    public int cancel(Integer id) {
        return DBUtil.update("UPDATE reservations SET status='cancelled' WHERE id = ?", ps -> ps.setInt(1, id));
    }

    /** 完成预约（借到书后调用） */
    public int complete(Integer id) {
        return DBUtil.update("UPDATE reservations SET status='completed' WHERE id = ?", ps -> ps.setInt(1, id));
    }

    /** 通知预约（书可借时调用） */
    public int notify(Integer id) {
        return DBUtil.update("UPDATE reservations SET status='notified' WHERE id = ?", ps -> ps.setInt(1, id));
    }

    // ======================================================================
    // 查询
    // ======================================================================

    /** 根据 id 查询预约 */
    public Reservation findById(Integer id) {
        if (id == null) {
            return null;
        }
        List<Reservation> list = DBUtil.query(BASE_SELECT + "WHERE r.id = ?", ps -> ps.setInt(1, id), this::mapRow);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 查询所有预约 */
    public List<Reservation> findAll() {
        return DBUtil.query(BASE_SELECT + "ORDER BY r.id DESC", null, this::mapRow);
    }

    /** 查询某用户的全部预约 */
    public List<Reservation> findByUserId(Integer userId) {
        return DBUtil.query(BASE_SELECT + "WHERE r.user_id = ? ORDER BY r.id DESC",
                ps -> ps.setInt(1, userId), this::mapRow);
    }

    /** 查询用户对某书"等待中"的预约 */
    public Reservation findWaitingByUserAndBook(Integer userId, Integer bookId) {
        List<Reservation> list = DBUtil.query(
                BASE_SELECT + "WHERE r.user_id = ? AND r.book_id = ? AND r.status = 'waiting'",
                ps -> {
                    ps.setInt(1, userId);
                    ps.setInt(2, bookId);
                }, this::mapRow);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 查询某书的所有"等待中"预约（按先后顺序） */
    public List<Reservation> findWaitingByBook(Integer bookId) {
        return DBUtil.query(BASE_SELECT + "WHERE r.book_id = ? AND r.status = 'waiting' ORDER BY r.id ASC",
                ps -> ps.setInt(1, bookId), this::mapRow);
    }

    /** 查询用户的有效预约（等待中或已通知） */
    public List<Reservation> findActiveByUserId(Integer userId) {
        return DBUtil.query(
                BASE_SELECT + "WHERE r.user_id = ? AND r.status IN ('waiting','notified') ORDER BY r.id DESC",
                ps -> ps.setInt(1, userId), this::mapRow);
    }

    /** 统计用户等待中的预约数量 */
    public int countWaitingByUserId(Integer userId) {
        return (int) DBUtil.queryLong(
                "SELECT COUNT(*) FROM reservations WHERE user_id = ? AND status = 'waiting'",
                ps -> ps.setInt(1, userId));
    }

    // ======================================================================
    // 内部工具
    // ======================================================================

    /** 结果集 → Reservation 实体 */
    private Reservation mapRow(ResultSet rs) throws SQLException {
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
}