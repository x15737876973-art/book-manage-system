package com.book.listener;

import com.book.util.DBUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 应用启动监听器：启动时做一次数据库连通性与数据量自检。
 *
 * <p>改造点：使用 try-with-resources 管理连接，避免启动阶段连接泄漏；
 * 数据库异常不会阻断应用启动，只在日志中给出清晰提示。</p>
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("========================================");
        System.out.println("  图书管理系统启动中...");
        System.out.println("========================================");

        checkDatabaseStatus();

        System.out.println("========================================");
        System.out.println("  系统启动完成");
        System.out.println("========================================");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("图书管理系统已关闭");
    }

    /** 检查数据库连接与关键表的数据量 */
    private void checkDatabaseStatus() {
        try (Connection conn = DBUtil.requireConnection()) {
            int categories = count(conn, "categories");
            int users = count(conn, "users");
            int books = count(conn, "books");
            int borrows = count(conn, "borrow_records");

            System.out.println("[启动自检] 数据库连接正常");
            System.out.println("  分类：" + categories + " 个");
            System.out.println("  用户：" + users + " 个");
            System.out.println("  图书：" + books + " 本");
            System.out.println("  借阅记录：" + borrows + " 条");

            if (users == 0) {
                System.out.println("[启动自检] 提示：暂无用户，请以管理员身份访问 /initSystem 初始化");
            }
            if (books == 0) {
                System.out.println("[启动自检] 提示：暂无图书，可在管理员首页执行系统初始化");
            }
        } catch (Exception e) {
            System.out.println("[启动自检] 数据库检查失败：" + e.getMessage());
            System.out.println("[启动自检] 请确认 MySQL 已启动，并检查 db.properties 中的连接配置");
        }
    }

    private int count(Connection conn, String table) throws Exception {
        // 表名来自代码内置常量，非用户输入，安全
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM " + table);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}