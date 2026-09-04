package com.mall.sharding;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.hint.HintManagerFactory;

/**
 * ShardingSphere Hint 分片强制路由工具
 *
 * 用途：
 * 当查询条件中没有分片键（如 user_id）时，
 * 通过 Hint 机制强制指定路由到某个分片，避免全路由（笛卡尔积）查询。
 *
 * 使用场景：
 * 1. 后台管理员查询所有订单（无 user_id）
 * 2. 订单导出功能
 * 3. 数据迁移脚本
 *
 * 
 * 使用 try-with-resources 确保 Hint 资源及时释放
 *
 * @author xiu
 * @date 2026/09/03
 */
@Slf4j
public class ShardingHintUtils {

    private ShardingHintUtils() {
    }

    /**
     * 强制路由到指定分片（库 + 表）
     *
     * @param dbIndex    库索引（0 或 1）
     * @param tableIndex 表索引（0 或 1）
     * @return HintManager（需在 try-with-resources 中使用）
     */
    public static HintManager forceRoute(int dbIndex, int tableIndex) {
        HintManager hintManager = HintManagerFactory.getInstance();
        hintManager.addDatabaseShardingValue("order_info", dbIndex);
        hintManager.addTableShardingValue("order_info", tableIndex);
        hintManager.addDatabaseShardingValue("order_item", dbIndex);
        hintManager.addTableShardingValue("order_item", tableIndex);
        log.debug("强制路由: db={}, table={}", dbIndex, tableIndex);
        return hintManager;
    }

    /**
     * 强制路由到指定分片（仅指定库，所有表）
     *
     * @param dbIndex 库索引
     * @return HintManager
     */
    public static HintManager forceDatabase(int dbIndex) {
        HintManager hintManager = HintManagerFactory.getInstance();
        hintManager.addDatabaseShardingValue("order_info", dbIndex);
        hintManager.addDatabaseShardingValue("order_item", dbIndex);
        log.debug("强制路由到库: db={}", dbIndex);
        return hintManager;
    }

    /**
     * 强制路由到用户所在的分片
     *
     * @param userId 用户ID
     * @return HintManager
     */
    public static HintManager forceRouteByUserId(Long userId) {
        int dbIndex = ShardingKeyUtils.getDatabaseIndex(userId);
        int tableIndex = ShardingKeyUtils.getTableIndex(userId);
        return forceRoute(dbIndex, tableIndex);
    }

    /**
     * 强制只读模式（主库或从库）
     *
     * @param writeOnly true=主库，false=从库
     * @return HintManager
     */
    public static HintManager readOnly(boolean writeOnly) {
        HintManager hintManager = HintManagerFactory.getInstance();
        hintManager.setWriteRouteOnly(writeOnly);
        return hintManager;
    }
}
