<template>
  <div class="checkout-page safe-area-top">
    <!-- 顶部 -->
    <header class="header">
      <button class="back-btn" @click="$router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </button>
      <h1 class="title">确认订单</h1>
      <div class="placeholder"></div>
    </header>

    <!-- 收货地址 -->
    <section class="section address-section" @click="selectAddress">
      <div class="section-content" v-if="selectedAddress">
        <div class="address-info">
          <div class="address-detail">
            <span class="contact">{{ selectedAddress.contact }}</span>
            <span class="phone">{{ selectedAddress.phone }}</span>
          </div>
          <p class="address-text">{{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }}{{ selectedAddress.detail }}</p>
        </div>
        <svg class="arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </div>
      <div class="add-address" v-else>
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
          <circle cx="12" cy="10" r="3"/>
        </svg>
        <span>添加收货地址</span>
        <svg class="arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </div>
    </section>

    <!-- 商品清单 -->
    <section class="section">
      <div class="order-items">
        <div class="order-item" v-for="item in orderItems" :key="item.id">
          <img :src="item.image" class="item-image" />
          <div class="item-info">
            <h4>{{ item.name }}</h4>
            <p>{{ item.spec }}</p>
          </div>
          <div class="item-price">
            <span class="price">¥{{ formatPrice(item.price) }}</span>
            <span class="qty">x{{ item.quantity }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 配送方式 -->
    <section class="section">
      <div class="section-header">
        <h3>配送方式</h3>
        <span class="highlight">顺丰快递 · 预计2-3天送达</span>
      </div>
    </section>

    <!-- 优惠券 -->
    <section class="section" @click="showCoupon = true">
      <div class="section-row">
        <span class="label">优惠券</span>
        <div class="value">
          <span v-if="selectedCoupon" class="coupon-tag">{{ selectedCoupon.name }}</span>
          <span v-else class="gray">暂无可用</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>
      </div>
    </section>

    <!-- 积分抵扣 -->
    <section class="section">
      <div class="section-row">
        <span class="label">积分抵扣</span>
        <div class="value">
          <span class="gray">使用{{ availablePoints }}积分可抵¥{{ pointsMoney }}</span>
          <label class="switch">
            <input type="checkbox" v-model="usePoints" />
            <span class="slider"></span>
          </label>
        </div>
      </div>
    </section>

    <!-- 订单备注 -->
    <section class="section">
      <div class="section-row">
        <span class="label">订单备注</span>
        <input type="text" class="remark-input" v-model="remark" placeholder="选填，可备注特殊需求" />
      </div>
    </section>

    <!-- 价格明细 -->
    <section class="section price-section">
      <div class="price-row">
        <span>商品金额</span>
        <span>¥{{ formatPrice(goodsAmount) }}</span>
      </div>
      <div class="price-row">
        <span>运费</span>
        <span class="highlight">免运费</span>
      </div>
      <div class="price-row" v-if="couponDiscount > 0">
        <span>优惠券</span>
        <span class="discount">-¥{{ formatPrice(couponDiscount) }}</span>
      </div>
      <div class="price-row" v-if="pointsMoney > 0 && usePoints">
        <span>积分抵扣</span>
        <span class="discount">-¥{{ formatPrice(pointsMoney) }}</span>
      </div>
    </section>

    <!-- 底部提交栏 -->
    <div class="submit-bar safe-area-bottom">
      <div class="total-info">
        <span class="total-label">合计</span>
        <span class="total-price">¥{{ formatPrice(totalPrice) }}</span>
      </div>
      <button class="submit-btn" @click="submitOrder">提交订单</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'

const router = useRouter()

const selectedAddress = ref({
  contact: '张三',
  phone: '138****8888',
  province: '广东省',
  city: '深圳市',
  district: '南山区',
  detail: '科技园南区A栋1201'
})

const orderItems = ref([
  { id: 1, name: 'iPhone 16 Pro 256GB', spec: '钛金属原色', price: 8999, quantity: 1, image: 'https://picsum.photos/100/100?random=1' },
  { id: 2, name: 'AirPods Pro 2', spec: 'USB-C充电盒', price: 1899, quantity: 1, image: 'https://picsum.photos/100/100?random=2' }
])

const selectedCoupon = ref(null)
const usePoints = ref(false)
const remark = ref('')
const availablePoints = ref(1280)
const showCoupon = ref(false)

const goodsAmount = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const couponDiscount = computed(() => {
  return selectedCoupon.value ? 100 : 0
})

const pointsMoney = computed(() => {
  return Math.floor(availablePoints.value / 100)
})

const totalPrice = computed(() => {
  let total = goodsAmount.value
  if (couponDiscount.value > 0) {
    total -= couponDiscount.value
  }
  if (usePoints.value) {
    total -= pointsMoney.value
  }
  return Math.max(0, total)
})

const formatPrice = (price) => {
  return Number(price || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const selectAddress = () => {
  router.push('/addresses')
}

const submitOrder = () => {
  if (!selectedAddress.value) {
    showToast('请选择收货地址')
    return
  }
  showToast('订单提交成功')
  setTimeout(() => {
    router.replace('/orders')
  }, 1000)
}

onMounted(() => {
  // 从localStorage获取选中的商品
  try {
    const items = JSON.parse(localStorage.getItem('checkout_items') || '[]')
    if (items.length > 0) {
      orderItems.value = items
    }
  } catch (e) {
    console.error(e)
  }
})
</script>

<style lang="scss" scoped>
.checkout-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-bottom: 80px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--bg-primary);
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.placeholder {
  width: 32px;
}

.section {
  background: var(--bg-primary);
  margin-bottom: 12px;
}

.address-section {
  padding: 20px 16px;
  cursor: pointer;
}

.section-content {
  display: flex;
  align-items: center;
}

.address-info {
  flex: 1;
}

.address-detail {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.contact {
  font-size: 17px;
  font-weight: 600;
}

.phone {
  font-size: 15px;
  color: var(--text-secondary);
}

.address-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.add-address {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--apple-blue);
  font-size: 15px;
}

.arrow {
  color: var(--text-tertiary);
}

.order-items {
  padding: 16px;
}

.order-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--bg-secondary);
  
  &:last-child {
    border-bottom: none;
  }
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-lg);
  object-fit: cover;
  background: var(--bg-secondary);
}

.item-info {
  flex: 1;
  
  h4 {
    font-size: 15px;
    font-weight: 500;
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  p {
    font-size: 13px;
    color: var(--text-tertiary);
  }
}

.item-price {
  text-align: right;
}

.price {
  font-size: 15px;
  font-weight: 600;
  display: block;
}

.qty {
  font-size: 13px;
  color: var(--text-tertiary);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  
  h3 {
    font-size: 15px;
    font-weight: 600;
  }
  
  .highlight {
    font-size: 13px;
    color: var(--apple-green);
  }
}

.section-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  
  .label {
    font-size: 15px;
    color: var(--text-primary);
  }
  
  .value {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: var(--text-secondary);
  }
  
  .gray {
    color: var(--text-tertiary);
  }
  
  .coupon-tag {
    color: var(--apple-red);
  }
}

.remark-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  text-align: right;
  outline: none;
  
  &::placeholder {
    color: var(--text-tertiary);
  }
}

.switch {
  position: relative;
  width: 44px;
  height: 26px;
  
  input {
    opacity: 0;
    width: 0;
    height: 0;
  }
  
  .slider {
    position: absolute;
    cursor: pointer;
    inset: 0;
    background: #ccc;
    border-radius: 13px;
    transition: 0.3s;
    
    &::before {
      content: '';
      position: absolute;
      height: 22px;
      width: 22px;
      left: 2px;
      bottom: 2px;
      background: white;
      border-radius: 50%;
      transition: 0.3s;
    }
  }
  
  input:checked + .slider {
    background: var(--apple-green);
    
    &::before {
      transform: translateX(18px);
    }
  }
}

.price-section {
  padding: 16px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  color: var(--text-secondary);
  
  .highlight {
    color: var(--apple-green);
  }
  
  .discount {
    color: var(--apple-red);
  }
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--bg-primary);
  border-top: 1px solid var(--bg-secondary);
  z-index: 100;
}

.total-info {
  display: flex;
  flex-direction: column;
}

.total-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.total-price {
  font-size: 22px;
  font-weight: 700;
  color: var(--apple-red);
}

.submit-btn {
  padding: 14px 40px;
  background: var(--apple-red);
  color: white;
  border: none;
  border-radius: var(--radius-full);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}
</style>
