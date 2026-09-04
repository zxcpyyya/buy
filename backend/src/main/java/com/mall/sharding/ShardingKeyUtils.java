package com.mall.sharding;

import lombok.extern.slf4j.Slf4j;

/**
 * 分库分表工具类
 *
 * 用于：
 * 1. 计算用户所在的分片库
 * 2. 计算用户所在的分片表
 * 3. 兼容单库模式和分片模式
 *
 * 分片规则：
 * - ds = user_id % 2
 * - table = user_id % 2
 *
 * @author xiu
 * @date 2024/01/01
 */
@Slf4j
public class ShardingKeyUtils {

    /**
     * 数据库分片数
     */
    public static final int DB_COUNT = 2;

    /**
     * 表分片数（与库数相同，方便订单项与订单一起分片）
     */
    public static final int TABLE_COUNT = 2;

    /**
     * 私有化构造器
     */
    private ShardingKeyUtils() {
    }

    /**
     * 计算用户所在的分片库
     *
     * @param userId 用户ID
     * @return 库索引 (0 或 1)
     */
    public static int getDatabaseIndex(Long userId) {
        if (userId == null) {
            return 0;
        }
        return (int) (Math.abs(userId) % DB_COUNT);
    }

    /**
     * 计算用户所在的分片表
     *
     * @param userId 用户ID
     * @return 表索引 (0 或 1)
     */
    public static int getTableIndex(Long userId) {
        if (userId == null) {
            return 0;
        }
        return (int) (Math.abs(userId) % TABLE_COUNT);
    }

    /**
     * 获取物理库名
     *
     * @param userId 用户ID
     * @return 物理库名
     */
    public static String getPhysicalDatabase(Long userId) {
        return "ds" + getDatabaseIndex(userId);
    }

    /**
     * 获取订单物理表名
     *
     * @param userId 用户ID
     * @return 物理表名
     */
    public static String getOrderPhysicalTable(Long userId) {
        return "order_info_" + getTableIndex(userId);
    }

    /**
     * 获取订单项物理表名
     *
     * @param userId 用户ID
     * @return 物理表名
     */
    public static String getOrderItemPhysicalTable(Long userId) {
        return "order_item_" + getTableIndex(userId);
    }

    /**
     * 调试：打印分片位置
     *
     * @param userId 用户ID
     */
    public static void logShardingLocation(Long userId) {
        log.debug("用户分片位置: userId={}, dbIndex={}, tableIndex={}, db={}, table={}",
                userId, getDatabaseIndex(userId), getTableIndex(userId),
                getPhysicalDatabase(userId), getOrderPhysicalTable(userId));
    }
}
