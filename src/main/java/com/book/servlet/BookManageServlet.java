package com.book.servlet;

import com.book.dao.BookDao;
import com.book.dao.CategoryDao;
import com.book.entity.Book;
import com.book.entity.Category;
import com.book.entity.PageResult;
import com.book.entity.User;
import com.book.util.AppException;
import com.book.util.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 图书管理 Servlet（管理员）。
 *
 * <h3>本次改造</h3>
 * <ul>
 *   <li><b>分页查询</b>：{@code list} / {@code search} 改为分页，避免一次性加载全部图书；</li>
 *   <li><b>参数校验</b>：图书编号、书名必填；价格、库存、分类 id 做范围与类型校验；</li>
 *   <li><b>身份校验</b>：写操作要求 admin 身份（过滤器已拦截，这里再兜底）；</li>
 *   <li><b>异常处理</b>：业务异常转友好提示，系统异常记日志并跳统一错误页。</li>
 * </ul>
 *
 * <p>原有业务动作（list / search / toAdd / add / save / toEdit / update / delete）
 * 名称与跳转逻辑保持不变，前端无需改动。</p>
 */
@WebServlet("/admin/bookManageServlet")
public class BookManageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final BookDao bookDao = new BookDao();
    private final CategoryDao categoryDao = new CategoryDao();

    /** 图书字段长度上限，与数据库列定义保持一致 */
    private static final int MAX_BID = 20;
    private static final int MAX_ISBN = 30;
    private static final int MAX_BNAME = 200;
    private static final int MAX_AUTHOR = 100;
    private static final int MAX_PUBLISHER = 100;
    private static final int MAX_LOCATION = 50;
    private static final int MAX_DESCRIPTION = 2000;
    /** 库存上限，防止误输入极大值 */
    private static final int MAX_STOCK = 999999;
    /** 价格上限 */
    private static final double MAX_PRICE = 999999.99;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 兜底鉴权：即使过滤器规则被调整，写操作也不会被越权调用
        User loginUser = ServletUtils.requireRole(request, response, "admin");
        if (loginUser == null) {
            return;
        }

        String action = ServletUtils.getTrimmed(request, "action");

        try {
            if ("search".equals(action)) {
                searchBooks(request, response);
            } else if ("add".equals(action) || "toAdd".equals(action)) {
                toAddBook(request, response);
            } else if ("save".equals(action)) {
                addBook(request, response);
            } else if ("toEdit".equals(action)) {
                toEditBook(request, response);
            } else if ("update".equals(action)) {
                updateBook(request, response);
            } else if ("delete".equals(action)) {
                deleteBook(request, response);
            } else {
                listBooks(request, response);
            }
        } catch (AppException | IllegalArgumentException e) {
            showMessagePage(request, response, e.getMessage(), true);
        } catch (Exception e) {
            getServletContext().log("图书管理操作失败", e);
            showMessagePage(request, response, "操作失败，请稍后重试或联系管理员", true);
        }
    }

    // ======================================================================
    // 查询
    // ======================================================================

    /** 图书列表（分页） */
    private void listBooks(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = ServletUtils.getPageNumber(request);
        int size = ServletUtils.getPageSize(request);
        Integer categoryId = ServletUtils.getPositiveInt(request, "categoryId", Integer.MAX_VALUE);

        PageResult<Book> pageResult = bookDao.findPage(page, size, categoryId);

        request.setAttribute("page", pageResult);
        request.setAttribute("bookList", pageResult.getRecords());
        request.setAttribute("categoryList", categoryDao.findAll());
        request.setAttribute("categoryId", categoryId);
        request.getRequestDispatcher("/admin/bookList.jsp").forward(request, response);
    }

    /** 图书搜索（分页） */
    private void searchBooks(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = ServletUtils.getParamWithLimit(request, "keyword", 50);
        Integer categoryId = ServletUtils.getPositiveInt(request, "categoryId", Integer.MAX_VALUE);
        int page = ServletUtils.getPageNumber(request);
        int size = ServletUtils.getPageSize(request);

        PageResult<Book> pageResult = bookDao.searchPage(keyword, categoryId, page, size);

        request.setAttribute("page", pageResult);
        request.setAttribute("bookList", pageResult.getRecords());
        request.setAttribute("categoryList", categoryDao.findAll());
        request.setAttribute("keyword", keyword);
        request.setAttribute("categoryId", categoryId);

        if (pageResult.getTotalRecords() == 0) {
            request.setAttribute("msg", "未找到符合条件的图书");
            request.setAttribute("msgType", "error");
        } else {
            request.setAttribute("msg", "查询成功，共找到 " + pageResult.getTotalRecords() + " 本图书");
            request.setAttribute("msgType", "success");
        }
        request.getRequestDispatcher("/admin/bookList.jsp").forward(request, response);
    }

    // ======================================================================
    // 表单页面
    // ======================================================================

    /** 跳转新增页 */
    private void toAddBook(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("categoryList", categoryDao.findAll());
        request.getRequestDispatcher("/admin/bookEdit.jsp").forward(request, response);
    }

    /** 跳转编辑页 */
    private void toEditBook(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bid = ServletUtils.requireParam(request, "bid", "图书编号");
        Book book = bookDao.findByBid(bid);
        if (book == null) {
            throw new AppException("图书不存在或已被删除");
        }
        request.setAttribute("book", book);
        request.setAttribute("categoryList", categoryDao.findAll());
        request.getRequestDispatcher("/admin/bookEdit.jsp").forward(request, response);
    }

    // ======================================================================
    // 写入
    // ======================================================================

    /** 新增图书 */
    private void addBook(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bid = ServletUtils.requireParam(request, "bid", "图书编号", MAX_BID);
        if (bookDao.findByBid(bid) != null) {
            throw new AppException("图书编号【" + bid + "】已存在，请换一个");
        }
        Book book = readBookForm(request);
        if (book.getAvailableStock() == null) {
            book.setAvailableStock(book.getStock());
        }

        int row = bookDao.addBook(book);
        if (row <= 0) {
            throw new AppException("添加失败，请检查填写内容");
        }
        request.setAttribute("msg", "图书添加成功");
        response.sendRedirect(request.getContextPath() + "/admin/bookManageServlet?action=list");
    }

    /** 更新图书 */
    private void updateBook(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bid = ServletUtils.requireParam(request, "bid", "图书编号", MAX_BID);
        Book existing = bookDao.findByBid(bid);
        if (existing == null) {
            throw new AppException("图书不存在或已被删除");
        }

        Book book = readBookForm(request);
        // 可借数量未填写时保持原值，避免编辑时误清零
        if (book.getAvailableStock() == null) {
            book.setAvailableStock(existing.getAvailableStock());
        }
        // 可借数量不能超过总库存
        if (book.getAvailableStock() > book.getStock()) {
            throw new AppException("可借数量（" + book.getAvailableStock() + "）不能大于总库存（" + book.getStock() + "）");
        }

        int row = bookDao.update(book);
        if (row <= 0) {
            throw new AppException("更新失败，请稍后重试");
        }
        response.sendRedirect(request.getContextPath() + "/admin/bookManageServlet?action=list");
    }

    /** 删除图书 */
    private void deleteBook(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String bid = ServletUtils.requireParam(request, "bid", "图书编号", MAX_BID);
        if (bookDao.findByBid(bid) == null) {
            throw new AppException("图书不存在或已被删除");
        }
        bookDao.delete(bid);
        response.sendRedirect(request.getContextPath() + "/admin/bookManageServlet?action=list");
    }

    // ======================================================================
    // 表单解析与校验
    // ======================================================================

    /**
     * 从请求中读取并校验图书表单，返回组装好的 Book。
     *
     * <p>所有字段都做了长度与格式校验，非法值抛出 {@link AppException}，
     * 由上层转成友好提示，不再出现 {@code NumberFormatException} 之类的 500 错误。</p>
     */
    private Book readBookForm(HttpServletRequest request) {
        String bid = ServletUtils.requireParam(request, "bid", "图书编号", MAX_BID);
        String bname = ServletUtils.requireParam(request, "bname", "书名", MAX_BNAME);

        Book book = new Book();
        book.setBid(bid);
        book.setBname(bname);
        book.setIsbn(ServletUtils.getParamWithLimit(request, "isbn", MAX_ISBN));
        book.setAuthor(ServletUtils.getParamWithLimit(request, "author", MAX_AUTHOR));
        book.setPublisher(ServletUtils.getParamWithLimit(request, "publisher", MAX_PUBLISHER));
        book.setLocation(ServletUtils.getParamWithLimit(request, "location", MAX_LOCATION));
        book.setDescription(ServletUtils.getParamWithLimit(request, "description", MAX_DESCRIPTION));

        // 出版日期
        book.setPublishDate(ServletUtils.getSqlDate(request, "publishDate", "出版日期"));

        // 价格：非负且不超过上限
        Double price = ServletUtils.getDouble(request, "price", null);
        if (price != null) {
            if (price < 0) {
                throw new AppException("价格不能为负数");
            }
            if (price > MAX_PRICE) {
                throw new AppException("价格不能超过 " + MAX_PRICE);
            }
        }
        book.setPrice(price == null ? 0d : price);

        // 分类：必须存在
        Integer categoryId = ServletUtils.getPositiveInt(request, "categoryId", Integer.MAX_VALUE);
        if (categoryId != null && categoryDao.findById(categoryId) == null) {
            throw new AppException("所选分类不存在，请重新选择");
        }
        book.setCategoryId(categoryId);

        // 库存与非负校验
        Integer stock = ServletUtils.getPositiveInt(request, "stock", MAX_STOCK);
        book.setStock(stock == null ? 0 : stock);

        Integer availableStock = ServletUtils.getPositiveInt(request, "availableStock", MAX_STOCK);
        book.setAvailableStock(availableStock);

        return book;
    }

    // ======================================================================
    // 辅助
    // ======================================================================

    /** 把业务提示渲染到错误提示页（不暴露堆栈） */
    private void showMessagePage(HttpServletRequest request, HttpServletResponse response,
                                 String message, boolean isError) throws ServletException, IOException {
        request.setAttribute("error", message);
        request.setAttribute("isError", isError);
        if (isError) {
            // 重新准备下拉数据，便于用户返回表单重填
            request.setAttribute("categoryList", safeCategories());
        }
        request.getRequestDispatcher("/admin/error.jsp").forward(request, response);
    }

    private List<Category> safeCategories() {
        try {
            return categoryDao.findAll();
        } catch (Exception e) {
            getServletContext().log("读取分类列表失败", e);
            return java.util.Collections.emptyList();
        }
    }
}