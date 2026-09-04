package com.mall.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器
 *
 * 重要：在 ShardingSphere 环境下，需要确保：
 * 1. fill 字段在 INSERT 时被正确填充
 * 2. fill 字段在 UPDATE 时被正确刷新
 *
 * 注意：这里的 LocalDateTime.now() 必须由应用生成，
 * 不能依赖数据库的 CURRENT_TIMESTAMP（避免分片表时间不一致）
 *
 * @author xiu
 * @date 2026/09/03
 */
@Slf4j
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始填充插入字段");
        LocalDateTime now = LocalDateTime.now();
        // 创建时间和更新时间都填充为当前时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始填充更新字段");
        // 只更新 updateTime
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
