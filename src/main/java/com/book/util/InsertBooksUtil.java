package com.book.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 示例图书导入工具。
 *
 * <h3>本次修复</h3>
 * <p>旧版本用 {@code Statement.executeUpdate("... VALUES('B014', '978-...', ...)")}
 * 直接把书名、作者等字符串拼进 SQL。虽然这些是内置常量，但只要将来改成
 * 从文件或用户输入读取，就会立刻变成 SQL 注入漏洞。<b>现统一改为
 * PreparedStatement 占位符</b>，并改为批量执行提升性能。</p>
 */
public final class InsertBooksUtil {

    /** 示例图书：{bid, isbn, 书名, 作者, 出版社, 出版日期, 价格, 分类id, 库存, 位置} */
    private static final String[][] SAMPLE_BOOKS = {
            {"B014", "978-7-5442-7153-6", "活着", "余华", "作家出版社", "2012-08-01", "35.00", "1", "20", "B区-1排-2架"},
            {"B015", "978-7-02-012845-0", "围城", "钱钟书", "人民文学出版社", "1991-02-01", "39.00", "1", "12", "B区-1排-3架"},
            {"B016", "978-7-5366-9293-0", "三体", "刘慈欣", "重庆出版社", "2008-01-01", "93.00", "1", "18", "B区-1排-3架"},
            {"B017", "978-7-5302-1938-8", "平凡的世界", "路遥", "北京十月文艺出版社", "2010-01-01", "99.00", "1", "16", "B区-1排-4架"},
            {"B018", "978-7-04-039661-2", "高等数学", "同济大学数学系", "高等教育出版社", "2014-07-01", "49.00", "2", "30", "C区-1排-1架"},
            {"B019", "978-7-04-028582-7", "大学物理", "程守洙", "高等教育出版社", "2016-12-01", "45.00", "2", "20", "C区-1排-1架"},
            {"B020", "978-7-04-021481-6", "线性代数", "同济大学数学系", "高等教育出版社", "2014-06-01", "28.00", "2", "25", "C区-1排-2架"},
            {"B021", "978-7-04-031234-9", "概率论与数理统计", "浙江大学", "高等教育出版社", "2015-03-01", "35.00", "2", "22", "C区-1排-2架"},
            {"B022", "978-7-5399-5436-9", "明朝那些事儿", "当年明月", "浙江人民出版社", "2011-12-01", "188.00", "3", "25", "D区-1排-1架"},
            {"B023", "978-7-101-05452-6", "万历十五年", "黄仁宇", "中华书局", "2007-01-01", "29.00", "3", "15", "D区-1排-1架"},
            {"B024", "978-7-101-00325-8", "史记", "司马迁", "中华书局", "2013-08-01", "128.00", "3", "10", "D区-1排-2架"},
            {"B025", "978-7-101-09876-5", "中国通史", "吕思勉", "中华书局", "2015-06-01", "98.00", "3", "12", "D区-1排-2架"},
            {"B026", "978-7-111-40701-0", "算法导论", "Thomas H.Cormen", "机械工业出版社", "2013-01-01", "128.00", "4", "8", "A区-3排-1架"},
            {"B027", "978-7-115-46812-5", "Linux命令行与Shell脚本编程", "Richard Blum", "人民邮电出版社", "2019-05-01", "79.00", "4", "10", "A区-3排-1架"},
            {"B028", "978-7-115-54538-2", "JavaScript高级程序设计", "Nicholas C.Zakas", "人民邮电出版社", "2020-03-01", "99.00", "4", "12", "A区-3排-2架"},
            {"B029", "978-7-115-42332-6", "MySQL必知必会", "Ben Forta", "人民邮电出版社", "2009-01-01", "49.00", "4", "15", "A区-3排-2架"},
            {"B030", "978-7-301-25649-8", "经济学原理", "N.Gregory Mankiw", "北京大学出版社", "2015-05-01", "98.00", "5", "10", "E区-1排-1架"},
            {"B031", "978-7-300-23456-7", "管理学", "Stephen P.Robbins", "中国人民大学出版社", "2017-01-01", "65.00", "5", "8", "E区-1排-1架"},
            {"B032", "978-7-5432-2789-4", "营销管理", "Philip Kotler", "格致出版社", "2016-04-01", "88.00", "5", "6", "E区-1排-2架"},
            {"B033", "978-7-5086-3456-2", "投资学", "Zvi Bodie", "中信出版社", "2018-09-01", "128.00", "5", "9", "E区-1排-2架"}
    };

    private static final String INSERT_SQL =
            "INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, "
                    + "category_id, stock, available_stock, location) VALUES(?,?,?,?,?,?,?,?,?,?,?)";

    private InsertBooksUtil() {
    }

    /**
     * 导入示例图书。若数据库中已有图书则跳过。
     *
     * @return 实际插入的条数
     */
    public static int insertBooks() {
        try (Connection conn = DBUtil.requireConnection()) {
            int existing = countBooks(conn);
            if (existing > 0) {
                System.out.println("[InsertBooksUtil] 数据库中已有 " + existing + " 本图书，跳过导入");
                return 0;
            }

            int inserted = 0;
            // ★ 预编译 + 批量执行：既安全又高效
            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
                for (String[] b : SAMPLE_BOOKS) {
                    ps.setString(1, b[0]);
                    ps.setString(2, b[1]);
                    ps.setString(3, b[2]);
                    ps.setString(4, b[3]);
                    ps.setString(5, b[4]);
                    ps.setDate(6, java.sql.Date.valueOf(b[5]));
                    ps.setDouble(7, Double.parseDouble(b[6]));
                    ps.setInt(8, Integer.parseInt(b[7]));
                    ps.setInt(9, Integer.parseInt(b[8]));
                    ps.setInt(10, Integer.parseInt(b[8]));
                    ps.setString(11, b[9]);
                    ps.addBatch();
                }
                int[] results = ps.executeBatch();
                for (int r : results) {
                    if (r > 0 || r == PreparedStatement.SUCCESS_NO_INFO) {
                        inserted++;
                    }
                }
            }
            System.out.println("[InsertBooksUtil] 成功导入 " + inserted + " 本图书");
            return inserted;

        } catch (SQLException e) {
            System.out.println("[InsertBooksUtil] 导入失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private static int countBooks(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM books");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** 命令行入口，便于脱离 Web 容器直接导入 */
    public static void main(String[] args) {
        int n = insertBooks();
        System.out.println("导入结果：" + n + " 条");
    }
}