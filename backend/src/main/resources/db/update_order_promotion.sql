-- =============================================
-- 订单表增强：添加积分、优惠券相关字段
-- =============================================

-- 添加积分和优惠券字段
ALTER TABLE order_info
ADD COLUMN use_points INT UNSIGNED DEFAULT 0 COMMENT '使用积分数量' AFTER remark,
ADD COLUMN points_discount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '积分抵扣金额' AFTER use_points,
ADD COLUMN coupon_id BIGINT UNSIGNED DEFAULT NULL COMMENT '使用的优惠券ID' AFTER points_discount,
ADD COLUMN coupon_name VARCHAR(100) DEFAULT NULL COMMENT '优惠券名称' AFTER coupon_id,
ADD COLUMN coupon_discount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '优惠券抵扣金额' AFTER coupon_name,
ADD COLUMN got_points INT UNSIGNED DEFAULT 0 COMMENT '获得积分数量' AFTER coupon_discount;

-- 创建索引
ALTER TABLE order_info ADD INDEX idx_coupon_id (coupon_id);
