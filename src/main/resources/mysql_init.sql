-- =============================================
-- 图书管理系统 MySQL数据库脚本
-- 数据库：book_manage
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS book_manage DEFAULT CHARACTER SET utf8mb4;

USE book_manage;

-- =============================================
-- 1. 用户表
-- =============================================
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'reader',
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status VARCHAR(20) DEFAULT 'active',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入默认管理员账号
INSERT INTO users(username, password, role, real_name, status) VALUES
('a', '123456', 'admin', '系统管理员', 'active'),
('d', '123456', 'reader', '张三', 'active'),
('reader2', '123456', 'reader', '李四', 'active');

-- =============================================
-- 2. 分类表
-- =============================================
DROP TABLE IF EXISTS categories;

CREATE TABLE categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 插入分类数据
INSERT INTO categories(name, description) VALUES
('文学', '文学作品、小说、散文等'),
('理工', '数学、物理、化学等理工科书籍'),
('历史', '历史类书籍'),
('计算机', '计算机编程、软件开发相关书籍'),
('经济管理', '经济学、管理学相关书籍'),
('艺术', '艺术、音乐、美术相关书籍'),
('教育', '教育类书籍'),
('其他', '其他分类');

-- =============================================
-- 3. 图书表
-- =============================================
DROP TABLE IF EXISTS books;

CREATE TABLE books (
    id INT PRIMARY KEY AUTO_INCREMENT,
    bid VARCHAR(50) NOT NULL UNIQUE COMMENT '图书编号',
    isbn VARCHAR(50) COMMENT 'ISBN号',
    bname VARCHAR(100) NOT NULL COMMENT '书名',
    author VARCHAR(50) COMMENT '作者',
    publisher VARCHAR(100) COMMENT '出版社',
    publish_date DATE COMMENT '出版日期',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT '价格',
    category_id INT COMMENT '分类ID',
    stock INT DEFAULT 0 COMMENT '总库存',
    available_stock INT DEFAULT 0 COMMENT '可借数量',
    location VARCHAR(50) COMMENT '存放位置',
    description TEXT COMMENT '简介',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- 插入图书数据
INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) VALUES
('B001', '9787111213826', 'Java编程思想', 'Bruce Eckel', '机械工业出版社', '2007-06-01', 108.00, 4, 10, 8, 'A区-1架'),
('B002', '9787111618566', 'Effective Java', 'Joshua Bloch', '机械工业出版社', '2018-01-01', 89.00, 4, 8, 6, 'A区-2架'),
('B003', '9787111544937', '深入理解计算机系统', 'Randal E.Bryant', '机械工业出版社', '2016-07-01', 139.00, 4, 5, 3, 'A区-3架'),
('B004', '9787111396175', '数据结构与算法分析', 'Mark Allen Weiss', '机械工业出版社', '2013-05-01', 69.00, 4, 12, 10, 'A区-4架'),
('B005', '9787115428028', 'Python编程从入门到实践', 'Eric Matthes', '人民邮电出版社', '2020-01-01', 89.00, 4, 15, 12, 'A区-5架'),
('B006', '9787040406641', '数据库系统概论', '王珊', '高等教育出版社', '2014-09-01', 49.00, 4, 20, 18, 'B区-1架'),
('B007', '9787121302856', '计算机网络', '谢希仁', '电子工业出版社', '2017-01-01', 45.00, 4, 18, 15, 'B区-2架'),
('B008', '9787040358303', '操作系统概念', 'Abraham Silberschatz', '高等教育出版社', '2013-07-01', 75.00, 4, 6, 4, 'B区-3架'),
('B009', '9787115417305', 'Spring实战', 'Craig Walls', '人民邮电出版社', '2016-04-01', 99.00, 4, 9, 7, 'B区-4架'),
('B010', '9787111075757', '设计模式', 'Erich Gamma', '机械工业出版社', '2010-07-01', 55.00, 4, 7, 5, 'B区-5架'),
('B011', '9787020008730', '红楼梦', '曹雪芹', '人民文学出版社', '2008-01-01', 59.00, 1, 25, 22, 'C区-1架'),
('B012', '9787020008723', '西游记', '吴承恩', '人民文学出版社', '2008-01-01', 45.00, 1, 30, 28, 'C区-2架'),
('B013', '9787020008716', '三国演义', '罗贯中', '人民文学出版社', '2008-01-01', 48.00, 1, 28, 25, 'C区-3架'),
('B014', '9787101003048', '史记', '司马迁', '中华书局', '2013-01-01', 128.00, 3, 15, 13, 'D区-1架'),
('B015', '9787301149838', '经济学原理', '曼昆', '北京大学出版社', '2015-01-01', 68.00, 5, 20, 18, 'E区-1架');

-- =============================================
-- 4. 借阅记录表
-- =============================================
DROP TABLE IF EXISTS borrow_records;

CREATE TABLE borrow_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    due_date DATETIME COMMENT '应还日期',
    return_date DATETIME COMMENT '实还日期',
    status VARCHAR(20) DEFAULT 'borrowed' COMMENT 'borrowed/returned/overdue',
    renew_count INT DEFAULT 0 COMMENT '续借次数',
    fine_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '罚款金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- =============================================
-- 5. 预约表
-- =============================================
DROP TABLE IF EXISTS reservations;

CREATE TABLE reservations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    reserve_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'waiting' COMMENT 'waiting/notified/completed/cancelled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- =============================================
-- 6. 罚款记录表
-- =============================================
DROP TABLE IF EXISTS fine_records;

CREATE TABLE fine_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    borrow_record_id INT NOT NULL,
    days INT DEFAULT 0 COMMENT '逾期天数',
    amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '罚款金额',
    status VARCHAR(20) DEFAULT 'unpaid' COMMENT 'unpaid/paid',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (borrow_record_id) REFERENCES borrow_records(id)
);

-- =============================================
-- 插入示例借阅记录
-- =============================================
INSERT INTO borrow_records(user_id, book_id, borrow_date, due_date, return_date, status) VALUES
(2, 1, '2024-01-15', '2024-02-15', '2024-02-14', 'returned'),
(2, 2, '2024-02-01', '2024-03-01', NULL, 'borrowed'),
(3, 3, '2024-02-10', '2024-03-10', NULL, 'borrowed'),
(2, 4, '2024-01-01', '2024-01-31', '2024-02-05', 'returned');

-- 插入示例罚款记录
INSERT INTO fine_records(user_id, borrow_record_id, days, amount, status) VALUES
(2, 4, 5, 2.50, 'unpaid');

-- =============================================
-- 查询验证
-- =============================================
SELECT '数据库初始化完成！' AS message;
SELECT COUNT(*) AS user_count FROM users;
SELECT COUNT(*) AS book_count FROM books;
SELECT COUNT(*) AS category_count FROM categories;