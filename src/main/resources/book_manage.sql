-- =============================================
-- 图书管理系统完整数据库脚本
-- 数据库：book_manage
-- 适用于 SQL Server
-- =============================================

-- 创建数据库
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'book_manage')
BEGIN
    CREATE DATABASE book_manage;
END
GO

USE book_manage;
GO

-- =============================================
-- 1. 创建用户表
-- =============================================
IF EXISTS (SELECT * FROM sysobjects WHERE name='users' AND xtype='U')
    DROP TABLE users;
GO

CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(50) NOT NULL,
    role NVARCHAR(20) NOT NULL CHECK(role IN ('admin', 'reader'))
);
GO

-- 插入用户数据
-- 管理员账号：ab / 123
INSERT INTO users(username, password, role, real_name, status) VALUES('ab', '123', 'admin', '管理员', 'active');
-- 普通用户账号：ab / 123
INSERT INTO users(username, password, role, real_name, status) VALUES('ab', '123', 'reader', '读者', 'active');
GO

-- =============================================
-- 2. 创建图书表
-- =============================================
IF EXISTS (SELECT * FROM sysobjects WHERE name='books' AND xtype='U')
    DROP TABLE books;
GO

CREATE TABLE books (
    bid NVARCHAR(50) PRIMARY KEY,
    bname NVARCHAR(100) NOT NULL,
    author NVARCHAR(50),
    stock INT DEFAULT 0,
    price DECIMAL(10,2) DEFAULT 0.00,
    publisher NVARCHAR(100),
    publish_date DATE,
    category NVARCHAR(50)
);
GO

-- 插入图书数据
INSERT INTO books(bid, bname, author, stock, price, publisher, publish_date, category) VALUES
('B001', 'Java编程思想', 'Bruce Eckel', 10, 108.00, '机械工业出版社', '2007-06-01', '编程'),
('B002', 'Effective Java', 'Joshua Bloch', 8, 89.00, '机械工业出版社', '2018-01-01', '编程'),
('B003', '深入理解计算机系统', 'Randal E.Bryant', 5, 139.00, '机械工业出版社', '2016-07-01', '计算机科学'),
('B004', '数据结构与算法分析', 'Mark Allen Weiss', 12, 69.00, '机械工业出版社', '2013-05-01', '编程'),
('B005', 'Python编程从入门到实践', 'Eric Matthes', 15, 89.00, '人民邮电出版社', '2020-01-01', '编程'),
('B006', '数据库系统概论', '王珊', 20, 49.00, '高等教育出版社', '2014-09-01', '数据库'),
('B007', '计算机网络', '谢希仁', 18, 45.00, '电子工业出版社', '2017-01-01', '网络'),
('B008', '操作系统概念', 'Abraham Silberschatz', 6, 75.00, '高等教育出版社', '2013-07-01', '操作系统'),
('B009', 'Spring实战', 'Craig Walls', 9, 99.00, '人民邮电出版社', '2016-04-01', '编程'),
('B010', '设计模式', 'Erich Gamma', 7, 55.00, '机械工业出版社', '2010-07-01', '编程');
GO

-- =============================================
-- 3. 创建借阅记录表
-- =============================================
IF EXISTS (SELECT * FROM sysobjects WHERE name='borrow_records' AND xtype='U')
    DROP TABLE borrow_records;
GO

CREATE TABLE borrow_records (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    bid NVARCHAR(50) NOT NULL,
    borrow_date DATETIME DEFAULT GETDATE(),
    return_date DATETIME,
    status NVARCHAR(20) DEFAULT 'borrowed' CHECK(status IN ('borrowed', 'returned')),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (bid) REFERENCES books(bid)
);
GO

-- 插入借阅记录示例
INSERT INTO borrow_records(user_id, bid, borrow_date, return_date, status) VALUES
(1, 'B001', '2024-01-15', '2024-02-15', 'returned'),
(2, 'B002', '2024-02-01', NULL, 'borrowed'),
(1, 'B003', '2024-02-10', NULL, 'borrowed');
GO

-- =============================================
-- 查询验证
-- =============================================
PRINT '========== 数据库初始化完成 ==========';
PRINT '';
PRINT '用户表数据：';
SELECT * FROM users;
PRINT '';
PRINT '图书表数据：';
SELECT * FROM books;
PRINT '';
PRINT '借阅记录表数据：';
SELECT * FROM borrow_records;
PRINT '';
PRINT '========================================';
PRINT '管理员账号：ab / 123';
PRINT '普通用户账号：ab / 123';
PRINT '========================================';
GO
