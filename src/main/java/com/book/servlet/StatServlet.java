package com.book.servlet;

import com.book.dao.BookDao;
import com.book.dao.BorrowDao;
import com.book.dao.CategoryDao;
import com.book.dao.FineDao;
import com.book.dao.UserDao;
import com.book.entity.Category;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据统计Servlet
 */
@WebServlet("/admin/statServlet")
public class StatServlet extends HttpServlet {
    private BookDao bookDao = new BookDao();
    private BorrowDao borrowDao = new BorrowDao();
    private UserDao userDao = new UserDao();
    private FineDao fineDao = new FineDao();
    private CategoryDao categoryDao = new CategoryDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        String action = request.getParameter("action");
        
        if ("overview".equals(action)) {
            showOverview(request, response);
        } else if ("ranking".equals(action)) {
            showRanking(request, response);
        } else if ("overdue".equals(action)) {
            showOverdue(request, response);
        } else {
            showOverview(request, response);
        }
    }
    
    /**
     * 显示统计概览
     */
    private void showOverview(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 图书统计
        int totalBooks = bookDao.count(); // 图书种类数
        int totalStock = bookDao.countTotalStock(); // 总库存（册数）
        int availableStock = bookDao.countAvailableStock(); // 可借数量（册数）
        int borrowedStock = totalStock - availableStock; // 已借出（册数）
        
        // 用户统计
        int totalReaders = userDao.findReaders().size();
        
        // 借阅统计
        int totalBorrows = borrowDao.findAll().size(); // 历史借阅总数
        int currentBorrows = borrowDao.findAllCurrentBorrows().size(); // 当前借阅数
        int overdueCount = borrowDao.findOverdue().size(); // 逾期数
        
        // 罚款统计
        double totalFine = fineDao.getTotalUnpaidAmount();
        
        // 封装统计数据
        StatData stat = new StatData();
        stat.setTotalBooks(totalBooks);
        stat.setTotalUsers(totalReaders);
        stat.setTotalBorrows(totalBorrows);
        stat.setOverdueCount(overdueCount);
        stat.setTotalStock(totalStock);
        stat.setAvailableStock(availableStock);
        stat.setBorrowedStock(borrowedStock);
        stat.setCurrentBorrows(currentBorrows);
        stat.setTotalFine(totalFine);
        
        request.setAttribute("stat", stat);
        
        // 分类统计
        List<CategoryStat> categoryStats = new ArrayList<>();
        List<Category> categories = categoryDao.findAll();
        for (Category category : categories) {
            int totalCount = bookDao.countByCategoryId(category.getId());
            int availableCount = bookDao.countAvailableByCategoryId(category.getId());
            int borrowedCount = totalCount - availableCount;
            double percentage = totalStock > 0 ? (totalCount * 100.0 / totalStock) : 0;
            
            CategoryStat cs = new CategoryStat();
            cs.setCategoryName(category.getName());
            cs.setTotalCount(totalCount);
            cs.setAvailableCount(availableCount);
            cs.setBorrowedCount(borrowedCount);
            cs.setPercentage(Math.round(percentage * 100.0) / 100.0);
            
            categoryStats.add(cs);
        }
        request.setAttribute("categoryStats", categoryStats);
        
        request.getRequestDispatcher("/admin/statOverview.jsp").forward(request, response);
    }
    
    /**
     * 显示借阅排行
     */
    private void showRanking(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Object[]> ranking = borrowDao.getBorrowRanking(10);
        request.setAttribute("rankingList", ranking);
        request.getRequestDispatcher("/admin/statRanking.jsp").forward(request, response);
    }
    
    /**
     * 显示逾期名单
     */
    private void showOverdue(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 先更新逾期状态
        borrowDao.updateOverdueStatus();
        
        request.getRequestDispatcher("/admin/overdueList.jsp").forward(request, response);
    }
    
    /**
     * 统计数据内部类
     */
    public static class StatData {
        private int totalBooks;
        private int totalUsers;
        private int totalBorrows;
        private int overdueCount;
        private int totalStock;
        private int availableStock;
        private int borrowedStock;
        private int currentBorrows;
        private double totalFine;
        
        public int getTotalBooks() {
            return totalBooks;
        }
        
        public void setTotalBooks(int totalBooks) {
            this.totalBooks = totalBooks;
        }
        
        public int getTotalUsers() {
            return totalUsers;
        }
        
        public void setTotalUsers(int totalUsers) {
            this.totalUsers = totalUsers;
        }
        
        public int getTotalBorrows() {
            return totalBorrows;
        }
        
        public void setTotalBorrows(int totalBorrows) {
            this.totalBorrows = totalBorrows;
        }
        
        public int getOverdueCount() {
            return overdueCount;
        }
        
        public void setOverdueCount(int overdueCount) {
            this.overdueCount = overdueCount;
        }
        
        public int getTotalStock() {
            return totalStock;
        }
        
        public void setTotalStock(int totalStock) {
            this.totalStock = totalStock;
        }
        
        public int getAvailableStock() {
            return availableStock;
        }
        
        public void setAvailableStock(int availableStock) {
            this.availableStock = availableStock;
        }
        
        public int getBorrowedStock() {
            return borrowedStock;
        }
        
        public void setBorrowedStock(int borrowedStock) {
            this.borrowedStock = borrowedStock;
        }
        
        public int getCurrentBorrows() {
            return currentBorrows;
        }
        
        public void setCurrentBorrows(int currentBorrows) {
            this.currentBorrows = currentBorrows;
        }
        
        public double getTotalFine() {
            return totalFine;
        }
        
        public void setTotalFine(double totalFine) {
            this.totalFine = totalFine;
        }
    }
    
    /**
     * 分类统计内部类
     */
    public static class CategoryStat {
        private String categoryName;
        private int totalCount;
        private int availableCount;
        private int borrowedCount;
        private double percentage;
        
        public String getCategoryName() {
            return categoryName;
        }
        
        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }
        
        public int getTotalCount() {
            return totalCount;
        }
        
        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }
        
        public int getAvailableCount() {
            return availableCount;
        }
        
        public void setAvailableCount(int availableCount) {
            this.availableCount = availableCount;
        }
        
        public int getBorrowedCount() {
            return borrowedCount;
        }
        
        public void setBorrowedCount(int borrowedCount) {
            this.borrowedCount = borrowedCount;
        }
        
        public double getPercentage() {
            return percentage;
        }
        
        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }
}