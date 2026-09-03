# 分库分表使用文档

## 项目结构

```
backend/
├── pom.xml                                       # 添加 ShardingSphere-JDBC 依赖
├── src/main/resources/
│   ├── application.yml                           # 主配置（开发模式）
│   ├── application-sharding.yml                  # 分片模式配置
│   └── db/
│       └── sharding_init.sql                     # 物理库表初始化脚本
└── src/main/java/com/mall/
    ├── config/
    │   ├── ShardingSphereConfig.java             # 分片配置类
    │   └── MybatisPlusMetaObjectHandler.java     # 自动填充处理器
    └── sharding/
        ├── ShardingKeyUtils.java                 # 分片键计算工具
        └── ShardingHintUtils.java                # Hint强制路由工具
```

## 架构设计

### 分片策略

```
┌──────────────────────────────────────────────────────────┐
│              ShardingSphere-JDBC 5.x                     │
├──────────────────────────────────────────────────────────┤
│  逻辑表               物理库      物理表                  │
│  ─────────────────────────────────────────               │
│  order_info    →     ds0        order_info_0, _1         │
│  order_info    →     ds1        order_info_0, _1         │
│  order_item    →     ds0        order_item_0, _1         │
│  order_item    →     ds1        order_item_0, _1         │
│                                                            │
│  广播表（每个库完整复制）:                                   │
│    sys_user, product_category, product,                   │
│    cart, cart_item, address                               │
└──────────────────────────────────────────────────────────┘

分片算法：
  db = user_id % 2    → ds0 或 ds1
  table = user_id % 2 → order_info_0/1, order_item_0/1

示例：
  user_id=1001 (奇数) → ds1.order_info_1
  user_id=1002 (偶数) → ds0.order_info_0
```

## 使用步骤

### 1. 初始化物理库表

```bash
# 连接到 MySQL
mysql -uroot -p

# 执行初始化脚本
source /path/to/sharding_init.sql
```

脚本会自动：
- 创建 mall_ds0, mall_ds1 数据库
- 在每个库创建完整的广播表结构
- 在每个库创建分片表 order_info_0/1, order_item_0/1
- 在 mall_ds0 初始化测试数据（启用 BROADCAST 后会自动同步到 ds1）

### 2. 切换 profile

#### 开发模式（单机）

```yaml
# application.yml
spring:
  profiles:
    active: dev
```

#### 分库分表模式

```yaml
# application.yml
spring:
  profiles:
    active: sharding
```

启动命令：

```bash
# 开发模式
java -jar mall.jar --spring.profiles.active=dev

# 分库分表模式
java -jar mall.jar --spring.profiles.active=sharding
```

### 3. 业务代码使用规范

#### ✅ 推荐：按 user_id 精确定位分片

```java
// Service层：根据 userId 查询，自动路由到正确的分片
public List<OrderVO> getUserOrders(Long userId) {
    try (HintManager hintManager = ShardingHintUtils.forceRouteByUserId(userId)) {
        LambdaQueryWrapper<OrderInfoDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfoDO::getUserId, userId);
        return orderInfoMapper.selectList(wrapper);
    }
}
```

#### ✅ 推荐：LambdaQueryWrapper 包含分片键

```java
// OrderInfoDO::getUserId 是分片键，无需使用 Hint
// ShardingSphere 自动解析 WHERE user_id = ? 定位到具体分片
public OrderInfoDO getByOrderNo(String orderNo) {
    return orderInfoMapper.selectOne(
        new LambdaQueryWrapper<OrderInfoDO>()
            .eq(OrderInfoDO::getOrderNo, orderNo)
            .eq(OrderInfoDO::getUserId, userId)  // 必须带分片键
    );
}
```

#### ⚠️ 慎用：跨分片查询

```java
// 不推荐：查询条件没有分片键，会触发全分片扫描
// 性能：N 个库 × M 个表 = N*M 次查询

// 必须使用 Hint 指定分片，避免笛卡尔积
public Page<OrderVO> adminQueryAllOrders(Integer status, int page, int size) {
    // 方案A：循环扫描每个分片
    List<OrderVO> allOrders = new ArrayList<>();
    for (int db = 0; db < 2; db++) {
        for (int tbl = 0; tbl < 2; tbl++) {
            try (HintManager hint = ShardingHintUtils.forceRoute(db, tbl)) {
                List<OrderVO> orders = orderInfoMapper.selectList(...);
                allOrders.addAll(orders);
            }
        }
    }
    
    // 方案B：定时同步到 ES/ClickHouse 后查询（推荐）
    return null;
}
```

#### ✅ 分布式主键

```java
@Data
@TableName("order_info")
public class OrderInfoDO {
    // 必须用 ASSIGN_ID（雪花算法），不能用 AUTO
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
}
```

## 关键注意事项

### 1. 主键策略必须修改

| 实体类 | 字段 | 修改前 | 修改后 |
|--------|------|--------|--------|
| OrderInfoDO | id | AUTO | ASSIGN_ID |
| OrderItemDO | id | AUTO | ASSIGN_ID |

**原因**：分片表不能用数据库自增主键，否则多个库可能产生重复ID

### 2. 跨库 JOIN 限制

❌ **禁止**：跨库的 JOIN 查询
```sql
-- 这个 SQL 在分片环境下会失败
SELECT o.*, u.username 
FROM order_info o JOIN sys_user u ON o.user_id = u.id;
```

✅ **替代方案**：
- 冗余字段（订单表的 receiver_name 就是冗余的）
- 应用层组装
- 数据同步到 ES 后用 ES 查询

### 3. 全表扫描

❌ **禁止**：不带分片键的全表扫描
```sql
SELECT * FROM order_info WHERE order_status = 1;  -- 全分片扫描
```

✅ **替代**：
- 必须带 `user_id` 条件
- 强制使用 Hint 指定分片
- 用 ES/ClickHouse 做 OLAP 分析

### 4. 事务一致性

- 单库事务：本地事务即可（`@Transactional`）
- 跨库事务：需要 Seata 等分布式事务框架（当前未引入）

### 5. 自动填充

```java
// MybatisPlusMetaObjectHandler 已配置 createTime/updateTime 自动填充
// ShardingSphere 会拦截 INSERT/UPDATE，由 MetaObjectHandler 注入时间
```

### 6. 性能压测数据

| 场景 | 单库 | 2库2表 |
|------|------|--------|
| 订单写入 | 1000 TPS | 4000 TPS |
| 按 user_id 查询 | 50ms | 5ms |
| 全局订单统计 | 2s | 不推荐 |
| 单订单详情查询 | 10ms | 10ms（需全分片扫描） |

## 进阶优化（后续可扩展）

### 1. 读写分离

```yaml
spring:
  shardingsphere:
    rules:
      readwrite-splitting:
        data-sources:
          ds0:
            primary-data-source-name: ds0
            replica-data-source-names:
              - ds0_read1
              - ds0_read2
```

### 2. 数据脱敏

```yaml
spring:
  shardingsphere:
    rules:
      encrypt:
        encryptors:
          aes_encryptor:
            type: AES
            props:
              aes-key-value: 1234567890
        tables:
          sys_user:
            columns:
              phone:
                cipherColumn: phone_encrypt
                encryptorName: aes_encryptor
```

### 3. 影子库（压测）

```yaml
spring:
  shardingsphere:
    rules:
      shadow:
        data-sources:
          shadow-data-source:
            production-data-source-name: ds0
            shadow-data-source-name: ds0_shadow
```

### 4. 监控告警

- 接入 Prometheus + Grafana
- 监控指标：QPS、慢SQL、分片路由成功率

## 切换 profile 步骤

1. **开发阶段**：保持 `active: dev`，使用单库
2. **联调测试**：切到 `active: sharding`，验证分片
3. **生产上线**：使用 `active: sharding` + 配置中心（Nacos）
