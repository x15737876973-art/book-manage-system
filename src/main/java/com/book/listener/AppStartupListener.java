package com.book.listener;

import com.book.util.DBUtil;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 应用启动监听器
 * 用于检查数据库状态
 */
@WebListener
public class AppStartupListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("========================================");
        System.out.println("图书管理系统启动中...");
        System.out.println("========================================");
        
        // 检查数据库状态
        checkDatabaseStatus();
        
        System.out.println("========================================");
        System.out.println("系统启动完成！");
        System.out.println("========================================");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("图书管理系统已关闭");
    }
    
    /**
     * 检查数据库状态
     */
    private void checkDatabaseStatus() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConn();
            
            // 检查分类数量
            pstmt = conn.prepareStatement("SELECT COUNT(*) FROM categories");
            rs = pstmt.executeQuery();
            rs.next();
            int categoryCount = rs.getInt(1);
            rs.close();
            pstmt.close();
            
            // 检查用户数量
            pstmt = conn.prepareStatement("SELECT COUNT(*) FROM users");
            rs = pstmt.executeQuery();
            rs.next();
            int userCount = rs.getInt(1);
            rs.close();
            pstmt.close();
            
            // 检查图书数量
            pstmt = conn.prepareStatement("SELECT COUNT(*) FROM books");
            rs = pstmt.executeQuery();
            rs.next();
            int bookCount = rs.getInt(1);
            rs.close();
            pstmt.close();
            
            System.out.println("数据库状态：");
            System.out.println("  - 分类数量：" + categoryCount);
            System.out.println("  - 用户数量：" + userCount);
            System.out.println("  - 图书数量：" + bookCount);
            
            if (bookCount == 0) {
                System.out.println("\n提示：数据库中没有图书数据");
                System.out.println("请访问管理员首页，点击'导入示例图书'按钮导入数据");
            }
            
        } catch (Exception e) {
            System.out.println("检查数据库状态失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }
}