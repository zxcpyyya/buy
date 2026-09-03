<template>
  <div class="checkout-page">
    <section class="page-hero">
      <div class="container-large">
        <h1 class="page-title">确认订单</h1>
        <p class="page-subtitle">请确认您的购物信息</p>
      </div>
    </section>
    
    <div class="container-large">
      <div class="checkout-content">
        <!-- 收货地址 -->
        <section class="checkout-section">
          <h2 class="section-title">收货地址</h2>
          <div v-if="addresses.length > 0" class="address-list">
            <div 
              v-for="addr in addresses" 
              :key="addr.id"
              class="address-card"
              :class="{ active: selectedAddressId === addr.id }"
              @click="selectedAddressId = addr.id"
            >
              <div class="address-radio"></div>
              <div class="address-info">
                <p class="address-name">{{ addr.consignee }} · {{ addr.phone }}</p>
                <p class="address-detail">{{ addr.fullAddress }}</p>
              </div>
              <span v-if="addr.isDefault" class="address-tag">默认</span>
            </div>
            <router-link to="/addresses" class="address-add">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 5v14M5 12h14"/>
              </svg>
              管理地址
            </router-link>
          </div>
          <div v-else class="empty-address">
            <p>您还没有添加收货地址</p>
            <router-link to="/addresses" class="btn-link">立即添加</router-link>
          </div>
        </section>
        
        <!-- 商品清单 -->
        <section class="checkout-section">
          <h2 class="section-title">商品清单</h2>
          <div class="product-list">
            <div v-for="item in cart.items" :key="item.id" class="product-row">
              <img :src="item.image" :alt="item.productName" class="product-thumb" />
              <div class="product-info">
                <h3 class="product-name">{{ item.productName }}</h3>
                <p class="product-qty">数量：{{ item.quantity }}</p>
              </div>
              <p class="product-price">¥{{ formatPrice(item.subtotal) }}</p>
            </div>
          </div>
        </section>
        
        <!-- 支付方式 -->
        <section class="checkout-section">
          <h2 class="section-title">支付方式</h2>
          <div class="pay-list">
            <div 
              v-for="pay in payTypes" 
              :key="pay.id"
              class="pay-card"
              :class="{ active: payType === pay.id }"
              @click="payType = pay.id"
            >
              <div class="pay-radio"></div>
              <span class="pay-icon">{{ pay.icon }}</span>
              <span class="pay-name">{{ pay.name }}</span>
            </div>
          </div>
        </section>
        
        <!-- 备注 -->
        <section class="checkout-section">
          <h2 class="section-title">订单备注</h2>
          <textarea 
            v-model="remark" 
            class="remark-input"
            placeholder="选填，请勿填写与商品无关的内容"
            rows="3"
          ></textarea>
        </section>
      </div>
      
      <!-- 底部结算栏 -->
      <div class="checkout-footer">
        <div class="footer-summary">
          <div class="footer-info">
            <p class="footer-label">实付金额</p>
            <p class="footer-price">¥{{ formatPrice(totalAmount) }}</p>
          </div>
          <p class="footer-detail">{{ cart.items.length }} 件商品</p>
        </div>
        <button 
          class="btn-submit" 
          :disabled="!selectedAddressId || cart.items.length === 0 || submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : '提交订单' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import request from '@/utils/request'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()

const cart = ref({ items: [], totalPrice: 0 })
const addresses = ref([])
const selectedAddressId = ref(null)
const payType = ref(1)
const remark = ref('')
const submitting = ref(false)

const payTypes = [
  { id: 1, name: '微信支付', icon: '💚' },
  { id: 2, name: '支付宝', icon: '💙' }
]

const totalAmount = computed(() => {
  const shipping = cart.value.totalPrice >= 99 ? 0 : 10
  return cart.value.totalPrice + shipping
})

const formatPrice = (price) => {
  return Number(price || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const fetchCart = async () => {
  try {
    cart.value = await request.get('/cart')
  } catch (e) {
    console.error('获取购物车失败', e)
  }
}

const fetchAddresses = async () => {
  try {
    const data = await request.get('/address')
    addresses.value = data || []
    // 自动选中默认地址
    const defaultAddr = addresses.value.find(a => a.isDefault === 1)
    if (defaultAddr) {
      selectedAddressId.value = defaultAddr.id
    } else if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].id
    }
  } catch (e) {
    console.error('获取地址失败', e)
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const data = await request.post('/order', {
      cartId: cart.value.id,
      addressId: selectedAddressId.value,
      payType: payType.value,
      remark: remark.value
    })
    
    showToast({
      message: '订单创建成功',
      icon: 'success'
    })
    
    // 刷新购物车
    await cartStore.fetchCartCount()
    
    setTimeout(() => {
      router.push(`/order/${data.id}`)
    }, 800)
  } catch (e) {
    console.error('提交订单失败', e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchCart()
  fetchAddresses()
})
</script>

<style lang="scss" scoped>
.checkout-page {
  padding-top: 44px;
  padding-bottom: 120px;
  background: #fbfbfd;
  min-height: 100vh;
}

.page-hero {
  padding: 80px 0 48px;
  text-align: center;
  background: #fff;
}

.page-title {
  font-size: 48px;
  font-weight: 600;
  letter-spacing: -0.015em;
  color: #1d1d1f;
  margin-bottom: 8px;
  
  @media (max-width: 833px) {
    font-size: 40px;
  }
}

.page-subtitle {
  font-size: 21px;
  color: #6e6e73;
}

.checkout-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 48px 22px;
}

.checkout-section {
  background: #fff;
  border-radius: 18px;
  padding: 32px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 21px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 24px;
}

// 地址
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border: 2px solid #f5f5f7;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  
  &.active {
    border-color: #0071e3;
    background: rgba(0, 113, 227, 0.04);
  }
}

.address-radio {
  width: 20px;
  height: 20px;
  border: 2px solid #d2d2d7;
  border-radius: 50%;
  position: relative;
  flex-shrink: 0;
  
  .active & {
    border-color: #0071e3;
    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      width: 10px;
      height: 10px;
      background: #0071e3;
      border-radius: 50%;
      transform: translate(-50%, -50%);
    }
  }
}

.address-info {
  flex: 1;
}

.address-name {
  font-size: 15px;
  font-weight: 500;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.address-detail {
  font-size: 13px;
  color: #6e6e73;
}

.address-tag {
  padding: 2px 8px;
  background: #1d1d1f;
  color: #fff;
  font-size: 11px;
  border-radius: 4px;
}

.address-add {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  border: 1px dashed #d2d2d7;
  border-radius: 12px;
  font-size: 14px;
  color: #6e6e73;
  transition: all 0.2s;
  
  &:hover {
    border-color: #0071e3;
    color: #0071e3;
  }
}

.empty-address {
  text-align: center;
  padding: 32px;
  color: #6e6e73;
}

// 商品
.product-list {
  display: flex;
  flex-direction: column;
}

.product-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f7;
  
  &:last-child {
    border-bottom: none;
  }
}

.product-thumb {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  object-fit: cover;
  background: #f5f5f7;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 15px;
  font-weight: 500;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.product-qty {
  font-size: 13px;
  color: #6e6e73;
}

.product-price {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
}

// 支付
.pay-list {
  display: flex;
  gap: 12px;
}

.pay-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border: 2px solid #f5f5f7;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  
  &.active {
    border-color: #0071e3;
    background: rgba(0, 113, 227, 0.04);
  }
}

.pay-radio {
  width: 20px;
  height: 20px;
  border: 2px solid #d2d2d7;
  border-radius: 50%;
  position: relative;
  
  .active & {
    border-color: #0071e3;
    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      width: 10px;
      height: 10px;
      background: #0071e3;
      border-radius: 50%;
      transform: translate(-50%, -50%);
    }
  }
}

.pay-icon {
  font-size: 24px;
}

.pay-name {
  font-size: 15px;
  font-weight: 500;
}

.remark-input {
  width: 100%;
  padding: 12px 16px;
  background: #f5f5f7;
  border: 1px solid transparent;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  transition: border-color 0.2s;
  
  &:focus {
    background: #fff;
    border-color: #0071e3;
  }
}

// 底部
.checkout-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: saturate(180%) blur(20px);
  border-top: 1px solid #f5f5f7;
  padding: 16px 22px;
  z-index: 100;
  
  > div {
    max-width: 1440px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
  }
}

.footer-summary {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.footer-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.footer-label {
  font-size: 13px;
  color: #6e6e73;
}

.footer-price {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
}

.footer-detail {
  font-size: 13px;
  color: #6e6e73;
}

.btn-submit {
  height: 48px;
  min-width: 180px;
  background: #0071e3;
  color: #fff;
  border-radius: 980px;
  font-size: 17px;
  font-weight: 500;
  transition: background 0.2s;
  
  &:hover:not(:disabled) {
    background: #0077ed;
  }
  
  &:disabled {
    background: #d2d2d7;
    cursor: not-allowed;
  }
}
</style>
