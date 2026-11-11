package com.book.dao;

import com.book.entity.Book;
import com.book.entity.PageResult;
import com.book.util.DBUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * 图书数据访问层。
 *
 * <h3>安全与资源说明</h3>
 * <ul>
 *   <li>所有 SQL 一律使用 {@link PreparedStatement} + {@code ?} 占位符，
 *       用户输入只作为参数绑定，<b>不参与 SQL 文本拼接</b>，彻底杜绝 SQL 注入；</li>
 *   <li>所有数据库操作走 {@link DBUtil#query} / {@link DBUtil#update} / {@link DBUtil#queryLong}，
 *       内部使用 try-with-resources，Connection / PreparedStatement / ResultSet 必然关闭。</li>
 * </ul>
 */
public class BookDao {

    private static final String BASE_SELECT =
            "SELECT b.*, c.name AS category_name FROM books b "
                    + "LEFT JOIN categories c ON b.category_id = c.id ";

    /** 图书排序：按 id 升序，与旧版行为保持一致 */
    private static final String ORDER_BY_ID = " ORDER BY b.id";

    // ======================================================================
    // 查询
    // ======================================================================

    /**
     * 查询所有图书（带分类名称）。
     *
     * <p>注意：图书量大时应使用 {@link #findPage} 分页查询。</p>
     */
    public List<Book> findAll() {
        return DBUtil.query(BASE_SELECT + ORDER_BY_ID, null, this::mapRow);
    }

    /**
     * 分页查询图书（可按分类筛选）。
     *
     * @param pageNumber 页码，从 1 开始
     * @param pageSize   每页条数
     * @param categoryId 分类 id，null 表示全部分类
     */
    public PageResult<Book> findPage(int pageNumber, int pageSize, Integer categoryId) {
        int safePage = Math.max(pageNumber, 1);
        int safeSize = pageSize < 1 ? 10 : pageSize;
        long offset = (long) (safePage - 1) * safeSize;

        String where = categoryId == null ? "" : " WHERE b.category_id = ? ";
        String countSql = "SELECT COUNT(*) FROM books b" + where;
        String dataSql = BASE_SELECT + where + ORDER_BY_ID + " LIMIT ? OFFSET ?";

        long total = (categoryId == null)
                ? DBUtil.queryLong(countSql, null)
                : DBUtil.queryLong(countSql, ps -> ps.setInt(1, categoryId));

        List<Book> records = DBUtil.query(dataSql, ps -> {
            int idx = 1;
            if (categoryId != null) {
                ps.setInt(idx++, categoryId);
            }
            ps.setInt(idx++, safeSize);
            ps.setLong(idx, offset);
        }, this::mapRow);

        return new PageResult<>(records, safePage, safeSize, total);
    }

    /**
     * 分页模糊搜索（书名 / 作者 / ISBN，可按分类筛选）。
     *
     * @param keyword    关键词，null 或空白表示不过滤
     * @param categoryId 分类 id，null 表示全部分类
     */
    public PageResult<Book> searchPage(String keyword, Integer categoryId, int pageNumber, int pageSize) {
        int safePage = Math.max(pageNumber, 1);
        int safeSize = pageSize < 1 ? 10 : pageSize;
        long offset = (long) (safePage - 1) * safeSize;

        boolean hasKeywordRaw = keyword != null && !keyword.trim().isEmpty();
        // 清洗后若只剩通配符（如用户只输入了 %），视同"无有效关键词"
        String cleaned = hasKeywordRaw ? escapeLike(keyword.trim()) : "";
        final boolean hasKeyword = hasKeywordRaw && !cleaned.isEmpty();
        final String likeKeyword = hasKeyword ? "%" + cleaned + "%" : null;

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        if (hasKeyword) {
            // 已由 escapeLike() 剔除 % _ \ 等通配符，无需 ESCAPE 子句（跨数据库兼容）
            where.append(" AND (b.bname LIKE ? OR b.author LIKE ? ")
                 .append(" OR b.isbn LIKE ? OR b.bid LIKE ?) ");
        }
        if (categoryId != null) {
            where.append(" AND b.category_id = ? ");
        }

        String countSql = "SELECT COUNT(*) FROM books b" + where;
        String dataSql = BASE_SELECT + where + ORDER_BY_ID + " LIMIT ? OFFSET ?";

        DBUtil.SqlBinder bindConditions = ps -> {
            int idx = 1;
            if (hasKeyword) {
                for (int i = 0; i < 4; i++) {
                    ps.setString(idx++, likeKeyword);
                }
            }
            if (categoryId != null) {
                ps.setInt(idx, categoryId);
            }
        };

        long total = DBUtil.queryLong(countSql, bindConditions);

        List<Book> records = DBUtil.query(dataSql, ps -> {
            bindConditions.bind(ps);
            int idx = 1;
            if (hasKeyword) {
                idx += 4;
            }
            if (categoryId != null) {
                idx++;
            }
            ps.setInt(idx++, safeSize);
            ps.setLong(idx, offset);
        }, this::mapRow);

        return new PageResult<>(records, safePage, safeSize, total);
    }

    /** 根据 id 查询图书 */
    public Book findById(Integer id) {
        if (id == null) {
            return null;
        }
        List<Book> list = DBUtil.query(BASE_SELECT + " WHERE b.id = ?", ps -> ps.setInt(1, id), this::mapRow);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 根据图书编号（bid）查询 */
    public Book findByBid(String bid) {
        if (bid == null || bid.trim().isEmpty()) {
            return null;
        }
        List<Book> list = DBUtil.query(BASE_SELECT + " WHERE b.bid = ?", ps -> ps.setString(1, bid.trim()), this::mapRow);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 根据分类查询图书 */
    public List<Book> findByCategoryId(Integer categoryId) {
        return DBUtil.query(BASE_SELECT + " WHERE b.category_id = ?" + ORDER_BY_ID,
                ps -> ps.setInt(1, categoryId), this::mapRow);
    }

    /** 搜索图书（按书名、作者）—— 兼容旧调用 */
    public List<Book> searchBooks(String keyword) {
        return searchBooks(keyword, null);
    }

    /** 多条件搜索（书名/作者 + 分类）—— 兼容旧调用 */
    public List<Book> searchBooks(String keyword, Integer categoryId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return categoryId == null ? findAll() : findByCategoryId(categoryId);
        }
        String pattern = "%" + escapeLike(keyword.trim()) + "%";
        if (categoryId == null) {
            return DBUtil.query(
                    BASE_SELECT + " WHERE (b.bname LIKE ? OR b.author LIKE ?)" + ORDER_BY_ID,
                    ps -> {
                        ps.setString(1, pattern);
                        ps.setString(2, pattern);
                    }, this::mapRow);
        }
        return DBUtil.query(
                BASE_SELECT + " WHERE (b.bname LIKE ? OR b.author LIKE ?) "
                        + "AND b.category_id = ?" + ORDER_BY_ID,
                ps -> {
                    ps.setString(1, pattern);
                    ps.setString(2, pattern);
                    ps.setInt(3, categoryId);
                }, this::mapRow);
    }

    /**
     * 供智能问答（RAG）使用的图书检索：按多个关键词做 OR 模糊匹配，
     * 按可借数量降序返回，最多 limit 条。
     *
     * @param keywords 已切分的关键词列表（非空；空列表返回空结果）
     * @param limit    返回条数上限
     */
    public List<Book> searchForAgent(List<String> keywords, int limit) {
        // 无关键词 → 返回空（表示"没有检索到相关图书"，由上层决定是否兜底推荐）
        if (keywords == null || keywords.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE ");
        List<String> usable = new java.util.ArrayList<>();
        for (String kw : keywords) {
            if (kw != null && !escapeLike(kw).isEmpty()) {
                usable.add(kw);
            }
        }
        if (usable.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        for (int i = 0; i < usable.size(); i++) {
            if (i > 0) {
                sql.append(" OR ");
            }
            sql.append("(b.bname LIKE ? OR b.author LIKE ? ")
               .append("OR b.publisher LIKE ? OR c.name LIKE ? ")
               .append("OR b.isbn LIKE ? OR b.bid LIKE ?)");
        }
        // 精确命中书名/作者的排前面，其次看可借数量
        sql.append(" ORDER BY (CASE WHEN b.bname LIKE ? THEN 0 ELSE 1 END), ")
           .append("b.available_stock DESC, b.id LIMIT ?");

        return DBUtil.query(sql.toString(), ps -> {
            int idx = 1;
            for (String kw : usable) {
                String pattern = "%" + escapeLike(kw) + "%";
                for (int i = 0; i < 6; i++) {
                    ps.setString(idx++, pattern);
                }
            }
            ps.setString(idx++, "%" + escapeLike(usable.get(0)) + "%");
            ps.setInt(idx, limit);
        }, this::mapRow);
    }

    /**
     * 兜底推荐：按可借数量降序返回若干本书。
     *
     * <p>用于用户只说了"你好""推荐点书"这类没有具体书名/作者的问题时，
     * 给出一些有库存的书作为选读建议。</p>
     *
     * @param limit 返回条数上限
     */
    public List<Book> recommendBest(int limit) {
        return DBUtil.query(
                BASE_SELECT + " WHERE b.available_stock > 0 ORDER BY b.available_stock DESC, b.id LIMIT ?",
                ps -> ps.setInt(1, limit), this::mapRow);
    }

    // ======================================================================
    // 写入
    // ======================================================================

    /** 新增图书（完整字段） */
    public int addBook(Book book) {
        String sql = "INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, "
                + "category_id, stock, available_stock, location, description) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        return DBUtil.update(sql, ps -> {
            ps.setString(1, book.getBid());
            setNullableString(ps, 2, book.getIsbn());
            ps.setString(3, book.getBname());
            setNullableString(ps, 4, book.getAuthor());
            setNullableString(ps, 5, book.getPublisher());
            setNullableDate(ps, 6, book.getPublishDate());
            ps.setDouble(7, book.getPrice() != null ? book.getPrice() : 0d);
            setNullableInt(ps, 8, book.getCategoryId());
            int stock = book.getStock() != null ? book.getStock() : 0;
            ps.setInt(9, stock);
            ps.setInt(10, book.getAvailableStock() != null ? book.getAvailableStock() : stock);
            setNullableString(ps, 11, book.getLocation());
            setNullableString(ps, 12, book.getDescription());
        });
    }

    /** 兼容旧方法名 */
    public int add(Book book) {
        return addBook(book);
    }

    /** 修改图书（完整字段，按 bid 定位） */
    public int update(Book book) {
        String sql = "UPDATE books SET isbn=?, bname=?, author=?, publisher=?, publish_date=?, price=?, "
                + "category_id=?, stock=?, available_stock=?, location=?, description=? WHERE bid=?";
        return DBUtil.update(sql, ps -> {
            setNullableString(ps, 1, book.getIsbn());
            ps.setString(2, book.getBname());
            setNullableString(ps, 3, book.getAuthor());
            setNullableString(ps, 4, book.getPublisher());
            setNullableDate(ps, 5, book.getPublishDate());
            ps.setDouble(6, book.getPrice() != null ? book.getPrice() : 0d);
            setNullableInt(ps, 7, book.getCategoryId());
            ps.setInt(8, book.getStock() != null ? book.getStock() : 0);
            ps.setInt(9, book.getAvailableStock() != null ? book.getAvailableStock() : 0);
            setNullableString(ps, 10, book.getLocation());
            setNullableString(ps, 11, book.getDescription());
            ps.setString(12, book.getBid());
        });
    }

    /** 兼容旧方法名 */
    public int updateBook(Book book) {
        return update(book);
    }

    /** 删除图书 */
    public int delete(String bid) {
        return DBUtil.update("DELETE FROM books WHERE bid = ?", ps -> ps.setString(1, bid));
    }

    /** 兼容旧方法名 */
    public int deleteBook(String bid) {
        return delete(bid);
    }

    /** 借书：可借数量 -1（带库存判断，返回 0 表示库存不足） */
    public int decreaseAvailableStock(Integer id) {
        return DBUtil.update(
                "UPDATE books SET available_stock = available_stock - 1 WHERE id = ? AND available_stock > 0",
                ps -> ps.setInt(1, id));
    }

    /** 还书：可借数量 +1 */
    public int increaseAvailableStock(Integer id) {
        return DBUtil.update("UPDATE books SET available_stock = available_stock + 1 WHERE id = ?",
                ps -> ps.setInt(1, id));
    }

    // ======================================================================
    // 统计
    // ======================================================================

    /** 图书种类数 */
    public int count() {
        return (int) DBUtil.queryLong("SELECT COUNT(*) FROM books", null);
    }

    /** 总库存（册） */
    public int countTotalStock() {
        return (int) DBUtil.queryLong("SELECT COALESCE(SUM(stock), 0) FROM books", null);
    }

    /** 可借数量（册） */
    public int countAvailableStock() {
        return (int) DBUtil.queryLong("SELECT COALESCE(SUM(available_stock), 0) FROM books", null);
    }

    /** 分类下的图书种类数 */
    public int countByCategoryId(Integer categoryId) {
        return (int) DBUtil.queryLong("SELECT COUNT(*) FROM books WHERE category_id = ?",
                ps -> ps.setInt(1, categoryId));
    }

    /** 分类下的可借数量 */
    public int countAvailableByCategoryId(Integer categoryId) {
        return (int) DBUtil.queryLong("SELECT COALESCE(SUM(available_stock), 0) FROM books WHERE category_id = ?",
                ps -> ps.setInt(1, categoryId));
    }

    // ======================================================================
    // 内部工具
    // ======================================================================

    /**
     * 清理 LIKE 关键字。
     *
     * <p>用户输入中的 {@code %} {@code _} {@code \} 是 LIKE 的通配符 / 转义符。</p>
     * <ul>
     *   <li><b>性能</b>：输入单个 {@code %} 会退化成全表扫描；</li>
     *   <li><b>兼容性</b>：用 {@code ESCAPE '\\'} 做转义时，MySQL 与 H2 对
     *       字符串中反斜杠的解析层级不同，容易报 {@code Error in LIKE ESCAPE}。</li>
     * </ul>
     * <p>因此这里直接<b>剔除</b>这些特殊字符。</p>
     *
     * <p><b>注意</b>：若剔除后字符串变为空（例如用户只输入了一个 {@code %}），
     * 调用方必须把"无有效关键词"当作"无关键词"处理，否则
     * {@code "%%"} 会匹配全部记录。本方法返回空串，由调用方判断。</p>
     */
    static String escapeLike(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("%", "").replace("_", "").replace("\\", "");
    }

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isEmpty()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    private static void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private static void setNullableDate(PreparedStatement ps, int index, java.util.Date value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, new java.sql.Date(value.getTime()));
        }
    }

    /** 结果集 → Book 实体 */
    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setBid(rs.getString("bid"));
        b.setIsbn(rs.getString("isbn"));
        b.setBname(rs.getString("bname"));
        b.setAuthor(rs.getString("author"));
        b.setPublisher(rs.getString("publisher"));
        b.setPublishDate(rs.getDate("publish_date"));
        b.setPrice(rs.getDouble("price"));
        int categoryId = rs.getInt("category_id");
        b.setCategoryId(rs.wasNull() ? null : categoryId);
        b.setCategoryName(rs.getString("category_name"));
        b.setStock(rs.getInt("stock"));
        b.setAvailableStock(rs.getInt("available_stock"));
        b.setLocation(rs.getString("location"));
        b.setDescription(rs.getString("description"));
        b.setCreateTime(rs.getTimestamp("create_time"));
        return b;
    }
}