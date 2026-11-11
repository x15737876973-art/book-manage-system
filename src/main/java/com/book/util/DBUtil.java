package com.book.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 数据库访问基础设施：<b>HikariCP 连接池</b> + 统一 JDBC 模板 + 事务支持。
 *
 * <h3>连接池（为什么必须用）</h3>
 * <p>裸用 {@code DriverManager.getConnection()} 时，每次获取连接都要
 * 经历 <b>TCP 三次握手 → MySQL 身份认证 → 会话初始化</b>，通常耗时
 * 5~20ms；高并发下会迅速耗尽数据库的 {@code max_connections}。</p>
 *
 * <p>HikariCP 通过<b>预建物理连接并复用</b>把获取连接的成本降到微秒级，
 * 同时提供：连接超时、空闲回收、最大生命周期、连接有效性检测等能力。
 * 它是 Spring Boot 2.x 之后的默认连接池，也是目前性能最好的实现。</p>
 *
 * <h3>连接配置的读取优先级（高 → 低）</h3>
 * <ol>
 *   <li>系统属性：{@code -Ddb.url=...}（IDEA 运行配置 VM options 里最方便）</li>
 *   <li>环境变量：{@code DB_URL} / {@code DB_USERNAME} / {@code DB_PASSWORD}</li>
 *   <li>配置文件：{@code classpath:db.properties}</li>
 *   <li>内置默认值（仅用于本地跑通演示）</li>
 * </ol>
 *
 * <h3>资源管理约定</h3>
 * <p>所有 DAO 都通过 {@link #query} / {@link #queryLong} / {@link #update}
 * 访问数据库，内部使用 <b>try-with-resources</b>，连接必归还池、
 * 语句与结果集必关闭。事务通过 {@link #inTransaction} 以
 * {@link ThreadLocal} 绑定连接实现。</p>
 */
public final class DBUtil {

    private static final Logger log = LoggerFactory.getLogger(DBUtil.class);

    private static final String CONFIG_FILE = "db.properties";

    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/book_manage?useSSL=false&serverTimezone=Asia/Shanghai"
                    + "&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USERNAME = "root";
    /** 默认密码故意留空：真实密码请写在 db.properties（不入库）或 DB_PASSWORD 环境变量里 */
    private static final String DEFAULT_PASSWORD = "";

    /** 连接池核心参数（可通过 db.pool.* 覆盖） */
    private static final int DEFAULT_POOL_SIZE = 10;
    private static final int DEFAULT_MIN_IDLE = 2;
    private static final long DEFAULT_CONN_TIMEOUT_MS = 10_000L;
    private static final long DEFAULT_MAX_LIFETIME_MS = 30 * 60 * 1000L;

    /** 数据源（进程内单例，由 HikariCP 管理物理连接） */
    private static volatile HikariDataSource dataSource;

    /** 当前线程绑定的事务连接 */
    private static final ThreadLocal<Connection> TX_CONNECTION = new ThreadLocal<>();

    private DBUtil() {
    }

    // ======================================================================
    // 数据源初始化
    // ======================================================================

    /**
     * 获取数据源（首次调用时初始化，双重检查锁定保证线程安全）。
     *
     * @return 已初始化好的 HikariCP 数据源
     */
    public static HikariDataSource getDataSource() {
        HikariDataSource ds = dataSource;
        if (ds == null) {
            synchronized (DBUtil.class) {
                ds = dataSource;
                if (ds == null) {
                    ds = createDataSource();
                    dataSource = ds;
                }
            }
        }
        return ds;
    }

    /** 根据配置创建连接池 */
    private static HikariDataSource createDataSource() {
        Properties props = loadProperties(CONFIG_FILE);
        if (props.isEmpty()) {
            log.warn("未找到 {}，使用默认连接配置或环境变量。"
                    + "自定义方式：把 src/main/resources/db.properties.example 复制为 db.properties 后修改。",
                    CONFIG_FILE);
        }

        String driver = getConfig(props, "db.driver", "DB_DRIVER", DEFAULT_DRIVER);
        String url = getConfig(props, "db.url", "DB_URL", DEFAULT_URL);
        String username = getConfig(props, "db.username", "DB_USERNAME", DEFAULT_USERNAME);
        String password = getConfig(props, "db.password", "DB_PASSWORD", DEFAULT_PASSWORD);

        HikariConfig cfg = new HikariConfig();
        cfg.setPoolName("BookManagePool");
        cfg.setDriverClassName(driver);
        cfg.setJdbcUrl(url);
        cfg.setUsername(username);
        cfg.setPassword(password);

        // 池容量：最大连接数 & 最小空闲连接
        cfg.setMaximumPoolSize(getIntConfig(props, "db.pool.maxSize",
                "DB_POOL_MAX", DEFAULT_POOL_SIZE));
        cfg.setMinimumIdle(getIntConfig(props, "db.pool.minIdle",
                "DB_POOL_MIN_IDLE", DEFAULT_MIN_IDLE));

        // 超时与回收
        cfg.setConnectionTimeout(getLongConfig(props, "db.pool.connectionTimeoutMs",
                "DB_POOL_CONN_TIMEOUT", DEFAULT_CONN_TIMEOUT_MS));
        cfg.setMaxLifetime(getLongConfig(props, "db.pool.maxLifetimeMs",
                "DB_POOL_MAX_LIFETIME", DEFAULT_MAX_LIFETIME_MS));
        cfg.setIdleTimeout(600_000L);

        // 启动时即建立最小连接数，让启动自检能第一时间暴露配置错误
        cfg.setInitializationFailTimeout(10_000L);

        // 允许在 JSP/Servlet 中通过 Tomcat 连接池以外的线程使用
        cfg.setRegisterMbeans(false);

        try {
            HikariDataSource ds = new HikariDataSource(cfg);
            log.info("连接池初始化成功: pool={}, url={}, maxPoolSize={}, minIdle={}",
                    cfg.getPoolName(), url, cfg.getMaximumPoolSize(), cfg.getMinimumIdle());
            return ds;
        } catch (Exception e) {
            log.error("连接池初始化失败: url={}, 请检查 MySQL 是否启动与 db.properties 配置", url, e);
            throw new IllegalStateException("数据库连接池初始化失败: " + e.getMessage(), e);
        }
    }

    /** 关闭连接池（应用停止或测试清理时调用） */
    public static void shutdown() {
        HikariDataSource ds = dataSource;
        if (ds != null) {
            synchronized (DBUtil.class) {
                ds = dataSource;
                if (ds != null) {
                    ds.close();
                    dataSource = null;
                    log.info("连接池已关闭");
                }
            }
        }
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
            log.warn("读取 {} 失败: {}", fileName, e.getMessage());
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

    private static int getIntConfig(Properties props, String key, String envKey, int defaultValue) {
        try {
            return Integer.parseInt(getConfig(props, key, envKey, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            log.warn("配置 {} 不是合法整数，使用默认值 {}", key, defaultValue);
            return defaultValue;
        }
    }

    private static long getLongConfig(Properties props, String key, String envKey, long defaultValue) {
        try {
            return Long.parseLong(getConfig(props, key, envKey, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            log.warn("配置 {} 不是合法整数，使用默认值 {}", key, defaultValue);
            return defaultValue;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // ======================================================================
    // 连接获取
    // ======================================================================

    /**
     * 从连接池获取连接。
     *
     * <p>若当前线程处于事务中，返回<b>事务连接</b>（调用方不得关闭）；
     * 否则从池中借出一个连接并返回（调用方负责归还）。</p>
     */
    public static Connection acquireConnection() {
        Connection tx = TX_CONNECTION.get();
        if (tx != null) {
            try {
                if (!tx.isClosed()) {
                    return tx;
                }
            } catch (SQLException ignored) {
                // 连接已失效，退化为从池中重新获取
            }
            TX_CONNECTION.remove();
        }
        return requireConnection();
    }

    /**
     * 从连接池借出连接；失败时抛异常。
     *
     * <p><b>调用方必须用 try-with-resources 关闭</b>，关闭动作只是把连接
     * <b>归还给池</b>，不会真正断开 TCP。</p>
     */
    public static Connection requireConnection() {
        try {
            Connection conn = getDataSource().getConnection();
            if (conn == null) {
                throw new IllegalStateException("连接池返回了 null 连接");
            }
            return conn;
        } catch (SQLException e) {
            log.error("从连接池获取连接失败", e);
            throw new IllegalStateException(
                    "无法从连接池获取连接，请检查 MySQL 服务状态与 db.properties 配置", e);
        }
    }

    /** 判断给定连接是否属于当前事务（是则调用方不应关闭它） */
    public static boolean isTransactional(Connection conn) {
        return conn != null && conn == TX_CONNECTION.get();
    }

    /** 释放连接：属于事务的连接不关闭，否则归还连接池 */
    public static void releaseConnection(Connection conn) {
        if (conn == null || isTransactional(conn)) {
            return;
        }
        closeQuietly(conn);
    }

    /** 静默关闭任意 AutoCloseable（关闭失败仅记日志） */
    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            log.debug("关闭资源失败: {}", e.getMessage());
        }
    }

    /** 连接池状态快照，便于健康检查页展示 */
    public static String poolStats() {
        HikariDataSource ds = dataSource;
        if (ds == null) {
            return "连接池未初始化";
        }
        return String.format("活跃=%d, 空闲=%d, 总数=%d, 等待线程=%d",
                ds.getHikariPoolMXBean().getActiveConnections(),
                ds.getHikariPoolMXBean().getIdleConnections(),
                ds.getHikariPoolMXBean().getTotalConnections(),
                ds.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }

    // ======================================================================
    // 查询模板
    // ======================================================================

    /**
     * 执行只读查询，返回映射后的结果列表。
     *
     * <p>内部使用 try-with-resources：连接归还池、语句与结果集关闭，
     * 即使中途抛异常也不例外。</p>
     *
     * @param sql    带 {@code ?} 占位符的 SQL
     * @param setter 参数绑定回调（可为 null）
     * @param mapper 单行映射
     * @param <T>    结果类型
     * @return 结果列表，永不为 null
     */
    public static <T> List<T> query(String sql, SqlBinder setter, RowMapper<T> mapper) {
        List<T> list = new ArrayList<>();
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
            log.error("数据库查询失败: sql={}, cause={}", sql, e.getMessage(), e);
            throw new IllegalStateException("数据库查询失败: " + e.getMessage(), e);
        } finally {
            if (!inTx) {
                closeQuietly(conn);
            }
        }
        return list;
    }

    /**
     * 执行返回单个数值的查询（COUNT / SUM 等）。
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
            log.error("数据库统计失败: sql={}, cause={}", sql, e.getMessage(), e);
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
            log.error("数据库更新失败: sql={}, cause={}", sql, e.getMessage(), e);
            throw new IllegalStateException("数据库更新失败: " + e.getMessage(), e);
        } finally {
            if (!inTx) {
                closeQuietly(conn);
            }
        }
    }

    // ======================================================================
    // 事务
    // ======================================================================

    /**
     * 在一个事务中执行多个写操作，任一失败则整体回滚。
     *
     * <p>事务期间连接被绑定到当前线程（{@link #TX_CONNECTION}），
     * 因此 DAO 内部调用的 {@code update/query} 会自动复用同一个连接，
     * 真正处于同一事务中——否则每个 DAO 方法各借一个连接，
     * 回滚只能撤销最后一条语句。</p>
     *
     * <p>支持嵌套：已在事务中时直接复用外层事务，不重复开启。</p>
     */
    public static void inTransaction(TransactionWork work) {
        Connection existing = TX_CONNECTION.get();
        if (existing != null) {
            try {
                work.doWork(existing);
                return;
            } catch (SQLException e) {
                log.error("嵌套事务执行失败", e);
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
            log.error("开启事务失败", e);
            throw new IllegalStateException("开启事务失败: " + e.getMessage(), e);
        }

        TX_CONNECTION.set(conn);
        try {
            work.doWork(conn);
            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
                log.warn("事务已回滚: {}", e.getMessage());
            } catch (SQLException rollbackEx) {
                log.error("事务回滚失败", rollbackEx);
            }
            throw e instanceof RuntimeException
                    ? (RuntimeException) e
                    : new IllegalStateException("事务执行失败: " + e.getMessage(), e);
        } finally {
            TX_CONNECTION.remove();
            try {
                conn.setAutoCommit(oldAutoCommit);
            } catch (SQLException ignored) {
                log.debug("恢复自动提交失败: {}", ignored.getMessage());
            }
            closeQuietly(conn);
        }
    }

    // ======================================================================
    // 函数式接口
    // ======================================================================

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