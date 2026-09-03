<template>
  <div class="order-detail" v-loading="loading">
    <section v-if="order" class="container-large">
      <button class="back-btn" @click="$router.back()">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M15 18l-6-6 6-6"/>
        </svg>
        返回
      </button>
      
      <!-- 状态横幅 -->
      <div class="status-banner" :class="`status-${order.orderStatus}`">
        <div class="banner-info">
          <h1 class="banner-title">{{ order.orderStatusName }}</h1>
          <p class="banner-desc">{{ getBannerDesc(order.orderStatus) }}</p>
        </div>
      </div>
      
      <!-- 收货信息 -->
      <section class="detail-section">
        <h2 class="section-title">收货信息</h2>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">收货人</span>
            <span class="info-value">{{ order.receiverName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">电话</span>
            <span class="info-value">{{ order.receiverPhone }}</span>
          </div>
          <div class="info-item full-width">
            <span class="info-label">地址</span>
            <span class="info-value">{{ order.receiverAddress }}</span>
          </div>
        </div>
      </section>
      
      <!-- 商品列表 -->
      <section class="detail-section">
        <h2 class="section-title">商品清单</h2>
        <div class="item-list">
          <div v-for="item in order.items" :key="item.id" class="item">
            <img :src="item.productImage" :alt="item.productName" />
            <div class="item-info">
              <p class="item-name">{{ item.productName }}</p>
              <p class="item-meta">¥{{ formatPrice(item.price) }} × {{ item.quantity }}</p>
            </div>
            <p class="item-total">¥{{ formatPrice(item.totalPrice) }}</p>
          </div>
        </div>
      </section>
      
      <!-- 订单信息 -->
      <section class="detail-section">
        <h2 class="section-title">订单信息</h2>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">订单号</span>
            <span class="info-value">{{ order.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">下单时间</span>
            <span class="info-value">{{ formatTime(order.createTime) }}</span>
          </div>
          <div v-if="order.payTime" class="info-item">
            <span class="info-label">支付时间</span>
            <span class="info-value">{{ formatTime(order.payTime) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">支付方式</span>
            <span class="info-value">{{ order.payTypeName || '-' }}</span>
          </div>
        </div>
        
        <div class="info-divider"></div>
        
        <div class="info-totals">
          <div class="total-row">
            <span>商品金额</span>
            <span>¥{{ formatPrice(order.totalPrice) }}</span>
          </div>
          <div class="total-row">
            <span>运费</span>
            <span>免运费</span>
          </div>
          <div class="total-row final">
            <span>实付</span>
            <span class="final-price">¥{{ formatPrice(order.payPrice) }}</span>
          </div>
        </div>
      </section>
      
      <!-- 操作按钮 -->
      <div class="action-bar">
        <button v-if="order.orderStatus === 1" class="btn-primary" @click="handlePay">
          立即支付 ¥{{ formatPrice(order.payPrice) }}
        </button>
        <button v-if="order.orderStatus === 1" class="btn-ghost" @click="handleCancel">
          取消订单
        </button>
        <button v-if="order.orderStatus === 3" class="btn-primary" @click="handleConfirm">
          确认收货
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import request from '@/utils/request'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const order = ref(null)
const loading = ref(false)

const formatPrice = (price) => {
  return Number(price || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getBannerDesc = (status) => {
  const map = {
    1: '请尽快完成支付，超时订单将自动取消',
    2: '商家正在备货中',
    3: '商品已发出，请耐心等待',
    4: '订单已完成，欢迎再次购买',
    5: '订单已取消'
  }
  return map[status] || ''
}

const fetchOrder = async () => {
  loading.value = true
  try {
    order.value = await request.get(`/order/${route.params.id}`)
  } catch (e) {
    console.error('获取订单失败', e)
  } finally {
    loading.value = false
  }
}

const handlePay = async () => {
  try {
    await request.post(`/order/${order.value.id}/pay`)
    showToast({ message: '支付成功', icon: 'success' })
    await cartStore.fetchCartCount()
    fetchOrder()
  } catch (e) {
    console.error('支付失败', e)
  }
}

const handleCancel = async () => {
  try {
    await showConfirmDialog({ title: '取消订单', message: '确认取消这个订单吗？' })
  } catch { return }
  
  try {
    await request.put(`/order/${order.value.id}/cancel`)
    showToast('订单已取消')
    fetchOrder()
  } catch (e) {
    console.error('取消失败', e)
  }
}

const handleConfirm = async () => {
  try {
    await showConfirmDialog({ title: '确认收货', message: '确认已收到商品吗？' })
  } catch { return }
  
  try {
    await request.put(`/order/${order.value.id}/confirm`)
    showToast('已确认收货')
    fetchOrder()
  } catch (e) {
    console.error('确认失败', e)
  }
}

onMounted(fetchOrder)
</script>

<style lang="scss" scoped>
.order-detail {
  padding: 80px 22px;
  max-width: 800px;
  margin: 0 auto;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  background: #fff;
  border-radius: 980px;
  font-size: 14px;
  color: #1d1d1f;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.status-banner {
  border-radius: 18px;
  padding: 32px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #0071e3 0%, #0077ed 100%);
  color: #fff;
  
  &.status-1 { background: linear-gradient(135deg, #ff9f0a 0%, #ffae3d 100%); }
  &.status-4 { background: linear-gradient(135deg, #30d158 0%, #46d465 100%); }
  &.status-5 { background: linear-gradient(135deg, #6e6e73 0%, #86868b 100%); }
}

.banner-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.banner-desc {
  font-size: 14px;
  opacity: 0.9;
}

.detail-section {
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

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 32px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  
  &.full-width {
    grid-column: 1 / -1;
  }
}

.info-label {
  font-size: 12px;
  color: #86868b;
}

.info-value {
  font-size: 15px;
  color: #1d1d1f;
}

.info-divider {
  height: 1px;
  background: #f5f5f7;
  margin: 24px 0;
}

.info-totals {
  .total-row {
    display: flex;
    justify-content: space-between;
    font-size: 14px;
    color: #6e6e73;
    margin-bottom: 12px;
  }
  
  .final {
    font-size: 17px;
    color: #1d1d1f;
    font-weight: 500;
    margin-bottom: 0;
  }
}

.final-price {
  color: #ff3b30;
  font-size: 24px;
  font-weight: 600;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.item {
  display: flex;
  align-items: center;
  gap: 16px;
  
  img {
    width: 72px;
    height: 72px;
    border-radius: 8px;
    object-fit: cover;
    background: #f5f5f7;
  }
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 15px;
  font-weight: 500;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.item-meta {
  font-size: 13px;
  color: #6e6e73;
}

.item-total {
  font-size: 15px;
  font-weight: 500;
  color: #1d1d1f;
}

.action-bar {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
  
  button {
    height: 48px;
    padding: 0 32px;
    border-radius: 980px;
    font-size: 15px;
    transition: all 0.2s;
  }
}

.btn-primary {
  background: #0071e3;
  color: #fff;
  
  &:hover {
    background: #0077ed;
  }
}

.btn-ghost {
  background: #fff;
  color: #1d1d1f;
  border: 1px solid #d2d2d7;
  
  &:hover {
    border-color: #1d1d1f;
  }
}
</style>
