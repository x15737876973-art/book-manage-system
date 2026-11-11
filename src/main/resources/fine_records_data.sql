-- ========================================
-- 罚款记录SQL数据
-- ========================================

-- 清空现有数据（可选）
-- DELETE FROM fine_records;

-- 插入30条罚款记录
INSERT INTO fine_records (user_id, borrow_record_id, amount, days, status, create_time) VALUES
(1, 1, 5.0, 10, 'unpaid', NOW()),
(1, 2, 7.5, 15, 'unpaid', NOW()),
(1, 3, 3.0, 6, 'unpaid', NOW()),
(1, 4, 10.0, 20, 'unpaid', NOW()),
(1, 5, 2.5, 5, 'unpaid', NOW()),
(2, 6, 8.0, 16, 'unpaid', NOW()),
(2, 7, 12.0, 24, 'unpaid', NOW()),
(2, 8, 4.0, 8, 'unpaid', NOW()),
(2, 9, 15.0, 30, 'unpaid', NOW()),
(2, 10, 6.0, 12, 'unpaid', NOW()),
(3, 11, 9.0, 18, 'unpaid', NOW()),
(3, 12, 11.0, 22, 'unpaid', NOW()),
(3, 13, 5.5, 11, 'unpaid', NOW()),
(3, 14, 13.0, 26, 'unpaid', NOW()),
(3, 15, 7.0, 14, 'unpaid', NOW()),
(4, 16, 3.5, 7, 'unpaid', NOW()),
(4, 17, 14.0, 28, 'unpaid', NOW()),
(4, 18, 8.5, 17, 'unpaid', NOW()),
(4, 19, 6.5, 13, 'unpaid', NOW()),
(4, 20, 10.5, 21, 'unpaid', NOW()),
(5, 21, 4.5, 9, 'unpaid', NOW()),
(5, 22, 16.0, 32, 'unpaid', NOW()),
(5, 23, 9.5, 19, 'unpaid', NOW()),
(5, 24, 7.5, 15, 'unpaid', NOW()),
(5, 25, 12.5, 25, 'unpaid', NOW()),
(6, 26, 5.0, 10, 'unpaid', NOW()),
(6, 27, 11.5, 23, 'unpaid', NOW()),
(6, 28, 8.0, 16, 'unpaid', NOW()),
(6, 29, 14.5, 29, 'unpaid', NOW()),
(6, 30, 6.0, 12, 'unpaid', NOW());

-- 插入10条已支付的罚款记录
INSERT INTO fine_records (user_id, borrow_record_id, amount, days, status, create_time) VALUES
(1, 31, 5.0, 10, 'paid', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 32, 8.0, 16, 'paid', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(3, 33, 9.0, 18, 'paid', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(4, 34, 6.0, 12, 'paid', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5, 35, 7.0, 14, 'paid', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(6, 36, 5.5, 11, 'paid', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 37, 10.0, 20, 'paid', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(2, 38, 12.0, 24, 'paid', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(3, 39, 8.5, 17, 'paid', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(4, 40, 11.5, 23, 'paid', DATE_SUB(NOW(), INTERVAL 11 DAY));

-- 查询验证
SELECT 
    fr.id AS 罚款ID,
    u.username AS 读者姓名,
    b.bname AS 图书名称,
    fr.amount AS 罚款金额,
    fr.days AS 逾期天数,
    fr.status AS 状态,
    fr.create_time AS 创建时间
FROM fine_records fr
LEFT JOIN users u ON fr.user_id = u.id
LEFT JOIN borrow_records br ON fr.borrow_record_id = br.id
LEFT JOIN books b ON br.book_id = b.id
ORDER BY fr.id DESC;
