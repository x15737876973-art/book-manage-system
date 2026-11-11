package com.book.servlet;

import com.book.dao.BorrowDao;
import com.book.dao.FineDao;
import com.book.dao.ReservationDao;
import com.book.entity.User;
import com.book.util.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 读者首页 Servlet：展示读者的借阅概览。
 *
 * <p>改造点：身份校验改为统一工具方法，异常统一处理。</p>
 */
@WebServlet("/reader/indexServlet")
public class ReaderIndexServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final BorrowDao borrowDao = new BorrowDao();
    private final ReservationDao reservationDao = new ReservationDao();
    private final FineDao fineDao = new FineDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = ServletUtils.requireRole(request, response, "reader");
        if (user == null) {
            return;
        }

        try {
            UserStat stat = new UserStat();
            stat.setBorrowedCount(borrowDao.countCurrentBorrowsByUserId(user.getId()));
            stat.setOverdueCount(borrowDao.countOverdueByUserId(user.getId()));
            stat.setReservationCount(reservationDao.countWaitingByUserId(user.getId()));
            stat.setUnpaidFine(fineDao.getUnpaidAmountByUserId(user.getId()));

            request.setAttribute("myStat", stat);
            request.getRequestDispatcher("/reader/index.jsp").forward(request, response);
        } catch (Exception e) {
            getServletContext().log("加载读者首页失败", e);
            request.setAttribute("error", "加载首页数据失败，请稍后重试");
            request.getRequestDispatcher("/admin/error.jsp").forward(request, response);
        }
    }

    /** 读者首页统计数据 */
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