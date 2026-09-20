-- 创建数据库
CREATE DATABASE IF NOT EXISTS finance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE finance;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    role VARCHAR(20) DEFAULT 'user' COMMENT '角色：user-普通用户，admin-管理员',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active-正常，disabled-禁用',
    register_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 财务指标表
CREATE TABLE IF NOT EXISTS financial_indicators (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    stock_code VARCHAR(20) NOT NULL COMMENT '股票代码',
    stock_name VARCHAR(100) NOT NULL COMMENT '股票名称',
    report_date INT NOT NULL COMMENT '报告日期（格式YYYYMMDD）',
    EPS DECIMAL(10, 4) COMMENT '每股收益',
    ROE DECIMAL(10, 2) COMMENT '净资产收益率（%）',
    gross_margin DECIMAL(10, 4) COMMENT '毛利率（%）',
    revenue_growth DECIMAL(10, 4) COMMENT '营收增长率（%）',
    profit_growth DECIMAL(10, 4) COMMENT '利润增长率（%）',
    cash_flow DECIMAL(10, 6) COMMENT '现金流',
    bps DECIMAL(10, 4) COMMENT '每股净资产',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_stock_code (stock_code),
    INDEX idx_report_date (report_date),
    UNIQUE KEY uk_stock_report (stock_code, report_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='财务指标表';

-- 插入测试数据
-- 管理员账号：用户名admin，密码admin123
INSERT INTO user (username, email, phone, password, role) VALUES 
('admin', 'admin@example.com', '13800138000', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'admin'),
('testuser', 'test@example.com', '13800138001', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'user');
-- 创建帖子表
CREATE TABLE IF NOT EXISTS forum_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    title VARCHAR(200),
    category VARCHAR(50),
    content TEXT,
    views INT DEFAULT 0,
    likes INT DEFAULT 0,
    is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 创建评论表
CREATE TABLE IF NOT EXISTS forum_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT,
    user_id BIGINT,
    content TEXT,
    is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES forum_post(id) ON DELETE CASCADE
);

-- 创建点赞表
CREATE TABLE IF NOT EXISTS forum_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT,
    user_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES forum_post(id) ON DELETE CASCADE,
    UNIQUE KEY uk_post_user (post_id, user_id)
);

-- 插入帖子测试数据
INSERT INTO forum_post (user_id, title, category, content, views, likes, create_time) VALUES
(1, '比亚迪2025年财报分析', '财报分析', '比亚迪2025年财报显示，公司营收突破5000亿元，净利润同比增长25%。新能源汽车销量持续领先，电池业务稳步发展。', 1520, 86, '2025-01-15 10:30:00'),
(1, '贵州茅台投资价值分析', '投资分析', '茅台作为白酒龙头，业绩稳健增长。品牌护城河深厚，现金流充沛，是长期投资的优质标的。', 2340, 156, '2025-01-18 14:20:00'),
(2, '科技股未来展望', '行业分析', '随着AI技术的快速发展，科技股有望迎来新的增长机遇。关注半导体、人工智能等细分领域。', 890, 45, '2025-01-20 09:15:00'),
(2, '如何分析上市公司财报', '投资技巧', '分析财报时应关注营收增长、净利润率、现金流状况等关键指标，综合评估公司基本面。', 1120, 78, '2025-01-22 16:45:00');

-- 插入评论测试数据
INSERT INTO forum_comment (post_id, user_id, content, create_time) VALUES
(1, 2, '分析得很到位，比亚迪确实值得关注！', '2025-01-15 11:30:00'),
(1, 1, '谢谢支持，后续会继续分享更多分析。', '2025-01-15 12:00:00'),
(2, 2, '茅台的估值一直是市场关注的焦点，您怎么看？', '2025-01-18 15:00:00'),
(3, 1, '同意您的观点，AI确实是未来的大趋势。', '2025-01-20 10:30:00');

-- 插入点赞测试数据
INSERT INTO forum_like (post_id, user_id, create_time) VALUES
(1, 2, '2025-01-15 11:35:00'),
(2, 2, '2025-01-18 14:30:00'),
(3, 1, '2025-01-20 09:45:00'),
(4, 1, '2025-01-22 17:00:00'),
(4, 2, '2025-01-22 17:30:00');

-- 财报表
CREATE TABLE IF NOT EXISTS financial_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_code VARCHAR(20) NOT NULL,
    stock_name VARCHAR(100) NOT NULL,
    report_year INT NOT NULL,
    pdf_path VARCHAR(500),
    status VARCHAR(20) DEFAULT 'uploaded' COMMENT '状态：uploaded-已上传，analyzed-已分析',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_stock_code (stock_code),
    INDEX idx_report_year (report_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='财报表';

-- 财报分析结果表（只保存第一次分析的结果）
CREATE TABLE IF NOT EXISTS analysis_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    stock_code VARCHAR(20) NOT NULL COMMENT '股票代码',
    stock_name VARCHAR(100) COMMENT '股票名称',
    report_year INT NOT NULL COMMENT '报告年份',
    revenue DECIMAL(20, 2) COMMENT '营业收入(亿元)',
    net_profit DECIMAL(20, 2) COMMENT '净利润(亿元)',
    deduct_non_recurring_profit DECIMAL(20, 2) COMMENT '扣非净利润(亿元)',
    total_assets DECIMAL(20, 2) COMMENT '总资产(亿元)',
    net_assets DECIMAL(20, 2) COMMENT '净资产(亿元)',
    eps DECIMAL(10, 4) COMMENT '每股收益',
    roe DECIMAL(10, 4) COMMENT '净资产收益率(%)',
    roa DECIMAL(10, 4) COMMENT '总资产收益率(%)',
    gross_margin DECIMAL(10, 4) COMMENT '毛利率(%)',
    net_margin DECIMAL(10, 4) COMMENT '净利率(%)',
    current_ratio DECIMAL(10, 4) COMMENT '流动比率',
    quick_ratio DECIMAL(10, 4) COMMENT '速动比率',
    debt_ratio DECIMAL(10, 4) COMMENT '资产负债率(%)',
    revenue_growth DECIMAL(10, 4) COMMENT '营收增长率(%)',
    profit_growth DECIMAL(10, 4) COMMENT '净利润增长率(%)',
    ai_analysis TEXT COMMENT 'AI分析报告',
    analyzed_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分析时间',
    UNIQUE KEY uk_stock_year (stock_code, report_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='财报分析结果表';

-- 为帖子表添加状态字段
ALTER TABLE forum_post ADD COLUMN status VARCHAR(20) DEFAULT 'normal' COMMENT '状态：normal-正常，deleted-已删除，pinned-已置顶';

-- 贴吧主理人申请表
CREATE TABLE IF NOT EXISTS bar_manager_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    category VARCHAR(50) NOT NULL COMMENT '申请的贴吧分类',
    reason TEXT NOT NULL COMMENT '申请理由',
    experience TEXT COMMENT '相关经验',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待审核，approved-已通过，rejected-已拒绝',
    review_user_id BIGINT COMMENT '审核人ID',
    review_comment TEXT COMMENT '审核意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    review_time DATETIME COMMENT '审核时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_category (category),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (review_user_id) REFERENCES user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='贴吧主理人申请表';

-- 贴吧主理人管理表
CREATE TABLE IF NOT EXISTS bar_manager (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '管理ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    category VARCHAR(50) NOT NULL COMMENT '管理的贴吧分类',
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始管理时间',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active-在职，inactive-离职',
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_category (user_id, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='贴吧主理人管理表';

-- 插入测试数据 - 管理员用户作为贴吧主理人
INSERT INTO bar_manager (user_id, category) VALUES (1, '财报分析'), (1, '投资分析'), (1, '行业分析'), (1, '投资技巧');