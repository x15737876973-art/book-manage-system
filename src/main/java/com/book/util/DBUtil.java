package com.book.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Properties;

/**
 * JDBC 连接工具类（配置在类加载时读取一次）。
 *
 * 连接配置的读取优先级（高 → 低）：
 *   1. 系统属性   -Ddb.url=...        （IDEA 运行配置的 VM options 里最方便）
 *   2. 环境变量   DB_URL / DB_USERNAME / DB_PASSWORD
 *   3. 配置文件   classpath:db.properties（即 src/main/resources/db.properties）
 *   4. 内置默认值 仅用于本地跑通演示；生产环境请务必使用 1~3 之一
 *
 * 注意：db.properties 不入库（.gitignore 已忽略），仓库中只保留 db.properties.example 模板。
 */
public class DBUtil {

    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/book_manage?useSSL=false&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USERNAME = "root";
    /**
     * 默认密码故意留空：真实密码请写在 db.properties（不入库）或 DB_PASSWORD 环境变量里。
     * 本地图省事可临时改成你自己数据库的密码，但提交前记得改回来。
     */
    private static final String DEFAULT_PASSWORD = "";

    private static final String DRIVER;
    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try {
            props = loadProperties("db.properties");
            if (props.isEmpty()) {
                System.out.println("[DBUtil] 未找到 db.properties，使用默认连接配置或环境变量。"
                        + "自定义方式：把 src/main/resources/db.properties.example 复制为 db.properties 后修改。");
            }
        } catch (Throwable t) {
            System.out.println("[DBUtil] 加载 db.properties 时出错，回退到默认配置：" + t.getMessage());
        }

        DRIVER = getConfig(props, "db.driver", "DB_DRIVER", DEFAULT_DRIVER);
        URL = getConfig(props, "db.url", "DB_URL", DEFAULT_URL);
        USERNAME = getConfig(props, "db.username", "DB_USERNAME", DEFAULT_USERNAME);
        PASSWORD = getConfig(props, "db.password", "DB_PASSWORD", DEFAULT_PASSWORD);
    }

    /** 读取 classpath 下的配置文件；文件不存在时返回空的 Properties */
    private static Properties loadProperties(String fileName) {
        Properties props = new Properties();
        ClassLoader cl = DBUtil.class.getClassLoader();
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
        }
        if (cl == null) {
            return props;
        }
        try (InputStream in = cl.getResourceAsStream(fileName)) {
            if (in != null) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            System.out.println("[DBUtil] 读取 " + fileName + " 失败：" + e.getMessage());
        }
        return props;
    }

    /** 依次从 系统属性 → 环境变量 → 配置文件 → 默认值 取值 */
    private static String getConfig(Properties props, String key, String envKey, String defaultValue) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            value = System.getenv(envKey);
        }
        if (isBlank(value)) {
            value = props.getProperty(key);
        }
        return isBlank(value) ? defaultValue : value.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    static {
        try {
            Class.forName(DRIVER);
            System.out.println("MySQL驱动加载成功！");
        } catch (ClassNotFoundException e) {
            System.out.println("错误：缺少MySQL驱动");
            e.printStackTrace();
        }
    }

    public static Connection getConn() {
        try {
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            if (conn != null) {
                return conn;
            }
            System.out.println("[DBUtil] 获取连接失败：返回 null");
        } catch (SQLException e) {
            System.out.println("[DBUtil] 获取MySQL连接失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 当前线程绑定的事务连接。
     *
     * <p>{@link #inTransaction} 执行期间，本线程内所有 {@code query/update/queryLong}
     * 都会<b>复用</b>这个连接，从而真正纳入同一个事务。
     * 没有事务时该值为 {@code null}，各方法各自取连接、用完即关（连接池语义）。</p>
     */
    private static final ThreadLocal<Connection> TX_CONNECTION = new ThreadLocal<>();

    /**
     * 获取一个"当前上下文可用"的连接。
     *
     * <p>若当前线程处于事务中，返回<b>事务连接</b>（调用方不得关闭它，
     * 关闭动作由 {@link #inTransaction} 负责）；否则新建连接并返回，
     * 由 try-with-resources 负责关闭。</p>
     *
     * @return 可用的连接
     */
    public static Connection acquireConnection() {
        Connection tx = TX_CONNECTION.get();
        if (tx != null) {
            try {
                if (!tx.isClosed()) {
                    return tx;
                }
            } catch (SQLException ignored) {
                // 连接已失效，退化为新建
            }
            TX_CONNECTION.remove();
        }
        return requireConnection();
    }

    /**
     * 判断给定连接是否属于当前事务（是则调用方不应关闭它）。
     */
    public static boolean isTransactional(Connection conn) {
        return conn != null && conn == TX_CONNECTION.get();
    }

    /**
     * 释放连接：属于事务的连接<b>不关闭</b>，否则关闭。
     *
     * <p>配合 {@link #acquireConnection()} 使用，保证事务内的语句不会误关共享连接。</p>
     */
    public static void releaseConnection(Connection conn) {
        if (conn == null || isTransactional(conn)) {
            return;
        }
        closeQuietly(conn);
    }

    /**
     * 获取连接，失败时抛出运行时异常（供 try-with-resources 使用）。
     *
     * <p>相比 {@link #getConn()} 返回 null 的写法，这里直接抛出异常，
     * 调用方的 try-with-resources 就一定能拿到非 null 连接，
     * 不会出现 {@code NullPointerException} 掩盖真实错误。</p>
     */
    public static Connection requireConnection() {
        Connection conn = getConn();
        if (conn == null) {
            throw new IllegalStateException("无法连接数据库，请检查 db.properties 配置与 MySQL 服务状态");
        }
        return conn;
    }

    /**
     * 关闭数据库连接（保留以兼容旧代码）。
     *
     * <p><b>新代码请直接使用 try-with-resources：</b></p>
     * <pre>
     *   String sql = "SELECT ... WHERE id = ?";
     *   try (Connection conn = DBUtil.requireConnection();
     *        PreparedStatement ps = conn.prepareStatement(sql)) {
     *       ps.setInt(1, id);
     *       try (ResultSet rs = ps.executeQuery()) {
     *           while (rs.next()) { ... }
     *       }
     *   }
     * </pre>
     * <p>连接、语句、结果集会在代码块结束时<b>自动关闭</b>，即使中途抛异常也不例外，
     * 从根本上杜绝连接泄漏。</p>
     *
     * @deprecated 使用 try-with-resources 替代
     */
    @Deprecated
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        closeQuietly(rs);
        closeQuietly(pstmt);
        closeQuietly(conn);
    }

    /**
     * @deprecated 使用 try-with-resources 替代
     */
    @Deprecated
    public static void close(Connection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }

    /**
     * @deprecated 使用 try-with-resources 替代
     */
    @Deprecated
    public static void close(Connection conn, Statement stmt) {
        closeQuietly(stmt);
        closeQuietly(conn);
    }

    /** 静默关闭任意 AutoCloseable，不抛异常 */
    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            // 关闭失败不影响业务结果，仅记录
            System.out.println("[DBUtil] 关闭资源失败: " + e.getMessage());
        }
    }

    /**
     * 在同一个连接上执行只读查询的简化模板。
     *
     * <p>把"获取连接 → 预编译 → 绑定参数 → 遍历结果 → 关闭资源"这套重复代码收敛到一处，
     * 所有 DAO 都通过它执行 SQL，既保证 PreparedStatement 全覆盖（防 SQL 注入），
     * 又保证资源必然释放。</p>
     *
     * @param sql    带占位符 ? 的 SQL
     * @param setter 参数绑定回调（可为 null）
     * @param mapper 单行结果映射
     * @param <T>    结果类型
     * @return 结果列表，永不为 null
     */
    public static <T> java.util.List<T> query(String sql,
                                              SqlBinder setter,
                                              RowMapper<T> mapper) {
        java.util.List<T> list = new java.util.ArrayList<>();
        Connection conn = acquireConnection();
        boolean inTx = isTransactional(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (setter != null) {
                setter.bind(ps);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("数据库查询失败: " + e.getMessage(), e);
        } finally {
            if (!inTx) {
                closeQuietly(conn);
            }
        }
        return list;
    }

    /**
     * 执行一条返回单个数值的查询（COUNT / SUM 等）。
     *
     * @return 查询到的数值；无结果或为 NULL 时返回 0
     */
    public static long queryLong(String sql, SqlBinder setter) {
        Connection conn = acquireConnection();
        boolean inTx = isTransactional(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (setter != null) {
                setter.bind(ps);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long value = rs.getLong(1);
                    return rs.wasNull() ? 0L : value;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("数据库统计失败: " + e.getMessage(), e);
        } finally {
            if (!inTx) {
                closeQuietly(conn);
            }
        }
        return 0L;
    }

    /**
     * 执行 INSERT / UPDATE / DELETE。
     *
     * @return 受影响的行数
     */
    public static int update(String sql, SqlBinder setter) {
        Connection conn = acquireConnection();
        boolean inTx = isTransactional(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (setter != null) {
                setter.bind(ps);
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("数据库更新失败: " + e.getMessage(), e);
        } finally {
            if (!inTx) {
                closeQuietly(conn);
            }
        }
    }

    /**
     * 在一个事务中执行多个写操作，任一失败则整体回滚。
     *
     * <p>用于"借书 + 扣库存"这类必须同时成功或同时失败的业务。</p>
     *
     * <p><b>实现要点：</b>事务期间把连接绑定到当前线程
     * （{@link #TX_CONNECTION}）。这样 DAO 内部调用的
     * {@code DBUtil.update/query} 会自动复用同一个连接，
     * 从而真正处于同一事务中——否则每个 DAO 方法各开一个连接，
     * 回滚只能撤销最后一条语句，达不到原子性。</p>
     */
    public static void inTransaction(TransactionWork work) {
        // 已在事务中：直接嵌套执行，复用外层事务
        Connection existing = TX_CONNECTION.get();
        if (existing != null) {
            try {
                work.doWork(existing);
                return;
            } catch (SQLException e) {
                throw new IllegalStateException("事务执行失败: " + e.getMessage(), e);
            }
        }

        Connection conn = requireConnection();
        boolean oldAutoCommit;
        try {
            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            closeQuietly(conn);
            throw new IllegalStateException("开启事务失败: " + e.getMessage(), e);
        }

        TX_CONNECTION.set(conn);
        try {
            work.doWork(conn);
            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("[DBUtil] 回滚失败: " + rollbackEx.getMessage());
            }
            throw e instanceof RuntimeException
                    ? (RuntimeException) e
                    : new IllegalStateException("事务执行失败: " + e.getMessage(), e);
        } finally {
            TX_CONNECTION.remove();
            try {
                conn.setAutoCommit(oldAutoCommit);
            } catch (SQLException ignored) {
                // 恢复自动提交失败不影响业务结果
            }
            closeQuietly(conn);
        }
    }

    /** 参数绑定回调 */
    @FunctionalInterface
    public interface SqlBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    /** 单行映射回调 */
    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /** 事务工作单元 */
    @FunctionalInterface
    public interface TransactionWork {
        void doWork(Connection conn) throws SQLException;
    }
}
