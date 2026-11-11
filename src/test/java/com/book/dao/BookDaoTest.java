package com.book.dao;

import com.book.entity.Book;
import com.book.entity.PageResult;
import com.book.entity.User;
import com.book.test.DatabaseTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link BookDao} 与 {@link UserDao} 集成测试（基于 H2 内存库）。
 *
 * <p>重点验证：</p>
 * <ul>
 *   <li><b>分页</b>：页码边界、总数、总页数、超范围页</li>
 *   <li><b>检索</b>：模糊匹配、分类筛选、无结果</li>
 *   <li><b>SQL 注入防护</b>：注入串被当作普通文本，不返回全表</li>
 *   <li><b>CRUD</b>：增删改查与库存原子更新</li>
 *   <li><b>事务</b>：异常时整体回滚</li>
 * </ul>
 */
@DisplayName("BookDao / UserDao 数据访问")
class BookDaoTest extends DatabaseTestBase {

    private BookDao bookDao;
    private UserDao userDao;
    private int catLiterature;
    private int catComputer;

    @BeforeEach
    void setUp() throws Exception {
        bookDao = new BookDao();
        userDao = new UserDao();
        catLiterature = insertCategory("文学", "文学类图书");
        catComputer = insertCategory("计算机", "计算机类图书");

        // 25 本书：20 本文学 + 5 本计算机，便于验证分页与分类筛选
        for (int i = 1; i <= 25; i++) {
            boolean computer = (i % 5 == 0);
            String name = (i == 1) ? "红楼梦" : ("测试图书" + i);
            String author = (i == 1) ? "曹雪芹" : ("作者" + i);
            insertBook(String.format("B%03d", i), name, author,
                    computer ? catComputer : catLiterature,
                    5, (i % 3 == 0) ? 0 : 5);
        }
    }

    // ======================================================================
    @Nested
    @DisplayName("分页查询")
    class Pagination {

        @Test
        @DisplayName("第 1 页返回 10 条，总数 25，总页数 3")
        void firstPage() {
            PageResult<Book> page = bookDao.findPage(1, 10, null);
            assertEquals(10, page.getRecords().size());
            assertEquals(25, page.getTotalRecords());
            assertEquals(3, page.getTotalPages());
            assertTrue(page.isHasNext());
            assertFalse(page.isHasPrevious());
            assertEquals(1, page.getStartIndex());
            assertEquals(10, page.getEndIndex());
        }

        @Test
        @DisplayName("最后一页只返回剩余 5 条且无下一页")
        void lastPage() {
            PageResult<Book> page = bookDao.findPage(3, 10, null);
            assertEquals(5, page.getRecords().size());
            assertFalse(page.isHasNext());
            assertTrue(page.isHasPrevious());
            assertEquals(21, page.getStartIndex());
            assertEquals(25, page.getEndIndex());
        }

        @ParameterizedTest(name = "页码={0}")
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("非法页码（0/负数）自动纠正为第 1 页")
        void normalizesInvalidPageNumber(int pageNumber) {
            PageResult<Book> page = bookDao.findPage(pageNumber, 10, null);
            assertEquals(1, page.getPageNumber());
            assertEquals(10, page.getRecords().size());
        }

        @Test
        @DisplayName("超出总页数的页码返回空列表，不报错")
        void beyondLastPage() {
            PageResult<Book> page = bookDao.findPage(999, 10, null);
            assertTrue(page.getRecords().isEmpty());
            assertEquals(25, page.getTotalRecords(), "总数仍应正确");
        }

        @Test
        @DisplayName("按分类筛选后总数只统计该类")
        void filterByCategory() {
            PageResult<Book> page = bookDao.findPage(1, 10, catComputer);
            assertEquals(5, page.getTotalRecords());
            assertEquals(5, page.getRecords().size());
            page.getRecords().forEach(b -> assertEquals(catComputer, b.getCategoryId()));
        }

        @Test
        @DisplayName("每页条数非法时回退到默认值")
        void invalidPageSize() {
            PageResult<Book> page = bookDao.findPage(1, 0, null);
            assertTrue(page.getPageSize() > 0, "pageSize 应被纠正为正数");
        }
    }

    // ======================================================================
    @Nested
    @DisplayName("检索")
    class Search {

        @Test
        @DisplayName("按书名精确检索")
        void searchByTitle() {
            PageResult<Book> page = bookDao.searchPage("红楼梦", null, 1, 10);
            assertEquals(1, page.getTotalRecords());
            assertEquals("红楼梦", page.getRecords().get(0).getBname());
        }

        @Test
        @DisplayName("按作者检索")
        void searchByAuthor() {
            PageResult<Book> page = bookDao.searchPage("曹雪芹", null, 1, 10);
            assertEquals(1, page.getTotalRecords());
            assertEquals("曹雪芹", page.getRecords().get(0).getAuthor());
        }

        @Test
        @DisplayName("模糊匹配命中的记录数正确")
        void searchFuzzy() {
            PageResult<Book> page = bookDao.searchPage("测试图书", null, 1, 10);
            assertEquals(24, page.getTotalRecords());
            assertEquals(10, page.getRecords().size(), "分页应生效");
        }

        @Test
        @DisplayName("检索不存在的书名返回空")
        void searchNoResult() {
            PageResult<Book> page = bookDao.searchPage("不存在的书名XYZ", null, 1, 10);
            assertEquals(0, page.getTotalRecords());
            assertTrue(page.getRecords().isEmpty());
        }

        @Test
        @DisplayName("检索结果可按分类进一步筛选")
        void searchWithCategory() {
            PageResult<Book> page = bookDao.searchPage("测试图书", catComputer, 1, 10);
            assertEquals(5, page.getTotalRecords(), "计算机类应只有 5 本");
        }
    }

    // ======================================================================
    @Nested
    @DisplayName("SQL 注入防护")
    class SqlInjection {

        @ParameterizedTest(name = "payload={0}")
        @ValueSource(strings = {
                "' OR '1'='1",
                "'; DROP TABLE books; --",
                "' UNION SELECT * FROM users --",
                "admin'--",
                "1' OR '1'='1"
        })
        @DisplayName("注入串被当作普通文本，不会返回全表数据")
        void injectionIsTreatedAsLiteral(String payload) {
            PageResult<Book> page = bookDao.searchPage(payload, null, 1, 10);
            assertNotEquals(25, page.getTotalRecords(),
                    "注入成功会返回全部 25 条，此处必须失败");
        }

        @Test
        @DisplayName("DROP TABLE 注入后表仍然存在")
        void tableSurvivesDropInjection() {
            bookDao.searchPage("'; DROP TABLE books; --", null, 1, 10);
            assertEquals(25, bookDao.count(), "books 表应完好无损");
        }
    }

    // ======================================================================
    @Nested
    @DisplayName("CRUD 与库存")
    class Crud {

        @Test
        @DisplayName("新增 → 查询 → 更新 → 删除 全链路")
        void fullCrud() {
            Book book = new Book();
            book.setBid("B999");
            book.setBname("新增测试书");
            book.setAuthor("测试作者");
            book.setStock(3);
            book.setAvailableStock(3);
            book.setCategoryId(catLiterature);

            assertEquals(1, bookDao.addBook(book));
            assertEquals(26, bookDao.count());

            Book loaded = bookDao.findByBid("B999");
            assertNotNull(loaded);
            assertEquals("新增测试书", loaded.getBname());

            loaded.setBname("改名后");
            assertEquals(1, bookDao.update(loaded));
            assertEquals("改名后", bookDao.findByBid("B999").getBname());

            assertEquals(1, bookDao.delete("B999"));
            assertNull(bookDao.findByBid("B999"));
            assertEquals(25, bookDao.count());
        }

        @Test
        @DisplayName("借书库存 -1，还书库存 +1")
        void stockChanges() {
            Book before = bookDao.findByBid("B001");
            int available = before.getAvailableStock();

            assertEquals(1, bookDao.decreaseAvailableStock(before.getId()));
            assertEquals(available - 1, bookDao.findById(before.getId()).getAvailableStock());

            assertEquals(1, bookDao.increaseAvailableStock(before.getId()));
            assertEquals(available, bookDao.findById(before.getId()).getAvailableStock());
        }

        @Test
        @DisplayName("库存为 0 时扣减失败（防超卖核心逻辑）")
        void cannotDecreaseBelowZero() throws Exception {
            // B003 的 available_stock 初始为 0（i%3==0）
            Book soldOut = bookDao.findByBid("B003");
            assertEquals(0, soldOut.getAvailableStock());

            int affected = bookDao.decreaseAvailableStock(soldOut.getId());
            assertEquals(0, affected, "库存为 0 时 UPDATE 应影响 0 行，而非扣成负数");
            assertEquals(0, bookDao.findById(soldOut.getId()).getAvailableStock());
        }

        @Test
        @DisplayName("查询不存在的记录返回 null 而非异常")
        void findMissing() {
            assertNull(bookDao.findByBid("NOT-EXIST"));
            assertNull(bookDao.findById(99999));
            assertNull(bookDao.findByBid(null));
        }

        @Test
        @DisplayName("统计方法返回正确数值")
        void statistics() {
            assertEquals(25, bookDao.count());
            assertEquals(5, bookDao.countByCategoryId(catComputer));
            assertEquals(20, bookDao.countByCategoryId(catLiterature));
            assertTrue(bookDao.countTotalStock() > 0);
        }
    }

    // ======================================================================
    @Nested
    @DisplayName("用户与密码")
    class Users {

        @Test
        @DisplayName("插入的用户密码为 BCrypt 密文且可校验")
        void userPasswordIsHashed() throws Exception {
            insertUser("alice", "alicePwd123", "reader");
            User user = userDao.findByUsernameForLogin("alice");
            assertNotNull(user);
            assertTrue(com.book.util.BCryptUtil.isBcryptHash(user.getPassword()),
                    "数据库中密码必须是 BCrypt 密文");
            assertTrue(com.book.util.BCryptUtil.checkpw("alicePwd123", user.getPassword()));
            assertFalse(com.book.util.BCryptUtil.checkpw("wrongPwd", user.getPassword()));
        }

        @Test
        @DisplayName("existsByUsername 正确判断重名")
        void detectsDuplicateUsername() throws Exception {
            insertUser("bob", "bobPwd123", "reader");
            assertTrue(userDao.existsByUsername("bob"));
            assertFalse(userDao.existsByUsername("nobody"));
        }

        @Test
        @DisplayName("重置密码后可用默认密码登录")
        void resetPasswordWorks() throws Exception {
            int id = insertUser("carol", "oldPwd123", "reader");
            userDao.resetPassword(id);

            User user = userDao.findById(id);
            assertTrue(com.book.util.BCryptUtil.checkpw("123456", user.getPassword()));
            assertFalse(com.book.util.BCryptUtil.checkpw("oldPwd123", user.getPassword()),
                    "旧密码应失效");
        }
    }

    // ======================================================================
    @Nested
    @DisplayName("事务")
    class Transaction {

        @Test
        @DisplayName("事务内抛异常时整体回滚")
        void rollbackOnException() {
            Book book = bookDao.findByBid("B001");
            int before = book.getAvailableStock();

            assertThrows(IllegalStateException.class, () ->
                    com.book.util.DBUtil.inTransaction(conn -> {
                        bookDao.decreaseAvailableStock(book.getId());
                        throw new IllegalStateException("模拟业务失败");
                    }));

            int after = bookDao.findById(book.getId()).getAvailableStock();
            assertEquals(before, after, "异常后库存必须回到原值");
        }

        @Test
        @DisplayName("事务正常提交")
        void commitOnSuccess() {
            Book book = bookDao.findByBid("B002");
            int before = book.getAvailableStock();

            com.book.util.DBUtil.inTransaction(conn ->
                    bookDao.decreaseAvailableStock(book.getId()));

            assertEquals(before - 1, bookDao.findById(book.getId()).getAvailableStock());
        }

        @Test
        @DisplayName("事务内多个 DAO 操作共享同一连接（关键：否则回滚无效）")
        void multipleOperationsShareConnection() {
            Book book = bookDao.findByBid("B004");
            int before = book.getAvailableStock();

            assertThrows(RuntimeException.class, () ->
                    com.book.util.DBUtil.inTransaction(conn -> {
                        bookDao.decreaseAvailableStock(book.getId());
                        bookDao.decreaseAvailableStock(book.getId());
                        throw new RuntimeException("第二步之后失败");
                    }));

            assertEquals(before, bookDao.findById(book.getId()).getAvailableStock(),
                    "两步扣减都应被回滚");
        }
    }

    // ======================================================================
    @Nested
    @DisplayName("Agent 检索")
    class AgentSearch {

        @Test
        @DisplayName("按关键词检索命中")
        void retrievesByKeyword() {
            List<Book> hits = bookDao.searchForAgent(java.util.Collections.singletonList("红楼梦"), 8);
            assertFalse(hits.isEmpty());
            assertTrue(hits.stream().anyMatch(b -> b.getBname().contains("红楼梦")));
        }

        @Test
        @DisplayName("关键词无命中时返回空列表（用于触发防幻觉话术）")
        void noMatchReturnsEmpty() {
            List<Book> hits = bookDao.searchForAgent(
                    java.util.Collections.singletonList("完全不存在的书XYZ"), 8);
            assertTrue(hits.isEmpty(), "无命中应返回空，而不是兜底返回全部图书");
        }

        @Test
        @DisplayName("空关键词列表返回空，避免误返回全库")
        void emptyKeywordsReturnsEmpty() {
            assertTrue(bookDao.searchForAgent(java.util.Collections.emptyList(), 8).isEmpty());
        }

        @Test
        @DisplayName("recommendBest 只返回有库存的书")
        void recommendBestOnlyAvailable() {
            List<Book> books = bookDao.recommendBest(10);
            assertFalse(books.isEmpty());
            books.forEach(b -> assertTrue(b.getAvailableStock() > 0, "推荐的书必须有库存"));
        }
    }
}