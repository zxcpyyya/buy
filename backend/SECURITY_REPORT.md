# 越权漏洞检查与修复报告

**检查时间**: 2026-09-11
**检查人**: AI Assistant

---

## 一、漏洞总览

| 漏洞编号 | 严重程度 | 模块 | 描述 | 状态 |
|----------|----------|------|------|------|
| VULN-001 | 🔴 高危 | 商品管理 | 管理员接口缺少权限校验 | ✅ 已修复 |
| VULN-002 | 🔴 高危 | 优惠券管理 | 管理员接口缺少权限校验 | ✅ 已修复 |
| VULN-003 | 🔴 高危 | 物流管理 | 管理员接口缺少权限校验 | ✅ 已修复 |

---

## 二、已修复漏洞详情

### VULN-001: 商品管理接口越权

**漏洞描述**:
商品创建、更新、删除接口（`POST /api/product`、`PUT /api/product/{id}`、`DELETE /api/product/{id}`）缺少权限注解，任何人都可以调用这些接口进行商品管理操作。

**修复方案**:
为所有管理员接口添加 `@RequireLogin` 和 `@RequirePermission` 注解：

```java
@PostMapping
@RequireLogin
@RequirePermission("product:create")
public Result<Long> createProduct(@RequestBody ProductDTO productDTO) { ... }

@PutMapping("/{id}")
@RequireLogin
@RequirePermission("product:update")
public Result<Boolean> updateProduct(...) { ... }

@DeleteMapping("/{id}")
@RequireLogin
@RequirePermission("product:delete")
public Result<Boolean> deleteProduct(...) { ... }
```

**修复文件**: `ProductController.java`

---

### VULN-002: 优惠券管理接口越权

**漏洞描述**:
优惠券模板创建、更新接口（`POST /api/coupon/template`、`PUT /api/coupon/template/{id}`）缺少权限校验，普通用户也可以创建和更新优惠券模板。

**修复方案**:
1. 为管理员接口添加权限注解
2. 新增删除和列表查询接口
3. 添加Service层方法实现

```java
@PostMapping("/template")
@RequireLogin
@RequirePermission("coupon:create")
public Result<Long> createTemplate(...) { ... }

@PutMapping("/template/{id}")
@RequireLogin
@RequirePermission("coupon:update")
public Result<Boolean> updateTemplate(...) { ... }

@DeleteMapping("/template/{id}")
@RequireLogin
@RequirePermission("coupon:delete")
public Result<Boolean> deleteTemplate(...) { ... }

@GetMapping("/template/list")
@RequireLogin
@RequirePermission("coupon:list")
public Result<PageResult<CouponVO>> getTemplateList(...) { ... }
```

**修复文件**:
- `CouponController.java`
- `CouponService.java`
- `CouponServiceImpl.java`

---

### VULN-003: 物流管理接口越权

**漏洞描述**:
物流创建、发货、更新状态接口（`POST /api/express`、`POST /api/express/ship/{orderId}`、`PUT /api/express/{expressId}/status`）缺少权限校验，任何登录用户都可以发货或修改物流状态。

**修复方案**:
为所有管理员接口添加权限注解：

```java
@PostMapping
@RequireLogin
@RequirePermission("express:create")
public Result<Long> createExpress(...) { ... }

@PostMapping("/ship/{orderId}")
@RequireLogin
@RequirePermission("express:ship")
public Result<Boolean> shipOrder(...) { ... }

@PutMapping("/{expressId}/status")
@RequireLogin
@RequirePermission("express:update")
public Result<Boolean> updateStatus(...) { ... }
```

**修复文件**: `ExpressController.java`

---

## 三、已正确实现的权限控制 ✅

以下模块已正确实现水平/垂直权限控制，不存在越权漏洞：

### 1. 收货地址模块
```java
// AddressServiceImpl.java
public AddressVO getAddressDetail(Long addressId, Long userId) {
    AddressDO addressDO = addressMapper.selectById(addressId);
    if (!addressDO.getUserId().equals(userId)) {
        throw BusinessException.of("A0301", "无权限访问该地址");
    }
    return convertToVO(addressDO);
}
```

### 2. 订单模块
```java
// OrderServiceImpl.java
public OrderVO getOrderDetail(Long orderId, Long userId) {
    OrderInfoDO order = getOrderById(orderId, userId);
    if (!order.getUserId().equals(userId)) {
        throw BusinessException.of("A0301", "无权限访问该订单");
    }
    return convertToVO(order);
}
```

### 3. 购物车模块
```java
// CartServiceImpl.java
public Boolean updateQuantity(Long cartItemId, Integer quantity, Long userId) {
    CartItemDO item = cartItemMapper.selectById(cartItemId);
    CartDO cart = cartMapper.selectById(item.getCartId());
    if (!cart.getUserId().equals(userId)) {
        throw BusinessException.of("A0301", "无权限操作该购物车");
    }
    ...
}
```

### 4. 优惠券模块（用户相关）
```java
// CouponServiceImpl.java
public Boolean useCoupon(Long couponId, Long orderId, Long userId) {
    UserCouponDO coupon = userCouponMapper.selectById(couponId);
    if (!coupon.getUserId().equals(userId)) {
        throw BusinessException.of("A0301", "无权限使用该优惠券");
    }
    ...
}
```

---

## 四、权限校验规范

### 1. 水平权限校验（用户数据隔离）
用户只能操作自己的数据，Service层必须校验：
```java
if (!entity.getUserId().equals(currentUserId)) {
    throw BusinessException.of("A0301", "无权限操作该数据");
}
```

### 2. 垂直权限校验（角色/权限控制）
管理员接口必须添加权限注解：
```java
@RequireLogin                    // 必须登录
@RequirePermission("xxx:xxx")    // 必须有对应权限
```

### 3. 注解使用规范

| 注解 | 用途 | 位置 |
|------|------|------|
| `@RequireLogin` | 要求登录 | Controller类或方法 |
| `@RequireRole` | 要求特定角色 | Controller类或方法 |
| `@RequirePermission` | 要求特定权限 | Controller类或方法 |

### 4. 推荐的权限码命名
```
模块:操作
示例:
- product:list, product:create, product:update, product:delete
- coupon:list, coupon:create, coupon:update, coupon:delete
- order:list, order:detail, order:cancel, order:confirm
- user:list, user:enable, user:disable
```

---

## 五、测试建议

### 1. 越权测试用例

**水平越权测试**:
```
1. 用户A登录，创建订单O1（订单ID=100）
2. 用户B登录
3. 用户B尝试访问 GET /api/order/100
4. 预期：返回403无权限错误
```

**垂直越权测试**:
```
1. 普通用户登录
2. 普通用户尝试访问 POST /api/product
3. 预期：返回403无权限错误
```

**未授权访问测试**:
```
1. 未登录状态
2. 尝试访问 POST /api/order
3. 预期：返回401请先登录错误
```

### 2. 安全测试脚本

```bash
# 水平越权测试
curl -X GET http://localhost:8080/api/order/100 \
  -H "Authorization: Bearer <USER_B_TOKEN>"

# 垂直越权测试
curl -X POST http://localhost:8080/api/product \
  -H "Authorization: Bearer <NORMAL_USER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Product","price":99.99}'
```

---

## 六、后续建议

1. **增加审计日志**: 对所有管理员操作记录详细日志
2. **敏感操作二次验证**: 对删除等敏感操作增加二次确认
3. **定期权限审计**: 定期检查权限配置是否正确
4. **API安全网关**: 考虑引入API安全网关进行统一防护
5. **参数化查询**: 确保所有SQL使用参数化查询防止注入

---

## 七、相关文件变更

### 新增文件
- 无

### 修改文件
| 文件 | 变更内容 |
|------|----------|
| `ProductController.java` | 添加 `@RequireLogin` 和 `@RequirePermission` 注解 |
| `CouponController.java` | 添加权限注解，新增删除和列表接口 |
| `CouponService.java` | 新增 `deleteTemplate` 和 `getTemplateList` 方法 |
| `CouponServiceImpl.java` | 实现新增的Service方法 |

---

**报告结束**
