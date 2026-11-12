package com.book.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book.admin.entity.Book;
import com.book.admin.mapper.BookMapper;
import org.springframework.stereotype.Service;

/**
 * 图书业务层。
 *
 * <h3>分层职责</h3>
 * <ul>
 *   <li><b>Controller</b>：只负责接收参数、调用 Service、返回结果，不含业务逻辑；</li>
 *   <li><b>Service</b>：业务规则（校验、多表协作、事务控制）都写在这一层；</li>
 *   <li><b>Mapper</b>：只负责数据存取。</li>
 * </ul>
 *
 * <p>这样分层的好处：换 Web 框架不影响业务、业务可被多个入口复用、便于单元测试。</p>
 */
@Service
public class BookService {

    private final BookMapper bookMapper;

    /**
     * 构造器注入：Spring 启动时自动把 BookMapper 的实现类传进来。
     * 相比字段注入（{@code @Autowired}），构造器注入保证依赖不可为 null，且便于测试时手动传入 mock。
     */
    public BookService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    /**
     * 分页查询图书，支持按书名模糊搜索与分类筛选。
     *
     * <h3>对比手写分页</h3>
     * <p>你的 Servlet 版需要：手写 {@code COUNT(*)} 查询 + 手写 {@code LIMIT ?, ?} 查询
     * + 手动计算 offset + 自己封装 PageResult。
     * MyBatis-Plus 的 {@code selectPage} 只需传一个 Page 对象，它内部自动执行两次查询并填充结果。</p>
     *
     * @param pageNum    页码，从 1 开始
     * @param pageSize   每页条数
     * @param keyword    书名/作者关键词，可为 null
     * @param categoryId 分类 id，可为 null
     */
    public Page<Book> page(int pageNum, int pageSize, String keyword, Integer categoryId) {
        Page<Book> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();

        // 关键词模糊搜索：书名或作者任一匹配即可
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Book::getBname, kw)
                              .or()
                              .like(Book::getAuthor, kw));
        }

        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(Book::getCategoryId, categoryId);
        }

        wrapper.orderByAsc(Book::getId);

        return bookMapper.selectPage(page, wrapper);
    }

    /**
     * 按 id 查询图书，不存在时抛异常由全局异常处理器转成友好提示。
     */
    public Book getById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("图书 ID 不能为空");
        }
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new IllegalArgumentException("图书不存在或已被删除");
        }
        return book;
    }

    /**
     * 新增图书。
     *
     * <p>业务规则：图书编号不能重复；新增时"可借库存"初始等于"总库存"。</p>
     */
    public Integer create(Book book) {
        // 校验编号唯一
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getBid, book.getBid());
        if (bookMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("图书编号【" + book.getBid() + "】已存在");
        }

        // 新增时可借库存默认等于总库存
        if (book.getAvailableStock() == null) {
            book.setAvailableStock(book.getStock() == null ? 0 : book.getStock());
        }

        bookMapper.insert(book);
        return book.getId();
    }
}