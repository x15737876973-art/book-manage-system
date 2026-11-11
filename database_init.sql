-- ========================================
-- 图书管理系统 MySQL 数据库初始化脚本
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS book_manage DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE book_manage;

-- 删除已存在的表（按依赖关系倒序删除）
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS fine_records;
DROP TABLE IF EXISTS borrow_records;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- ========================================
-- 1. 用户表
-- ========================================
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('admin', 'reader') NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status ENUM('active', 'disabled') DEFAULT 'active',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========================================
-- 2. 分类表
-- ========================================
CREATE TABLE categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- ========================================
-- 3. 图书表
-- ========================================
CREATE TABLE books (
    id INT PRIMARY KEY AUTO_INCREMENT,
    bid VARCHAR(20) NOT NULL UNIQUE COMMENT '图书编号',
    isbn VARCHAR(30),
    bname VARCHAR(200) NOT NULL COMMENT '书名',
    author VARCHAR(100),
    publisher VARCHAR(100),
    publish_date DATE,
    price DECIMAL(10, 2),
    category_id INT,
    stock INT DEFAULT 0 COMMENT '总库存',
    available_stock INT DEFAULT 0 COMMENT '可借库存',
    location VARCHAR(50) COMMENT '存放位置',
    description TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- ========================================
-- 4. 借阅记录表
-- ========================================
CREATE TABLE borrow_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status ENUM('borrowed', 'returned', 'overdue') DEFAULT 'borrowed',
    renew_count INT DEFAULT 0,
    fine_amount DECIMAL(10, 2) DEFAULT 0,
    is_paid TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- ========================================
-- 5. 罚款记录表
-- ========================================
CREATE TABLE fine_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    borrow_record_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    days INT NOT NULL,
    status ENUM('unpaid', 'paid') DEFAULT 'unpaid',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (borrow_record_id) REFERENCES borrow_records(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='罚款记录表';

-- ========================================
-- 6. 预约表
-- ========================================
CREATE TABLE reservations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    reserve_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('waiting', 'notified', 'cancelled', 'completed') DEFAULT 'waiting',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- ========================================
-- 初始化数据
-- ========================================

-- 插入30个分类
INSERT INTO categories(name, description) VALUES
('文学', '文学类图书'),
('历史', '历史类图书'),
('哲学', '哲学类图书'),
('计算机', '计算机类图书'),
('经济管理', '经济管理类图书'),
('数学', '数学类图书'),
('物理', '物理学类图书'),
('化学', '化学类图书'),
('生物', '生物学类图书'),
('医学', '医学类图书'),
('法学', '法学类图书'),
('教育学', '教育学类图书'),
('心理学', '心理学类图书'),
('社会学', '社会学类图书'),
('政治学', '政治学类图书'),
('建筑', '建筑类图书'),
('艺术', '艺术类图书'),
('音乐', '音乐类图书'),
('外语', '外语类图书'),
('新闻传播', '新闻传播类图书'),
('天文', '天文学类图书'),
('地理', '地理学类图书'),
('军事', '军事类图书'),
('农学', '农学类图书'),
('环境科学', '环境科学类图书'),
('电子工程', '电子工程类图书'),
('机械工程', '机械工程类图书'),
('土木工程', '土木工程类图书'),
('体育', '体育类图书'),
('自然科学', '自然科学综合类图书');

-- 插入管理员用户
INSERT INTO users(username, password, role, real_name, phone, email, status) VALUES
('admin', '123456', 'admin', '系统管理员', '13800000000', 'admin@library.com', 'active');

-- 插入29个读者用户
INSERT INTO users(username, password, role, real_name, phone, email, status) VALUES
('reader01', '123456', 'reader', '读者1', '13800000001', 'reader01@library.com', 'active'),
('reader02', '123456', 'reader', '读者2', '13800000002', 'reader02@library.com', 'active'),
('reader03', '123456', 'reader', '读者3', '13800000003', 'reader03@library.com', 'active'),
('reader04', '123456', 'reader', '读者4', '13800000004', 'reader04@library.com', 'active'),
('reader05', '123456', 'reader', '读者5', '13800000005', 'reader05@library.com', 'active'),
('reader06', '123456', 'reader', '读者6', '13800000006', 'reader06@library.com', 'active'),
('reader07', '123456', 'reader', '读者7', '13800000007', 'reader07@library.com', 'active'),
('reader08', '123456', 'reader', '读者8', '13800000008', 'reader08@library.com', 'active'),
('reader09', '123456', 'reader', '读者9', '13800000009', 'reader09@library.com', 'active'),
('reader10', '123456', 'reader', '读者10', '13800000010', 'reader10@library.com', 'active'),
('reader11', '123456', 'reader', '读者11', '13800000011', 'reader11@library.com', 'active'),
('reader12', '123456', 'reader', '读者12', '13800000012', 'reader12@library.com', 'active'),
('reader13', '123456', 'reader', '读者13', '13800000013', 'reader13@library.com', 'active'),
('reader14', '123456', 'reader', '读者14', '13800000014', 'reader14@library.com', 'active'),
('reader15', '123456', 'reader', '读者15', '13800000015', 'reader15@library.com', 'active'),
('reader16', '123456', 'reader', '读者16', '13800000016', 'reader16@library.com', 'active'),
('reader17', '123456', 'reader', '读者17', '13800000017', 'reader17@library.com', 'active'),
('reader18', '123456', 'reader', '读者18', '13800000018', 'reader18@library.com', 'active'),
('reader19', '123456', 'reader', '读者19', '13800000019', 'reader19@library.com', 'active'),
('reader20', '123456', 'reader', '读者20', '13800000020', 'reader20@library.com', 'active'),
('reader21', '123456', 'reader', '读者21', '13800000021', 'reader21@library.com', 'active'),
('reader22', '123456', 'reader', '读者22', '13800000022', 'reader22@library.com', 'active'),
('reader23', '123456', 'reader', '读者23', '13800000023', 'reader23@library.com', 'active'),
('reader24', '123456', 'reader', '读者24', '13800000024', 'reader24@library.com', 'active'),
('reader25', '123456', 'reader', '读者25', '13800000025', 'reader25@library.com', 'active'),
('reader26', '123456', 'reader', '读者26', '13800000026', 'reader26@library.com', 'active'),
('reader27', '123456', 'reader', '读者27', '13800000027', 'reader27@library.com', 'active'),
('reader28', '123456', 'reader', '读者28', '13800000028', 'reader28@library.com', 'active'),
('reader29', '123456', 'reader', '读者29', '13800000029', 'reader29@library.com', 'active');

-- 插入30本图书
INSERT INTO books(bid, isbn, bname, author, publisher, publish_date, price, category_id, stock, available_stock, location) VALUES
('B001', '978-7-02-013757-5', '红楼梦', '曹雪芹', '人民文学出版社', '2008-07-01', 59.70, 1, 5, 5, 'B区-1排-1架'),
('B002', '978-7-02-013758-2', '三国演义', '罗贯中', '人民文学出版社', '2008-07-01', 49.00, 1, 5, 5, 'B区-1排-1架'),
('B003', '978-7-101-05452-6', '万历十五年', '黄仁宇', '中华书局', '2007-01-01', 29.00, 2, 15, 15, 'D区-1排-1架'),
('B004', '978-7-101-00325-8', '史记', '司马迁', '中华书局', '2013-08-01', 128.00, 2, 10, 10, 'D区-1排-2架'),
('B005', '978-7-101-09876-5', '中国哲学简史', '冯友兰', '北京大学出版社', '2013-01-01', 45.00, 3, 8, 8, 'D区-2排-1架'),
('B006', '978-7-208-06347-8', '苏菲的世界', '乔斯坦·贾德', '作家出版社', '2015-04-01', 38.00, 3, 12, 12, 'D区-2排-2架'),
('B007', '978-7-111-21382-6', 'Java编程思想', 'Bruce Eckel', '机械工业出版社', '2007-06-01', 108.00, 4, 10, 10, 'A区-1排-3架'),
('B008', '978-7-111-55842-2', 'Effective Java', 'Joshua Bloch', '机械工业出版社', '2018-01-01', 89.00, 4, 8, 8, 'A区-1排-3架'),
('B009', '978-7-301-25649-8', '经济学原理', 'N.Gregory Mankiw', '北京大学出版社', '2015-05-01', 98.00, 5, 10, 10, 'E区-1排-1架'),
('B010', '978-7-300-23456-7', '管理学', 'Stephen P.Robbins', '中国人民大学出版社', '2017-01-01', 65.00, 5, 8, 8, 'E区-1排-1架'),
('B011', '978-7-04-039661-2', '高等数学', '同济大学数学系', '高等教育出版社', '2014-07-01', 49.00, 6, 30, 30, 'C区-1排-1架'),
('B012', '978-7-04-021481-6', '线性代数', '同济大学数学系', '高等教育出版社', '2014-06-01', 28.00, 6, 25, 25, 'C区-1排-2架'),
('B013', '978-7-04-028582-7', '大学物理', '程守洙', '高等教育出版社', '2016-12-01', 45.00, 7, 20, 20, 'C区-2排-1架'),
('B014', '978-7-04-031234-9', '量子力学导论', '曾谨言', '科学出版社', '2015-03-01', 56.00, 7, 10, 10, 'C区-2排-2架'),
('B015', '978-7-04-045678-3', '有机化学', '邢其毅', '高等教育出版社', '2018-09-01', 68.00, 8, 15, 15, 'C区-3排-1架'),
('B016', '978-7-04-056789-0', '无机化学', '武汉大学', '高等教育出版社', '2017-03-01', 52.00, 8, 12, 12, 'C区-3排-2架'),
('B017', '978-7-03-008567-8', '细胞生物学', '翟中和', '高等教育出版社', '2019-07-01', 58.00, 9, 18, 18, 'C区-4排-1架'),
('B018', '978-7-117-00892-3', '内科学', '葛均波', '人民卫生出版社', '2018-08-01', 120.00, 10, 6, 6, 'F区-1排-1架'),
('B019', '978-7-5118-4567-2', '民法总论', '王泽鉴', '北京大学出版社', '2016-09-01', 55.00, 11, 10, 10, 'G区-1排-1架'),
('B020', '978-7-5197-0823-5', '刑法学', '张明楷', '法律出版社', '2016-07-01', 78.00, 11, 8, 8, 'G区-1排-2架'),
('B021', '978-7-107-31234-2', '教育学基础', '全国十二所重点师范大学', '教育科学出版社', '2014-12-01', 42.00, 12, 20, 20, 'H区-1排-1架'),
('B022', '978-7-303-15678-9', '发展心理学', '林崇德', '人民教育出版社', '2018-05-01', 48.00, 13, 15, 15, 'H区-2排-1架'),
('B023', '978-7-208-14321-5', '乡土中国', '费孝通', '北京大学出版社', '2012-01-01', 25.00, 14, 20, 20, 'I区-1排-1架'),
('B024', '978-7-01-012345-8', '政治学原理', '王浦劬', '北京大学出版社', '2016-07-01', 45.00, 15, 12, 12, 'I区-2排-1架'),
('B025', '978-7-112-19234-6', '中国建筑史', '梁思成', '百花文艺出版社', '2005-01-01', 35.00, 16, 8, 8, 'J区-1排-1架'),
('B026', '978-7-5306-6789-3', '艺术的故事', '贡布里希', '广西美术出版社', '2015-10-01', 78.00, 17, 6, 6, 'J区-2排-1架'),
('B027', '978-7-103-05234-8', '音乐理论基础', '李重光', '人民音乐出版社', '2013-06-01', 32.00, 18, 14, 14, 'K区-1排-1架'),
('B028', '978-7-5600-9876-1', '大学英语综合教程', '郑树棠', '外语教学与研究出版社', '2017-06-01', 55.00, 19, 25, 25, 'L区-1排-1架'),
('B029', '978-7-300-19876-4', '新闻学概论', '郭庆光', '中国人民大学出版社', '2011-04-01', 39.00, 20, 10, 10, 'M区-1排-1架'),
('B030', '978-7-111-67890-5', '时间简史', '史蒂芬·霍金', '湖南科学技术出版社', '2010-01-01', 45.00, 30, 18, 18, 'N区-1排-1架');

-- 插入借阅记录
INSERT INTO borrow_records(user_id, book_id, borrow_date, due_date, return_date, status) VALUES
(2, 1, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(3, 2, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(4, 3, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(5, 4, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(6, 5, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(7, 6, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(8, 7, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(9, 8, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(10, 9, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(11, 10, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(12, 11, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(13, 12, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(14, 13, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(15, 14, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(16, 15, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(17, 16, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(18, 17, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(19, 18, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(20, 19, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(21, 20, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(22, 21, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(23, 22, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(24, 23, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(25, 24, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(26, 25, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(27, 26, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(28, 27, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(29, 28, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(30, 29, '2024-01-01', '2024-01-31', '2024-01-25', 'returned'),
(30, 30, '2024-06-15', '2024-07-15', NULL, 'borrowed');

-- ========================================
-- 初始化完成提示
-- ========================================
SELECT '========================================' AS '';
SELECT '✅ 数据库初始化完成！' AS '';
SELECT '========================================' AS '';
SELECT '管理员账号：admin / 123456' AS '';
SELECT '读者账号：reader01-reader29 / 123456' AS '';
SELECT '分类数量：30个' AS '';
SELECT '图书数量：30本' AS '';
SELECT '用户数量：30个' AS '';
SELECT '借阅记录：30条' AS '';
SELECT '========================================' AS '';