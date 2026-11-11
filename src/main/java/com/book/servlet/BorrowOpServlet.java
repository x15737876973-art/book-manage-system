package com.book.servlet;

import com.book.dao.BookDao;
import com.book.dao.BorrowDao;
import com.book.dao.FineDao;
import com.book.dao.ReservationDao;
import com.book.entity.Book;
import com.book.entity.BorrowRecord;
import com.book.entity.FineRecord;
import com.book.entity.Reservation;
import com.book.entity.User;
import com.book.util.AppException;
import com.book.util.DBUtil;
import com.book.util.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 读者自助借阅 Servlet。
 *
 * <h3>安全与健壮性改造</h3>
 * <ul>
 *   <li><b>身份校验</b>：每个动作都确认已登录且为 reader；</li>
 *   <li><b>越权防护</b>：取消预约时校验该预约确属本人，不能凭 id 取消别人的预约；</li>
 *   <li><b>参数校验</b>：bookId / recordId / reservationId 做整数校验；</li>
 *   <li><b>事务一致性</b>：借书 = 写记录 + 扣库存，还书 = 改记录 + 加库存，均放在事务中。</li>
 * </ul>
 *
 * <p>原有 action：myBorrows / borrow / renew / reserve / myReservations /
 * cancelReservation / myFines，跳转页面保持不变。</p>
 */
@WebServlet("/reader/borrowOpServlet")
public class BorrowOpServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** 借阅期限（天） */
    private static final int BORROW_DAYS = 30;
    /** 最大续借次数 */
    private static final int MAX_RENEW_COUNT = 2;
    /** 单个读者最多同时借阅的册数 */
    private static final int MAX_BORROW_LIMIT = 5;

    private final BorrowDao borrowDao = new BorrowDao();
    private final BookDao bookDao = new BookDao();
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
        request.setCharacterEncoding("UTF-8");

        User user = ServletUtils.requireRole(request, response, "reader");
        if (user == null) {
            return;
        }

        String action = ServletUtils.getTrimmed(request, "action");
        try {
            if ("borrow".equals(action)) {
                borrowBook(request, response, user);
            } else if ("renew".equals(action)) {
                renewBook(request, response, user);
            } else if ("reserve".equals(action)) {
                reserveBook(request, response, user);
            } else if ("myReservations".equals(action)) {
                showMyReservations(request, response, user);
            } else if ("cancelReservation".equals(action)) {
                cancelReservation(request, response, user);
            } else if ("myFines".equals(action)) {
                showMyFines(request, response, user);
            } else {
                showMyBorrows(request, response, user);
            }
        } catch (AppException | IllegalArgumentException e) {
            request.setAttribute("msg", e.getMessage());
            showMyBorrows(request, response, user);
        } catch (Exception e) {
            getServletContext().log("读者借阅操作失败", e);
            request.setAttribute("msg", "操作失败，请稍后重试");
            showMyBorrows(request, response, user);
        }
    }

    // ======================================================================
    // 我的借阅
    // ======================================================================

    private void showMyBorrows(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        List<BorrowRecord> all = borrowDao.findByUserId(user.getId());
        List<BorrowRecord> current = new ArrayList<>();
        List<BorrowRecord> history = new ArrayList<>();
        for (BorrowRecord r : all) {
            if ("borrowed".equals(r.getStatus()) || "overdue".equals(r.getStatus())) {
                current.add(r);
            } else {
                history.add(r);
            }
        }
        request.setAttribute("borrowList", current);
        request.setAttribute("historyList", history);
        request.getRequestDispatcher("/reader/myBorrows.jsp").forward(request, response);
    }

    // ======================================================================
    // 借书
    // ======================================================================

    private void borrowBook(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        int bookId = ServletUtils.requireInt(request, "bookId", "图书");

        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new AppException("图书不存在或已下架");
        }
        if (borrowDao.hasBorrowed(user.getId(), bookId)) {
            throw new AppException("您已借阅《" + book.getBname() + "》，请归还后再借");
        }
        if (book.getAvailableStock() == null || book.getAvailableStock() <= 0) {
            throw new AppException("《" + book.getBname() + "》暂无可借库存，可先预约");
        }
        if (borrowDao.countCurrentBorrowsByUserId(user.getId()) >= MAX_BORROW_LIMIT) {
            throw new AppException("最多同时借阅 " + MAX_BORROW_LIMIT + " 本，请先归还部分图书");
        }
        if (fineDao.getUnpaidAmountByUserId(user.getId()) > 0) {
            throw new AppException("您有未缴清的罚款，请先缴纳后再借书");
        }

        final String bookName = book.getBname();
        final Calendar cal = Calendar.getInstance();
        DBUtil.inTransaction(conn -> {
            if (bookDao.decreaseAvailableStock(bookId) <= 0) {
                throw new IllegalStateException("《" + bookName + "》库存不足，借阅失败");
            }
            BorrowRecord record = new BorrowRecord();
            record.setUserId(user.getId());
            record.setBookId(bookId);
            record.setBorrowDate(new Date(System.currentTimeMillis()));
            Calendar dueCal = Calendar.getInstance();
            dueCal.add(Calendar.DAY_OF_MONTH, BORROW_DAYS);
            record.setDueDate(new Date(dueCal.getTimeInMillis()));
            record.setStatus("borrowed");
            record.setRenewCount(0);
            record.setFineAmount(0d);
            record.setIsPaid(false);
            if (borrowDao.add(record) <= 0) {
                throw new IllegalStateException("写入借阅记录失败");
            }
            // 若有等待中的预约，标记完成
            Reservation waiting = reservationDao.findWaitingByUserAndBook(user.getId(), bookId);
            if (waiting != null) {
                reservationDao.complete(waiting.getId());
            }
        });

        cal.add(Calendar.DAY_OF_MONTH, BORROW_DAYS);
        request.setAttribute("msg", "借阅成功！请于 " + new java.text.SimpleDateFormat("yyyy-MM-dd")
                .format(cal.getTime()) + " 前归还");
        showMyBorrows(request, response, user);
    }

    // ======================================================================
    // 续借
    // ======================================================================

    private void renewBook(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        int recordId = ServletUtils.requireInt(request, "recordId", "借阅记录");

        BorrowRecord record = borrowDao.findById(recordId);
        // 关键：必须确认这条记录属于当前登录读者，防止越权续借他人图书
        if (record == null || !user.getId().equals(record.getUserId())) {
            throw new AppException("借阅记录不存在");
        }
        if ("returned".equals(record.getStatus())) {
            throw new AppException("该图书已归还，无法续借");
        }
        int renewCount = record.getRenewCount() == null ? 0 : record.getRenewCount();
        if (renewCount >= MAX_RENEW_COUNT) {
            throw new AppException("已达到最大续借次数（" + MAX_RENEW_COUNT + " 次）");
        }
        if (new Date(System.currentTimeMillis()).after(record.getDueDate())) {
            throw new AppException("图书已逾期，请先归还并缴纳罚款");
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(record.getDueDate());
        cal.add(Calendar.DAY_OF_MONTH, BORROW_DAYS);

        if (borrowDao.renew(recordId, new Date(cal.getTimeInMillis())) <= 0) {
            throw new AppException("续借失败，请稍后重试");
        }
        request.setAttribute("msg", "续借成功，新的应还日期："
                + new java.text.SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()));
        showMyBorrows(request, response, user);
    }

    // ======================================================================
    // 预约
    // ======================================================================

    private void reserveBook(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        int bookId = ServletUtils.requireInt(request, "bookId", "图书");

        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new AppException("图书不存在或已下架");
        }
        if (borrowDao.hasBorrowed(user.getId(), bookId)) {
            throw new AppException("您已借阅《" + book.getBname() + "》，无需预约");
        }
        if (reservationDao.findWaitingByUserAndBook(user.getId(), bookId) != null) {
            throw new AppException("您已预约《" + book.getBname() + "》，请勿重复预约");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setBookId(bookId);
        reservation.setStatus("waiting");
        if (reservationDao.add(reservation) <= 0) {
            throw new AppException("预约失败，请稍后重试");
        }
        request.setAttribute("msg", "预约成功！图书可借时将优先通知您");
        showMyReservations(request, response, user);
    }

    private void showMyReservations(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        request.setAttribute("reservationList", reservationDao.findByUserId(user.getId()));
        request.getRequestDispatcher("/reader/myReservations.jsp").forward(request, response);
    }

    /**
     * 取消预约。
     *
     * <p><b>越权修复</b>：旧实现直接 {@code reservationDao.cancel(id)}，
     * 只要改一下 URL 里的 id 就能取消别人的预约。现在先查出记录并校验归属。</p>
     */
    private void cancelReservation(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        int reservationId = ServletUtils.requireInt(request, "reservationId", "预约记录");

        Reservation reservation = reservationDao.findById(reservationId);
        if (reservation == null || !user.getId().equals(reservation.getUserId())) {
            throw new AppException("预约记录不存在");
        }
        if (!"waiting".equals(reservation.getStatus()) && !"notified".equals(reservation.getStatus())) {
            throw new AppException("该预约已结束，无法取消");
        }

        reservationDao.cancel(reservationId);
        request.setAttribute("msg", "预约已取消");
        showMyReservations(request, response, user);
    }

    // ======================================================================
    // 我的罚款
    // ======================================================================

    private void showMyFines(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        List<FineRecord> fines = fineDao.findByUserId(user.getId());
        double unpaid = fineDao.getUnpaidAmountByUserId(user.getId());
        request.setAttribute("fineList", fines);
        request.setAttribute("unpaidAmount", unpaid);
        request.getRequestDispatcher("/reader/myFines.jsp").forward(request, response);
    }
}