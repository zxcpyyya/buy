-- =============================================
-- 积分、优惠券、优惠券模板、用户优惠券、物流表
-- =============================================

-- =============================================
-- 第1步：优惠券模板表（后台管理）
-- =============================================
DROP TABLE IF EXISTS coupon_template;
CREATE TABLE coupon_template (
    id BIGINT NOT NULL COMMENT '模板ID',
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    
    -- 优惠券类型
    coupon_type TINYINT UNSIGNED NOT NULL COMMENT '类型：1-满减券，2-折扣券，3-无门槛券',
    -- 优惠内容（满减时：减免金额；折扣时：折扣率；无门槛时：减免金额）
    discount_value DECIMAL(10, 2) NOT NULL COMMENT '优惠值',
    -- 满减条件：消费满多少才能用（0表示无门槛）
    min_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛金额',
    
    -- 发行相关
    total_count INT NOT NULL DEFAULT 0 COMMENT '发行总量（0表示不限量）',
    per_user_limit INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '每人限领数量',
    received_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '已领取数量',
    
    -- 时间有效期（任选一种方式）
    valid_type TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '有效期类型：1-固定日期，2-领取后N天有效',
    start_time DATETIME DEFAULT NULL COMMENT '有效期开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '有效期结束时间',
    valid_days INT UNSIGNED DEFAULT NULL COMMENT '领取后有效天数（valid_type=2时）',
    
    -- 状态
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
    
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_valid_type (valid_type),
    KEY idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券模板表';

-- =============================================
-- 第2步：用户优惠券表
-- =============================================
DROP TABLE IF EXISTS user_coupon;
CREATE TABLE user_coupon (
    id BIGINT NOT NULL COMMENT '用户优惠券ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    template_id BIGINT NOT NULL COMMENT '优惠券模板ID',
    
    -- 优惠券快照（冗余，避免模板修改影响）
    coupon_name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    coupon_type TINYINT UNSIGNED NOT NULL COMMENT '类型：1-满减，2-折扣，3-无门槛',
    discount_value DECIMAL(10, 2) NOT NULL COMMENT '优惠值',
    min_amount DECIMAL(10, 2) NOT NULL COMMENT '使用门槛',
    
    -- 有效期
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    
    -- 使用状态
    status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0-未使用，1-已使用，2-已过期',
    used_order_id BIGINT DEFAULT NULL COMMENT '使用该优惠券的订单ID',
    used_time DATETIME DEFAULT NULL COMMENT '使用时间',
    
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_user_template (user_id, template_id),  -- 防止重复领取
    KEY idx_user_id (user_id),
    KEY idx_template_id (template_id),
    KEY idx_status (status),
    KEY idx_end_time (end_time),
    KEY idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- =============================================
-- 第3步：积分记录表
-- =============================================
DROP TABLE IF EXISTS points_record;
CREATE TABLE points_record (
    id BIGINT NOT NULL COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    
    -- 积分变动
    points INT NOT NULL COMMENT '积分变动数量（正数=获取，负数=使用）',
    balance INT NOT NULL COMMENT '变动后余额',
    
    -- 类型
    type TINYINT UNSIGNED NOT NULL COMMENT '类型：1-订单获取，2-订单使用，3-活动赠送，4-过期扣除',
    
    -- 关联业务
    order_id BIGINT DEFAULT NULL COMMENT '关联订单ID',
    description VARCHAR(200) DEFAULT NULL COMMENT '描述/备注',
    
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_type (type),
    KEY idx_order_id (order_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分记录表';

-- =============================================
-- 第4步：物流信息表
-- =============================================
DROP TABLE IF EXISTS express;
CREATE TABLE express (
    id BIGINT NOT NULL COMMENT '物流ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    express_no VARCHAR(50) NOT NULL COMMENT '快递单号',
    
    -- 快递公司
    company_code VARCHAR(20) NOT NULL COMMENT '快递公司编码',
    company_name VARCHAR(50) NOT NULL COMMENT '快递公司名称',
    
    -- 状态
    status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0-待发货，1-运输中，2-派送中，3-已签收，4-拒收/退回',
    
    -- 发货信息
    ship_time DATETIME DEFAULT NULL COMMENT '发货时间',
    receiver_name VARCHAR(50) DEFAULT NULL COMMENT '收货人',
    receiver_phone VARCHAR(20) DEFAULT NULL COMMENT '收货人电话',
    receiver_address VARCHAR(500) DEFAULT NULL COMMENT '收货地址',
    
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_id (order_id),
    KEY idx_express_no (express_no),
    KEY idx_company_code (company_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流信息表';

-- =============================================
-- 第5步：物流轨迹表
-- =============================================
DROP TABLE IF EXISTS express_trace;
CREATE TABLE express_trace (
    id BIGINT NOT NULL COMMENT '轨迹ID',
    express_id BIGINT NOT NULL COMMENT '物流ID',
    
    -- 轨迹信息
    status TINYINT UNSIGNED NOT NULL COMMENT '状态：同express.status',
    status_name VARCHAR(50) NOT NULL COMMENT '状态描述',
    location VARCHAR(100) DEFAULT NULL COMMENT '地点',
    message VARCHAR(500) NOT NULL COMMENT '详细描述',
    
    trace_time DATETIME NOT NULL COMMENT '轨迹时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    PRIMARY KEY (id),
    KEY idx_express_id (express_id),
    KEY idx_trace_time (trace_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流轨迹表';

-- =============================================
-- 第6步：初始化数据
-- =============================================

-- 优惠券模板示例
INSERT INTO coupon_template (id, name, description, coupon_type, discount_value, min_amount, total_count, per_user_limit, valid_type, start_time, end_time, status) VALUES
(1, '新人专享券', '新用户首单满100减20', 1, 20.00, 100.00, 1000, 1, 1, '2024-01-01 00:00:00', '2026-12-31 23:59:59', 1),
(2, '满200减30', '全场通用满200减30', 1, 30.00, 200.00, 0, 3, 1, '2024-01-01 00:00:00', '2026-12-31 23:59:59', 1),
(3, '8折折扣券', '指定商品8折优惠', 2, 0.80, 0.00, 500, 1, 2, NULL, NULL, 30, 1),
(4, '无门槛10元券', '下单立减10元', 3, 10.00, 0.00, 0, 1, 2, NULL, NULL, 7, 1);

-- 快递公司编码参照表（常用）
--顺丰: SF | 圆通: YTO | 中通: ZTO | 韵达: YD | 申通: STO | 京东: JD | EMS: EMS | 邮政:YZPY
