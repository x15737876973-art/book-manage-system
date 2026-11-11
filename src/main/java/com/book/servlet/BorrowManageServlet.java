package com.book.servlet;

import com.book.dao.BookDao;
import com.book.dao.BorrowDao;
import com.book.dao.FineDao;
import com.book.dao.UserDao;
import com.book.entity.Book;
import com.book.entity.BorrowRecord;
import com.book.entity.FineRecord;
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
import java.util.Calendar;
import java.util.List;

/**
 * 借阅管理 Servlet（管理员）。
 *
 * <h3>改造要点</h3>
 * <ul>
 *   <li>所有 id 参数做整数与范围校验，非法 id 不再抛 {@code NumberFormatException}；</li>
 *   <li>借书 / 还书使用 <b>事务</b>：借阅记录与库存变动要么都成功，要么都回滚；</li>
 *   <li>业务提示统一走 {@link AppException}，页面显示友好文案。</li>
 * </ul>
 *
 * <p>原有 action 名称与跳转保持不变：list / current / overdue / search / borrow /
 * return / renew / toBorrow / payFine / toEditDueDate / updateDueDate。</p>
 */
@WebServlet("/admin/borrowServlet")
public class BorrowManageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** 借阅期限（天） */
    private static final int BORROW_DAYS = 30;
    /** 最大续借次数 */
    private static final int MAX_RENEW_COUNT = 2;
    /** 每日罚款金额（元） */
    private static final double FINE_PER_DAY = 0.5;

    private final BorrowDao borrowDao = new BorrowDao();
    private final BookDao bookDao = new BookDao();
    private final UserDao userDao = new UserDao();
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

        User loginUser = ServletUtils.requireRole(request, response, "admin");
        if (loginUser == null) {
            return;
        }

        String action = ServletUtils.getTrimmed(request, "action");
        try {
            if ("current".equals(action)) {
                listCurrentBorrows(request, response);
            } else if ("overdue".equals(action)) {
                listOverdue(request, response);
            } else if ("search".equals(action)) {
                searchBorrows(request, response);
            } else if ("borrow".equals(action)) {
                borrowBook(request, response);
            } else if ("return".equals(action)) {
                returnBook(request, response);
            } else if ("renew".equals(action)) {
                renewBook(request, response);
            } else if ("toBorrow".equals(action)) {
                toBorrowPage(request, response);
            } else if ("payFine".equals(action)) {
                payFine(request, response);
            } else if ("toEditDueDate".equals(action)) {
                toEditDueDate(request, response);
            } else if ("updateDueDate".equals(action)) {
                updateDueDate(request, response);
            } else {
                listBorrows(request, response);
            }
        } catch (AppException | IllegalArgumentException e) {
            request.setAttribute("msg", e.getMessage());
            request.setAttribute("msgType", "error");
            listBorrows(request, response);
        } catch (Exception e) {
            getServletContext().log("借阅管理操作失败", e);
            request.setAttribute("error", "借阅管理操作失败，请稍后重试");
            request.getRequestDispatcher("/admin/error.jsp").forward(request, response);
        }
    }

    // ======================================================================
    // 列表
    // ======================================================================

    private void listBorrows(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("borrowList", borrowDao.findAll());
        request.getRequestDispatcher("/admin/borrowList.jsp").forward(request, response);
    }

    private void listCurrentBorrows(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("borrowList", borrowDao.findAllCurrentBorrows());
        request.getRequestDispatcher("/admin/borrowList.jsp").forward(request, response);
    }

    private void listOverdue(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        borrowDao.updateOverdueStatus();
        List<FineRecord> fineList = fineDao.findAll();
        List<BorrowRecord> records = borrowDao.findOverdue();

        request.setAttribute("fineList", fineList);
        request.setAttribute("overdueList", records);
        request.getRequestDispatcher("/admin/overdueList.jsp").forward(request, response);
    }

    private void searchBorrows(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = ServletUtils.getParamWithLimit(request, "keyword", 50);
        List<BorrowRecord> records = borrowDao.search(keyword);
        request.setAttribute("borrowList", records);
        request.setAttribute("keyword", keyword);
        request.setAttribute("msg", records.isEmpty()
                ? "未找到符合条件的借阅记录"
                : "查询成功，共找到 " + records.size() + " 条借阅记录");
        request.setAttribute("msgType", records.isEmpty() ? "error" : "success");
        request.getRequestDispatcher("/admin/borrowList.jsp").forward(request, response);
    }

    private void toBorrowPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("readerList", userDao.findReaders());
        request.setAttribute("bookList", bookDao.findAll());
        request.getRequestDispatcher("/admin/borrowBook.jsp").forward(request, response);
    }

    // ======================================================================
    // 借书 / 还书 / 续借
    // ======================================================================

    /** 管理员代借：事务内完成"写借阅记录 + 扣库存" */
    private void borrowBook(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int userId = ServletUtils.requireInt(request, "userId", "读者");
        int bookId = ServletUtils.requireInt(request, "bookId", "图书");

        User reader = userDao.findById(userId);
        if (reader == null || !"reader".equals(reader.getRole())) {
            throw new AppException("所选读者不存在");
        }
        if (!"active".equals(reader.getStatus())) {
            throw new AppException("该读者账号已被禁用，无法借书");
        }

        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new AppException("所选图书不存在");
        }

        // 同一本书未归还前不能重复借
        if (borrowDao.hasBorrowed(userId, bookId)) {
            throw new AppException("该读者已借阅《" + book.getBname() + "》且尚未归还");
        }
        if (book.getAvailableStock() == null || book.getAvailableStock() <= 0) {
            throw new AppException("《" + book.getBname() + "》库存不足，无法借出");
        }
        if (fineDao.getUnpaidAmountByUserId(userId) > 0) {
            throw new AppException("该读者有未缴清的罚款，请先处理后再借书");
        }

        final String bookName = book.getBname();
        DBUtil.inTransaction(conn -> {
            // 库存扣减带条件，UPDATE 影响 0 行说明被并发抢空
            if (bookDao.decreaseAvailableStock(bookId) <= 0) {
                throw new IllegalStateException("《" + bookName + "》库存不足，借出失败");
            }
            BorrowRecord record = new BorrowRecord();
            record.setUserId(userId);
            record.setBookId(bookId);
            record.setBorrowDate(new Date(System.currentTimeMillis()));
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, BORROW_DAYS);
            record.setDueDate(new Date(cal.getTimeInMillis()));
            record.setStatus("borrowed");
            record.setRenewCount(0);
            record.setFineAmount(0d);
            record.setIsPaid(false);
            if (borrowDao.add(record) <= 0) {
                throw new IllegalStateException("写入借阅记录失败");
            }
        });

        response.sendRedirect(request.getContextPath() + "/admin/borrowServlet?action=list");
    }

    /** 还书：事务内完成"标记归还 + 增加库存 + 生成罚款记录" */
    private void returnBook(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int recordId = ServletUtils.requireInt(request, "recordId", "借阅记录");

        BorrowRecord record = borrowDao.findById(recordId);
        if (record == null) {
            throw new AppException("借阅记录不存在");
        }
        if ("returned".equals(record.getStatus())) {
            throw new AppException("该记录已归还，无需重复操作");
        }

        Date returnDate = new Date(System.currentTimeMillis());
        double fineAmount = 0d;
        int overdueDays = 0;

        if (record.getDueDate() != null && returnDate.after(record.getDueDate())) {
            long diff = returnDate.getTime() - record.getDueDate().getTime();
            // 向上取整：逾期不足一天也按一天计
            overdueDays = (int) Math.ceil(diff / (1000.0 * 60 * 60 * 24));
            fineAmount = overdueDays * FINE_PER_DAY;
        }

        final double finalFine = fineAmount;
        final int finalDays = overdueDays;
        DBUtil.inTransaction(conn -> {
            if (borrowDao.returnBook(recordId, returnDate, finalFine) <= 0) {
                throw new IllegalStateException("更新借阅记录失败");
            }
            bookDao.increaseAvailableStock(record.getBookId());
            if (finalDays > 0) {
                FineRecord fine = new FineRecord();
                fine.setUserId(record.getUserId());
                fine.setBorrowRecordId(recordId);
                fine.setAmount(finalFine);
                fine.setDays(finalDays);
                fine.setStatus("unpaid");
                fineDao.add(fine);
            }
        });

        response.sendRedirect(request.getContextPath() + "/admin/borrowServlet?action=list");
    }

    /** 续借 */
    private void renewBook(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int recordId = ServletUtils.requireInt(request, "recordId", "借阅记录");

        BorrowRecord record = borrowDao.findById(recordId);
        if (record == null) {
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
            throw new AppException("图书已逾期，不能续借");
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(record.getDueDate());
        cal.add(Calendar.DAY_OF_MONTH, BORROW_DAYS);

        if (borrowDao.renew(recordId, new Date(cal.getTimeInMillis())) <= 0) {
            throw new AppException("续借失败，请稍后重试");
        }
        request.setAttribute("msg", "续借成功，新到期日：" + cal.getTime());
        request.setAttribute("msgType", "success");
        listCurrentBorrows(request, response);
    }

    /** 缴纳罚款 */
    private void payFine(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int fineId = ServletUtils.requireInt(request, "fineId", "罚款记录");
        if (fineDao.findById(fineId) == null) {
            throw new AppException("罚款记录不存在");
        }
        fineDao.pay(fineId);
        response.sendRedirect(request.getContextPath() + "/admin/borrowServlet?action=overdue");
    }

    // ======================================================================
    // 应还日期调整
    // ======================================================================

    private void toEditDueDate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int recordId = ServletUtils.requireInt(request, "recordId", "借阅记录");
        BorrowRecord record = borrowDao.findById(recordId);
        if (record == null) {
            throw new AppException("借阅记录不存在");
        }
        request.setAttribute("record", record);
        request.getRequestDispatcher("/admin/editDueDate.jsp").forward(request, response);
    }

    private void updateDueDate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int recordId = ServletUtils.requireInt(request, "recordId", "借阅记录");
        Date dueDate = ServletUtils.getSqlDate(request, "dueDate", "应还日期");
        if (dueDate == null) {
            throw new AppException("请填写应还日期");
        }
        if (borrowDao.findById(recordId) == null) {
            throw new AppException("借阅记录不存在");
        }
        if (borrowDao.updateDueDate(recordId, dueDate) <= 0) {
            throw new AppException("更新失败，请稍后重试");
        }
        request.setAttribute("msg", "应还日期已更新");
        request.setAttribute("msgType", "success");
        listBorrows(request, response);
    }
}