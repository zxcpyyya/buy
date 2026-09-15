<template>
  <div class="order-detail-page safe-area-top">
    <header class="header">
      <button class="back-btn" @click="$router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </button>
      <h1 class="title">订单详情</h1>
      <div class="placeholder"></div>
    </header>

    <!-- 物流状态 -->
    <div class="status-card">
      <div class="status-icon">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="1" y="3" width="15" height="13"/>
          <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/>
          <circle cx="5.5" cy="18.5" r="2.5"/>
          <circle cx="18.5" cy="18.5" r="2.5"/>
        </svg>
      </div>
      <div class="status-info">
        <h3>{{ order.statusText }}</h3>
        <p>预计 {{ estimateDays }} 天后送达</p>
      </div>
    </div>

    <!-- 收货地址 -->
    <div class="section address-section">
      <div class="address-icon">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
          <circle cx="12" cy="10" r="3"/>
        </svg>
      </div>
      <div class="address-info">
        <div class="contact">{{ order.address?.contact }} {{ order.address?.phone }}</div>
        <div class="address">{{ order.address?.province }}{{ order.address?.city }}{{ order.address?.district }}{{ order.address?.detail }}</div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="section goods-section">
      <div class="shop-header">
        <img src="https://picsum.photos/32/32?random=shop" class="shop-icon" />
        <span class="shop-name">Store</span>
      </div>
      <div class="goods-list">
        <div class="goods-item" v-for="item in order.items" :key="item.id" @click="$router.push(`/product/${item.productId}`)">
          <img :src="item.image" class="goods-image" />
          <div class="goods-info">
            <h4>{{ item.name }}</h4>
            <p>{{ item.spec }}</p>
          </div>
          <div class="goods-price">
            <span>¥{{ formatPrice(item.price) }}</span>
            <span class="qty">x{{ item.quantity }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 订单信息 -->
    <div class="section info-section">
      <div class="info-row">
        <span class="label">订单编号</span>
        <span class="value">{{ order.orderNo }}</span>
      </div>
      <div class="info-row">
        <span class="label">下单时间</span>
        <span class="value">{{ order.createTime }}</span>
      </div>
      <div class="info-row">
        <span class="label">支付方式</span>
        <span class="value">{{ order.payType || '在线支付' }}</span>
      </div>
    </div>

    <!-- 价格明细 -->
    <div class="section price-section">
      <div class="price-row">
        <span>商品金额</span>
        <span>¥{{ formatPrice(goodsAmount) }}</span>
      </div>
      <div class="price-row">
        <span>运费</span>
        <span class="free">免运费</span>
      </div>
      <div class="price-row" v-if="order.couponDiscount">
        <span>优惠券</span>
        <span class="discount">-¥{{ formatPrice(order.couponDiscount) }}</span>
      </div>
      <div class="price-row total">
        <span>实付款</span>
        <span class="total-price">¥{{ formatPrice(order.payPrice) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const order = ref({
  orderNo: '20240904001',
  createTime: '2024-09-04 15:30:22',
  status: 'shipped',
  statusText: '配送中',
  payType: '微信支付',
  payPrice: 10898,
  couponDiscount: 100,
  address: {
    contact: '张三',
    phone: '138****8888',
    province: '广东省',
    city: '深圳市',
    district: '南山区',
    detail: '科技园南区A栋1201'
  },
  items: [
    { id: 1, productId: 1, name: 'iPhone 16 Pro 256GB', spec: '钛金属原色', price: 8999, quantity: 1, image: 'https://picsum.photos/100/100?random=1' },
    { id: 2, productId: 2, name: 'AirPods Pro 2', spec: 'USB-C充电盒', price: 1899, quantity: 1, image: 'https://picsum.photos/100/100?random=2' }
  ]
})

const estimateDays = computed(() => 2)
const goodsAmount = computed(() => order.value.items.reduce((sum, item) => sum + item.price * item.quantity, 0))

const formatPrice = (price) => Number(price || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
</script>

<style lang="scss" scoped>
.order-detail-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-bottom: 30px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--bg-primary);
}

.back-btn { width: 32px; height: 32px; border: none; background: transparent; cursor: pointer; }
.title { font-size: 18px; font-weight: 600; }
.placeholder { width: 32px; }

.status-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, var(--apple-blue) 0%, var(--apple-purple) 100%);
  color: white;
  margin: 16px;
  border-radius: var(--radius-xl);
}

.status-icon {
  width: 48px;
  height: 48px;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-info h3 { font-size: 18px; font-weight: 600; margin-bottom: 4px; }
.status-info p { font-size: 14px; opacity: 0.9; }

.section {
  background: var(--bg-primary);
  margin: 0 16px 12px;
  border-radius: var(--radius-xl);
}

.address-section {
  display: flex;
  gap: 12px;
  padding: 16px;
}

.address-icon {
  width: 32px;
  height: 32px;
  background: var(--bg-secondary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--apple-blue);
}

.address-info { flex: 1; }
.contact { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.address { font-size: 14px; color: var(--text-secondary); }

.goods-section { padding: 16px; }

.shop-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--bg-secondary);
}

.shop-icon { width: 24px; height: 24px; border-radius: 4px; }
.shop-name { font-size: 14px; font-weight: 600; }

.goods-item {
  display: flex;
  gap: 12px;
  padding: 8px 0;
}

.goods-image { width: 64px; height: 64px; border-radius: var(--radius-md); object-fit: cover; background: var(--bg-secondary); }
.goods-info { flex: 1; h4 { font-size: 14px; font-weight: 500; margin-bottom: 4px; } p { font-size: 12px; color: var(--text-tertiary); } }
.goods-price { text-align: right; span:first-child { font-size: 14px; font-weight: 600; display: block; } .qty { font-size: 12px; color: var(--text-tertiary); } }

.info-section { padding: 16px; }
.info-row { display: flex; justify-content: space-between; padding: 8px 0; .label { font-size: 14px; color: var(--text-secondary); } .value { font-size: 14px; } }

.price-section { padding: 16px; }
.price-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; color: var(--text-secondary); .free { color: var(--apple-green); } .discount { color: var(--apple-red); } &.total { padding-top: 12px; border-top: 1px solid var(--bg-secondary); font-weight: 600; color: var(--text-primary); .total-price { font-size: 20px; color: var(--apple-red); } } }
</style>
