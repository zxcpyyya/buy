<template>
  <div class="orders-page">
    <section class="page-hero">
      <div class="container-large">
        <h1 class="page-title">我的订单</h1>
        <p class="page-subtitle">查看您的所有订单</p>
      </div>
    </section>
    
    <div class="container-large">
      <!-- 状态筛选 -->
      <div class="order-tabs">
        <span 
          v-for="tab in tabs" 
          :key="tab.value"
          class="tab"
          :class="{ active: status === tab.value }"
          @click="changeStatus(tab.value)"
        >{{ tab.label }}</span>
      </div>
      
      <div v-loading="loading" class="order-list" element-loading-text="加载中...">
        <div 
          v-for="order in orders" 
          :key="order.id"
          class="order-card"
        >
          <div class="order-header">
            <div class="order-meta">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ formatTime(order.createTime) }}</span>
            </div>
            <span class="order-status" :class="`status-${order.orderStatus}`">
              {{ order.orderStatusName }}
            </span>
          </div>
          
          <div class="order-items">
            <div 
              v-for="item in order.items.slice(0, 3)" 
              :key="item.id"
              class="order-item"
            >
              <img :src="item.productImage" :alt="item.productName" />
              <div class="item-info">
                <p class="item-name">{{ item.productName }}</p>
                <p class="item-meta">数量：{{ item.quantity }}</p>
              </div>
              <p class="item-price">¥{{ formatPrice(item.price) }}</p>
            </div>
            <div v-if="order.items.length > 3" class="more-items">
              还有 {{ order.items.length - 3 }} 件商品...
            </div>
          </div>
          
          <div class="order-footer">
            <div class="order-total">
              共 {{ order.items.length }} 件，实付：
              <span class="total-price">¥{{ formatPrice(order.payPrice) }}</span>
            </div>
            <div class="order-actions">
              <button class="btn-ghost" @click="$router.push(`/order/${order.id}`)">
                查看详情
              </button>
              <button 
                v-if="order.orderStatus === 1" 
                class="btn-primary-outline"
                @click="handlePay(order)"
              >立即支付</button>
              <button 
                v-if="order.orderStatus === 1" 
                class="btn-ghost"
                @click="handleCancel(order)"
              >取消订单</button>
              <button 
                v-if="order.orderStatus === 3" 
                class="btn-primary"
                @click="handleConfirm(order)"
              >确认收货</button>
              <button 
                v-if="order.orderStatus === 4 || order.orderStatus === 5" 
                class="btn-ghost"
                @click="handleDelete(order)"
              >删除订单</button>
            </div>
          </div>
        </div>
        
        <div v-if="!loading && orders.length === 0" class="empty">
          <div class="empty-icon">
            <svg viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="14" y="10" width="36" height="48" rx="3"/>
              <path d="M22 22h20M22 32h20M22 42h12"/>
            </svg>
          </div>
          <h2 class="empty-title">暂无订单</h2>
          <p class="empty-desc">去看看有什么心仪的好物吧</p>
          <router-link to="/products" class="btn btn-primary">立即购买</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import request from '@/utils/request'

const orders = ref([])
const loading = ref(false)
const status = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)

const tabs = [
  { label: '全部', value: null },
  { label: '待支付', value: 1 },
  { label: '已支付', value: 2 },
  { label: '已发货', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 }
]

const formatPrice = (price) => {
  return Number(price || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', { 
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const data = await request.get('/order/list', {
      params: { status: status.value, pageNum: pageNum.value, pageSize: pageSize.value }
    })
    orders.value = data.records || []
  } catch (e) {
    console.error('获取订单失败', e)
  } finally {
    loading.value = false
  }
}

const changeStatus = (val) => {
  status.value = val
  pageNum.value = 1
  fetchOrders()
}

const handlePay = async (order) => {
  try {
    await request.post(`/order/${order.id}/pay`)
    showToast({ message: '支付成功', icon: 'success' })
    fetchOrders()
  } catch (e) {
    console.error('支付失败', e)
  }
}

const handleCancel = async (order) => {
  try {
    await showConfirmDialog({ title: '取消订单', message: '确认取消这个订单吗？' })
  } catch { return }
  
  try {
    await request.put(`/order/${order.id}/cancel`)
    showToast({ message: '订单已取消', icon: 'success' })
    fetchOrders()
  } catch (e) {
    console.error('取消失败', e)
  }
}

const handleConfirm = async (order) => {
  try {
    await showConfirmDialog({ title: '确认收货', message: '确认已收到商品吗？' })
  } catch { return }
  
  try {
    await request.put(`/order/${order.id}/confirm`)
    showToast({ message: '已确认收货', icon: 'success' })
    fetchOrders()
  } catch (e) {
    console.error('确认收货失败', e)
  }
}

const handleDelete = async (order) => {
  try {
    await showConfirmDialog({ title: '删除订单', message: '确认删除这个订单吗？' })
  } catch { return }
  
  try {
    await request.delete(`/order/${order.id}`)
    showToast('已删除')
    fetchOrders()
  } catch (e) {
    console.error('删除失败', e)
  }
}

onMounted(fetchOrders)
</script>

<style lang="scss" scoped>
.orders-page {
  padding-top: 44px;
  padding-bottom: 80px;
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

.order-tabs {
  display: flex;
  gap: 8px;
  padding: 24px 0;
  border-bottom: 1px solid #f5f5f7;
  margin-bottom: 24px;
  overflow-x: auto;
}

.tab {
  padding: 8px 16px;
  font-size: 14px;
  color: #6e6e73;
  border-radius: 980px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
  
  &:hover {
    background: #f5f5f7;
  }
  
  &.active {
    background: #1d1d1f;
    color: #fff;
  }
}

// 订单卡片
.order-card {
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  margin-bottom: 16px;
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f7;
}

.order-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #6e6e73;
}

.order-status {
  padding: 4px 12px;
  border-radius: 980px;
  font-size: 12px;
  font-weight: 500;
  
  &.status-1 { background: rgba(255, 159, 10, 0.1); color: #ff9f0a; }
  &.status-2 { background: rgba(48, 209, 88, 0.1); color: #30d158; }
  &.status-3 { background: rgba(0, 113, 227, 0.1); color: #0071e3; }
  &.status-4 { background: rgba(110, 110, 115, 0.1); color: #6e6e73; }
  &.status-5 { background: rgba(255, 59, 48, 0.1); color: #ff3b30; }
}

.order-items {
  padding: 16px 24px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  
  img {
    width: 64px;
    height: 64px;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  font-size: 13px;
  color: #6e6e73;
}

.item-price {
  font-size: 15px;
  font-weight: 500;
  color: #1d1d1f;
}

.more-items {
  padding: 8px 0;
  font-size: 13px;
  color: #86868b;
  text-align: center;
}

.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #f5f5f7;
  border-top: 1px solid #f5f5f7;
}

.order-total {
  font-size: 14px;
  color: #1d1d1f;
}

.total-price {
  font-size: 17px;
  font-weight: 600;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.btn-ghost,
.btn-primary,
.btn-primary-outline {
  height: 32px;
  padding: 0 16px;
  font-size: 13px;
  border-radius: 980px;
  transition: all 0.2s;
}

.btn-ghost {
  background: transparent;
  color: #1d1d1f;
  border: 1px solid #d2d2d7;
  
  &:hover {
    background: #fff;
    border-color: #1d1d1f;
  }
}

.btn-primary-outline {
  background: transparent;
  color: #0071e3;
  border: 1px solid #0071e3;
  
  &:hover {
    background: rgba(0, 113, 227, 0.08);
  }
}

.btn-primary {
  background: #0071e3;
  color: #fff;
  
  &:hover {
    background: #0077ed;
  }
}

// 空状态
.empty {
  text-align: center;
  padding: 80px 0;
  background: #fff;
  border-radius: 18px;
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
