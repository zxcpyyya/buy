# 分库分表架构设计文档

> 本文档说明当前项目的分库分表架构设计、技术选型和使用规范。

## 一、架构概览

### 1.1 拓扑结构

```
应用层
   │
   ▼
ShardingSphere-JDBC 5.x（嵌入式客户端）
   │
   ├── ds0 ──→ MySQL: mall_ds0
   │              ├── order_info_0（user_id 偶数）
   │              ├── order_info_1（user_id 奇数）
   │              ├── order_item_0
   │              ├── order_item_1
   │              └── 广播表（sys_user, product_category, product, cart, cart_item, address）
   │
   └── ds1 ──→ MySQL: mall_ds1
                  ├── order_info_0（user_id 偶数）
                  ├── order_info_1（user_id 奇数）
                  ├── order_item_0
                  ├── order_item_1
                  └── 广播表（sys_user, product_category, product, cart, cart_item, address）
```

### 1.2 分片规则

| 逻辑表 | 物理库数量 | 物理表数量 | 分片键 | 算法 |
|--------|-----------|-----------|--------|------|
| `order_info` | 2 | 2 | user_id | hash(user_id) % 2 |
| `order_item` | 2 | 2 | user_id | 与 order_info 一致 |
| 广播表 | 2 | 1 | 无 | 全复制 |
| 其他表 | 2 | 1 | 无 | 单库单表 |

## 二、技术选型理由

### 为什么选择 ShardingSphere-JDBC 5.x？

| 维度 | ShardingSphere-JDBC | MyCat | dynamic-datasource |
|------|---------------------|-------|---------------------|
| 部署方式 | 客户端嵌入 | 中间件 | 多数据源 |
| 性能损耗 | 低（< 5%） | 中（多一跳） | 低 |
| 分表支持 | ✅ 完善 | ✅ 完善 | ❌ |
| 生态 | Apache 顶级 | 社区 | 个人 |
| Spring Boot 集成 | ✅ 完美 | 一般 | ✅ |

## 三、配置切换

### 3.1 切换到分片模式

修改 `application.yml`：

```yaml
spring:
  profiles:
    active: sharding  # 原为 dev
```

### 3.2 保持单库模式（开发用）

```yaml
spring:
  profiles:
    active: dev
```

## 四、关键规范

### 4.1 实体类规范

```java
// ✅ 正确：使用逻辑表名 + 雪花主键
@TableName("order_info")
public class OrderInfoDO {
    @TableId(type = IdType.ASSIGN_ID)  // 雪花算法
    private Long id;
    
    @TableField("user_id")  // 分片键
    private Long userId;
}

// ❌ 错误：使用数据库自增（雪花场景下不适用）
@TableId(type = IdType.AUTO)
private Long id;
```

### 4.2 查询规范

#### ✅ 场景1：带 user_id 查询（推荐，性能最优）

```java
// 自动走单分片路由
LambdaQueryWrapper<OrderInfoDO> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(OrderInfoDO::getUserId, userId)
       .eq(OrderInfoDO::getOrderStatus, 1);
orderInfoMapper.selectList(wrapper);
```

#### ⚠️ 场景2：管理后台无 user_id 全量查询（需要 Hint 强制路由）

```java
try (HintManager hint = ShardingHintUtils.forceRoute(0, 0)) {
    orderInfoMapper.selectList(null);
}
// 循环每个分片路由
for (int i = 0; i < 2; i++) {
    try (HintManager hint = ShardingHintUtils.forceDatabase(i)) {
        // 查 ds0/ds1 的所有表
    }
}
```

#### ❌ 错误：跨分片 JOIN

```java
// ❌ 禁止：跨分片 JOIN 会产生笛卡尔积
SELECT o.*, u.username FROM order_info o 
LEFT JOIN sys_user u ON o.user_id = u.id;

// ✅ 替代：分两步查询
// 1. 查 order_info
// 2. 根据 user_id 批量查 sys_user
```

### 4.3 分页规范

ShardingSphere 5.x 支持分页合并排序（`UNION ALL` + 内存归并），但性能较差。

```java
// ✅ 安全：带 user_id 的分页
Page<OrderInfoDO> page = new Page<>(1, 10);
wrapper.eq(OrderInfoDO::getUserId, userId);
orderInfoMapper.selectPage(page, wrapper);

// ⚠️ 慎用：不带分片键的分页（性能差）
Page<OrderInfoDO> page = new Page<>(1, 10);
orderInfoMapper.selectPage(page, null); // 全路由 + 内存归并
```

### 4.4 事务规范

```yaml
# application-sharding.yml 已配置 LOCAL 事务
spring.shardingsphere.props.transaction-type: LOCAL
```

```java
// ✅ 正确：单分片内事务
@Transactional(rollbackFor = Exception.class)
public void createOrder() { ... }

// ⚠️ 慎用：跨分片事务（需要 XA 事务，性能差）
// 当前配置为 LOCAL（不支持跨分片事务）
// 如需支持：改为 XA，并引入 atomikos 依赖
```

## 五、扩容方案

### 5.1 从 2 库 2 表扩容到 4 库 4 表

**步骤**：

1. 新增 ds2, ds3 物理库
2. 修改 `application-sharding.yml`：

```yaml
# 数据库扩容：actualDataNodes 增加 ds2, ds3
order_info:
  actualDataNodes: ds$->{0..3}.order_info_$->{0..3}

shardingAlgorithms:
  database_mod_order:
    type: INLINE
    props:
      algorithm-expression: ds${user_id % 4}  # 改为 % 4
  table_mod_order:
    type: INLINE
    props:
      algorithm-expression: order_info_${user_id % 4}
```

3. 数据迁移脚本（双写 + 历史数据迁移）

> ⚠️ 扩容涉及数据迁移，建议业务低峰期执行。

## 六、常见问题

### Q1: 启动报错 `Cannot find data source ds0`？

检查 `application-sharding.yml` 是否包含 `ds0`、`ds1` 数据源配置。

### Q2: 报错 `Table 'mall_ds0.order_info' doesn't exist`？

执行 `sharding_init.sql` 创建物理表。

### Q3: 主键冲突？

确保所有分片表都使用 `@TableId(type = IdType.ASSIGN_ID)`（雪花算法）。

### Q4: 广播表数据不一致？

检查 `spring.shardingsphere.rules.broadcast-tables` 配置是否包含该表。

### Q5: 如何调试 SQL 路由？

设置 `sql-show: true` 即可在控制台看到实际执行的 SQL：

```
Actual SQL: ds0 ::: SELECT * FROM order_info_1 WHERE user_id = ?
```

## 七、监控建议

### 7.1 慢 SQL 监控

ShardingSphere 默认不输出慢 SQL，建议自定义过滤器：

```java
// 待补充：SQL 监控埋点
```

### 7.2 分片均匀度监控

通过运维平台定期检查每个物理表的数据量，确保分布均匀。

## 八、参考文档

- [ShardingSphere 5.4 官方文档](https://shardingsphere.apache.org/document/5.4.0/cn/overview/)
- [ShardingSphere 5.4 概念 - 分片](https://shardingsphere.apache.org/document/5.4.0/cn/features/sharding/)
- [Apache ShardingSphere GitHub](https://github.com/apache/shardingsphere)
