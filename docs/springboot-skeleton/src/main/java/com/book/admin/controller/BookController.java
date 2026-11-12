package com.book.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book.admin.common.Result;
import com.book.admin.entity.Book;
import com.book.admin.service.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 图书接口。
 *
 * <h3>RESTful 约定</h3>
 * <table border="1">
 *   <tr><th>操作</th><th>HTTP 方法</th><th>URL</th></tr>
 *   <tr><td>分页列表</td><td>GET</td><td>/api/books?page=1&pageSize=10</td></tr>
 *   <tr><td>详情</td><td>GET</td><td>/api/books/1</td></tr>
 *   <tr><td>新增</td><td>POST</td><td>/api/books</td></tr>
 * </table>
 *
 * <p>用名词复数表示资源、用 HTTP 方法表示动作，是 REST 的核心思想。</p>
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 分页查询图书。
     *
     * <p>示例：{@code GET /api/books?page=1&pageSize=10&keyword=java&categoryId=4}</p>
     *
     * @param page       页码，默认 1
     * @param pageSize   每页条数，默认 10
     * @param keyword    书名/作者关键词，可选
     * @param categoryId 分类 id，可选
     */
    @GetMapping
    public Result<Page<Book>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId) {

        // 参数纠偏：防止恶意传入极大值拖垮数据库
        if (pageSize > 100) {
            pageSize = 100;
        }
        if (page < 1) {
            page = 1;
        }

        Page<Book> result = bookService.page(page, pageSize, keyword, categoryId);
        return Result.success(result);
    }

    /**
     * 查询图书详情。
     *
     * <p>示例：{@code GET /api/books/1}</p>
     *
     * <p>{@code @PathVariable} 把 URL 中的 {id} 绑定到方法参数。
     * 图书不存在时 Service 抛 IllegalArgumentException，
     * 由 {@code GlobalExceptionHandler} 统一转成 {@code {code:400, message:"图书不存在"}}。</p>
     */
    @GetMapping("/{id}")
    public Result<Book> detail(@PathVariable Integer id) {
        return Result.success(bookService.getById(id));
    }

    /**
     * 新增图书。
     *
     * <pre>
     * POST /api/books
     * Content-Type: application/json
     *
     * {
     *   "bid": "B099",
     *   "bname": "深入理解Java虚拟机",
     *   "author": "周志明",
     *   "publisher": "机械工业出版社",
     *   "price": 129.00,
     *   "categoryId": 4,
     *   "stock": 10,
     *   "location": "A区-2排-1架"
     * }
     * </pre>
     *
     * <p>{@code @RequestBody} 把请求体 JSON 自动反序列化成 Book 对象。</p>
     *
     * @return 新增记录的 id
     */
    @PostMapping
    public Result<Integer> create(@RequestBody Book book) {
        if (book.getBname() == null || book.getBname().trim().isEmpty()) {
            throw new IllegalArgumentException("书名不能为空");
        }
        if (book.getBid() == null || book.getBid().trim().isEmpty()) {
            throw new IllegalArgumentException("图书编号不能为空");
        }
        return Result.success(bookService.create(book));
    }

    /**
     * 一个演示接口：返回几个提示，帮助你快速自测环境。
     *
     * <p>访问 {@code GET /api/books/ping}，看到 {@code {"code":200,"message":"成功","data":"pong"}}
     * 说明 Controller 层工作正常。</p>
     */
    @GetMapping("/ping")
    public Result<List<String>> ping() {
        return Result.success(List.of("pong", "Spring Boot is running"));
    }
}