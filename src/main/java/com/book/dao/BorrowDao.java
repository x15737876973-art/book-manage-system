package com.book.dao;

import com.book.entity.BorrowRecord;
import com.book.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 借阅记录数据访问层。
 *
 * <p>所有 SQL 使用 PreparedStatement 预编译，资源由 try-with-resources 自动释放。</p>
 */
public class BorrowDao {

    /** 借阅记录连表查询的公共 SELECT 片段 */
    private static final String BASE_SELECT =
            "SELECT br.*, u.username, u.real_name, b.bname AS title, b.author "
                    + "FROM borrow_records br "
                    + "LEFT JOIN users u ON br.user_id = u.id "
                    + "LEFT JOIN books b ON br.book_id = b.id ";

    // ======================================================================
    // 查询
    // ======================================================================

    /** 查询所有借阅记录（按借出日期倒序） */
    public List<BorrowRecord> findAll() {
        return DBUtil.query(BASE_SELECT + "ORDER BY br.borrow_date DESC", null, this::mapRow);
    }

    /** 根据 id 查询借阅记录 */
    public BorrowRecord findById(Integer id) {
        if (id == null) {
            return null;
        }
        List<BorrowRecord> list = DBUtil.query(BASE_SELECT + "WHERE br.id = ?", ps -> ps.setInt(1, id), this::mapRow);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 根据用户查询借阅记录 */
    public List<BorrowRecord> findByUserId(Integer userId) {
        return DBUtil.query(BASE_SELECT + "WHERE br.user_id = ? ORDER BY br.borrow_date DESC",
                ps -> ps.setInt(1, userId), this::mapRow);
    }

    /** 查询所有当前借阅（未归还） */
    public List<BorrowRecord> findAllCurrentBorrows() {
        return DBUtil.query(BASE_SELECT + "WHERE br.status IN ('borrowed', 'overdue') ORDER BY br.borrow_date DESC",
                null, this::mapRow);
    }

    /** 查询逾期记录 */
    public List<BorrowRecord> findOverdue() {
        return DBUtil.query(BASE_SELECT
                        + "WHERE br.status = 'overdue' OR (br.status = 'borrowed' AND br.due_date < CURDATE()) "
                        + "ORDER BY br.borrow_date DESC",
                null, this::mapRow);
    }

    /**
     * 搜索借阅记录（按读者姓名或图书名称）。
     *
     * @param keyword 关键词；传空返回全部
     */
    public List<BorrowRecord> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String cleaned = escapeLike(keyword.trim());
        if (cleaned.isEmpty()) {
            // 清洗后只剩通配符（如用户只输入 %），视同未输入关键词
            return findAll();
        }
        String pattern = "%" + cleaned + "%";
        return DBUtil.query(
                BASE_SELECT + "WHERE u.real_name LIKE ? OR b.bname LIKE ? "
                        + "ORDER BY br.borrow_date DESC",
                ps -> {
                    ps.setString(1, pattern);
                    ps.setString(2, pattern);
                }, this::mapRow);
    }

    // ======================================================================
    // 写入
    // ======================================================================

    /** 新增借阅记录 */
    public int add(BorrowRecord record) {
        String sql = "INSERT INTO borrow_records(user_id, book_id, borrow_date, due_date, status) VALUES(?,?,?,?,?)";
        return DBUtil.update(sql, ps -> {
            ps.setInt(1, record.getUserId());
            ps.setInt(2, record.getBookId());
            ps.setDate(3, toSqlDate(record.getBorrowDate()));
            ps.setDate(4, toSqlDate(record.getDueDate()));
            ps.setString(5, record.getStatus());
        });
    }

    /** 更新借阅记录 */
    public int update(BorrowRecord record) {
        String sql = "UPDATE borrow_records SET user_id=?, book_id=?, borrow_date=?, due_date=?, "
                + "return_date=?, status=?, fine_amount=? WHERE id=?";
        return DBUtil.update(sql, ps -> {
            ps.setInt(1, record.getUserId());
            ps.setInt(2, record.getBookId());
            ps.setDate(3, toSqlDate(record.getBorrowDate()));
            ps.setDate(4, toSqlDate(record.getDueDate()));
            if (record.getReturnDate() == null) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, toSqlDate(record.getReturnDate()));
            }
            ps.setString(6, record.getStatus());
            ps.setDouble(7, record.getFineAmount() == null ? 0d : record.getFineAmount());
            ps.setInt(8, record.getId());
        });
    }

    /** 还书：写入归还日期、状态改为 returned、记录罚款金额 */
    public int returnBook(Integer recordId, Date returnDate, double fineAmount) {
        return DBUtil.update(
                "UPDATE borrow_records SET return_date=?, status='returned', fine_amount=? WHERE id=?",
                ps -> {
                    ps.setDate(1, toSqlDate(returnDate));
                    ps.setDouble(2, fineAmount);
                    ps.setInt(3, recordId);
                });
    }

    /** 更新应还日期 */
    public int updateDueDate(int recordId, Date dueDate) {
        return DBUtil.update("UPDATE borrow_records SET due_date = ? WHERE id = ?",
                ps -> {
                    ps.setDate(1, toSqlDate(dueDate));
                    ps.setInt(2, recordId);
                });
    }

    /** 续借：应还日期顺延，续借次数 +1 */
    public int renew(Integer recordId, Date newDueDate) {
        return DBUtil.update(
                "UPDATE borrow_records SET due_date=?, renew_count=renew_count+1 WHERE id=?",
                ps -> {
                    ps.setDate(1, toSqlDate(newDueDate));
                    ps.setInt(2, recordId);
                });
    }

    /**
     * 批量把已过应还日期的 borrowed 记录标记为 overdue。
     *
     * @return 受影响行数
     */
    public int updateOverdueStatus() {
        return DBUtil.update(
                "UPDATE borrow_records SET status='overdue' WHERE status='borrowed' AND due_date < CURDATE()", null);
    }

    // ======================================================================
    // 统计
    // ======================================================================

    /** 统计用户当前借阅数量 */
    public int countCurrentBorrowsByUserId(Integer userId) {
        return (int) DBUtil.queryLong(
                "SELECT COUNT(*) FROM borrow_records WHERE user_id=? AND status IN ('borrowed','overdue')",
                ps -> ps.setInt(1, userId));
    }

    /** 统计用户逾期数量 */
    public int countOverdueByUserId(Integer userId) {
        return (int) DBUtil.queryLong(
                "SELECT COUNT(*) FROM borrow_records WHERE user_id=? AND status='overdue'",
                ps -> ps.setInt(1, userId));
    }

    /** 用户当前是否正在借阅某本书（未归还） */
    public boolean hasBorrowed(Integer userId, Integer bookId) {
        return DBUtil.queryLong(
                "SELECT COUNT(*) FROM borrow_records WHERE user_id=? AND book_id=? "
                        + "AND status IN ('borrowed','overdue')",
                ps -> {
                    ps.setInt(1, userId);
                    ps.setInt(2, bookId);
                }) > 0;
    }

    /**
     * 借阅排行榜。
     *
     * @return 每项为 [书名, 作者, 借阅次数]
     */
    public List<Object[]> getBorrowRanking(int limit) {
        return DBUtil.query(
                "SELECT b.bname AS title, b.author, COUNT(br.id) AS borrow_count "
                        + "FROM borrow_records br "
                        + "LEFT JOIN books b ON br.book_id = b.id "
                        + "GROUP BY br.book_id, b.bname, b.author "
                        + "ORDER BY borrow_count DESC LIMIT ?",
                ps -> ps.setInt(1, limit),
                rs -> new Object[]{rs.getString("title"), rs.getString("author"), rs.getInt("borrow_count")});
    }

    // ======================================================================
    // 内部工具
    // ======================================================================

    private static java.sql.Date toSqlDate(Date date) {
        return date == null ? null : new java.sql.Date(date.getTime());
    }

    /** 清理 LIKE 通配符（与 BookDao 保持一致：直接剔除 % _ \，跨数据库安全） */
    private static String escapeLike(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("%", "").replace("_", "").replace("\\", "");
    }

    /** 结果集 → BorrowRecord 实体，并计算逾期天数与应付罚款 */
    private BorrowRecord mapRow(ResultSet rs) throws SQLException {
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

        record.setUsername(rs.getString("username"));
        record.setRealName(rs.getString("real_name"));
        record.setBookTitle(rs.getString("title"));
        record.setBookAuthor(rs.getString("author"));

        // 逾期天数与应缴罚款（0.5 元/天）仅作展示用，不影响数据库存储值
        Date dueDate = record.getDueDate();
        if (dueDate != null
                && ("overdue".equals(record.getStatus()) || "borrowed".equals(record.getStatus()))) {
            long diff = System.currentTimeMillis() - dueDate.getTime();
            int days = (int) (diff / (1000L * 60 * 60 * 24));
            if (days > 0) {
                record.setOverdueDays(days);
                if (record.getFineAmount() == null || record.getFineAmount() == 0) {
                    record.setFineAmount(days * 0.5);
                }
            }
        }
        return record;
    }

    /**
     * 构造函数（保留无参形式）。
     */
    public BorrowDao() {
        // 无状态 DAO
    }

    /** 兜底：空列表常量，避免调用方拿到 null */
    public static List<BorrowRecord> emptyList() {
        return new ArrayList<>();
    }
}