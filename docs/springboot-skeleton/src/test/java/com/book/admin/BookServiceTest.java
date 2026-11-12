package com.book.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book.admin.entity.Book;
import com.book.admin.mapper.BookMapper;
import com.book.admin.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 图书服务集成测试。
 *
 * <h3>为什么这样写</h3>
 * <ul>
 *   <li>{@code @SpringBootTest}：启动完整 Spring 容器，测试真实的 Bean 装配；</li>
 *   <li>{@code @ActiveProfiles("test")}：激活 {@code application-test.properties}，
 *       让测试连 H2 内存库而不是真实 MySQL；</li>
 *   <li>每个测试方法前清空并重新插入种子数据，保证用例互相独立。</li>
 * </ul>
 *
 * <p><b>对比你的 Servlet 版</b>：那边需要自己 new DAO、自己建 H2、自己写建表 SQL；
 * 这里 Spring 自动完成全部装配。</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("图书服务")
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookMapper bookMapper;

    @BeforeEach
    void setUp() {
        bookMapper.delete(null);

        for (int i = 1; i <= 25; i++) {
            Book b = new Book();
            b.setBid(String.format("B%03d", i));
            b.setBname(i == 1 ? "红楼梦" : "测试图书" + i);
            b.setAuthor(i == 1 ? "曹雪芹" : "作者" + i);
            b.setPrice(new BigDecimal("50.00"));
            b.setCategoryId(i % 5 == 0 ? 2 : 1);
            b.setStock(5);
            b.setAvailableStock(i % 3 == 0 ? 0 : 5);
            b.setLocation("A区-1排");
            bookMapper.insert(b);
        }
    }

    @Test
    @DisplayName("分页查询：第 1 页返回 10 条，总数 25")
    void testPage() {
        Page<Book> page = bookService.page(1, 10, null, null);

        assertEquals(10, page.getRecords().size());
        assertEquals(25, page.getTotal(), "总记录数应为 25");
        assertEquals(3, page.getPages(), "总页数应为 3");
    }

    @Test
    @DisplayName("分页查询：最后一页只剩 5 条")
    void testLastPage() {
        Page<Book> page = bookService.page(3, 10, null, null);
        assertEquals(5, page.getRecords().size());
    }

    @Test
    @DisplayName("分页查询：超出范围的页码返回空列表")
    void testPageOverflow() {
        Page<Book> page = bookService.page(999, 10, null, null);
        assertTrue(page.getRecords().isEmpty());
        assertEquals(25, page.getTotal(), "总数仍然正确");
    }

    @Test
    @DisplayName("关键词搜索：按书名命中")
    void testSearchByKeyword() {
        Page<Book> page = bookService.page(1, 10, "红楼梦", null);
        assertEquals(1, page.getTotal());
        assertEquals("红楼梦", page.getRecords().get(0).getBname());
    }

    @Test
    @DisplayName("关键词搜索：按作者命中")
    void testSearchByAuthor() {
        Page<Book> page = bookService.page(1, 10, "曹雪芹", null);
        assertEquals(1, page.getTotal());
    }

    @Test
    @DisplayName("分类筛选：只返回该分类的记录")
    void testFilterByCategory() {
        Page<Book> page = bookService.page(1, 10, null, 2);
        assertEquals(5, page.getTotal(), "分类 2 应有 5 本");
        page.getRecords().forEach(b -> assertEquals(2, b.getCategoryId()));
    }

    @Test
    @DisplayName("按 id 查询：存在时返回实体")
    void testGetById() {
        Book any = bookService.page(1, 1, null, null).getRecords().get(0);
        Book found = bookService.getById(any.getId());
        assertNotNull(found);
        assertEquals(any.getBid(), found.getBid());
    }

    @Test
    @DisplayName("按 id 查询：不存在时抛异常")
    void testGetByIdNotFound() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookService.getById(99999));
        assertTrue(ex.getMessage().contains("不存在"));
    }

    @Test
    @DisplayName("新增图书：成功并返回 id")
    void testCreate() {
        Book book = new Book();
        book.setBid("B999");
        book.setBname("新增测试书");
        book.setAuthor("测试作者");
        book.setStock(3);

        Integer id = bookService.create(book);

        assertNotNull(id, "应返回新增记录的 id");
        Book saved = bookService.getById(id);
        assertEquals("新增测试书", saved.getBname());
        assertEquals(3, saved.getAvailableStock(), "未传可借库存时应等于总库存");
    }

    @Test
    @DisplayName("新增图书：编号重复时拒绝")
    void testCreateDuplicateBid() {
        Book book = new Book();
        book.setBid("B001");   // 已存在
        book.setBname("重复编号的书");
        book.setStock(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookService.create(book));
        assertTrue(ex.getMessage().contains("已存在"));
    }
}