package com.book.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 插入图书数据工具类
 */
public class InsertBooksUtil {
    
    public static void insertBooks() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            stmt = conn.createStatement();
            
            // 检查是否已有图书数据
            rs = stmt.executeQuery("SELECT COUNT(*) FROM books");
            rs.next();
            int bookCount = rs.getInt(1);
            rs.close();
            
            if (bookCount > 0) {
                System.out.println("========================================");
                System.out.println("数据库中已有 " + bookCount + " 本图书，跳过导入");
                System.out.println("========================================");
                return;
            }
            
            System.out.println("正在导入20本图书数据...");
            
            // 文学类图书 (category_id = 1)
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B014', '978-7-5442-7153-6', '活着', '余华', '作家出版社', '2012-08-01', 35.00, 1, 20, 20, 'B区-1排-2架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B015', '978-7-02-012845-0', '围城', '钱钟书', '人民文学出版社', '1991-02-01', 39.00, 1, 12, 12, 'B区-1排-3架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B016', '978-7-5366-9293-0', '三体', '刘慈欣', '重庆出版社', '2008-01-01', 93.00, 1, 18, 18, 'B区-1排-3架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B017', '978-7-5302-1938-8', '平凡的世界', '路遥', '北京十月文艺出版社', '2010-01-01', 99.00, 1, 16, 16, 'B区-1排-4架')");
            
            // 理工类图书 (category_id = 2)
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B018', '978-7-04-039661-2', '高等数学', '同济大学数学系', '高等教育出版社', '2014-07-01', 49.00, 2, 30, 30, 'C区-1排-1架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B019', '978-7-04-028582-7', '大学物理', '程守洙', '高等教育出版社', '2016-12-01', 45.00, 2, 20, 20, 'C区-1排-1架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B020', '978-7-04-021481-6', '线性代数', '同济大学数学系', '高等教育出版社', '2014-06-01', 28.00, 2, 25, 25, 'C区-1排-2架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B021', '978-7-04-031234-9', '概率论与数理统计', '浙江大学', '高等教育出版社', '2015-03-01', 35.00, 2, 22, 22, 'C区-1排-2架')");
            
            // 历史类图书 (category_id = 3)
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B022', '978-7-5399-5436-9', '明朝那些事儿', '当年明月', '浙江人民出版社', '2011-12-01', 188.00, 3, 25, 25, 'D区-1排-1架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B023', '978-7-101-05452-6', '万历十五年', '黄仁宇', '中华书局', '2007-01-01', 29.00, 3, 15, 15, 'D区-1排-1架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B024', '978-7-101-00325-8', '史记', '司马迁', '中华书局', '2013-08-01', 128.00, 3, 10, 10, 'D区-1排-2架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B025', '978-7-101-09876-5', '中国通史', '吕思勉', '中华书局', '2015-06-01', 98.00, 3, 12, 12, 'D区-1排-2架')");
            
            // 计算机类图书 (category_id = 4)
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B026', '978-7-111-40701-0', '算法导论', 'Thomas H.Cormen', '机械工业出版社', '2013-01-01', 128.00, 4, 8, 8, 'A区-3排-1架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B027', '978-7-115-46812-5', 'Linux命令行与Shell脚本编程', 'Richard Blum', '人民邮电出版社', '2019-05-01', 79.00, 4, 10, 10, 'A区-3排-1架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B028', '978-7-115-54538-2', 'JavaScript高级程序设计', 'Nicholas C.Zakas', '人民邮电出版社', '2020-03-01', 99.00, 4, 12, 12, 'A区-3排-2架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B029', '978-7-115-42332-6', 'MySQL必知必会', 'Ben Forta', '人民邮电出版社', '2009-01-01', 49.00, 4, 15, 15, 'A区-3排-2架')");
            
            // 经济管理类图书 (category_id = 5)
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B030', '978-7-301-25649-8', '经济学原理', 'N.Gregory Mankiw', '北京大学出版社', '2015-05-01', 98.00, 5, 10, 10, 'E区-1排-1架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B031', '978-7-300-23456-7', '管理学', 'Stephen P.Robbins', '中国人民大学出版社', '2017-01-01', 65.00, 5, 8, 8, 'E区-1排-1架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B032', '978-7-5432-2789-4', '营销管理', 'Philip Kotler', '格致出版社', '2016-04-01', 88.00, 5, 6, 6, 'E区-1排-2架')");
            stmt.executeUpdate("INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) " +
                "VALUES('B033', '978-7-5086-3456-2', '投资学', 'Zvi Bodie', '中信出版社', '2018-09-01', 128.00, 5, 9, 9, 'E区-1排-2架')");
            
            System.out.println("========================================");
            System.out.println("成功插入20本图书数据！");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.out.println("插入图书数据失败");
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt);
        }
    }
    
    public static void main(String[] args) {
        insertBooks();
    }
}