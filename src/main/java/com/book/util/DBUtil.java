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
                System.out.println("MySQL数据库连接成功！");
                return conn;
            } else {
                System.out.println("MySQL数据库连接失败：返回null");
            }
        } catch (SQLException e) {
            System.out.println("获取MySQL连接失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(Connection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }

    public static void close(Connection conn, Statement stmt) {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
