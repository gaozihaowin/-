-- 证书测试数据插入脚本
-- 为用户 2026000001 插入3个测试证书

INSERT INTO t_certificate (user_id, type, number, image_url, issue_time) VALUES
(2026000001, '荣誉证书', 'CERT-2026-001', 'http://localhost:8080/uploads/images/cert_001.png', '2026-01-15 10:00:00'),
(2026000001, '学习证书', 'CERT-2026-002', 'http://localhost:8080/uploads/images/cert_002.png', '2026-02-20 14:30:00'),
(2026000001, '结业证书', 'CERT-2026-003', 'http://localhost:8080/uploads/images/cert_003.png', '2026-03-25 09:15:00');