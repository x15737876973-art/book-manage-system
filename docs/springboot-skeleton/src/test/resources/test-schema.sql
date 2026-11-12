-- ==========================================================
-- 测试用建表脚本（H2 内存库）
-- 仅测试时执行，不影响生产数据库
-- ==========================================================

DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS categories;

CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200)
);

CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bid VARCHAR(20) NOT NULL UNIQUE,
    isbn VARCHAR(30),
    bname VARCHAR(200) NOT NULL,
    author VARCHAR(100),
    publisher VARCHAR(100),
    publish_date DATE,
    price DECIMAL(10, 2),
    category_id INT,
    stock INT DEFAULT 0,
    available_stock INT DEFAULT 0,
    location VARCHAR(50),
    description TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);