-- 图书管理系统数据库初始化脚本
-- 数据库：library_manage (SQL Server)

-- 如果数据库不存在则创建
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'library_manage')
BEGIN
    CREATE DATABASE library_manage;
END
GO

USE library_manage;
GO

-- 创建用户表（如果不存在）
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='user' AND xtype='U')
BEGIN
    CREATE TABLE [user] (
        id INT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) NOT NULL,
        password NVARCHAR(50) NOT NULL,
        role NVARCHAR(20) NOT NULL
    );
END
GO

-- 创建图书表（如果不存在）
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='book' AND xtype='U')
BEGIN
    CREATE TABLE book (
        bid NVARCHAR(50) PRIMARY KEY,
        bname NVARCHAR(100) NOT NULL,
        author NVARCHAR(50),
        stock INT DEFAULT 0
    );
END
GO

-- 清空用户表（可选，如果需要重新初始化）
-- DELETE FROM [user];

-- 插入管理员账号：账号 ab，密码 123，角色 admin
IF NOT EXISTS (SELECT * FROM [user] WHERE username='ab' AND role='admin')
BEGIN
    INSERT INTO [user](username, password, role) VALUES('ab', '123', 'admin');
    PRINT '管理员账号创建成功：账号=ab，密码=123';
END
GO

-- 插入普通用户账号：账号 ab，密码 123，角色 reader
IF NOT EXISTS (SELECT * FROM [user] WHERE username='ab' AND role='reader')
BEGIN
    INSERT INTO [user](username, password, role) VALUES('ab', '123', 'reader');
    PRINT '普通用户账号创建成功：账号=ab，密码=123';
END
GO

-- 查询所有用户
SELECT * FROM [user];
GO

PRINT '数据库初始化完成！';
PRINT '管理员账号：ab / 123';
PRINT '普通用户账号：ab / 123';