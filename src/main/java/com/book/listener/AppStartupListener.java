package com.book.listener;

import com.book.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 应用启动监听器：启动时做一次数据库连通性与数据量自检，
 * 应用停止时关闭连接池。
 *
 * <p>数据库异常不会阻断应用启动（避免启动即失败难以排查），
 * 只在日志中给出清晰的排查提示。</p>
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(AppStartupListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("================ 图书管理系统启动中 ================");
        checkDatabaseStatus();
        log.info("================ 系统启动完成 ================");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("图书管理系统正在关闭，释放连接池...");
        DBUtil.shutdown();
        log.info("图书管理系统已关闭");
    }

    /** 检查数据库连接与关键表的数据量 */
    private void checkDatabaseStatus() {
        try (Connection conn = DBUtil.requireConnection()) {
            int categories = count(conn, "categories");
            int users = count(conn, "users");
            int books = count(conn, "books");
            int borrows = count(conn, "borrow_records");

            log.info("启动自检：数据库连接正常 | 分类 {} 个 | 用户 {} 个 | 图书 {} 本 | 借阅记录 {} 条",
                    categories, users, books, borrows);
            log.info("连接池状态：{}", DBUtil.poolStats());

            if (users == 0) {
                log.warn("启动自检：暂无用户，请以管理员身份访问 /initSystem 完成初始化");
            }
            if (books == 0) {
                log.warn("启动自检：暂无图书，可在管理员首页执行系统初始化");
            }
        } catch (Exception e) {
            log.error("启动自检：数据库检查失败 —— 请确认 MySQL 已启动，并检查 db.properties 中的连接配置", e);
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