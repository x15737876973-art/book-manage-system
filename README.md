# 图书管理系统（Book Manage System）

基于 **Java Web（Servlet + JSP）+ MySQL + Maven** 的图书管理系统，包含管理员与读者两种角色，支持图书、分类、借阅、罚款、预约管理与数据统计，适合课程设计、实训项目与 Java Web 入门学习。

> 仓库地址：https://github.com/x15737876973-art/book-manage-system

---

## 项目简介

- **后端**：Java Servlet（`@WebServlet` 注解映射）+ JDBC（原生 SQL + PreparedStatement），无 Spring 等重型框架
- **前端**：JSP + JSTL + 原生 CSS/JavaScript，服务端渲染、表单提交
- **数据库**：MySQL 8.x，6 张表（用户 / 分类 / 图书 / 借阅 / 罚款 / 预约）
- **构建**：Maven，打包为 `war`，部署方式二选一：IDEA 内嵌或外置 Tomcat 运行 / war 包放进 Tomcat `webapps`
- **架构分层**：`servlet（控制层）→ dao（数据访问层）→ entity（实体）`，配合 `filter`（编码、登录校验）与 `listener`（启动检查）

## 功能特性

### 管理员端（`/admin/*`）

- [x] 管理员登录 / 退出登录
- [x] 数据统计总览（图书数量、用户数量、借阅情况）
- [x] 借阅排行榜统计
- [x] 图书管理：列表、新增、编辑、删除、库存维护
- [x] 图书编号（bid）与 ISBN 管理
- [x] 图书分类管理
- [x] 用户（读者）管理：列表、新增、编辑
- [x] 借阅管理：借书、还书、修改应还日期
- [x] 逾期记录查询与罚款记录生成
- [x] 个人信息查看与修改
- [x] 一键导入示例图书 / 测试数据（便于演示）

### 读者端（`/reader/*`）

- [x] 读者注册 / 登录 / 退出
- [x] 图书检索（按书名、作者、分类、ISBN 等条件）
- [x] 在线借书 / 还书（受可借库存限制）
- [x] 我的借阅记录查看（含逾期状态）
- [x] 我的罚款记录查看与缴纳状态
- [x] 图书预约与预约记录查看
- [x] 个人资料修改

### 系统通用

- [x] 全局 UTF-8 编码过滤器，解决中文乱码
- [x] 登录拦截过滤器：未登录访问 `/admin/` 或 `/reader/` 自动跳转登录页
- [x] 角色权限校验：读者访问管理员页面会被拒绝
- [x] 应用启动监听器：打印数据库连接状态与数据统计

## 技术栈

| 层次 | 技术 / 组件 | 版本（见 pom.xml） |
|---|---|---|
| 开发语言 | Java | 8 / 11（推荐）※ |
| 构建工具 | Apache Maven | 3.6+ |
| Web 容器 | Servlet API / JSP API | 3.1.0 / 2.3.1 |
| 视图标签 | JSTL | 1.2 |
| 数据库 | MySQL | 8.0 |
| JDBC 驱动 | mysql-connector-java | 8.0.33 |
| 前端 | JSP + 原生 CSS / JavaScript | — |
| 服务器 | Apache Tomcat | 8.5 / 9.0 ※ |
| 开发工具 | IntelliJ IDEA | 2022.3+ |

※ **版本约定**：`pom.xml` 中 `maven.compiler.source/target` 已统一为 **`11`**。原因是 Tomcat 8.5/9 以 Java 8 为基线，若编译版本过高（例如 17、25），运行时会报 `UnsupportedClassVersionError`。请确保 IDEA 的 `Project SDK` 与 `Language level` 与之一致（见“常见问题 Q2”）。

## 项目结构

```
book-manage-system
├── pom.xml                          # Maven 配置，finalName=book_manage
├── .gitignore                       # 忽略 target/、.idea/、*.db、*.zip 等
├── README.md
├── database_init.sql                # MySQL 建库建表 + 初始数据（推荐使用）
├── src
│   ├── main
│   │   ├── java/com/book
│   │   │   ├── dao         # BookDao / BorrowDao / CategoryDao / FineDao / ReservationDao / UserDao
│   │   │   ├── entity      # Book / BorrowRecord / Category / FineRecord / Reservation / User
│   │   │   ├── filter      # EncodingFilter（编码） / LoginCheckFilter（登录+角色校验）
│   │   │   ├── listener    # AppStartupListener（启动检查数据库）
│   │   │   ├── main        # MainTest（本地测试入口）
│   │   │   ├── servlet     # 各业务 Servlet（见“页面与接口说明”）
│   │   │   └── util        # DBUtil（JDBC 连接与资源关闭） / InsertBooksUtil（示例数据）
│   │   ├── resources
│   │   │   └── *.sql       # mysql_init.sql（MySQL，与 database_init.sql 同构）
│   │   │                   # 历史脚本：book_manage.sql / init.sql / simple_init.sql（SQL Server 版本，保留备查）
│   │   └── webapp
│   │       ├── WEB-INF/web.xml      # 编码过滤器、登录过滤器、欢迎页 index.jsp
│   │       ├── admin/              # 管理员页面（index、statOverview、bookList、userList…）
│   │       ├── reader/             # 读者页面（index、bookSearch、myBorrows、myFines…）
│   │       ├── css/                # style.css / supplement.css
│   │       ├── js/common.js
│   │       ├── index.jsp           # 系统入口
│   │       ├── login.jsp           # 登录页
│   │       └── register.jsp        # 读者注册页
└── target/                         # Maven 构建产物（已被 .gitignore 忽略）
```

> `.mvn/`、`.feisuan/`、`book_manage.db`、`project_source_code.zip` 不会进入仓库：前两个是工具/编辑器目录，后两个已由 `.gitignore`（`*.db`、`*.zip`）排除。

## 环境准备

| 软件 | 要求 |
|---|---|
| JDK | 8 或 11（与 Tomcat 版本匹配） |
| Maven | 3.6 以上（IDEA 自带亦可） |
| MySQL | 5.7 / 8.0，需记住 root 密码 |
| Tomcat | 8.5.x 或 9.0.x（注意不要用 Tomcat 10+，其包名为 `jakarta.servlet`，与本项目 `javax.servlet` 不兼容） |
| IDEA | 2022.3 及以上（Community 版用外置 Tomcat 方式运行） |

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/x15737876973-art/book-manage-system.git
cd book-manage-system
```

### 2. 初始化数据库

项目提供了多份脚本，**只需执行 `database_init.sql` 一份**（MySQL 语法，建库 + 6 张表 + 初始数据）：

**方式一：命令行**

```bash
mysql -u root -p < database_init.sql
```

**方式二：IDEA / Navicat / DataGrip 图形化**

1. IDEA 右侧 `Database` 面板 → `+` → `Data Source` → `MySQL`，填写主机 `localhost`、端口 `3306`、用户名 `root`、密码；
2. 右键数据源 → `Open Console`，把 `database_init.sql` 全部内容粘贴进去（**不选中 USE 之外的库**）；
3. 执行后刷新，即可在 `book_manage` 库下看到 `users / categories / books / borrow_records / fine_records / reservations` 六张表。

> 脚本内部已包含 `CREATE DATABASE IF NOT EXISTS book_manage` 与 `USE book_manage`，无需手动建库。
> `src/main/resources/` 下的 `book_manage.sql`、`init.sql`、`simple_init.sql` 是早期 **SQL Server** 版本，用 MySQL 执行会报语法错误，请勿混用。

### 3. 配置数据库连接

连接信息**不写在源码里**，由 `DBUtil` 按以下优先级读取（高 → 低）：

| 优先级 | 来源 | 写法示例 |
|---|---|---|
| 1 | JVM 系统属性 | IDEA `Run → Edit Configurations → VM options` 填 `-Ddb.password=你的密码` |
| 2 | 环境变量 | `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` |
| 3 | 配置文件 | 把 `src/main/resources/db.properties.example` 复制为 `db.properties` 后修改 |
| 4 | 内置默认值 | `root` + 空密码 + 本地 `book_manage` 库，仅用于跑通演示 |

**推荐做法（配置文件）**：

1. 在 IDEA 中右键 `src/main/resources/db.properties.example` → `Copy`，同目录 `Paste`，重命名为 `db.properties`；
2. 打开 `db.properties`，把 `db.password=123456` 改成你本机 MySQL 的实际密码；
3. `db.properties` 已被 `.gitignore` 忽略，**不会**进入公开仓库，仓库里只保留模板 `db.properties.example`。

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/book_manage?useSSL=false&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
db.username=root
db.password=你的MySQL密码
```

> 只想临时改一次密码、不想建文件？直接在 IDEA 运行配置的 `VM options` 里写 `-Ddb.password=你的密码` 即可，优先级高于配置文件。

### 4. 在 IDEA 中运行

1. `File → Open`，选择项目根目录下的 `pom.xml`，按提示 `Open as Project`，等待 Maven 依赖下载完成；
2. `File → Project Structure → Project`：SDK 选择 JDK 8 或 11，Language level 与之对齐；
3. `File → Project Structure → Facets`：确认存在 `Web` facet，`Web Resource Directory` 指向 `src/main/webapp`；
4. `File → Project Structure → Artifacts`：确认存在 `book_manage:war exploded`（若没有则 `+ → Web Application: Exploded → From Modules`）；
5. `Run → Edit Configurations → + → Tomcat Server → Local`：
   - `Server` 页指定本机 Tomcat 安装目录；
   - `Deployment` 页 `+ → Artifact` 选 `book_manage:war exploded`，`Application context` 填 `/book_manage`；
6. 点击运行，日志出现 `图书管理系统启动中...` 与数据库连接成功提示后，浏览器访问：

```
http://localhost:8080/book_manage/
```

### 5. 打包部署到独立 Tomcat（可选）

```bash
mvn clean package
```

`target` 目录下生成 `book_manage.war`，复制到 Tomcat 的 `webapps` 目录，启动 Tomcat 后访问
`http://localhost:8080/book_manage/`。

## 默认账号

脚本 `database_init.sql` 初始化了 1 个管理员与 29 个读者账号：

| 角色 | 用户名 | 密码 | 说明 |
|---|---|---|---|
| 管理员 | `admin` | `123456` | 可进入 `/admin/index.jsp` 后台 |
| 读者 | `reader01` ~ `reader29` | `123456` | 可进入 `/reader/index.jsp` 读者中心 |

> 以上仅为演示数据，正式使用请修改密码（更好的做法是对密码做 MD5 / BCrypt 加密后入库）。

## 页面与接口说明

### 主要页面

| 路径 | 说明 |
|---|---|
| `/index.jsp` | 系统首页（欢迎页） |
| `/login.jsp` | 登录页（`?msg=no_permission` 表示无权限） |
| `/register.jsp` | 读者注册 |
| `/admin/index.jsp` | 管理员首页 |
| `/admin/statOverview.jsp` | 数据统计总览 |
| `/admin/statRanking.jsp` | 借阅排行榜 |
| `/admin/bookList.jsp` | 图书列表管理 |
| `/admin/categoryList.jsp` | 分类管理 |
| `/admin/userList.jsp` | 用户管理 |
| `/admin/borrowList.jsp` | 借阅记录管理 |
| `/admin/overdueList.jsp` | 逾期列表 |
| `/reader/index.jsp` | 读者首页 |
| `/reader/bookSearch.jsp` | 图书检索 |
| `/reader/myBorrows.jsp` | 我的借阅 |
| `/reader/myFines.jsp` | 我的罚款 |
| `/reader/myReservations.jsp` | 我的预约 |

### Servlet 接口（`@WebServlet` 映射）

| URL | Servlet | 说明 |
|---|---|---|
| `/loginServlet` | LoginServlet | 登录处理（区分 admin / reader 角色） |
| `/logoutServlet` | LogoutServlet | 退出登录 |
| `/registerServlet` | RegisterServlet | 读者注册 |
| `/bookServlet` | BookServlet | 图书基础操作 |
| `/admin/indexServlet` | AdminIndexServlet | 管理员首页数据 |
| `/admin/bookManageServlet` | BookManageServlet | 图书增删改查 |
| `/admin/categoryServlet` | CategoryServlet | 分类增删改查 |
| `/admin/userServlet` | UserServlet | 用户增删改查 |
| `/admin/borrowServlet` | BorrowManageServlet | 借阅记录管理 |
| `/admin/statServlet` | StatServlet | 统计与排行数据 |
| `/admin/adminProfileServlet` | AdminProfileServlet | 管理员资料 |
| `/admin/checkBooks` | CheckBooksServlet | 图书数据自检 |
| `/admin/testBooksServlet` | TestBooksServlet | 测试图书数据 |
| `/reader/indexServlet` | ReaderIndexServlet | 读者首页数据 |
| `/reader/bookSearchServlet` | BookSearchServlet | 图书检索 |
| `/reader/borrowOpServlet` | BorrowOpServlet | 借书 / 还书 / 预约操作 |
| `/reader/profileServlet` | ProfileServlet | 读者资料 |
| `/initSystem` | InitSystemServlet | 系统初始化（建表） |
| `/insertTestData` | InsertTestDataServlet | 导入测试数据 |
| `/insertOverdueAndFine` | InsertOverdueAndFineServlet | 生成逾期与罚款测试数据 |

## 数据库设计

数据库名：`book_manage`，字符集 `utf8mb4`，共 6 张表。

### users 用户表

| 字段 | 类型 | 约束 / 说明 |
|---|---|---|
| id | INT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一，非空 |
| password | VARCHAR(100) | 密码 |
| role | ENUM('admin','reader') | 角色，非空 |
| real_name | VARCHAR(50) | 真实姓名 |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| status | ENUM('active','disabled') | 状态，默认 active |
| create_time | DATETIME | 注册时间，默认当前时间 |

### categories 图书分类表

| 字段 | 类型 | 约束 / 说明 |
|---|---|---|
| id | INT | 主键，自增 |
| name | VARCHAR(50) | 分类名，唯一，非空 |
| description | VARCHAR(200) | 分类描述 |

### books 图书表

| 字段 | 类型 | 约束 / 说明 |
|---|---|---|
| id | INT | 主键，自增 |
| bid | VARCHAR(20) | 图书编号，唯一，非空 |
| isbn | VARCHAR(30) | ISBN |
| bname | VARCHAR(200) | 书名，非空 |
| author | VARCHAR(100) | 作者 |
| publisher | VARCHAR(100) | 出版社 |
| publish_date | DATE | 出版日期 |
| price | DECIMAL(10,2) | 价格 |
| category_id | INT | 外键 → categories.id |
| stock | INT | 总库存 |
| available_stock | INT | 可借库存 |
| location | VARCHAR(50) | 馆藏位置 |
| description | TEXT | 简介 |
| create_time | DATETIME | 入库时间 |

### borrow_records 借阅记录表

| 字段 | 类型 | 约束 / 说明 |
|---|---|---|
| id | INT | 主键，自增 |
| user_id | INT | 外键 → users.id |
| book_id | INT | 外键 → books.id |
| borrow_date | DATE | 借出日期，非空 |
| due_date | DATE | 应还日期，非空 |
| return_date | DATE | 实际归还日期 |
| status | ENUM('borrowed','returned','overdue') | 借阅状态，默认 borrowed |
| renew_count | INT | 续借次数，默认 0 |
| fine_amount | DECIMAL(10,2) | 罚款金额，默认 0 |
| is_paid | TINYINT | 是否已缴，默认 0 |
| create_time | DATETIME | 创建时间 |

### fine_records 罚款记录表

| 字段 | 类型 | 约束 / 说明 |
|---|---|---|
| id | INT | 主键，自增 |
| user_id | INT | 外键 → users.id |
| borrow_record_id | INT | 外键 → borrow_records.id |
| amount | DECIMAL(10,2) | 罚款金额，非空 |
| days | INT | 逾期天数，非空 |
| status | ENUM('unpaid','paid') | 缴纳状态，默认 unpaid |
| create_time | DATETIME | 创建时间 |

### reservations 预约表

| 字段 | 类型 | 约束 / 说明 |
|---|---|---|
| id | INT | 主键，自增 |
| user_id | INT | 外键 → users.id |
| book_id | INT | 外键 → books.id |
| reserve_date | DATETIME | 预约时间，默认当前时间 |
| status | ENUM('waiting','notified','cancelled','completed') | 预约状态，默认 waiting |

## 常见问题（FAQ）

**Q1：启动报 `java.lang.ClassNotFoundException: com.mysql.cj.jdbc.Driver`**
A：`pom.xml` 中 mysql-connector-java 依赖未下载成功。IDEA 右侧 `Maven` 面板点刷新图标重新导入；仍失败请检查网络 / Maven 镜像（可配置阿里云镜像）。

**Q2：启动报 `UnsupportedClassVersionError` / class file version 不匹配**
A：通常是因为 `pom.xml` 的编译版本、IDEA 的 `Project SDK`、Tomcat 使用的 JRE 三者不一致。按以下顺序对齐：`File → Project Structure → Project`（SDK 与 Language level 都选 JDK 11）→ `Modules` 同页核对 → `Run → Edit Configurations → Tomcat` 的 `JRE` 选同一个 JDK 11 → `Maven` 面板点刷新 → 重启 Tomcat。注意 Tomcat 8.5/9 不支持用 17/21/25 编译出的 class 文件。

**Q3：报 `Communications link failure`**
A：MySQL 服务未启动，或端口被占用。命令行执行 `net start mysql`（服务名视安装而定）启动服务，或用 IDEA 的 `Database` 面板测试连接。

**Q4：报 `Unknown database 'book_manage'`**
A：没有执行 `database_init.sql`，或脚本只执行了建表部分。重新执行完整的 `database_init.sql`。

**Q5：报 `Access denied for user 'root'@'localhost'`**
A：密码没配对。三种改法任选其一：① 建 `src/main/resources/db.properties` 并写对 `db.password`；② IDEA 运行配置 `VM options` 加 `-Ddb.password=你的密码`；③ 设置环境变量 `DB_PASSWORD`。改完需**重启 Tomcat** 才会重新加载。

**Q6：中文乱码 / 页面问号**
A：确认 `web.xml` 中 `EncodingFilter` 已注册（路径 `/*`），JSP 顶部含 `<%@ page contentType="text/html;charset=UTF-8" %>`，JDBC URL 带 `characterEncoding=UTF-8`，数据库为 `utf8mb4`。

**Q7：访问 `/admin/` 页面总跳回登录页**
A：`LoginCheckFilter` 生效中，需先登录；或登录账号角色不是 `admin`（会被重定向并带 `msg=no_permission`）。请用 `admin / 123456` 登录。

**Q8：上传到仓库后只剩源码，看不到 class 和 war**
A：这是正常的。`.gitignore` 已排除 `target/`、`.idea/`、`*.iml`，构建产物和 IDE 配置不入库。

**Q9：Tomcat 10 启动报 `javax.servlet 不存在` / 404**
A：Tomcat 10+ 使用 `jakarta.servlet` 包名，本项目基于 `javax.servlet`（Servlet 3.1），请使用 **Tomcat 8.5 或 9.0**。

**Q10：`pom.xml` 在 IDEA 中显示为普通文件、没有 Maven 图标**
A：右键 `pom.xml` → `Add as Maven Project`。若文件在 IDEA 中名字前带空格（Windows 允许这种文件名，但 IDEA/命令行会出问题），请重命名去掉空格。

## Git 与推送说明

项目已通过 IntelliJ IDEA 图形化操作推送到 GitHub：

- 仓库：https://github.com/x15737876973-art/book-manage-system
- 已忽略：`target/`、`.idea/`、`*.iml`、`*.db`、`*.zip`、日志等（见 `.gitignore`）
- 认证方式：**Personal Access Token**（GitHub 自 2021-08-13 起停用账号密码认证）

日常更新流程：`Git → Commit`（`Ctrl+K`）勾选文件 → 填说明 → `Commit and Push` → `Push`。

## 贡献与许可

本项目用于学习交流，可自由参考、修改与二次开发；转载请注明出处。

## 作者

- GitHub：[@x15737876973-art](https://github.com/x15737876973-art)
- 仓库：[book-manage-system](https://github.com/x15737876973-art/book-manage-system)