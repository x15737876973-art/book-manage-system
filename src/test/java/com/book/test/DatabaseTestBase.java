package com.book.test;

import com.book.util.DBUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.Statement;

/**
 * 数据库测试基类。
 *
 * <h3>为什么用 H2 而不是真实 MySQL</h3>
 * <ul>
 *   <li><b>可重复</b>：每次运行都是全新内存库，不受本地数据影响；</li>
 *   <li><b>零依赖</b>：CI 或同学电脑上不需要装 MySQL，{@code mvn test} 直接跑；</li>
 *   <li><b>快</b>：内存数据库建表 + 插数据在毫秒级。</li>
 * </ul>
 *
 * <p>H2 以 {@code MODE=MySQL} 运行，兼容绝大多数 MySQL 语法。</p>
 *
 * <p>本类在每个测试类开始时：</p>
 * <ol>
 *   <li>通过系统属性把 {@link DBUtil} 指向 H2 内存库；</li>
 *   <li>创建 6 张业务表；</li>
 *   <li>每个测试方法执行前清空数据并重新填充种子数据，保证测试互相独立。</li>
 * </ol>
 */
public abstract class DatabaseTestBase {

    /** 测试库地址：内存库，连接关闭后销毁（DB_CLOSE_DELAY=-1 保证整个 JVM 期间存活） */
    private static final String TEST_JDBC_URL =
            "jdbc:h2:mem:booktest;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";

    @BeforeAll
    static void initDataSource() {
        // 必须在首次调用 DBUtil 之前设置，DBUtil 会读取这些系统属性
        System.setProperty("db.driver", "org.h2.Driver");
        System.setProperty("db.url", TEST_JDBC_URL);
        System.setProperty("db.username", "sa");
        System.setProperty("db.password", "");
        createSchema();
    }

    @AfterAll
    static void closeDataSource() {
        DBUtil.shutdown();
    }

    /** 每个测试方法前重置数据，保证用例之间互不影响 */
    @BeforeEach
    void resetData() throws Exception {
        try (Connection conn = DBUtil.requireConnection();
             Statement st = conn.createStatement()) {
            st.execute("DELETE FROM fine_records");
            st.execute("DELETE FROM reservations");
            st.execute("DELETE FROM borrow_records");
            st.execute("DELETE FROM books");
            st.execute("DELETE FROM users");
            st.execute("DELETE FROM categories");
            st.execute("ALTER TABLE books ALTER COLUMN id RESTART WITH 1");
            st.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
            st.execute("ALTER TABLE categories ALTER COLUMN id RESTART WITH 1");
        }
    }

    /** 建表（IF NOT EXISTS 保证重复调用安全） */
    private static void createSchema() {
        try (Connection conn = DBUtil.requireConnection();
             Statement st = conn.createStatement()) {

            st.execute("CREATE TABLE IF NOT EXISTS categories ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(50) NOT NULL UNIQUE, "
                    + "description VARCHAR(200))");

            st.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(50) NOT NULL UNIQUE, "
                    + "password VARCHAR(100) NOT NULL, "
                    + "role VARCHAR(20) NOT NULL, "
                    + "real_name VARCHAR(50), "
                    + "phone VARCHAR(20), "
                    + "email VARCHAR(100), "
                    + "status VARCHAR(20) DEFAULT 'active', "
                    + "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS books ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "bid VARCHAR(20) NOT NULL UNIQUE, "
                    + "isbn VARCHAR(30), "
                    + "bname VARCHAR(200) NOT NULL, "
                    + "author VARCHAR(100), "
                    + "publisher VARCHAR(100), "
                    + "publish_date DATE, "
                    + "price DECIMAL(10,2), "
                    + "category_id INT, "
                    + "stock INT DEFAULT 0, "
                    + "available_stock INT DEFAULT 0, "
                    + "location VARCHAR(50), "
                    + "description TEXT, "
                    + "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS borrow_records ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT, book_id INT, "
                    + "borrow_date DATE, due_date DATE, return_date DATE, "
                    + "status VARCHAR(20), "
                    + "renew_count INT DEFAULT 0, "
                    + "fine_amount DECIMAL(10,2) DEFAULT 0, "
                    + "is_paid TINYINT DEFAULT 0, "
                    + "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS fine_records ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT, borrow_record_id INT, "
                    + "amount DECIMAL(10,2), days INT, "
                    + "status VARCHAR(20) DEFAULT 'unpaid', "
                    + "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS reservations ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT, book_id INT, "
                    + "reserve_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "status VARCHAR(20) DEFAULT 'waiting')");

        } catch (Exception e) {
            throw new IllegalStateException("初始化测试数据库失败", e);
        }
    }

    /** 插入一条分类，返回 id */
    protected int insertCategory(String name, String desc) throws Exception {
        try (Connection conn = DBUtil.requireConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO categories(name, description) VALUES(?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, desc);
            ps.executeUpdate();
            try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    /**
     * 插入一本书。
     *
     * @param bid      书号
     * @param bname    书名
     * @param author   作者
     * @param category 分类 id
     * @param stock    总库存
     * @param available 可借数量
     */
    protected void insertBook(String bid, String bname, String author,
                              Integer category, int stock, int available) throws Exception {
        try (Connection conn = DBUtil.requireConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO books(bid, isbn, bname, author, publisher, price, "
                             + "category_id, stock, available_stock, location) VALUES(?,?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, bid);
            ps.setString(2, "978-" + bid);
            ps.setString(3, bname);
            ps.setString(4, author);
            ps.setString(5, "测试出版社");
            ps.setDouble(6, 50.0);
            ps.setInt(7, category);
            ps.setInt(8, stock);
            ps.setInt(9, available);
            ps.setString(10, "A区-1排");
            ps.executeUpdate();
        }
    }

    /** 插入一个用户，密码自动 BCrypt 加密 */
    protected int insertUser(String username, String plainPassword, String role) throws Exception {
        try (Connection conn = DBUtil.requireConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users(username, password, role, real_name, status) VALUES(?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, com.book.util.BCryptUtil.hashpw(plainPassword));
            ps.setString(3, role);
            ps.setString(4, username + "的名字");
            ps.setString(5, "active");
            ps.executeUpdate();
            try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }
}