# Spring Boot 版图书管理系统 · 快速启动指南

> **这份指南帮你从 0 跑通 Spring Boot 版项目。**
> 目标：**7 天后**你能说"我跑通了一个 Spring Boot 项目，实现了图书查询接口"。
>
> **注意：不要跳过任何一步。** 每一步都有"验证方式"，没通过就不要往下走。

---

## 开始之前：你需要什么

| 软件 | 版本 | 检查方法 |
|---|---|---|
| **JDK** | **17**（Spring Boot 3.x 要求 17+） | 命令行执行 `java -version`，看到 17.x |
| **Maven** | 3.6+ | `mvn -v` |
| **MySQL** | 8.0 | 你已经有了（老项目在用的那个） |
| **IDEA** | 2022+ | 你已经有了 |

> ⚠️ **重要**：你的老项目编译目标是 Java 11，但 **Spring Boot 3.x 要求 Java 17**。
> 请确认已安装 JDK 17，并在 IDEA 里切换。

---

## 第 1 步：生成项目骨架（10 分钟）

### 方式 A：网页生成（推荐新手）

1. 打开 **https://start.spring.io**
2. 按下面填写：

| 选项 | 选择 |
|---|---|
| Project | **Maven** |
| Language | **Java** |
| Spring Boot | **3.2.x**（选最新的 3.2） |
| Group | `com.book` |
| Artifact | `book-admin` |
| Name | `book-admin` |
| Package name | `com.book.admin` |
| Packaging | **Jar** |
| Java | **17** |

3. 右侧 **ADD DEPENDENCIES**，点击后添加：

| 依赖 | 作用 |
|---|---|
| **Spring Web** | 提供 REST API 能力 |
| **MySQL Driver** | 连接 MySQL |
| **MyBatis Framework** | 数据访问（先加这个，后面换 MyBatis-Plus） |
| **Lombok** | 减少 getter/setter 样板代码 |

4. 点击底部 **GENERATE**，下载 `book-admin.zip`
5. 解压到一个**没有中文和空格**的路径，例如 `C:\java\book-admin`

### 方式 B：IDEA 内置（如果你会用）

`File → New → Project → Spring Initializr`，其余同上。

---

## 第 2 步：用 IDEA 打开并跑起来（20 分钟）

1. IDEA 中 `File → Open`，选择 `C:\java\book-admin` 目录下的 **`pom.xml`**
2. 选择 **Open as Project**
3. 等待 Maven 下载依赖（第一次可能要 5-10 分钟）
4. 找到 `src/main/java/com/book/admin/BookAdminApplication.java`
5. 右键 → **Run 'BookAdminApplication'**

### 验证方式

控制台出现类似内容：

```
Tomcat started on port(s): 8080 (http)
Started BookAdminApplication in 2.345 seconds
```

**看到这个就成功了。**

> ⚠️ 如果报错说端口 8080 被占用，先确认你的老项目 Tomcat 已经关闭。

---

## 第 3 步：写第一个接口（30 分钟）

### 3.1 创建 Controller

在 `src/main/java/com/book/admin/` 下新建包 `controller`，然后新建 `HelloController.java`：

```java
package com.book.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第一个接口：验证 Spring Boot 能正常工作。
 *
 * @RestController  = @Controller + @ResponseBody
 *                    表示这个类的所有方法返回值直接作为 HTTP 响应体
 * @GetMapping("/hello") 表示处理 GET /hello 请求
 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello Spring Boot!";
    }
}
```

### 3.2 重启应用

1. 点击 IDEA 顶部的**红色停止按钮**
2. 再次点击**绿色运行按钮**

### 3.3 验证方式

浏览器访问：**http://localhost:8080/hello**

看到 `Hello Spring Boot!` = **成功。**

> 🎉 **到这里你已经完成了最重要的一步**：跑通了 Spring Boot。
> 现在你的简历可以写"熟悉 Spring Boot，能独立搭建 RESTful 接口"。

---

## 第 4 步：连接 MySQL（30 分钟）

### 4.1 修改配置

打开 `src/main/resources/application.properties`，把内容全部替换成：

```properties
# ===== 服务器配置 =====
server.port=8080

# ===== 数据库连接 =====
spring.datasource.url=jdbc:mysql://localhost:3306/book_manage?useSSL=false&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ===== MyBatis 配置 =====
# 自动把数据库下划线字段映射成 Java 驼峰命名
# 例如 publish_date -> publishDate
mybatis.configuration.map-underscore-to-camel-case=true

# 打印 SQL 到控制台（开发阶段用，上线要关掉）
mybatis.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl

# ===== 日志 =====
logging.level.com.book.admin=debug
```

> ⚠️ **把 `spring.datasource.password` 改成你自己的 MySQL 密码。**
> 这个项目直接复用了你老项目的 `book_manage` 数据库，**不需要重新建表**。

### 4.2 验证方式

重启应用，控制台**没有报数据库连接错误**即可。

如果报 `Access denied`，说明密码错了，改 `spring.datasource.password`。

---

## 第 5 步：查询图书列表（1 小时）

### 5.1 创建实体类

新建包 `entity`，创建 `Book.java`：

```java
package com.book.admin.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 图书实体，对应数据库 books 表。
 * @Data 是 Lombok 注解，自动生成 getter/setter/toString
 */
@Data
public class Book {

    private Integer id;
    /** 图书编号，如 B001 */
    private String bid;
    private String isbn;
    /** 书名 */
    private String bname;
    private String author;
    private String publisher;
    /** 出版日期 */
    private LocalDate publishDate;
    private BigDecimal price;
    private Integer categoryId;
    /** 总库存 */
    private Integer stock;
    /** 可借库存 */
    private Integer availableStock;
    /** 存放位置 */
    private String location;
    private String description;
}
```

### 5.2 创建 Mapper

新建包 `mapper`，创建 `BookMapper.java`：

```java
package com.book.admin.mapper;

import com.book.admin.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 图书 Mapper。
 * @Mapper 让 MyBatis 自动生成实现类（JDK 动态代理）。
 */
@Mapper
public interface BookMapper {

    /**
     * 查询全部图书。
     * 注意：这里用 @Select 注解写 SQL，也可以写在 XML 里。
     */
    @Select("SELECT * FROM books ORDER BY id")
    List<Book> findAll();
}
```

> 💡 **注意**：`publish_date` 会自动映射到 `publishDate`，因为配置了 `map-underscore-to-camel-case=true`。

### 5.3 创建 Service

新建包 `service`，创建 `BookService.java`：

```java
package com.book.admin.service;

import com.book.admin.entity.Book;
import com.book.admin.mapper.BookMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 图书业务层。
 * @Service 标记为 Spring 管理的 Bean，可被自动注入。
 */
@Service
public class BookService {

    private final BookMapper bookMapper;

    /**
     * 构造器注入（Spring 官方推荐方式，比 @Autowired 字段注入更好）。
     */
    public BookService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public List<Book> listAll() {
        return bookMapper.findAll();
    }
}
```

### 5.4 创建 Controller

新建 `BookController.java`：

```java
package com.book.admin.controller;

import com.book.admin.entity.Book;
import com.book.admin.service.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 图书接口。
 * @RequestMapping("/api/book") 给这个类下所有接口加统一前缀
 */
@RestController
@RequestMapping("/api/book")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * GET /api/book/list
     * 返回全部图书。
     */
    @GetMapping("/list")
    public List<Book> list() {
        return bookService.listAll();
    }
}
```

### 5.5 验证方式

1. 重启应用
2. 浏览器访问：**http://localhost:8080/api/book/list**

**你应该看到一大段 JSON**，里面是你数据库里的 31 本书。

```json
[
  {"id":1,"bid":"B001","bname":"红楼梦","author":"曹雪芹",...},
  {"id":2,"bid":"B002",...}
]
```

> 🎉 **如果看到了**，说明你完成了 Spring Boot 的核心链路：
> **Controller → Service → Mapper → MySQL**
>
> **这就是简历上可以写的东西**：
> "基于 Spring Boot + MyBatis 实现图书列表查询接口，打通 Controller-Service-Mapper 三层架构"

---

## 第 6 步：理解你写的东西（30 分钟）

跑通以后，**花 30 分钟搞清楚以下几个问题**。面试一定会问：

### Q1：`@RestController` 和 `@Controller` 有什么区别？

- `@Controller`：返回的是**视图名**（配合 JSP/Thymeleaf）
- `@RestController` = `@Controller` + `@ResponseBody`，返回值直接作为**响应体**（JSON）

### Q2：`@Service`、`@Mapper` 是什么？

Spring 的**组件扫描**机制。启动时扫描到这些注解，自动创建对象放进 IoC 容器，需要时自动注入。

### Q3：依赖注入为什么推荐构造器注入？

- 字段注入（`@Autowired` 打在字段上）无法用 `final`，对象可能为 null
- 构造器注入能保证**依赖不为空**，且便于单元测试时传入 mock

### Q4：MyBatis 的 `@Mapper` 做了什么？

MyBatis 启动时扫描到接口上的 `@Mapper`，用 **JDK 动态代理**生成实现类，把方法调用翻译成 SQL 执行。

### Q5：`map-underscore-to-camel-case` 解决了什么问题？

数据库字段是 `publish_date`（下划线），Java 属性是 `publishDate`（驼峰）。开启后自动映射，不用手写别名。

---

## 第 7 步：提交到 GitHub（30 分钟）

```bash
cd C:\java\book-admin

# 1. 初始化仓库
git init

# 2. 创建 .gitignore（重要！别把 target 和 IDEA 配置传上去）
```

新建 `.gitignore` 文件，内容：

```gitignore
target/
out/
*.class
.idea/
*.iml
*.log
```

```bash
# 3. 提交
git add .
git commit -m "feat: Spring Boot 图书管理系统 - 基础骨架与图书查询接口"

# 4. 关联远程仓库（先在 GitHub 上新建一个空仓库，名字 book-admin）
git remote add origin https://github.com/x15737876973-art/book-admin.git
git branch -M main
git push -u origin main
```

---

## 完成后的自检清单

做到以下全部，你就完成了"7 天跑通 Spring Boot"：

- [ ] IDEA 能运行 `BookAdminApplication`
- [ ] 访问 `http://localhost:8080/hello` 返回 `Hello Spring Boot!`
- [ ] 访问 `http://localhost:8080/api/book/list` 返回图书 JSON
- [ ] 能说清楚 `@RestController` / `@Service` / `@Mapper` 各自作用
- [ ] 能说清楚 Controller → Service → Mapper 的调用链路
- [ ] 代码已推到 GitHub

---

## 下一步做什么（第 2 周）

跑通之后，按这个顺序继续：

| 顺序 | 任务 | 学到什么 |
|---|---|---|
| 1 | **换成 MyBatis-Plus** | 省掉手写 SQL，体验框架威力 |
| 2 | **加分页查询** | `IPage` 分页，对比你 Servlet 版手写分页 |
| 3 | **加统一返回格式** | `Result<T>` 封装 code/message/data |
| 4 | **加全局异常处理** | `@ControllerAdvice` + `@ExceptionHandler` |
| 5 | **加参数校验** | `@Valid` + `@NotBlank` 等注解 |

---

## 常见问题

### Q：报 `Port 8080 was already in use`

老项目的 Tomcat 还在跑。关掉它，或改 `server.port=8081`。

### Q：报 `Access denied for user 'root'@'localhost'`

`spring.datasource.password` 写错了，改成你自己的 MySQL 密码。

### Q：报 `Unknown database 'book_manage'`

你的 MySQL 里没有这个库。执行老项目根目录的 `database_init.sql`。

### Q：访问接口返回 500，控制台报 `Invalid bound statement`

Mapper 接口没被扫描到。确认 `BookMapper` 上有 `@Mapper` 注解。

### Q：JSON 里的 `publishDate` 是 null

数据库里这个字段本来就是 null，或者 `map-underscore-to-camel-case` 没生效。

### Q：中文乱码

确认数据库连接串里有 `characterEncoding=UTF-8`。

---

## 给你的一句话

**你今天跑通了 `/hello`，就已经从"没用过 Spring Boot"变成"用过 Spring Boot"了。**

**这一步的难度不在技术，在于不放弃。** 第一次配环境总会有各种报错，遇到就搜，搜不到就问。

**7 天后你会有：**
- 一个能跑的项目
- 简历上的一行 "Spring Boot"
- 面试时能讲清楚的三层架构

**这就是从 0 到 1。**
---

## ⚠️ 两个必踩的坑（已验证，提前告诉你）

### 坑 1：你的 Maven 本地仓库路径含中文

**症状**：

```
Error opening zip file or JAR manifest missing: C:\Users\谢\.m2\repository\...
[Mockito] Could not initialize plugin: interface org.mockito.plugins.MockMaker
```

**原因**：你的 Windows 用户名是「谢」，Maven 把依赖下到 `C:\Users\谢\.m2\repository`。
Mockito 需要反射加载 `byte-buddy-agent.jar`，**中文路径会导致 zip 解析失败**。

**解决方案（任选其一）**：

**方案 A：指定纯英文的本地仓库（推荐，一次配置永久生效）**

在 `C:\Users\谢\.m2\settings.xml`（没有就新建）里加入：

```xml
<settings>
  <localRepository>C:\m2repo</localRepository>
</settings>
```

然后 IDEA 里 `File → Settings → Build, Execution, Deployment → Build Tools → Maven`，
把 `Local repository` 改成 `C:\m2repo`。

**方案 B：命令行临时指定**

```bash
mvn -Dmaven.repo.local=C:\m2repo clean test
```

**我的验证结果**：用方案 B 后，骨架的 10 个测试全部通过。

### 坑 2：JDK 版本必须用 17

**症状**：

```
无效的目标发行版: 17
或 Unsupported class file major version 61
```

**原因**：Spring Boot 3.x **强制要求 JDK 17 及以上**。
你电脑上默认是 JDK 25，但需要显式指定 17（JDK 25 在某些依赖上有兼容问题）。

**解决方案**：

1. 你已经有 JDK 17：`C:\Users\谢\.jdks\ms-17.0.20.1`
2. IDEA 中：`File → Project Structure → Project`
   - **SDK** 选 `17`
   - **Language level** 选 `17`
3. `File → Project Structure → Modules → 你的模块 → Sources`
   - **Language level** 也选 17
4. `Settings → Build, Execution, Deployment → Compiler → Java Compiler`
   - **Target bytecode version** 选 17

### 坑 3：不要用 Lombok（新手慎用）

**症状**：

```
找不到符号: 方法 getBname()
位置: 类 com.book.admin.entity.Book
```

**原因**：Lombok 需要 IDE 装插件 + Maven 配注解处理器，新手极易踩坑。

**本项目骨架已经不用 Lombok**，实体类的 getter/setter 是手写的。
虽然代码长一点，但**零配置、零踩坑**。

> 如果以后想用 Lombok，需要额外做：
> 1. IDEA 装 Lombok 插件
> 2. `Settings → Build → Compiler → Annotation Processors` 勾选 `Enable annotation processing`
> 3. pom.xml 里确认有 `lombok` 依赖

---

## 📦 完整骨架代码在哪里

本仓库 `docs/springboot-skeleton/` 目录下有**可直接运行的完整骨架**：

```
docs/springboot-skeleton/
├── pom.xml                                    # Maven 配置（Spring Boot 3.2.5 + MyBatis-Plus 3.5.5）
├── src/main/java/com/book/admin/
│   ├── BookAdminApplication.java              # 启动类
│   ├── common/
│   │   ├── Result.java                        # 统一响应结构
│   │   └── GlobalExceptionHandler.java        # 全局异常处理
│   ├── config/MybatisPlusConfig.java          # 分页插件配置
│   ├── controller/BookController.java         # 图书接口（分页/详情/新增）
│   ├── entity/Book.java                       # 实体类
│   ├── mapper/BookMapper.java                 # MyBatis-Plus Mapper
│   └── service/BookService.java               # 业务层
├── src/main/resources/application.properties  # 配置（改成你的密码）
└── src/test/
    ├── java/com/book/admin/BookServiceTest.java   # 10 个单元测试
    └── resources/
        ├── application-test.properties         # 测试配置（H2）
        └── test-schema.sql                     # 测试建表脚本
```

**已验证**：用 JDK 17 + 英文 Maven 仓库，`mvn test` 通过 **10/10**。

### 怎么用这份骨架

**方式一：直接复制（推荐）**

```bash
# 1. 复制骨架到新目录
cp -r C:\java\untitled6\docs\springboot-skeleton C:\java\book-admin

# 2. 进入目录
cd C:\java\book-admin

# 3. 修改 application.properties 里的数据库密码

# 4. 运行
mvn spring-boot:run
```

**方式二：用 IDEA 打开**

`File → Open` → 选择 `docs/springboot-skeleton/pom.xml` → Open as Project

### 启动后能访问的接口

| 接口 | 说明 |
|---|---|
| `GET /api/books` | 分页查询（默认第 1 页，每页 10 条） |
| `GET /api/books?page=2&pageSize=20` | 指定分页 |
| `GET /api/books?keyword=java` | 关键词搜索 |
| `GET /api/books?categoryId=4` | 分类筛选 |
| `GET /api/books/1` | 根据 id 查详情 |
| `GET /api/books/ping` | 环境自测 |
| `POST /api/books` | 新增图书（JSON 请求体） |

### 骨架已实现的 CRUD

- ✅ 分页查询（MyBatis-Plus `selectPage`）
- ✅ 关键词模糊搜索
- ✅ 分类筛选
- ✅ 根据 id 查详情
- ✅ 新增图书（含编号唯一性校验）
- ✅ 统一响应格式
- ✅ 全局异常处理
- ✅ 10 个单元测试

### 骨架还没做的（下一周的任务）

- ⬜ 修改 / 删除图书
- ⬜ 借阅模块（借书还书 + `@Transactional`）
- ⬜ 登录鉴权（JWT）
- ⬜ 分类管理

---

## 验证记录（给你信心）

我用你的环境实际跑过：

```
BUILD SUCCESS
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
```

**这份骨架不是纸上的代码，是验证过的。** 你复制过去就能跑。