package com.book.servlet;

import com.book.dao.*;
import com.book.entity.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 读者借阅操作Servlet
 */
@WebServlet("/reader/borrowOpServlet")
public class BorrowOpServlet extends HttpServlet {
    private BorrowDao borrowDao = new BorrowDao();
    private BookDao bookDao = new BookDao();
    private ReservationDao reservationDao = new ReservationDao();
    private FineDao fineDao = new FineDao();
    
    // 借阅期限（天）
    private static final int BORROW_DAYS = 30;
    // 最大续借次数
    private static final int MAX_RENEW_COUNT = 2;
    // 每日罚款金额
    private static final double FINE_PER_DAY = 0.5;
    
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
        request.setCharacterEncoding("utf-8");
        String action = request.getParameter("action");
        
        if ("myBorrows".equals(action)) {
            showMyBorrows(request, response);
        } else if ("borrow".equals(action)) {
            borrowBook(request, response);
        } else if ("renew".equals(action)) {
            renewBook(request, response);
        } else if ("reserve".equals(action)) {
            reserveBook(request, response);
        } else if ("myReservations".equals(action)) {
            showMyReservations(request, response);
        } else if ("cancelReservation".equals(action)) {
            cancelReservation(request, response);
        } else if ("myFines".equals(action)) {
            showMyFines(request, response);
        } else {
            showMyBorrows(request, response);
        }
    }
    
    /**
     * 查看我的借阅记录
     */
    private void showMyBorrows(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        
        if (user == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        List<BorrowRecord> allRecords = borrowDao.findByUserId(user.getId());
        
        // 分离当前借阅和历史记录
        List<BorrowRecord> borrowList = new ArrayList<>();
        List<BorrowRecord> historyList = new ArrayList<>();
        
        for (BorrowRecord record : allRecords) {
            if ("borrowed".equals(record.getStatus()) || "overdue".equals(record.getStatus())) {
                borrowList.add(record);
            } else {
                historyList.add(record);
            }
        }
        
        request.setAttribute("borrowList", borrowList);
        request.setAttribute("historyList", historyList);
        request.getRequestDispatcher("/reader/myBorrows.jsp").forward(request, response);
    }
    
    /**
     * 借书（自助借书）
     */
    private void borrowBook(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        
        if (user == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        String bookIdStr = request.getParameter("bookId");
        
        if (bookIdStr == null) {
            request.setAttribute("msg", "参数错误！");
            showMyBorrows(request, response);
            return;
        }
        
        Integer bookId = Integer.parseInt(bookIdStr);
        
        // 检查是否当前正在借阅此书（未归还）
        List<BorrowRecord> currentBorrows = borrowDao.findByUserId(user.getId());
        boolean alreadyBorrowed = false;
        for (BorrowRecord r : currentBorrows) {
            if (r.getBookId().equals(bookId) && ("borrowed".equals(r.getStatus()) || "overdue".equals(r.getStatus()))) {
                alreadyBorrowed = true;
                break;
            }
        }
        if (alreadyBorrowed) {
            request.setAttribute("msg", "您已借阅此书，不能重复借阅！");
            showMyBorrows(request, response);
            return;
        }
        
        // 检查库存
        Book book = bookDao.findById(bookId);
        if (book == null || book.getAvailableStock() <= 0) {
            request.setAttribute("msg", "该图书库存不足！");
            showMyBorrows(request, response);
            return;
        }
        
        // 检查是否有未支付的罚款
        double unpaidFine = fineDao.getUnpaidAmountByUserId(user.getId());
        if (unpaidFine > 0) {
            request.setAttribute("msg", "您有未支付的罚款，请先缴纳罚款！");
            showMyFines(request, response);
            return;
        }
        
        // 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(user.getId());
        record.setBookId(bookId);
        record.setBorrowDate(new Date(System.currentTimeMillis()));
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, BORROW_DAYS);
        record.setDueDate(new Date(cal.getTimeInMillis()));
        record.setStatus("borrowed");
        record.setRenewCount(0);
        record.setFineAmount(0.0);
        record.setIsPaid(false);
        
        int row = borrowDao.add(record);
        if (row > 0) {
            // 减少可借数量
            bookDao.decreaseAvailableStock(bookId);
            
            // 如果有预约记录，标记为已完成
            Reservation reservation = reservationDao.findWaitingByUserAndBook(user.getId(), bookId);
            if (reservation != null) {
                reservationDao.complete(reservation.getId());
            }
            
            request.setAttribute("msg", "借书成功！到期日期：" + cal.getTime());
        } else {
            request.setAttribute("msg", "借书失败！");
        }
        
        showMyBorrows(request, response);
    }
    
    /**
     * 续借
     */
    private void renewBook(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        
        if (user == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        String recordIdStr = request.getParameter("recordId");
        
        if (recordIdStr == null) {
            showMyBorrows(request, response);
            return;
        }
        
        Integer recordId = Integer.parseInt(recordIdStr);
        BorrowRecord record = borrowDao.findById(recordId);
        
        if (record == null || !record.getUserId().equals(user.getId())) {
            request.setAttribute("msg", "记录不存在！");
            showMyBorrows(request, response);
            return;
        }
        
        // 检查续借次数
        if (record.getRenewCount() >= MAX_RENEW_COUNT) {
            request.setAttribute("msg", "已达到最大续借次数！");
            showMyBorrows(request, response);
            return;
        }
        
        // 检查是否逾期
        if (new Date(System.currentTimeMillis()).after(record.getDueDate())) {
            request.setAttribute("msg", "图书已逾期，不能续借！");
            showMyBorrows(request, response);
            return;
        }
        
        // 计算新的到期日期
        Calendar cal = Calendar.getInstance();
        cal.setTime(record.getDueDate());
        cal.add(Calendar.DAY_OF_MONTH, BORROW_DAYS);
        
        int row = borrowDao.renew(recordId, new Date(cal.getTimeInMillis()));
        if (row > 0) {
            request.setAttribute("msg", "续借成功！新到期日期：" + cal.getTime());
        } else {
            request.setAttribute("msg", "续借失败！");
        }
        
        showMyBorrows(request, response);
    }
    
    /**
     * 预约图书
     */
    private void reserveBook(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        
        if (user == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        String bookIdStr = request.getParameter("bookId");
        
        if (bookIdStr == null) {
            request.setAttribute("msg", "参数错误！");
            showMyReservations(request, response);
            return;
        }
        
        Integer bookId = Integer.parseInt(bookIdStr);
        
        // 检查是否已借阅此书
        if (borrowDao.hasBorrowed(user.getId(), bookId)) {
            request.setAttribute("msg", "您已借阅此书，无需预约！");
            showMyReservations(request, response);
            return;
        }
        
        // 检查是否已预约
        if (reservationDao.findWaitingByUserAndBook(user.getId(), bookId) != null) {
            request.setAttribute("msg", "您已预约此书！");
            showMyReservations(request, response);
            return;
        }
        
        // 创建预约记录
        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setBookId(bookId);
        reservation.setStatus("waiting");
        
        int row = reservationDao.add(reservation);
        if (row > 0) {
            request.setAttribute("msg", "预约成功！");
        } else {
            request.setAttribute("msg", "预约失败！");
        }
        
        showMyReservations(request, response);
    }
    
    /**
     * 查看我的预约
     */
    private void showMyReservations(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        
        if (user == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        List<Reservation> reservations = reservationDao.findByUserId(user.getId());
        request.setAttribute("reservationList", reservations);
        request.getRequestDispatcher("/reader/myReservations.jsp").forward(request, response);
    }
    
    /**
     * 取消预约
     */
    private void cancelReservation(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        
        if (user == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        String reservationIdStr = request.getParameter("reservationId");
        
        if (reservationIdStr != null) {
            reservationDao.cancel(Integer.parseInt(reservationIdStr));
            request.setAttribute("msg", "取消预约成功！");
        }
        
        showMyReservations(request, response);
    }
    
    /**
     * 查看我的罚款
     */
    private void showMyFines(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        
        if (user == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        List<FineRecord> fines = fineDao.findByUserId(user.getId());
        double unpaidAmount = fineDao.getUnpaidAmountByUserId(user.getId());
        
        request.setAttribute("fineList", fines);
        request.setAttribute("unpaidAmount", unpaidAmount);
        request.getRequestDispatcher("/reader/myFines.jsp").forward(request, response);
    }
}