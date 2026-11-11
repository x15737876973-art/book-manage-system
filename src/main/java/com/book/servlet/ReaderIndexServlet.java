package com.book.servlet;

import com.book.dao.BorrowDao;
import com.book.dao.FineDao;
import com.book.dao.ReservationDao;
import com.book.entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 读者首页Servlet - 提供统计数据
 */
@WebServlet("/reader/indexServlet")
public class ReaderIndexServlet extends HttpServlet {
    private BorrowDao borrowDao = new BorrowDao();
    private ReservationDao reservationDao = new ReservationDao();
    private FineDao fineDao = new FineDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 处理GET请求的中文参数
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 手动解析查询字符串并正确解码
        String queryString = request.getQueryString();
        if (queryString != null) {
            try {
                // 使用request的参数获取方法，它会自动处理编码
                String action = request.getParameter("action");

                // 打印调试信息
                System.out.println("=== GET请求调试信息 ===");
                System.out.println("原始queryString: " + queryString);
                System.out.println("action参数: " + action);

                // 将参数重新设置到request中
                if (action != null) {
                    request.setAttribute("action", action);
                }
            } catch (Exception e) {
                System.out.println("处理GET参数时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }

        doPost(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        
        if (user == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        // 获取当前用户的统计数据
        int borrowedCount = borrowDao.countCurrentBorrowsByUserId(user.getId());
        int overdueCount = borrowDao.countOverdueByUserId(user.getId());
        int reservationCount = reservationDao.countWaitingByUserId(user.getId());
        double unpaidFine = fineDao.getUnpaidAmountByUserId(user.getId());
        
        // 封装统计数据
        UserStat myStat = new UserStat();
        myStat.setBorrowedCount(borrowedCount);
        myStat.setOverdueCount(overdueCount);
        myStat.setReservationCount(reservationCount);
        myStat.setUnpaidFine(unpaidFine);
        
        request.setAttribute("myStat", myStat);
        request.getRequestDispatcher("/reader/index.jsp").forward(request, response);
    }
    
    /**
     * 用户统计数据内部类
     */
    public static class UserStat {
        private int borrowedCount;
        private int overdueCount;
        private int reservationCount;
        private double unpaidFine;
        
        public int getBorrowedCount() {
            return borrowedCount;
        }
        
        public void setBorrowedCount(int borrowedCount) {
            this.borrowedCount = borrowedCount;
        }
        
        public int getOverdueCount() {
            return overdueCount;
        }
        
        public void setOverdueCount(int overdueCount) {
            this.overdueCount = overdueCount;
        }
        
        public int getReservationCount() {
            return reservationCount;
        }
        
        public void setReservationCount(int reservationCount) {
            this.reservationCount = reservationCount;
        }
        
        public double getUnpaidFine() {
            return unpaidFine;
        }
        
        public void setUnpaidFine(double unpaidFine) {
            this.unpaidFine = unpaidFine;
        }
    }
}
