-- =============================================
-- 后台管理系统 RBAC 权限数据库
-- =============================================

USE mall;

-- =============================================
-- 1. 权限表 (sys_permission)
-- =============================================
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父权限ID',
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    code VARCHAR(100) NOT NULL COMMENT '权限标识',
    permission_type TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '类型：1-菜单，2-按钮/操作',
    path VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
    icon VARCHAR(50) DEFAULT NULL COMMENT '图标',
    sort INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    KEY idx_parent_id (parent_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- =============================================
-- 2. 角色表 (sys_role)
-- =============================================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL COMMENT '角色标识',
    description VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    role_type TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '角色类型：1-后台管理员，2-商家',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- =============================================
-- 3. 用户角色关联表 (sys_user_role)
-- =============================================
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =============================================
-- 4. 角色权限关联表 (sys_role_permission)
-- =============================================
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    permission_id BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- =============================================
-- 5. 后台用户表 (sys_admin)
-- =============================================
DROP TABLE IF EXISTS sys_admin;
CREATE TABLE sys_admin (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台管理员表';

-- =============================================
-- 6. 操作日志表 (sys_operation_log)
-- =============================================
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型',
    module VARCHAR(50) DEFAULT NULL COMMENT '模块',
    description VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
    request_method VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
    request_url VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    ip_address VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    execute_time INT UNSIGNED DEFAULT NULL COMMENT '执行时间(ms)',
    status TINYINT UNSIGNED DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =============================================
-- 7. 商家表 (sys_merchant)
-- =============================================
DROP TABLE IF EXISTS sys_merchant;
CREATE TABLE sys_merchant (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商家ID',
    merchant_name VARCHAR(100) NOT NULL COMMENT '商家名称',
    merchant_code VARCHAR(50) NOT NULL COMMENT '商家编码',
    contact_name VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    business_license VARCHAR(255) DEFAULT NULL COMMENT '营业执照',
    address VARCHAR(255) DEFAULT NULL COMMENT '地址',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用，2-待审核',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (merchant_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入超级管理员角色（拥有所有权限）
INSERT INTO sys_role (name, code, description, role_type, status) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有系统所有权限', 1, 1),
('运营管理员', 'OPERATION_ADMIN', '负责日常运营管理', 1, 1),
('商品管理员', 'PRODUCT_ADMIN', '负责商品管理', 1, 1),
('商家', 'MERCHANT', '商家端用户', 2, 1),
('客服', 'CUSTOMER_SERVICE', '客服人员', 1, 1);

-- 插入超级管理员用户（密码: admin123，使用BCrypt加密）
INSERT INTO sys_admin (username, password, nickname, email, phone, status) VALUES
('superadmin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '超级管理员', 'admin@example.com', '13800000000', 1),
('operator', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '运营人员', 'operator@example.com', '13800000001', 1),
('merchant001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '测试商家', 'merchant@example.com', '13800000002', 1);

-- 给超级管理员分配超级管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1);

-- 给运营管理员分配运营角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(2, 2);

-- 给商家分配商家角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(3, 4);

-- 插入权限数据（树形结构）
INSERT INTO sys_permission (id, parent_id, name, code, permission_type, path, sort, status) VALUES
-- 一级菜单：系统管理
(1, 0, '系统管理', 'system', 1, '/system', 1, 1),
-- 系统管理子菜单
(101, 1, '管理员管理', 'system:admin', 1, '/system/admin', 1, 1),
(102, 1, '角色管理', 'system:role', 1, '/system/role', 2, 1),
(103, 1, '权限管理', 'system:permission', 1, '/system:permission', 3, 1),
(104, 1, '操作日志', 'system:log', 1, '/system/log', 4, 1),

-- 一级菜单：商品管理
(2, 0, '商品管理', 'product', 1, '/product', 2, 1),
-- 商品管理子菜单
(201, 2, '商品列表', 'product:list', 1, '/product/list', 1, 1),
(202, 2, '商品分类', 'product:category', 1, '/product/category', 2, 1),
(203, 2, '添加商品', 'product:add', 2, NULL, 3, 1),
(204, 2, '编辑商品', 'product:edit', 2, NULL, 4, 1),
(205, 2, '删除商品', 'product:delete', 2, NULL, 5, 1),
(206, 2, '上下架商品', 'product:publish', 2, NULL, 6, 1),

-- 一级菜单：订单管理
(3, 0, '订单管理', 'order', 1, '/order', 3, 1),
-- 订单管理子菜单
(301, 3, '订单列表', 'order:list', 1, '/order/list', 1, 1),
(302, 3, '订单详情', 'order:detail', 2, NULL, 2, 1),
(303, 3, '发货', 'order:ship', 2, NULL, 3, 1),
(304, 3, '取消订单', 'order:cancel', 2, NULL, 4, 1),

-- 一级菜单：用户管理
(4, 0, '用户管理', 'user', 1, '/user', 4, 1),
-- 用户管理子菜单
(401, 4, '用户列表', 'user:list', 1, '/user/list', 1, 1),
(402, 4, '用户详情', 'user:detail', 2, NULL, 2, 1),
(403, 4, '禁用用户', 'user:disable', 2, NULL, 3, 1),

-- 一级菜单：营销管理
(5, 0, '营销管理', 'marketing', 1, '/marketing', 5, 1),
-- 营销管理子菜单
(501, 5, '优惠券管理', 'marketing:coupon', 1, '/marketing/coupon', 1, 1),
(502, 5, '创建优惠券', 'marketing:coupon:add', 2, NULL, 2, 1),
(503, 5, '积分规则', 'marketing:points', 1, '/marketing/points', 3, 1),

-- 一级菜单：商家管理
(6, 0, '商家管理', 'merchant', 1, '/merchant', 6, 1),
-- 商家管理子菜单
(601, 6, '商家列表', 'merchant:list', 1, '/merchant/list', 1, 1),
(602, 6, '商家审核', 'merchant:audit', 1, '/merchant/audit', 2, 1),

-- 一级菜单：数据统计
(7, 0, '数据统计', 'statistics', 1, '/statistics', 7, 1),
-- 数据统计子菜单
(701, 7, '销售统计', 'statistics:sales', 1, '/statistics/sales', 1, 1),
(702, 7, '用户统计', 'statistics:user', 1, '/statistics/user', 2, 1);

-- 给超级管理员角色分配所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE status = 1;

-- 给运营管理员分配运营相关权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 3), (2, 4), (2, 5),  -- 系统管理
(2, 201), (2, 202), (2, 203), (2, 204), (2, 205), (2, 206),  -- 商品管理
(2, 301), (2, 302), (2, 303), (2, 304),  -- 订单管理
(2, 401), (2, 402), (2, 403),  -- 用户管理
(2, 501), (2, 502), (2, 503),  -- 营销管理
(2, 701), (2, 702);  -- 数据统计

-- 给商品管理员分配商品相关权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(3, 201), (3, 202), (3, 203), (3, 204), (3, 205), (3, 206),  -- 商品管理
(3, 301), (3, 302), (3, 303), (3, 304);  -- 订单管理（查看和发货）

-- 给商家角色分配商家相关权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(4, 201), (4, 202), (4, 203), (4, 204), (4, 205), (4, 206),  -- 商品管理（商家自己的）
(4, 301), (4, 302), (4, 303);  -- 订单管理（查看和发货）

-- 给客服分配客服相关权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(5, 301), (5, 302), (5, 304),  -- 订单管理（查看和取消）
(5, 401), (5, 402);  -- 用户管理（查看）

-- 初始化商家数据
INSERT INTO sys_merchant (merchant_name, merchant_code, contact_name, contact_phone, status) VALUES
('测试商家001', 'MERCHANT001', '张经理', '13900000001', 1),
('测试商家002', 'MERCHANT002', '李经理', '13900000002', 1);
