package com.book.dao;

import com.book.entity.FineRecord;
import com.book.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 罚款记录数据访问层。
 *
 * <p>所有 SQL 使用 PreparedStatement 预编译，资源由 try-with-resources 自动释放。</p>
 */
public class FineDao {

    private static final String BASE_SELECT =
            "SELECT fr.*, u.username, b.bname AS book_name "
                    + "FROM fine_records fr "
                    + "LEFT JOIN users u ON fr.user_id = u.id "
                    + "LEFT JOIN borrow_records br ON fr.borrow_record_id = br.id "
                    + "LEFT JOIN books b ON br.book_id = b.id ";

    /** 新增罚款记录 */
    public int add(FineRecord record) {
        String sql = "INSERT INTO fine_records(user_id, borrow_record_id, amount, days, status) VALUES(?,?,?,?,?)";
        return DBUtil.update(sql, ps -> {
            ps.setInt(1, record.getUserId());
            ps.setInt(2, record.getBorrowRecordId());
            ps.setDouble(3, record.getAmount() == null ? 0d : record.getAmount());
            ps.setInt(4, record.getDays() == null ? 0 : record.getDays());
            ps.setString(5, record.getStatus() != null ? record.getStatus() : "unpaid");
        });
    }

    /** 标记罚款已支付 */
    public int pay(Integer id) {
        return DBUtil.update("UPDATE fine_records SET status='paid' WHERE id = ?", ps -> ps.setInt(1, id));
    }

    /** 根据 id 查询 */
    public FineRecord findById(Integer id) {
        if (id == null) {
            return null;
        }
        List<FineRecord> list = DBUtil.query(BASE_SELECT + "WHERE fr.id = ?", ps -> ps.setInt(1, id), this::mapRow);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 查询所有罚款记录 */
    public List<FineRecord> findAll() {
        return DBUtil.query(BASE_SELECT + "ORDER BY fr.id DESC", null, this::mapRow);
    }

    /** 查询某用户的罚款记录 */
    public List<FineRecord> findByUserId(Integer userId) {
        return DBUtil.query(BASE_SELECT + "WHERE fr.user_id = ? ORDER BY fr.id DESC",
                ps -> ps.setInt(1, userId), this::mapRow);
    }

    /** 查询某用户未支付的罚款记录 */
    public List<FineRecord> findUnpaidByUserId(Integer userId) {
        return DBUtil.query(BASE_SELECT + "WHERE fr.user_id = ? AND fr.status = 'unpaid' ORDER BY fr.id DESC",
                ps -> ps.setInt(1, userId), this::mapRow);
    }

    /** 计算某用户未支付罚款总额 */
    public double getUnpaidAmountByUserId(Integer userId) {
        return DBUtil.queryLong(
                "SELECT COALESCE(SUM(amount), 0) FROM fine_records WHERE user_id = ? AND status = 'unpaid'",
                ps -> ps.setInt(1, userId));
    }

    /** 计算所有未支付罚款总额 */
    public double getTotalUnpaidAmount() {
        return DBUtil.queryLong("SELECT COALESCE(SUM(amount), 0) FROM fine_records WHERE status = 'unpaid'", null);
    }

    /** 结果集 → FineRecord 实体 */
    private FineRecord mapRow(ResultSet rs) throws SQLException {
        FineRecord record = new FineRecord();
        record.setId(rs.getInt("id"));
        record.setUserId(rs.getInt("user_id"));
        record.setBorrowRecordId(rs.getInt("borrow_record_id"));
        record.setAmount(rs.getDouble("amount"));
        record.setDays(rs.getInt("days"));
        record.setStatus(rs.getString("status"));
        record.setCreateTime(rs.getTimestamp("create_time"));
        record.setUsername(rs.getString("username"));
        record.setBookName(rs.getString("book_name"));
        return record;
    }
}