# 图书管理系统（Book Manage System）

基于 **Java Web（Servlet + JSP）+ MySQL + Maven** 的图书管理系统，包含管理员与读者两种角色，覆盖图书、分类、借阅、罚款、预约管理与数据统计，并内置**基于 DeepSeek 的轻量 RAG 智能图书问答**模块。

[![Java](https://img.shields.io/badge/Java-8%2F11-blue.svg)](https://www.oracle.com/java/)
[![Servlet](https://img.shields.io/badge/Servlet-3.1-orange.svg)](https://javaee.github.io/servlet-spec/)
[![Tomcat](https://img.shields.io/badge/Tomcat-8.5%2F9.0-yellow.svg)](https://tomcat.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-5.7%2F8.0-4479A1.svg)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-C71A36.svg)](https://maven.apache.org/)

> 仓库地址：https://github.com/x15737876973-art/book-manage-system

---

## 目录

- [环境版本](#环境版本)
- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [完整部署步骤](#完整部署步骤)
- [测试账号](#测试账号)
- [智能 Agent 模块（RAG）](#智能-agent-模块rag)
- [安全设计说明](#安全设计说明)
- [常见问题 FAQ](#常见问题-faq)
- [接口一览](#接口一览)
- [数据库设计](#数据库设计)

---

## 环境版本

以下为**推荐且已验证**的版本组合。版本不符时常出现的问题见 [常见问题 FAQ](#常见问题-faq)。

| 组件 | 推荐版本 | 说明 |
|---|---|---|
| **JDK** | **11**（兼容 8） | `pom.xml` 中 `maven.compiler.source/target = 11`。用 17/21 编译出的 class 在 Tomcat 9 上会报 `UnsupportedClassVersionError` |
| **Maven** | **3.6.3 及以上** | IDEA 自带 Maven 也可，需配置阿里云镜像加速 |
| **Tomcat** | **9.0.x**（推荐）或 **8.5.x** | ⚠️ **不要用 Tomcat 10+**：它改用 `jakarta.servlet` 包名，本项目基于 `javax.servlet`，部署后必然 404 / 类找不到。<br>⚠️ **Tomcat 7 能跑但不推荐**：`web.xml` 已降级为 Servlet 3.0 可兼容，但 Tomcat 7 与 JDK 11+ 存在已知兼容问题（见 FAQ Q16），且已停止维护 |
| **MySQL** | **8.0.x**（兼容 5.7） | 驱动 `mysql-connector-java 8.0.33`；5.7 也需用 `com.mysql.cj.jdbc.Driver` |
| **IDEA** | IntelliJ IDEA 2022.3 及以上 | Community 版用外置 Tomcat 运行；Ultimate 版可用 Smart Tomcat 或外置 Tomcat |
| 操作系统 | Windows 10/11（亦支持 macOS / Linux） | 部署步骤以 Windows 为例 |
| 浏览器 | Chrome / Edge 最新版 | 智能问答页面用到 `fetch` / `URLSearchParams` |

依赖版本（`pom.xml`）：

| 依赖 | 版本 |
|---|---|
| javax.servlet-api | 3.1.0（provided） |
| javax.servlet.jsp-api | 2.3.1（provided） |
| jstl | 1.2 |
| mysql-connector-java | 8.0.33 |
| **jbcrypt** | **0.4**（密码加密，新增） |

---

## 功能特性

### 管理员端（`/admin/**`）

- [x] 管理员登录 / 退出（Session 会话，30 分钟超时）
- [x] 首页看板：图书总数、读者数、当前借阅、逾期数量
- [x] **图书管理（分页）**：列表 / 搜索 / 新增 / 编辑 / 删除，支持按分类筛选
- [x] 图书分类管理（增删改查，删除前校验是否被引用）
- [x] 用户管理：读者与管理员列表、新增、编辑、启用/禁用、重置密码
- [x] 借阅管理：代借、还书、续借、应还日期调整
- [x] 逾期管理：自动标记逾期、查看逾期名单、登记缴纳罚款
- [x] 数据统计：馆藏统计、分类占比、借阅排行榜
- [x] 数据库健康检查、图书数据诊断
- [x] 系统初始化 / 测试数据生成（演示用）

### 读者端（`/reader/**`）

- [x] 注册 / 登录 / 退出（注册密码 BCrypt 加密入库）
- [x] **图书检索（分页）**：按书名 / 作者 / ISBN + 分类组合搜索
- [x] 自助借书、还书、续借（事务保证库存一致）
- [x] 我的借阅：当前借阅与历史记录分开展示，逾期天数自动计算
- [x] 我的预约：预约、取消（校验归属，不能操作他人预约）
- [x] 我的罚款：查看未缴金额与缴纳状态
- [x] 个人中心：查看资料、修改密码（BCrypt 校验原密码）

### 智能图书助手（`/agent.jsp`）★ 新增

- [x] 自然语言问答：向 DeepSeek 提问，回答严格基于本馆数据库
- [x] 轻量 RAG：JDBC 检索 MySQL 图书表 → 构造 Prompt → 调用大模型
- [x] 防幻觉：查不到的书明确告知，禁止编造
- [x] 降级模式：未配置 API Key 或调用失败时，自动回退为本地检索结果
- [x] 对话式界面：气泡消息、图书卡片、快捷提问、正在输入动画

---

## 技术栈

| 层次 | 技术 |
|---|---|
| 开发语言 | Java 8 / 11 |
| 构建工具 | Apache Maven |
| Web 层 | Servlet 3.1（`@WebServlet` 注解）、JSP 2.3、JSTL 1.2 |
| 数据访问 | 原生 JDBC + `PreparedStatement`（全量预编译，无 SQL 拼接） |
| 连接管理 | `DBUtil` 统一封装，`try-with-resources` 自动释放，支持事务 |
| 密码安全 | **BCrypt**（jBCrypt 0.4，cost=10） |
| 前端 | JSP + 原生 CSS / JavaScript（无框架依赖） |
| 大模型 | DeepSeek Open API（兼容 OpenAI `/chat/completions` 协议） |
| 服务器 | Apache Tomcat 8.5 / 9.0 |
| 数据库 | MySQL 5.7 / 8.0 |

---

## 项目结构

```
book-manage-system
├── pom.xml                              # Maven 配置（含 jbcrypt 依赖）
├── README.md
├── database_init.sql                    # ★ 建库建表 + 初始数据（密码已 BCrypt 加密）
├── sql/
│   └── password_migration.sql           # 旧库明文密码批量升级为 BCrypt 的脚本
└── src/main
    ├── java/com/book
    │   ├── agent/                       # ★ 智能问答模块（RAG）
    │   │   ├── AgentConfig.java         #   配置读取（API Key / 模型 / 超时）
    │   │   ├── BookKnowledgeBase.java   #   检索：问题→关键词→JDBC 查 MySQL
    │   │   ├── PromptBuilder.java       #   组装：图书事实 → Prompt（防幻觉）
    │   │   ├── DeepSeekClient.java      #   生成：调用 DeepSeek API
    │   │   └── AgentQueryServlet.java   #   入口：/agentQueryServlet
    │   ├── dao/                         # 数据访问层（全量 PreparedStatement）
    │   │   ├── BookDao.java             #   含分页查询与 Agent 检索
    │   │   ├── BorrowDao.java
    │   │   ├── CategoryDao.java
    │   │   ├── FineDao.java
    │   │   ├── ReservationDao.java
    │   │   └── UserDao.java
    │   ├── entity/                      # 实体类
    │   │   ├── Book.java / BorrowRecord.java / Category.java
    │   │   ├── FineRecord.java / Reservation.java / User.java
    │   │   └── PageResult.java          # ★ 分页结果封装
    │   ├── filter/
    │   │   ├── EncodingFilter.java      # UTF-8 编码
    │   │   └── LoginCheckFilter.java    # ★ 登录 + 角色校验（修复越权）
    │   ├── listener/
    │   │   └── AppStartupListener.java  # 启动自检
    │   ├── servlet/                     # 控制器
    │   │   ├── LoginServlet.java        #   登录（BCrypt 校验 + 明文自动升级）
    │   │   ├── RegisterServlet.java     #   注册（BCrypt 加密入库）
    │   │   ├── LogoutServlet.java
    │   │   ├── AbstractProfileServlet.java  # ★ 个人中心基类
    │   │   ├── AdminProfileServlet.java / ProfileServlet.java
    │   │   ├── AdminIndexServlet.java / ReaderIndexServlet.java
    │   │   ├── BookManageServlet.java   # ★ 图书管理（分页 + 参数校验）
    │   │   ├── BookSearchServlet.java   # ★ 读者检索（分页）
    │   │   ├── BookServlet.java         # ★ 简化版图书操作（已加鉴权）
    │   │   ├── CategoryServlet.java / UserServlet.java
    │   │   ├── BorrowManageServlet.java # ★ 借阅管理（事务）
    │   │   ├── BorrowOpServlet.java     # ★ 读者借阅（越权修复）
    │   │   ├── StatServlet.java
    │   │   ├── CheckBooksServlet.java / TestBooksServlet.java
    │   │   ├── InitSystemServlet.java   # ★ 系统初始化（SQL 注入修复）
    │   │   ├── InsertTestDataServlet.java   # ★ 测试数据（SQL 注入修复）
    │   │   └── InsertOverdueAndFineServlet.java # ★ 逾期数据（SQL 注入修复）
    │   └── util/
    │       ├── DBUtil.java              # ★ 连接管理 + query/update/事务模板
    │       ├── BCryptUtil.java          # ★ 密码加密（对接 jBCrypt）
    │       ├── ServletUtils.java        # ★ 参数校验 / 分页 / 鉴权工具
    │       ├── AppException.java        # ★ 业务异常（友好提示）
    │       └── InsertBooksUtil.java
    ├── resources/
    │   ├── db.properties.example        # 数据库连接模板（复制为 db.properties）
    │   ├── agent.properties.example     # ★ 大模型配置模板（复制为 agent.properties）
    │   └── *.sql                        # 历史脚本（SQL Server 语法，MySQL 请勿执行）
    └── webapp/
        ├── WEB-INF/web.xml              # ★ 过滤器 + 统一错误页配置
        ├── index.jsp / login.jsp / register.jsp
        ├── agent.jsp                    # ★ 智能问答页面
        ├── error.jsp                    # ★ 统一友好错误页
        ├── admin/                       # 管理员页面（bookList.jsp 含分页）
        ├── reader/                      # 读者页面（bookSearch.jsp 含分页）
        ├── css/ / js/
        └── WEB-INF/
```

---

## 完整部署步骤

### 步骤 0：准备环境

1. 安装 **JDK 11**，配置 `JAVA_HOME`；
2. 安装 **Apache Tomcat 9.0.x**，解压到无空格路径（如 `D:\apache-tomcat-9.0.85`）；
3. 安装 **MySQL 8.0**，记住 root 密码，确保服务已启动；
4. 安装 **Maven 3.6+**（或直接用 IDEA 内置）；
5. 安装 **IntelliJ IDEA**。

验证环境：

```bash
java -version      # 应显示 11.x
mvn -v             # 应显示 3.6+
mysql --version    # 应显示 8.0.x
```

---

### 步骤 1：获取项目

```bash
git clone https://github.com/x15737876973-art/book-manage-system.git
cd book-manage-system
```

或在 IDEA 中：`File → New → Project from Version Control` → 粘贴上面的地址 → `Clone`。

---

### 步骤 2：执行 `database_init.sql` 建库建表

项目根目录的 **`database_init.sql`** 是唯一需要执行的初始化脚本，它包含：
建库、建 6 张表、插入 30 个分类、30 个用户、30 本图书、30 条借阅记录。

**方式一：IDEA 图形化（推荐）**

1. IDEA 右侧 `Database` 面板 → `+` → `Data Source` → `MySQL`；
2. 填写 `Host: localhost`、`Port: 3306`、`User: root`、`Password: 你的密码` → `Test Connection` → `OK`；
3. 右键该数据源 → `Open Console`（或按 `Ctrl+Shift+Q` 打开查询控制台）；
4. 打开项目内的 `database_init.sql`，**全选（Ctrl+A）并复制**，粘贴到控制台；
5. 点击 ▶ 执行。看到 `✅ 数据库初始化完成！` 即成功。

**方式二：命令行**

```bash
mysql -u root -p < database_init.sql
```

**方式三：Navicat / DataGrip**

新建连接 → 右键连接 → `运行 SQL 文件` → 选择 `database_init.sql` → 执行。

> ⚠️ 注意：`src/main/resources/` 下的 `book_manage.sql`、`init.sql`、`simple_init.sql`
> 是早期 **SQL Server** 语法版本，在 MySQL 中执行会报语法错误，**请勿使用**。
> 只执行根目录的 `database_init.sql`。

验证：

```sql
USE book_manage;
SHOW TABLES;                                -- 应有 6 张表
SELECT COUNT(*) FROM books;                 -- 应为 30
SELECT username, LEFT(password, 7) FROM users LIMIT 3;   -- password 应以 $2a$10$ 开头
```

---

### 步骤 3：修改 JDBC 数据库连接配置

连接信息**不写死在源码里**，由 `DBUtil` 按以下优先级读取：

| 优先级 | 来源 | 示例 |
|---|---|---|
| 1 | JVM 系统属性 | `-Ddb.password=你的密码` |
| 2 | 环境变量 | `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` |
| 3 | 配置文件 | `src/main/resources/db.properties` |
| 4 | 内置默认值 | `root` + 空密码，仅用于跑通演示 |

**推荐做法（配置文件）**：

1. 在 IDEA 中打开 `src/main/resources/db.properties.example`；
2. 右键 → `Copy`，同目录 `Paste`，重命名为 **`db.properties`**；
3. 修改内容：

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/book_manage?useSSL=false&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
db.username=root
db.password=你的MySQL密码
```

> `db.properties` 已在 `.gitignore` 中，**不会**提交到公开仓库。

**备选做法（不改文件）**：IDEA → `Run → Edit Configurations` → Tomcat 配置 → `VM options` 填：

```
-Ddb.password=你的MySQL密码
```

---

### 步骤 4：Maven 打包

**方式一：IDEA 图形化**

1. 右侧 `Maven` 面板 → 点刷新图标（`Reload All Maven Projects`）下载依赖；
2. 展开 `book-manage-system → Lifecycle`；
3. 双击 **`clean`**，再双击 **`package`**；
4. 控制台出现 `BUILD SUCCESS` 即成功；
5. 产物：**`target/book_manage.war`**（`finalName` 决定名字为 `book_manage`）。

**方式二：命令行**

```bash
mvn clean package -DskipTests
```

> 若下载依赖很慢，配置阿里云镜像：`C:\Users\你的用户名\.m2\settings.xml`
> ```xml
> <mirrors>
>   <mirror>
>     <id>aliyun</id>
>     <mirrorOf>central</mirrorOf>
>     <url>https://maven.aliyun.com/repository/public</url>
>   </mirror>
> </mirrors>
> ```

---

### 步骤 5：部署到 Tomcat

#### 方式 A：IDEA 中配置外置 Tomcat 运行（开发调试推荐）

1. `Run → Edit Configurations` → `+` → **`Tomcat Server` → `Local`**；
2. `Server` 页：
   - `Application server` → `Configure...` → 选择 Tomcat 安装目录；
   - `JRE` → 选择 **JDK 11**；
   - `VM options`（可选）：`-Ddb.password=你的密码`；
3. `Deployment` 页：
   - 点 `+` → **`Artifact`** → 选 **`book_manage:war exploded`**；
   - `Application context` 填 **`/book_manage`**；
4. `Apply → OK`；
5. 点击绿色 ▶ 运行。控制台出现以下日志即启动成功：

```
========================================
  图书管理系统启动中...
========================================
[启动自检] 数据库连接正常
  分类：30 个
  用户：30 个
  图书：30 本
```

6. 浏览器访问：**http://localhost:8080/book_manage/**

#### 方式 B：打成 war 包放入 Tomcat（生产部署）

1. 把 `target/book_manage.war` 复制到 Tomcat 的 **`webapps/`** 目录；
   - Windows 默认路径：`D:\apache-tomcat-9.0.85\webapps\`
2. 启动 Tomcat：
   - Windows：双击 `bin/startup.bat`；
   - Linux/macOS：`./bin/startup.sh`；
3. 等待 Tomcat 自动解压 war 包（`webapps/book_manage/` 出现即完成）；
4. 浏览器访问：**http://localhost:8080/book_manage/**

排查日志：`logs/catalina.out`（Linux）或 `logs/catalina.日期.log`（Windows）。

#### 方式 C：IDEA Community 版（无 Tomcat 集成）

1. `File → Settings → Plugins` 搜索并安装 **`Smart Tomcat`** 插件；
2. `Run → Edit Configurations` → `+` → `Smart Tomcat`；
3. `Tomcat Server` 选安装目录，`Deployment Directory` 选 `src/main/webapp`，`Context Path` 填 `/book_manage`；
4. 运行后同样访问 `http://localhost:8080/book_manage/`。

---

### 步骤 6：首次使用

1. 打开 http://localhost:8080/book_manage/；
2. 点击 **登录**，用下表测试账号进入；
3. 若数据库是空的，可用管理员登录后执行：
   - 访问 `http://localhost:8080/book_manage/initSystem` 初始化基础数据；
   - 访问 `http://localhost:8080/book_manage/insertTestData` 生成测试借阅数据。

---

## 测试账号

`database_init.sql` 已预置 1 个管理员和 29 个读者，**密码均为 `123456`（数据库中存的是 BCrypt 密文）**。

| 角色 | 用户名 | 密码 | 说明 |
|---|---|---|---|
| **管理员** | `admin` | `123456` | 可进入 `/admin/index.jsp` 后台，拥有全部管理权限 |
| **读者** | `reader01` | `123456` | 可进入 `/reader/index.jsp` 读者中心 |
| 读者 | `reader02` ~ `reader29` | `123456` | 同上，共 29 个测试读者 |

> 新注册的读者账号默认角色为 `reader`，密码以 BCrypt 加密存储。
> 管理员可在「用户管理」中把任意用户重置为 `123456`（同样以密文入库）。

**验证密码确实已加密**（可选）：

```sql
USE book_manage;
SELECT username, password FROM users WHERE username = 'admin';
-- 输出应类似：admin | $2a$10$MY3qE9NIr8rrkLzz5HtnfeJEs2L1zhoBFLhRBGT6pB4KrL3XLwF8K
-- 若看到 123456 明文，说明用的是旧数据库，请执行 sql/password_migration.sql
```

---

## 智能 Agent 模块（RAG）

### 原理

```
用户提问「有《三体》吗？」
        │
        ▼
┌───────────────────────────────────────────────────────┐
│ ① 检索 Retrieval                                       │
│   BookKnowledgeBase：问题 → 切关键词 → JDBC 查 MySQL   │
│   SQL：bname/author/publisher/category/ISBN LIKE ?     │
│   返回最多 topK=8 本最相关的书                          │
└───────────────────────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────────────────────┐
│ ② 增强 Augmentation                                    │
│   PromptBuilder：把查到的书（书名/作者/库存/位置）      │
│   拼进 Prompt，并在 System 中写明"只能依据资料回答"     │
└───────────────────────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────────────────────┐
│ ③ 生成 Generation                                      │
│   DeepSeekClient：POST https://api.deepseek.com        │
│                    /chat/completions                   │
│   返回自然语言回答                                      │
└───────────────────────────────────────────────────────┘
        │
        ▼
   页面展示（回答 + 命中图书卡片）
```

**防幻觉的三重保障**：

1. **检索为空不调用模型**：数据库里没有相关书时，直接返回固定话术，模型完全没有编造的机会；
2. **System Prompt 硬约束**：明文规定"禁止编造馆藏资料中不存在的书籍、作者、ISBN、库存"；
3. **低温度 0.3**：事实型问答用低温，输出更稳定、更少发散。

### 一、申请 DeepSeek API Key

1. 打开 **https://platform.deepseek.com**，注册并登录；
2. 左侧菜单 → **`API keys`** → 点击 **`创建 API key`**；
3. 输入名称（如 `book-manage-system`）→ 创建；
4. **立刻复制**弹出的 `sk-xxxxxxxxxxxxxxxx` —— **只显示一次**，关闭后无法再查看；
5. 妥善保管，**不要**提交到 Git、不要截图外发。

> 计费说明：DeepSeek 按 token 计费，图书问答这类短对话消耗很低，
> 新注册账号通常有赠送额度，足够课程演示使用。

### 二、配置 API Key

**方式一：环境变量（推荐，最方便）**

Windows：
1. `Win + R` → 输入 `sysdm.cpl` → `高级` → `环境变量`；
2. 用户变量 → `新建`：
   - 变量名：`DEEPSEEK_API_KEY`
   - 变量值：`sk-你的key`
3. 确定后**重启 IDEA / Tomcat** 使其生效。

macOS / Linux：

```bash
export DEEPSEEK_API_KEY=sk-你的key
```

**方式二：配置文件**

1. 复制 `src/main/resources/agent.properties.example` 为 **`agent.properties`**；
2. 修改 `agent.api.key`：

```properties
agent.api.key=sk-你的key
agent.base.url=https://api.deepseek.com
agent.model=deepseek-chat
agent.timeout.seconds=60
agent.top.k=8
```

3. 重新编译部署（`mvn clean package` 或重启 Tomcat）。

> `agent.properties` 已在 `.gitignore` 中，不会进入仓库。
> **读取优先级**：环境变量 `DEEPSEEK_API_KEY` > 系统属性 `-Ddeepseek.api.key` > `agent.properties`。

**方式三：JVM 参数**

IDEA Tomcat 配置的 `VM options` 里加：

```
-Ddeepseek.api.key=sk-你的key
```

### 三、使用

1. 启动项目，访问 **http://localhost:8080/book_manage/agent.jsp**；
2. 页面顶部会显示服务状态：
   - 🟢 已就绪：显示模型名与脱敏后的 Key（如 `sk-3f****a9c2`）；
   - 🟡 本地检索模式：未配置 Key，只按数据库结果回答；
3. 在输入框提问，例如：
   - `有《三体》吗？`
   - `推荐几本计算机类的图书`
   - `刘慈欣的书有哪些？`
   - `有哪些书现在可以借？`
4. 也可在登录后的侧边栏「🤖 智能助手」进入。

### 四、降级机制（没有 Key 也能用）

| 情况 | 系统行为 |
|---|---|
| 未配置 API Key | 进入**本地检索模式**：把数据库命中的图书整理成文字回答，页面提示"未调用大模型" |
| Key 无效 / 余额不足 / 超时 | 捕获异常，**降级**为本地检索结果，并提示具体原因（如"API Key 无效或已过期"） |
| 检索不到相关图书 | 直接回复"本馆暂未收录……"，**不调用模型**，杜绝编造 |

### 五、常见问题

| 现象 | 原因 | 解决 |
|---|---|---|
| 页面显示"本地检索模式" | 未读到 Key | 检查环境变量名是否为 `DEEPSEEK_API_KEY`，改完**必须重启 Tomcat** |
| 提示"API Key 无效或已过期"（401） | Key 错误或已删除 | 到平台重新生成，注意不要多复制空格 |
| 提示"账户余额不足"（402） | 额度用尽 | 到平台充值 |
| 提示"请求过于频繁"（429） | 触发限流 | 稍等再试 |
| 提示"调用大模型超时" | 网络慢或代理 | 调大 `agent.timeout.seconds`；检查是否能访问 `api.deepseek.com` |
| 回答里出现数据库没有的书 | 模型幻觉 | 本项目已用 Prompt 约束，若仍出现请把问题反馈到 Issue；同时可把 `temperature` 进一步降低 |

### 六、自定义扩展

| 需求 | 改动位置 |
|---|---|
| 换模型（如 `deepseek-reasoner`） | `agent.properties` 的 `agent.model` |
| 检索更多/更少图书 | `agent.top.k` |
| 调整回答风格 | `PromptBuilder.SYSTEM_PROMPT` |
| 改成向量检索 | 替换 `BookKnowledgeBase.retrieve()` 实现（当前为关键词 LIKE 匹配） |
| 支持流式输出（打字机效果） | `DeepSeekClient` 中把 `stream` 改为 `true`，逐行解析 SSE |

---

## 安全设计说明

本项目在原有功能基础上做了系统性安全加固，**业务流程保持不变**。

### 1. SQL 注入防护

全部数据库操作统一使用 `PreparedStatement` + `?` 占位符，用户输入一律走参数绑定。

修复的具体位置：

| 文件 | 原问题 |
|---|---|
| `InitSystemServlet` | `String.format` 拼 INSERT，靠手工替换单引号防注入 |
| `InsertTestDataServlet` | `String.format` 拼 INSERT + `"LIMIT " + n` 拼接 |
| `InsertOverdueAndFineServlet` | `String.format` 拼 INSERT + `"LIMIT " + n` 拼接 |
| `InsertBooksUtil` | `Statement.executeUpdate` 直接拼字符串 |
| 全部 DAO | 统一改为 `DBUtil.query/update/queryLong` 模板 |

额外的防御：

- LIKE 查询对 `%` `_` `\` 做转义（`ESCAPE '\\'`），防止用户输入通配符导致全表扫描；
- 动态表名（统计、删表）走白名单校验。

### 2. JDBC 资源释放

`DBUtil` 提供三个模板方法，内部全部使用 **try-with-resources**：

```java
DBUtil.query(sql, binder, mapper)      // 查询
DBUtil.queryLong(sql, binder)         // 统计
DBUtil.update(sql, binder)            // 增删改
DBUtil.inTransaction(work)            // 事务
```

即使中途抛异常，`Connection` / `PreparedStatement` / `ResultSet` 也必然关闭，
从根本上杜绝连接泄漏。旧的 `DBUtil.close()` 保留但标记 `@Deprecated`。

### 3. 权限校验

**旧版漏洞**：`LoginCheckFilter` 只按 URL 是否包含 `/admin/`、`/reader/` 判断，
导致根路径下的 `/initSystem`、`/insertTestData`、`/bookServlet`、`/insertOverdueAndFine`
**完全未受保护**，未登录即可调用并篡改数据。

**修复后规则**：

| 路径 | 要求 |
|---|---|
| `/admin/**`、`/initSystem`、`/insertTestData`、`/insertOverdueAndFine`、`/bookServlet`、`/checkBooks` | 已登录 **且** `role=admin` |
| `/reader/**` | 已登录 **且** `role=reader` |
| `/login.jsp`、`/register.jsp`、`/index.jsp`、`/agent.jsp`、`/loginServlet`、`/registerServlet`、`/logoutServlet`、`/error.jsp`、`/css/**`、`/js/**` | 公开 |

- 未登录 → 跳转 `login.jsp?msg=need_login`
- 角色不符 → 跳转 `login.jsp?msg=no_permission`
- 每个 Servlet 内部**再兜底校验一次**，双重保险

**越权修复**：`BorrowOpServlet.cancelReservation` 原来只按 id 取消，
改 URL 就能取消他人预约；现在先查记录并校验 `reservation.getUserId()` 归属。
续借同理。

### 4. 请求参数校验

`ServletUtils` 统一提供：

- `requireParam`：必填 + 长度上限
- `getInt` / `getPositiveInt` / `getDouble` / `getSqlDate`：类型与范围校验，非法值给中文提示
- `validateUsername` / `validatePassword` / `validatePhone` / `validateEmail`：格式校验
- `getPageNumber` / `getPageSize`：分页参数纠偏（每页上限 100）

所有 Servlet 的 `NumberFormatException` 都被捕获并转为友好提示，
不再出现"参数错误 → 500 堆栈页面"。

### 5. 密码安全（BCrypt）

- **加密入库**：注册、新增用户、重置密码、初始化脚本，全部存 BCrypt 密文（`$2a$10$...`，60 字符）
- **校验方式**：`BCryptUtil.checkpw(明文, 密文)`，不再 `WHERE password = 明文`
- **随机盐**：相同密码每次加密结果不同，彩虹表失效
- **可调强度**：cost=10（约 55ms/次），可随硬件升级调整
- **历史明文兼容**：旧账号首次登录成功后**自动升级**为密文，无需手动迁移
- **重置密码**：管理员重置为 `123456`（以密文存储）

> BCrypt 实现来自 **jBCrypt 0.4**（官方 Java 参考实现），
> 已通过 21 项测试，包括与 jBCrypt 的双向交叉校验。

### 6. 统一异常处理

- `web.xml` 配置 400/403/404/405/500 与 `Throwable` 全部指向 `error.jsp`
- `error.jsp` 只展示中文友好提示，**不输出堆栈**
- 技术细节通过 `getServletContext().log()` 写入服务器日志
- 业务异常用 `AppException` 承载中文提示，与程序 Bug 区分

### 7. 其他加固

- **XSS**：智能问答页面所有模型输出用 `textContent` 渲染，不用 `innerHTML`；
  JSP 动态输出沿用 JSTL 默认转义
- **会话固定**：登录成功后 `invalidate()` 旧 session 再新建
- **会话 Cookie**：`web.xml` 中配置 `httpOnly=true`
- **账号枚举**：登录失败统一提示"用户名或密码错误"，不区分是账号不存在还是密码错误
- **自我锁定防护**：管理员不能删除/禁用/降级自己

---

## 常见问题 FAQ

**Q1：启动报 `ClassNotFoundException: com.mysql.cj.jdbc.Driver`**

`pom.xml` 中 MySQL 驱动未被下载。右侧 Maven 面板点刷新；仍失败则检查网络或配置阿里云镜像（见步骤 4）。

**Q2：报 `UnsupportedClassVersionError: ... class file version 55.0/61.0/65.0`**

编译版本高于运行环境的 JDK。
1. `pom.xml` 中 `maven.compiler.source/target` 应为 `11`；
2. `File → Project Structure → Project`：SDK 与 Language level 都选 JDK 11；
3. Tomcat 运行配置的 `JRE` 也选 JDK 11；
4. Maven 面板刷新 → 重新 `clean package` → 重启 Tomcat。

**Q3：报 `Communications link failure`**

MySQL 未启动或端口不对。检查 Windows 服务的 MySQL80 是否运行：
`Win + R` → `services.msc` → 找 MySQL80 → 启动。

**Q4：报 `Unknown database 'book_manage'`**

没有执行 `database_init.sql`。按步骤 2 重新执行。

**Q5：报 `Access denied for user 'root'@'localhost'`**

密码不对。三种改法任选：
1. 建 `db.properties` 并写对 `db.password`；
2. Tomcat `VM options` 加 `-Ddb.password=你的密码`；
3. 设环境变量 `DB_PASSWORD`。
改完**必须重启 Tomcat**。

**Q6：中文乱码**

确认 `web.xml` 中 `encodingFilter` 映射为 `/*`；JSP 顶部有 `contentType="text/html;charset=UTF-8"`；
JDBC URL 带 `characterEncoding=UTF-8`；数据库字符集为 `utf8mb4`。

**Q7：访问 `/admin/index.jsp` 一直跳回登录页**

这是**正常的安全行为**：
- 未登录 → 跳 `login.jsp?msg=need_login`
- 已登录但角色不是 admin → 跳 `login.jsp?msg=no_permission`

请用 `admin / 123456` 登录。

**Q8：Tomcat 10 启动报 `javax.servlet` 找不到 / 404**

Tomcat 10+ 改用 `jakarta.servlet`，本项目基于 `javax.servlet`。
**请使用 Tomcat 8.5 或 9.0**。

**Q16：Tomcat 7 启动报 `发生在第[N]行，属性[N]` / `Context 启动失败`**

这是 `web.xml` 格式或版本不被 Tomcat 7 接受。两种情况：

**情况 A：XML 注释里有连续减号**

`org.apache.catalina.startup.ContextConfig parseWebXml` 报错并指出行号时，
先看那一行是不是注释。XML 规范禁止注释内容出现连续两个减号，例如：

```xml
<!-- ============ 分隔线 ============ -->   <!-- 非法！ -->
<!-- 说明文字 ---- 分隔 ---- -->            <!-- 非法！ -->
```

改成不含连续减号的写法即可：

```xml
<!-- 分隔线（用等号或中文符号） -->
<!-- 说明文字（用括号或空行分隔） -->
```

**情况 B：`web.xml` 声明了 Servlet 3.1 但 Tomcat 是 7**

Tomcat 7 只支持到 **Servlet 3.0**，不认 `version="3.1"`，
也不认 `<cookie-config>`、`<tracking-mode>` 这些 3.1 新增元素。

本项目当前 `web.xml` 已改为兼容写法：

```xml
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
         http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0">
```

> Cookie 的 `HttpOnly` 由 Tomcat 的 cookie processor 默认提供（Tomcat 7 起默认开启），
> 去掉 `<cookie-config>` 不会降低安全性。

自检命令（改完 `web.xml` 后务必执行，能提前发现格式错误）：

```bash
# 用 PowerShell 校验 XML 是否合法
[xml]$x = Get-Content src/main/webapp/WEB-INF/web.xml
$x.DocumentElement.version
```

**Q17：Tomcat 7 启动后日志末尾报 `ClassCastException: java.io.ObjectStreamClass$Caches$1 cannot be cast to java.util.Map`**

**这是 Tomcat 7 与 JDK 16+ 的已知兼容问题，与项目代码无关，不影响应用运行。**

原因：`java.io.ObjectStreamClass$Caches` 在 JDK 16 中改变了内部实现，
不再是 `Map`；而 Tomcat 7 的 `WebappClassLoaderBase.clearCache()` 仍按 `Map` 强转，
于是抛出该异常。Tomcat 7 最后版本（7.0.109）发布于 2021 年，不覆盖 JDK 17+。

处理方案（按推荐度排序）：

| 方案 | 做法 | 效果 |
|---|---|---|
| **① 升级 Tomcat（推荐）** | 换用 **Tomcat 9.0.x** | 彻底解决，且支持 Servlet 4.0 / JDK 11+ |
| ② 降级 JDK | 给 Tomcat 单独配 **JDK 8**（不影响系统默认 JDK） | 消除告警，但失去了新 JDK 的安全更新 |
| ③ 忽略 | 不改动 | 该异常发生在**应用停止时的资源清理阶段**，不影响启动和运行 |

如果你必须继续用 Tomcat 7 + JDK 25，方案 ③ 是可接受的——它只是一条警告（`警告` 级别），
发生在 `StandardContext.startInternal` 之后的清理环节，应用本身可以正常访问。

**Q9：`pom.xml` 在 IDEA 中没有 Maven 图标**

右键 `pom.xml` → `Add as Maven Project`。
若文件名前有空格（`  pom.xml`），需先重命名为 `pom.xml`。

**Q10：编译报 `程序包 org.mindrot.jbcrypt 不存在`**

新增的 jBCrypt 依赖未下载。Maven 面板点刷新，或执行 `mvn clean package` 让其重新解析依赖。

**Q11：登录提示"用户名或密码错误"，但账号密码没输错**

检查数据库中的密码字段：
```sql
SELECT username, password FROM users WHERE username = 'admin';
```
- 若是 `$2a$10$...` 开头：正常，请确认密码确实是 `123456`；
- 若是 `123456` 明文：也能登录（系统会**自动升级**为密文），或执行 `sql/password_migration.sql` 批量升级。

**Q12：智能问答一直显示"本地检索模式"**

未读到 API Key。检查：
1. 环境变量名是否精确为 `DEEPSEEK_API_KEY`；
2. 是否**重启了 Tomcat**（环境变量在进程启动时读取）；
3. 若用 `agent.properties`，确认文件放在 `src/main/resources/` 且重新编译过。

**Q13：图书列表显示空白 / 分页不显示**

说明查询结果为 0 条。先确认数据库有数据：
`SELECT COUNT(*) FROM books;`，若为 0，用管理员访问 `/initSystem`。

**Q14：修改密码后仍能用旧密码登录**

浏览器缓存了旧的会话？请退出登录后重新登录。
或数据库中的密码确实没更新，检查 `UPDATE users SET password = ?` 的执行结果。

**Q15：部署后访问出现 404**

- 检查 `Application context` 是否为 `/book_manage`；
- 检查 war 包是否已解压（`webapps/book_manage/` 存在）；
- 检查 Tomcat 端口（默认 8080）是否被占用。

---

## 接口一览

### 页面

| 路径 | 说明 | 权限 |
|---|---|---|
| `/index.jsp` | 系统首页 | 公开 |
| `/login.jsp` | 登录页 | 公开 |
| `/register.jsp` | 读者注册 | 公开 |
| `/agent.jsp` | **智能图书助手** | 公开 |
| `/error.jsp` | 统一错误页 | 公开 |
| `/admin/index.jsp` | 管理员首页 | admin |
| `/admin/bookList.jsp` | 图书管理（分页） | admin |
| `/admin/categoryList.jsp` | 分类管理 | admin |
| `/admin/userList.jsp` | 用户管理 | admin |
| `/admin/borrowList.jsp` | 借阅管理 | admin |
| `/admin/overdueList.jsp` | 逾期与罚款 | admin |
| `/admin/statOverview.jsp` | 数据统计 | admin |
| `/admin/statRanking.jsp` | 借阅排行 | admin |
| `/reader/index.jsp` | 读者首页 | reader |
| `/reader/bookSearch.jsp` | 图书检索（分页） | reader |
| `/reader/myBorrows.jsp` | 我的借阅 | reader |
| `/reader/myReservations.jsp` | 我的预约 | reader |
| `/reader/myFines.jsp` | 我的罚款 | reader |
| `/reader/profile.jsp` | 个人中心 | reader |

### Servlet

| URL | 方法 | 说明 | 权限 |
|---|---|---|---|
| `/loginServlet` | GET/POST | 登录（BCrypt 校验，成功后可自动升级旧明文） | 公开 |
| `/registerServlet` | GET/POST | 读者注册（BCrypt 加密入库） | 公开 |
| `/logoutServlet` | GET/POST | 退出登录 | 公开 |
| **`/agentQueryServlet`** | **POST** | **智能问答接口（返回 JSON）** | 公开 |
| `/admin/indexServlet` | GET/POST | 管理员首页数据 | admin |
| `/admin/bookManageServlet` | GET/POST | 图书增删改查（`action=list/search/add/save/toEdit/update/delete`，支持 `page`/`pageSize`） | admin |
| `/admin/categoryServlet` | GET/POST | 分类增删改查 | admin |
| `/admin/userServlet` | GET/POST | 用户增删改查、重置密码、启停用 | admin |
| `/admin/borrowServlet` | GET/POST | 借阅管理（`list/current/overdue/search/borrow/return/renew/payFine/...`） | admin |
| `/admin/statServlet` | GET/POST | 统计（`overview/ranking`） | admin |
| `/admin/adminProfileServlet` | GET/POST | 管理员个人中心 + 改密 | admin |
| `/admin/checkBooks` | GET/POST | 数据库健康检查 | admin |
| `/admin/testBooksServlet` | GET/POST | 图书数据诊断 | admin |
| `/reader/indexServlet` | GET/POST | 读者首页数据 | reader |
| `/reader/bookSearchServlet` | GET/POST | 图书检索（`list/search`，支持分页） | reader |
| `/reader/borrowOpServlet` | GET/POST | 借书/续借/预约/取消/我的借阅罚款 | reader |
| `/reader/profileServlet` | GET/POST | 读者个人中心 + 改密 | reader |
| `/initSystem` | GET/POST | 系统初始化（建分类/用户/图书） | admin |
| `/insertTestData` | GET/POST | 生成测试数据 | admin |
| `/insertOverdueAndFine` | GET/POST | 生成逾期与罚款数据 | admin |
| `/bookServlet` | GET/POST | 简化版图书增删改 | admin |

### 智能问答接口细节

**请求**

```
POST /book_manage/agentQueryServlet
Content-Type: application/x-www-form-urlencoded

question=有《三体》吗？
```

**成功响应**

```json
{
  "success": true,
  "answer": "根据馆藏资料，本馆有《三体》……",
  "mode": "deepseek",
  "note": null,
  "books": [
    {"bid": "B016", "bname": "三体", "author": "刘慈欣", "available": 18, "location": "B区-1排-3架"}
  ]
}
```

**降级响应**（未配置 Key 或调用失败）

```json
{
  "success": true,
  "answer": "根据馆藏数据库检索，与「有《三体》吗？」相关的图书共 1 本：……",
  "mode": "local",
  "note": "未配置 DeepSeek API Key，当前为本地检索模式（仅展示数据库命中的图书）",
  "books": [ ... ]
}
```

---

## 数据库设计

数据库名 `book_manage`，字符集 `utf8mb4`，共 6 张表。

### users 用户表

| 字段 | 类型 | 约束 / 说明 |
|---|---|---|
| id | INT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一，非空 |
| **password** | **VARCHAR(100)** | **BCrypt 密文（60 字符，`$2a$10$...`）** |
| role | ENUM('admin','reader') | 角色，非空 |
| real_name | VARCHAR(50) | 真实姓名 |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| status | ENUM('active','disabled') | 状态，默认 active |
| create_time | DATETIME | 创建时间 |

### categories 图书分类表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INT | 主键，自增 |
| name | VARCHAR(50) | 分类名，唯一 |
| description | VARCHAR(200) | 描述 |

### books 图书表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INT | 主键，自增 |
| bid | VARCHAR(20) | 图书编号，唯一 |
| isbn | VARCHAR(30) | ISBN |
| bname | VARCHAR(200) | 书名 |
| author | VARCHAR(100) | 作者 |
| publisher | VARCHAR(100) | 出版社 |
| publish_date | DATE | 出版日期 |
| price | DECIMAL(10,2) | 价格 |
| category_id | INT | 外键 → categories.id |
| stock | INT | 总库存 |
| available_stock | INT | 可借库存 |
| location | VARCHAR(50) | 存放位置 |
| description | TEXT | 简介 |

### borrow_records 借阅记录表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INT | 主键，自增 |
| user_id | INT | 外键 → users.id |
| book_id | INT | 外键 → books.id |
| borrow_date | DATE | 借出日期 |
| due_date | DATE | 应还日期 |
| return_date | DATE | 实际归还日期 |
| status | ENUM('borrowed','returned','overdue') | 状态 |
| renew_count | INT | 续借次数 |
| fine_amount | DECIMAL(10,2) | 罚款金额 |
| is_paid | TINYINT | 是否已缴 |

### fine_records 罚款记录表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INT | 主键，自增 |
| user_id | INT | 外键 → users.id |
| borrow_record_id | INT | 外键 → borrow_records.id |
| amount | DECIMAL(10,2) | 罚款金额 |
| days | INT | 逾期天数 |
| status | ENUM('unpaid','paid') | 缴纳状态 |

### reservations 预约表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INT | 主键，自增 |
| user_id | INT | 外键 → users.id |
| book_id | INT | 外键 → books.id |
| reserve_date | DATETIME | 预约时间 |
| status | ENUM('waiting','notified','cancelled','completed') | 状态 |

### 业务规则

| 规则 | 值 | 位置 |
|---|---|---|
| 借阅期限 | 30 天 | `BorrowOpServlet.BORROW_DAYS` |
| 最大续借次数 | 2 次 | `BorrowOpServlet.MAX_RENEW_COUNT` |
| 单读者最大同时借阅 | 5 本 | `BorrowOpServlet.MAX_BORROW_LIMIT` |
| 逾期罚款 | 0.5 元/天 | `BorrowManageServlet.FINE_PER_DAY` |
| 有未缴罚款时 | 禁止借书 | `BorrowOpServlet.borrowBook` |

---

## 贡献与许可

本项目用于学习交流，可自由参考、修改与二次开发；转载请注明出处。

## 作者

- GitHub：[@x15737876973-art](https://github.com/x15737876973-art)
- 仓库：[book-manage-system](https://github.com/x15737876973-art/book-manage-system)