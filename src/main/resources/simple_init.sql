-- =============================================
-- 简化版数据库脚本 - 直接在 IDEA 中执行
-- 数据库：book_manage
-- =============================================

-- 创建数据库
CREATE DATABASE book_manage;
GO

USE book_manage;
GO

-- 创建用户表
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(50) NOT NULL,
    role NVARCHAR(20) NOT NULL
);
GO

-- 插入用户
INSERT INTO users(username, password, role, real_name, status) VALUES('ab', '123', 'admin', '管理员', 'active');
INSERT INTO users(username, password, role, real_name, status) VALUES('ab', '123', 'reader', '读者', 'active');
GO

-- 创建图书表
CREATE TABLE books (
    bid NVARCHAR(50) PRIMARY KEY,
    bname NVARCHAR(100) NOT NULL,
    author NVARCHAR(50),
    stock INT DEFAULT 0
);
GO

-- 插入图书
INSERT INTO books(bid, bname, author, stock) VALUES
('B001', 'Java编程思想', 'Bruce Eckel', 10),
('B002', 'Effective Java', 'Joshua Bloch', 8),
('B003', '深入理解计算机系统', 'Randal E.Bryant', 5),
('B004', '数据结构与算法分析', 'Mark Allen Weiss', 12),
('B005', 'Python编程从入门到实践', 'Eric Matthes', 15);
GO

SELECT '数据库创建成功！' AS Result;
SELECT * FROM users;
SELECT * FROM books;
GO
