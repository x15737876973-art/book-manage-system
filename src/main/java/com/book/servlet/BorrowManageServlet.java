package com.book.servlet;

import com.book.dao.BookDao;
import com.book.dao.BorrowDao;
import com.book.dao.FineDao;
import com.book.dao.UserDao;
import com.book.entity.Book;
import com.book.entity.BorrowRecord;
import com.book.entity.FineRecord;
import com.book.entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 借阅管理Servlet
 */
@WebServlet("/admin/borrowServlet")
public class BorrowManageServlet extends HttpServlet {
    private BorrowDao borrowDao = new BorrowDao();
    private BookDao bookDao = new BookDao();
    private UserDao userDao = new UserDao();
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
        response.setContentType("text/html;charset=UTF-8");

        // 手动解析查询字符串并正确解码
        String queryString = request.getQueryString();
        if (queryString != null) {
            try {
                // 使用安全解析方法
                Map<String, String> params = safeParseQueryString(queryString);

                // 打印调试信息
                System.out.println("=== GET请求调试信息 ===");
                System.out.println("原始queryString: " + queryString);

                String keyword = params.get("keyword");
                String categoryId = params.get("categoryId");
                String action = params.get("action");

                System.out.println("keyword参数: " + keyword);
                System.out.println("categoryId参数: " + categoryId);
                System.out.println("action参数: " + action);

                // 将参数重新设置到request中
                if (keyword != null) {
                    request.setAttribute("keyword", keyword);
                }
                if (categoryId != null) {
                    request.setAttribute("categoryId", categoryId);
                }
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

    /**
     * 安全地解析查询字符串
     */
    private Map<String, String> safeParseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return params;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx == -1) continue;

            String key = pair.substring(0, idx);
            String value = pair.substring(idx + 1);

            try {
                // 先尝试UTF-8解码
                key = URLDecoder.decode(key, "UTF-8");
                value = URLDecoder.decode(value, "UTF-8");
            } catch (Exception e) {
                try {
                    key = URLDecoder.decode(key, "GBK");
                    value = URLDecoder.decode(value, "GBK");
                } catch (Exception ex) {
                    try {
                        key = URLDecoder.decode(key, "ISO-8859-1");
                        value = URLDecoder.decode(value, "ISO-8859-1");
                    } catch (Exception exc) {
                        // 所有编码方式都失败，保留原始值
                    }
                }
            }
            params.put(key, value);
        }
        return params;
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        String action = request.getParameter("action");
        
        if ("list".equals(action)) {
            listBorrows(request, response);
        } else if ("current".equals(action)) {
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
    }
    
    /**
     * 查询所有借阅记录
     */
    private void listBorrows(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<BorrowRecord> records = borrowDao.findAll();
            request.setAttribute("borrowList", records);
            request.getRequestDispatcher("/admin/borrowList.jsp").forward(request, response);
        } catch (Exception e) {
            System.out.println("查询借阅列表失败: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "查询借阅列表失败：" + e.getMessage());
            request.getRequestDispatcher("/admin/error.jsp").forward(request, response);
        }
    }
    
    /**
     * 搜索借阅记录
     */
    private void searchBorrows(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            System.out.println("=== 开始搜索借阅记录 ===");
            String keyword = request.getParameter("keyword");
            System.out.println("搜索参数 - keyword: " + keyword);
            System.out.println("keyword的长度: " + (keyword != null ? keyword.length() : 0));
            System.out.println("keyword的字符编码: " + (keyword != null ? keyword.getBytes("UTF-8").length : 0));
            
            List<BorrowRecord> records;
            if (keyword == null || keyword.trim().isEmpty()) {
                records = borrowDao.findAll();
            } else {
                records = borrowDao.search(keyword.trim());
            }
            
            System.out.println("搜索到的借阅记录数量: " + records.size());
            
            request.setAttribute("borrowList", records);
            request.setAttribute("keyword", keyword);
            
            // 添加查询结果提示
            if (records.isEmpty()) {
                request.setAttribute("msg", "未找到符合条件的借阅记录");
            } else {
                request.setAttribute("msg", "查询成功，共找到 " + records.size() + " 条借阅记录");
            }
            
            request.getRequestDispatcher("/admin/borrowList.jsp").forward(request, response);
        } catch (Exception e) {
            System.out.println("搜索借阅记录失败: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "搜索借阅记录失败：" + e.getMessage());
            request.getRequestDispatcher("/admin/error.jsp").forward(request, response);
        }
    }
    
    /**
     * 查询当前借阅（未还）
     */
    private void listCurrentBorrows(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<BorrowRecord> records = borrowDao.findAllCurrentBorrows();
        request.setAttribute("borrowList", records);
        request.getRequestDispatcher("/admin/borrowList.jsp").forward(request, response);
    }
    
    /**
     * 查询逾期记录
     */
    private void listOverdue(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 先更新逾期状态
        borrowDao.updateOverdueStatus();
        
        // 查询所有罚款记录（包含已支付和未支付）
        List<FineRecord> fineList = fineDao.findAll();
        request.setAttribute("fineList", fineList);
        
        // 查询当前正在逾期的借阅记录（还未还书）
        List<BorrowRecord> records = borrowDao.findOverdue();
        request.setAttribute("overdueList", records);
        
        System.out.println("=== 逾期列表查询 ===");
        System.out.println("当前逾期记录数量: " + records.size());
        System.out.println("罚款记录数量: " + fineList.size());
        
        request.getRequestDispatcher("/admin/overdueList.jsp").forward(request, response);
    }
    
    /**
     * 跳转到借书页面
     */
    private void toBorrowPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<User> readers = userDao.findReaders();
        List<Book> books = bookDao.findAll();
        
        request.setAttribute("readerList", readers);
        request.setAttribute("bookList", books);
        request.getRequestDispatcher("/admin/borrowBook.jsp").forward(request, response);
    }
    
    /**
     * 借书
     */
    private void borrowBook(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String userIdStr = request.getParameter("userId");
        String bookIdStr = request.getParameter("bookId");
        
        if (userIdStr == null || bookIdStr == null) {
            request.setAttribute("msg", "参数错误！");
            toBorrowPage(request, response);
            return;
        }
        
        Integer userId = Integer.parseInt(userIdStr);
        Integer bookId = Integer.parseInt(bookIdStr);
        
        // 检查用户是否当前正在借阅此书（未归还）
        List<BorrowRecord> currentBorrows = borrowDao.findByUserId(userId);
        boolean alreadyBorrowed = false;
        for (BorrowRecord r : currentBorrows) {
            if (r.getBookId().equals(bookId) && ("borrowed".equals(r.getStatus()) || "overdue".equals(r.getStatus()))) {
                alreadyBorrowed = true;
                break;
            }
        }
        if (alreadyBorrowed) {
            request.setAttribute("msg", "该读者已借阅此书，不能重复借阅！");
            toBorrowPage(request, response);
            return;
        }
        
        // 检查库存
        Book book = bookDao.findById(bookId);
        if (book == null || book.getAvailableStock() <= 0) {
            request.setAttribute("msg", "该图书库存不足！");
            toBorrowPage(request, response);
            return;
        }
        
        // 检查用户是否有未支付的罚款
        double unpaidFine = fineDao.getUnpaidAmountByUserId(userId);
        if (unpaidFine > 0) {
            request.setAttribute("msg", "该读者有未支付的罚款，请先缴纳罚款！");
            toBorrowPage(request, response);
            return;
        }
        
        // 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
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
            response.sendRedirect("borrowServlet?action=list");
        } else {
            request.setAttribute("msg", "借阅失败！");
            toBorrowPage(request, response);
        }
    }
    
    /**
     * 还书
     */
    private void returnBook(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String recordIdStr = request.getParameter("recordId");
        
        if (recordIdStr == null) {
            response.sendRedirect("borrowServlet?action=current");
            return;
        }
        
        Integer recordId = Integer.parseInt(recordIdStr);
        BorrowRecord record = borrowDao.findById(recordId);
        
        if (record == null) {
            response.sendRedirect("borrowServlet?action=current");
            return;
        }
        
        // 计算罚款
        Date returnDate = new Date(System.currentTimeMillis());
        double fineAmount = 0;
        
        if (returnDate.after(record.getDueDate())) {
            long diff = returnDate.getTime() - record.getDueDate().getTime();
            int days = (int) (diff / (1000 * 60 * 60 * 24));
            fineAmount = days * FINE_PER_DAY;
            
            // 添加罚款记录
            FineRecord fineRecord = new FineRecord();
            fineRecord.setUserId(record.getUserId());
            fineRecord.setBorrowRecordId(recordId);
            fineRecord.setAmount(fineAmount);
            fineRecord.setDays(days);
            fineRecord.setStatus("unpaid");
            fineDao.add(fineRecord);
        }
        
        // 更新借阅记录
        int row = borrowDao.returnBook(recordId, returnDate, fineAmount);
        if (row > 0) {
            // 增加可借数量
            bookDao.increaseAvailableStock(record.getBookId());
        }
        
        response.sendRedirect("borrowServlet?action=list");
    }
    
    /**
     * 续借
     */
    private void renewBook(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String recordIdStr = request.getParameter("recordId");
        
        if (recordIdStr == null) {
            response.sendRedirect("borrowServlet?action=current");
            return;
        }
        
        Integer recordId = Integer.parseInt(recordIdStr);
        BorrowRecord record = borrowDao.findById(recordId);
        
        if (record == null) {
            response.sendRedirect("borrowServlet?action=current");
            return;
        }
        
        // 检查续借次数
        if (record.getRenewCount() >= MAX_RENEW_COUNT) {
            request.setAttribute("msg", "已达到最大续借次数！");
            listCurrentBorrows(request, response);
            return;
        }
        
        // 检查是否逾期
        if (new Date(System.currentTimeMillis()).after(record.getDueDate())) {
            request.setAttribute("msg", "图书已逾期，不能续借！");
            listCurrentBorrows(request, response);
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
        
        listCurrentBorrows(request, response);
    }
    
    /**
     * 支付罚款
     */
    private void payFine(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fineIdStr = request.getParameter("fineId");
        
        if (fineIdStr != null) {
            fineDao.pay(Integer.parseInt(fineIdStr));
        }
        
        response.sendRedirect("borrowServlet?action=overdue");
    }
    
    /**
     * 跳转到编辑归还日期页面
     */
    private void toEditDueDate(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String recordIdStr = request.getParameter("recordId");
        
        if (recordIdStr != null) {
            BorrowRecord record = borrowDao.findById(Integer.parseInt(recordIdStr));
            request.setAttribute("record", record);
        }
        
        request.getRequestDispatcher("/admin/editDueDate.jsp").forward(request, response);
    }
    
    /**
     * 更新归还日期
     */
    private void updateDueDate(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String recordIdStr = request.getParameter("recordId");
        String dueDateStr = request.getParameter("dueDate");
        
        if (recordIdStr != null && dueDateStr != null) {
            try {
                int recordId = Integer.parseInt(recordIdStr);
                java.sql.Date dueDate = java.sql.Date.valueOf(dueDateStr);
                
                int row = borrowDao.updateDueDate(recordId, dueDate);
                if (row > 0) {
                    request.setAttribute("msg", "归还日期已更新！");
                    request.setAttribute("msgType", "success");
                } else {
                    request.setAttribute("msg", "更新失败！");
                    request.setAttribute("msgType", "error");
                }
            } catch (Exception e) {
                request.setAttribute("msg", "日期格式错误！");
                request.setAttribute("msgType", "error");
            }
        }
        
        listBorrows(request, response);
    }
}