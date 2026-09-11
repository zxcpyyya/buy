# Apple Store - 商城前端应用

一个精美的苹果风格移动端商城应用。

## 技术栈

- Vue 3 + Composition API
- Vite
- Vue Router 4
- Pinia
- Vant 4
- SCSS

## 启动项目

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

## 项目结构

```
store-app/
├── src/
│   ├── components/     # 公共组件
│   ├── views/         # 页面组件
│   ├── router/        # 路由配置
│   ├── stores/        # Pinia 状态管理
│   ├── utils/         # 工具函数
│   └── styles/        # 全局样式
├── public/            # 静态资源
└── vite.config.js    # Vite 配置
```

## 页面列表

- 首页 (Home)
- 分类 (Category)
- 购物车 (Cart)
- 个人中心 (Profile)
- 商品详情 (ProductDetail)
- 订单确认 (Checkout)
- 订单列表 (Orders)
- 订单详情 (OrderDetail)
- 收货地址 (Addresses)
- 优惠券 (Coupons)
- 积分中心 (Points)
- 物流追踪 (ExpressTrack)
