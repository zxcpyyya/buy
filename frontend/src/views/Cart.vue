<template>
  <div class="cart-page">
    <!-- 大标题 -->
    <section class="page-hero">
      <div class="container-large">
        <h1 class="page-title">购物袋</h1>
        <p class="page-subtitle" v-if="cart.items.length > 0">
          您的购物袋中有 {{ cart.items.length }} 件商品
        </p>
        <p class="page-subtitle" v-else>您的购物袋是空的</p>
      </div>
    </section>
    
    <div class="container-large">
      <div v-loading="loading" class="cart-content" element-loading-text="加载中...">
        <!-- 购物袋内容 -->
        <div v-if="cart.items.length > 0" class="cart-grid">
          <!-- 左：商品列表 -->
          <div class="cart-items">
            <div 
              v-for="item in cart.items" 
              :key="item.id"
              class="cart-item"
            >
              <div class="item-image" @click="$router.push(`/product/${item.productId}`)">
                <img :src="item.image" :alt="item.productName" />
              </div>
              <div class="item-info">
                <h3 class="item-name" @click="$router.push(`/product/${item.productId}`)">
                  {{ item.productName }}
                </h3>
                <p class="item-price">¥{{ formatPrice(item.price) }}</p>
                <div class="item-actions">
                  <div class="qty-control">
                    <button 
                      class="qty-btn" 
                      :disabled="item.quantity <= 1"
                      @click="updateQty(item, -1)"
                    >−</button>
                    <span class="qty-value">{{ item.quantity }}</span>
                    <button 
                      class="qty-btn" 
                      :disabled="item.quantity >= item.stock"
                      @click="updateQty(item, 1)"
                    >+</button>
                  </div>
                  <button class="btn-remove" @click="removeItem(item)">删除</button>
                </div>
              </div>
              <div class="item-subtotal">
                <p class="subtotal-label">小计</p>
                <p class="subtotal-value">¥{{ formatPrice(item.subtotal) }}</p>
              </div>
            </div>
          </div>
          
          <!-- 右：结算卡片 -->
          <div class="cart-summary">
            <div class="summary-card">
              <h3 class="summary-title">订单摘要</h3>
              
              <div class="summary-row">
                <span>商品小计</span>
                <span>¥{{ formatPrice(cart.totalPrice) }}</span>
              </div>
              <div class="summary-row">
                <span>运费</span>
                <span>{{ cart.totalPrice >= 99 ? '免运费' : '¥10.00' }}</span>
              </div>
              
              <div class="summary-divider"></div>
              
              <div class="summary-total">
                <span>总计</span>
                <span class="total-value">¥{{ formatPrice(totalAmount) }}</span>
              </div>
              
              <button class="btn-checkout" @click="handleCheckout">
                结算 ({{ cart.items.length }})
              </button>
              
              <div class="summary-services">
                <div class="service-item">
                  <svg viewBox="0 0 20 20" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M3 8h14v5H3z"/>
                  </svg>
                  <span>满99免运费</span>
                </div>
                <div class="service-item">
                  <svg viewBox="0 0 20 20" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M10 3l2 2v4c0 3-2 4-2 4s-2-1-2-4V5l2-2z"/>
                  </svg>
                  <span>正品保障</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 空购物袋 -->
        <div v-else class="empty-cart">
          <div class="empty-icon">
            <svg viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M16 20h32l-4 32a3 3 0 01-3 2.8H23a3 3 0 01-3-2.8L16 20z"/>
              <path d="M24 20v-4a8 8 0 0116 0v4"/>
            </svg>
          </div>
          <h2 class="empty-title">购物袋空空如也</h2>
          <p class="empty-desc">去看看有什么心仪的好物吧</p>
          <router-link to="/products" class="btn btn-primary">继续购物</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import request from '@/utils/request'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()

const loading = ref(false)
const cart = ref({
  items: [],
  totalPrice: 0,
  totalCount: 0
})

const totalAmount = computed(() => {
  const shipping = cart.value.totalPrice >= 99 ? 0 : 10
  return cart.value.totalPrice + shipping
})

const formatPrice = (price) => {
  return Number(price || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const fetchCart = async () => {
  loading.value = true
  try {
    const data = await request.get('/cart')
    cart.value = data
  } catch (e) {
    console.error('获取购物车失败', e)
  } finally {
    loading.value = false
  }
}

const updateQty = async (item, delta) => {
  const newQty = item.quantity + delta
  try {
    await request.put(`/cart/item/${item.id}`, null, { params: { quantity: newQty } })
    await fetchCart()
    cartStore.fetchCartCount()
  } catch (e) {
    console.error('更新数量失败', e)
  }
}

const removeItem = async (item) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定要从购物袋移除"${item.productName}"吗？`
    })
  } catch {
    return
  }
  
  try {
    await request.delete(`/cart/item/${item.id}`)
    showToast('已移除')
    await fetchCart()
    cartStore.fetchCartCount()
  } catch (e) {
    console.error('删除失败', e)
  }
}

const handleCheckout = () => {
  router.push('/checkout')
}

onMounted(fetchCart)
</script>

<style lang="scss" scoped>
.cart-page {
  padding-top: 44px;
  padding-bottom: 80px;
  min-height: 100vh;
  background: #fbfbfd;
}

.page-hero {
  padding: 80px 0 48px;
  text-align: center;
  background: #fff;
}

.page-title {
  font-size: 56px;
  font-weight: 600;
  letter-spacing: -0.015em;
  color: #1d1d1f;
  margin-bottom: 8px;
  line-height: 1.07;
  
  @media (max-width: 833px) {
    font-size: 40px;
  }
}

.page-subtitle {
  font-size: 21px;
  color: #6e6e73;
}

.cart-content {
  padding: 48px 0;
}

.cart-grid {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 32px;
  align-items: start;
  
  @media (max-width: 833px) {
    grid-template-columns: 1fr;
  }
}

// 商品列表
.cart-items {
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
}

.cart-item {
  display: grid;
  grid-template-columns: 96px 1fr auto;
  gap: 24px;
  padding: 24px;
  border-bottom: 1px solid #f5f5f7;
  
  &:last-child {
    border-bottom: none;
  }
}

.item-image {
  width: 96px;
  height: 96px;
  background: #f5f5f7;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.item-info {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.item-name {
  font-size: 17px;
  font-weight: 500;
  color: #1d1d1f;
  cursor: pointer;
  margin-bottom: 8px;
  
  &:hover {
    color: #0071e3;
  }
}

.item-price {
  font-size: 14px;
  color: #6e6e73;
  margin-bottom: 12px;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 24px;
}

.qty-control {
  display: inline-flex;
  align-items: center;
  border: 1px solid #d2d2d7;
  border-radius: 980px;
  overflow: hidden;
}

.qty-btn {
  width: 32px;
  height: 32px;
  font-size: 16px;
  
  &:hover:not(:disabled) {
    background: #f5f5f7;
  }
  
  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
}

.qty-value {
  min-width: 32px;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
}

.btn-remove {
  font-size: 14px;
  color: #6e6e73;
  
  &:hover {
    color: #ff3b30;
  }
}

.item-subtotal {
  text-align: right;
}

.subtotal-label {
  font-size: 12px;
  color: #86868b;
  margin-bottom: 4px;
}

.subtotal-value {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
}

// 结算卡片
.summary-card {
  background: #fff;
  border-radius: 18px;
  padding: 32px;
  position: sticky;
  top: 80px;
}

.summary-title {
  font-size: 21px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 24px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #6e6e73;
  margin-bottom: 12px;
}

.summary-divider {
  height: 1px;
  background: #f5f5f7;
  margin: 16px 0;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 24px;
  font-size: 14px;
  color: #1d1d1f;
}

.total-value {
  font-size: 28px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.btn-checkout {
  width: 100%;
  height: 48px;
  background: #0071e3;
  color: #fff;
  border-radius: 980px;
  font-size: 17px;
  font-weight: 500;
  transition: background 0.2s;
  margin-bottom: 16px;
  
  &:hover {
    background: #0077ed;
  }
}

.summary-services {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid #f5f5f7;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6e6e73;
}

// 空购物袋
.empty-cart {
  text-align: center;
  padding: 80px 0;
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  color: #d2d2d7;
}

.empty-title {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 17px;
  color: #6e6e73;
  margin-bottom: 24px;
}
</style>
