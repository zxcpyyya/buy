-- =============================================
-- ShardingSphere 分库分表 - 物理库表初始化脚本
--
-- 架构说明：
-- - 2个分片库：mall_ds0, mall_ds1
-- - 每个库都包含完整的广播表（sys_user, product_category, product, cart, cart_item, address）
-- - 每个库包含2张订单表：order_info_0/1, order_item_0/1
--
-- 重要：广播表（每个库都存）通过 ShardingSphere BROADCAST 规则自动同步
--        如果不启用 BROADCAST，需要在两个库都执行本脚本
--
-- 执行顺序：
-- 1. 在 mall_ds0 上执行（建库、建表、初始化数据）
-- 2. 在 mall_ds1 上执行（建库、建表，广播表数据由 ShardingSphere 自动同步）
-- 3. 启用 BROADCAST 后，每个库的广播表数据会自动一致
--
-- author: mall
-- date: 2024/01/01
-- =============================================

-- =============================================
-- 第1步：创建分片库
-- =============================================
CREATE DATABASE IF NOT EXISTS mall_ds0 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mall_ds1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- =============================================
-- 第2步：在两个库上分别创建表结构
-- 使用参数化方式：手动切换 USE 语句执行
-- =============================================

-- =============================================
-- mall_ds0 库表结构
-- =============================================
USE mall_ds0;

-- 用户表（广播表）
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL COMMENT '用户ID（雪花算法生成）',
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

-- 商品分类表（广播表）
DROP TABLE IF EXISTS product_category;
CREATE TABLE product_category (
    id BIGINT NOT NULL COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
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

-- 商品表（广播表）
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT NOT NULL COMMENT '商品ID',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
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
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 购物车表（广播表）
DROP TABLE IF EXISTS cart;
CREATE TABLE cart (
    id BIGINT NOT NULL COMMENT '购物车ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_price DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '总价',
    total_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '商品总数量',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- 购物车商品项表（广播表）
DROP TABLE IF EXISTS cart_item;
CREATE TABLE cart_item (
    id BIGINT NOT NULL COMMENT '购物车商品ID',
    cart_id BIGINT NOT NULL COMMENT '购物车ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
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

-- 地址表（广播表）
DROP TABLE IF EXISTS address;
CREATE TABLE address (
    id BIGINT NOT NULL COMMENT '地址ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
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

-- ==================== 订单表（分表） ====================
-- 物理表 order_info_0（ds0）：user_id 为偶数的订单
DROP TABLE IF EXISTS order_info_0;
CREATE TABLE order_info_0 (
    id BIGINT NOT NULL COMMENT '订单ID（雪花算法）',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID（分片键）',
    total_price DECIMAL(12, 2) NOT NULL COMMENT '订单总价',
    pay_price DECIMAL(12, 2) NOT NULL COMMENT '实付金额',
    pay_type TINYINT UNSIGNED DEFAULT NULL COMMENT '支付方式：1-微信，2-支付宝',
    order_status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单状态',
    delivery_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '配送状态',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
    receiver_address VARCHAR(500) NOT NULL COMMENT '收货地址',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    delivery_time DATETIME DEFAULT NULL COMMENT '发货时间',
    receive_time DATETIME DEFAULT NULL COMMENT '收货时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_order_status (order_status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表0（user_id偶数）';

-- 物理表 order_info_1（ds0）：user_id 为奇数的订单
DROP TABLE IF EXISTS order_info_1;
CREATE TABLE order_info_1 (
    -- 同 order_info_0 结构
    id BIGINT NOT NULL COMMENT '订单ID（雪花算法）',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID（分片键）',
    total_price DECIMAL(12, 2) NOT NULL COMMENT '订单总价',
    pay_price DECIMAL(12, 2) NOT NULL COMMENT '实付金额',
    pay_type TINYINT UNSIGNED DEFAULT NULL COMMENT '支付方式：1-微信，2-支付宝',
    order_status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单状态',
    delivery_status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '配送状态',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
    receiver_address VARCHAR(500) NOT NULL COMMENT '收货地址',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    delivery_time DATETIME DEFAULT NULL COMMENT '发货时间',
    receive_time DATETIME DEFAULT NULL COMMENT '收货时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_order_status (order_status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表1（user_id奇数）';

-- 物理表 order_item_0（ds0）：跟随 order_info_0
DROP TABLE IF EXISTS order_item_0;
CREATE TABLE order_item_0 (
    id BIGINT NOT NULL COMMENT '订单项ID（雪花算法）',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID（分片键）',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称（冗余）',
    product_image VARCHAR(500) DEFAULT NULL COMMENT '商品图片（冗余）',
    price DECIMAL(10, 2) NOT NULL COMMENT '商品单价',
    quantity INT UNSIGNED NOT NULL COMMENT '购买数量',
    total_price DECIMAL(12, 2) NOT NULL COMMENT '小计',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_user_id (user_id),
    KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品项表0';

-- 物理表 order_item_1（ds0）：跟随 order_info_1
DROP TABLE IF EXISTS order_item_1;
CREATE TABLE order_item_1 (
    id BIGINT NOT NULL COMMENT '订单项ID（雪花算法）',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID（分片键）',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称（冗余）',
    product_image VARCHAR(500) DEFAULT NULL COMMENT '商品图片（冗余）',
    price DECIMAL(10, 2) NOT NULL COMMENT '商品单价',
    quantity INT UNSIGNED NOT NULL COMMENT '购买数量',
    total_price DECIMAL(12, 2) NOT NULL COMMENT '小计',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_user_id (user_id),
    KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品项表1';

-- =============================================
-- mall_ds1 库表结构（与 ds0 完全一致）
-- =============================================
USE mall_ds1;

-- 用户表
CREATE TABLE sys_user LIKE mall_ds0.sys_user;
-- 商品分类表
CREATE TABLE product_category LIKE mall_ds0.product_category;
-- 商品表
CREATE TABLE product LIKE mall_ds0.product;
-- 购物车表
CREATE TABLE cart LIKE mall_ds0.cart;
-- 购物车商品项表
CREATE TABLE cart_item LIKE mall_ds0.cart_item;
-- 地址表
CREATE TABLE address LIKE mall_ds0.address;
-- 订单表
CREATE TABLE order_info_0 LIKE mall_ds0.order_info_0;
CREATE TABLE order_info_1 LIKE mall_ds0.order_info_1;
-- 订单项表
CREATE TABLE order_item_0 LIKE mall_ds0.order_item_0;
CREATE TABLE order_item_1 LIKE mall_ds0.order_item_1;

-- =============================================
-- 第3步：初始化数据（仅在 ds0 执行，启用 BROADCAST 后会自动同步）
-- =============================================
USE mall_ds0;

-- 商品分类
INSERT INTO product_category (id, name, parent_id, level, icon, sort, status) VALUES
(1, '数码电子', 0, 1, 'icon-digital', 1, 1),
(2, '服装鞋包', 0, 1, 'icon-clothing', 2, 1),
(3, '食品生鲜', 0, 1, 'icon-food', 3, 1),
(4, '家居百货', 0, 1, 'icon-home', 4, 1),
(5, '图书音像', 0, 1, 'icon-book', 5, 1),
(6, '手机通讯', 1, 2, 'icon-phone', 1, 1),
(7, '电脑办公', 1, 2, 'icon-computer', 2, 1),
(8, '智能设备', 1, 2, 'icon-smart', 3, 1);

-- 商品
INSERT INTO product (id, name, category_id, price, stock, image, images, description, sales, status) VALUES
(1001, 'iPhone 15 Pro Max 256GB', 6, 9999.00, 100, 'https://picsum.photos/400/400?random=1', '["https://picsum.photos/400/400?random=1"]', '苹果旗舰手机，A17 Pro芯片', 520, 1),
(1002, '小米14 Ultra 影像旗舰', 6, 6499.00, 200, 'https://picsum.photos/400/400?random=3', '["https://picsum.photos/400/400?random=3"]', '小米旗舰手机，徕卡影像', 380, 1),
(1003, '联想拯救者Y9000P游戏本', 7, 10999.00, 50, 'https://picsum.photos/400/400?random=5', '["https://picsum.photos/400/400?random=5"]', '16英寸游戏本，i9-14900HX', 150, 1),
(1004, 'Apple MacBook Pro 14寸', 7, 15999.00, 80, 'https://picsum.photos/400/400?random=7', '["https://picsum.photos/400/400?random=7"]', 'M3 Pro芯片，18G+512G', 220, 1),
(1005, '华为FreeBuds Pro 3耳机', 8, 1499.00, 500, 'https://picsum.photos/400/400?random=9', '["https://picsum.photos/400/400?random=9"]', '主动降噪耳机，麒麟A2芯片', 890, 1);

-- 测试用户
-- 密码加密格式：salt:hash，例如 123456 加密后
INSERT INTO sys_user (id, username, password, nickname, email, phone, status) VALUES
(1001, 'admin', 'MTI2MzY4OmU1ZTFhODg2ZjdkODFjY2UwZjBkNWE3ZTk1ZjgxOTVi', '管理员', 'admin@example.com', '13800138000', 1),
(1002, 'test',  'MTI2MzY4OmU1ZTFhODg2ZjdkODFjY2UwZjBkNWE3ZTk1ZjgxOTVi', '测试用户', 'test@example.com', '13900139000', 1);

-- 提示：
-- 1001 是奇数 → order_info_1 / order_item_1
-- 1002 是偶数 → order_info_0 / order_item_0

SELECT '===================== 初始化完成 =====================' AS message;
SELECT
    'mall_ds0' AS database_name,
    (SELECT COUNT(*) FROM mall_ds0.sys_user) AS user_count,
    (SELECT COUNT(*) FROM mall_ds0.product) AS product_count;

-- 验证广播表同步（启用 BROADCAST 后执行）
-- SELECT * FROM mall_ds1.sys_user;
-- SELECT * FROM mall_ds1.product;
