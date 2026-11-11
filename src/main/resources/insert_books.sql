-- 插入20本图书数据
-- 图书表结构: books(bid, bname, author, stock, price, publisher, publish_date, category)

-- 文学类
INSERT INTO books(bid, bname, author, stock, price, publisher, publish_date, category) VALUES
('B011', '百年孤独', '加西亚·马尔克斯', 15, 55.00, '南海出版公司', '2011-06-01', '文学'),
('B012', '活着', '余华', 20, 35.00, '作家出版社', '2012-08-01', '文学'),
('B013', '围城', '钱钟书', 12, 39.00, '人民文学出版社', '1991-02-01', '文学'),
('B014', '红楼梦', '曹雪芹', 10, 68.00, '人民文学出版社', '2008-07-01', '文学');

-- 计算机类
INSERT INTO books(bid, bname, author, stock, price, publisher, publish_date, category) VALUES
('B015', '算法导论', 'Thomas H.Cormen', 8, 128.00, '机械工业出版社', '2013-01-01', '计算机'),
('B016', 'Linux命令行与Shell脚本编程', 'Richard Blum', 10, 79.00, '人民邮电出版社', '2019-05-01', '计算机'),
('B017', 'JavaScript高级程序设计', 'Nicholas C.Zakas', 12, 99.00, '人民邮电出版社', '2020-03-01', '计算机'),
('B018', 'MySQL必知必会', 'Ben Forta', 15, 49.00, '人民邮电出版社', '2009-01-01', '计算机');

-- 经济管理类
INSERT INTO books(bid, bname, author, stock, price, publisher, publish_date, category) VALUES
('B019', '经济学原理', 'N.Gregory Mankiw', 10, 98.00, '北京大学出版社', '2015-05-01', '经济管理'),
('B020', '管理学', 'Stephen P.Robbins', 8, 65.00, '中国人民大学出版社', '2017-01-01', '经济管理'),
('B021', '营销管理', 'Philip Kotler', 6, 88.00, '格致出版社', '2016-04-01', '经济管理');

-- 历史类
INSERT INTO books(bid, bname, author, stock, price, publisher, publish_date, category) VALUES
('B022', '明朝那些事儿', '当年明月', 25, 188.00, '浙江人民出版社', '2011-12-01', '历史'),
('B023', '万历十五年', '黄仁宇', 15, 29.00, '中华书局', '2007-01-01', '历史'),
('B024', '史记', '司马迁', 10, 128.00, '中华书局', '2013-08-01', '历史');

-- 理工类
INSERT INTO books(bid, bname, author, stock, price, publisher, publish_date, category) VALUES
('B025', '高等数学', '同济大学数学系', 30, 49.00, '高等教育出版社', '2014-07-01', '理工'),
('B026', '大学物理', '程守洙', 20, 45.00, '高等教育出版社', '2016-12-01', '理工'),
('B027', '线性代数', '同济大学数学系', 25, 28.00, '高等教育出版社', '2014-06-01', '理工'),
('B028', '概率论与数理统计', '浙江大学', 22, 35.00, '高等教育出版社', '2015-03-01', '理工');

-- 文学类（补充）
INSERT INTO books(bid, bname, author, stock, price, publisher, publish_date, category) VALUES
('B029', '三体', '刘慈欣', 18, 93.00, '重庆出版社', '2008-01-01', '文学'),
('B030', '平凡的世界', '路遥', 16, 99.00, '北京十月文艺出版社', '2010-01-01', '文学');