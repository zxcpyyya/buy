-- =============================================
-- 商城系统数据库初始化脚本
-- 遵循阿里MySQL数据库规约
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mall;

-- =============================================
-- 1. 用户表
-- =============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    gender TINYINT UNSIGNED DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_phone (phone),
    KEY idx_email (email),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 商品分类表
-- =============================================
DROP TABLE IF EXISTS product_category;
CREATE TABLE product_category (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID',
    level TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '层级',
    icon VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    sort INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- =============================================
-- 3. 商品表
-- =============================================
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category_id BIGINT UNSIGNED DEFAULT NULL COMMENT '分类ID',
    price DECIMAL(10, 2) NOT NULL COMMENT '商品价格',
    stock INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '库存',
    image VARCHAR(500) DEFAULT NULL COMMENT '商品主图',
    images TEXT COMMENT '商品图片集，JSON格式',
    description TEXT COMMENT '商品描述',
    sales INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '销量',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_sales (sales),
    KEY idx_create_time (create_time),
    FULLTEXT KEY ft_name_desc (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- =============================================
-- 4. 购物车表
-- =============================================
DROP TABLE IF EXISTS cart;
CREATE TABLE cart (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    total_price DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '总价',
    total_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '商品总数量',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- =============================================
-- 5. 购物车商品项表
-- =============================================
DROP TABLE IF EXISTS cart_item;
CREATE TABLE cart_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '购物车商品ID',
    cart_id BIGINT UNSIGNED NOT NULL COMMENT '购物车ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID（冗余，便于查询）',
    product_id BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
    quantity INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '购买数量',
    price DECIMAL(10, 2) NOT NULL COMMENT '商品单价',
    selected TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否选中：0-否，1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_cart_id (cart_id),
    KEY idx_user_id (user_id),
    KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车商品项表';

-- =============================================
-- 6. 收货地址表
-- =============================================
DROP TABLE IF EXISTS address;
CREATE TABLE address (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    consignee VARCHAR(50) NOT NULL COMMENT '收货人',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    district VARCHAR(50) NOT NULL COMMENT '区县',
    detail_address VARCHAR(255) NOT NULL COMMENT '详细地址',
    is_default TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否默认：0-否，1-是',
    label VARCHAR(50) DEFAULT NULL COMMENT '地址标签（家、公司等）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- =============================================
-- 7. 订单表
-- =============================================
DROP TABLE IF EXISTS order_info;
CREATE TABLE order_info (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    total_price DECIMAL(12, 2) NOT NULL COMMENT '订单总价',
    pay_price DECIMAL(12, 2) NOT NULL COMMENT '实付金额',
    pay_type TINYINT UNSIGNED DEFAULT NULL COMMENT '支付方式：1-微信，2-支付宝',
    order_status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单状态：1-待支付，2-已支付，3-已发货，4-已完成，5-已取消',
    delivery_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '配送状态：0-未发货，1-已发货',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
    receiver_address VARCHAR(500) NOT NULL COMMENT '收货地址',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    delivery_time DATETIME DEFAULT NULL COMMENT '发货时间',
    receive_time DATETIME DEFAULT NULL COMMENT '收货时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_order_status (order_status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =============================================
-- 8. 订单商品项表
-- =============================================
DROP TABLE IF EXISTS order_item;
CREATE TABLE order_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
    order_id BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID（冗余，便于查询）',
    product_id BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称（冗余）',
    product_image VARCHAR(500) DEFAULT NULL COMMENT '商品图片（冗余）',
    price DECIMAL(10, 2) NOT NULL COMMENT '商品单价',
    quantity INT UNSIGNED NOT NULL COMMENT '购买数量',
    total_price DECIMAL(12, 2) NOT NULL COMMENT '小计',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_user_id (user_id),
    KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品项表';

-- =============================================
-- 插入初始化数据
-- =============================================

-- 插入商品分类数据
INSERT INTO product_category (id, name, parent_id, level, icon, sort, status) VALUES
(1, '数码电子', 0, 1, 'icon-digital', 1, 1),
(2, '服装鞋包', 0, 1, 'icon-clothing', 2, 1),
(3, '食品生鲜', 0, 1, 'icon-food', 3, 1),
(4, '家居百货', 0, 1, 'icon-home', 4, 1),
(5, '图书音像', 0, 1, 'icon-book', 5, 1),
(6, '手机通讯', 1, 2, 'icon-phone', 1, 1),
(7, '电脑办公', 1, 2, 'icon-computer', 2, 1),
(8, '智能设备', 1, 2, 'icon-smart', 3, 1);

-- 插入测试商品数据
INSERT INTO product (name, category_id, price, stock, image, images, description, sales, status) VALUES
('iPhone 15 Pro Max 256GB', 6, 9999.00, 100, 'https://picsum.photos/400/400?random=1', '["https://picsum.photos/400/400?random=1", "https://picsum.photos/400/400?random=2"]', '苹果旗舰手机，A17 Pro芯片，钛金属设计', 520, 1),
('小米14 Ultra 影像旗舰', 6, 6499.00, 200, 'https://picsum.photos/400/400?random=3', '["https://picsum.photos/400/400?random=3", "https://picsum.photos/400/400?random=4"]', '小米旗舰手机，徕卡影像，骁龙8 Gen3', 380, 1),
('联想拯救者Y9000P游戏本', 7, 10999.00, 50, 'https://picsum.photos/400/400?random=5', '["https://picsum.photos/400/400?random=5", "https://picsum.photos/400/400?random=6"]', '16英寸游戏本，i9-14900HX，RTX4060', 150, 1),
('Apple MacBook Pro 14寸', 7, 15999.00, 80, 'https://picsum.photos/400/400?random=7', '["https://picsum.photos/400/400?random=7", "https://picsum.photos/400/400?random=8"]', 'M3 Pro芯片，18G+512G，深空黑', 220, 1),
('华为FreeBuds Pro 3耳机', 8, 1499.00, 500, 'https://picsum.photos/400/400?random=9', '["https://picsum.photos/400/400?random=9", "https://picsum.photos/400/400?random=10"]', '主动降噪耳机，麒麟A2芯片', 890, 1),
('男士纯棉休闲T恤', 2, 99.00, 1000, 'https://picsum.photos/400/400?random=11', '["https://picsum.photos/400/400?random=11", "https://picsum.photos/400/400?random=12"]', '2024夏季新款，纯棉面料，透气舒适', 2300, 1),
('女士手提包真皮', 2, 699.00, 300, 'https://picsum.photos/400/400?random=13', '["https://picsum.photos/400/400?random=13", "https://picsum.photos/400/400?random=14"]', '头层牛皮，简约时尚，多色可选', 560, 1),
('阳澄湖大闸蟹礼券', 3, 388.00, 2000, 'https://picsum.photos/400/400?random=15', '["https://picsum.photos/400/400?random=15", "https://picsum.photos/400/400?random=16"]', '公4两母3两，8只装，冷链配送', 1200, 1),
('智能扫地机器人', 4, 2599.00, 150, 'https://picsum.photos/400/400?random=17', '["https://picsum.photos/400/400?random=17", "https://picsum.photos/400/400?random=18"]', '激光导航，自动集尘，扫拖一体', 340, 1),
('空气炸锅多功能', 4, 399.00, 800, 'https://picsum.photos/400/400?random=19', '["https://picsum.photos/400/400?random=19", "https://picsum.photos/400/400?random=20"]', '5.5L大容量，可视窗口，不粘涂层', 1100, 1);

-- 插入测试用户（密码为123456，使用MD5+盐值加密）
INSERT INTO sys_user (username, password, nickname, email, phone, status) VALUES
('admin', 'MTI2MzY4OmU1ZTFhODg2ZjdkODFjY2UwZjBkNWE3ZTk1ZjgxOTVi', '管理员', 'admin@example.com', '13800138000', 1),
('test', 'MTI2MzY4OmU1ZTFhODg2ZjdkODFjY2UwZjBkNWE3ZTk1ZjgxOTVi', '测试用户', 'test@example.com', '13900139000', 1);
