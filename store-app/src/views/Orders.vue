<template>
  <div class="orders-page safe-area-top">
    <!-- 顶部 -->
    <header class="header">
      <button class="back-btn" @click="$router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </button>
      <h1 class="title">我的订单</h1>
      <div class="placeholder"></div>
    </header>

    <!-- 订单状态Tab -->
    <div class="order-tabs">
      <div 
        v-for="tab in tabs" 
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="currentTab = tab.value"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="orders-list" v-if="orders.length > 0">
      <div class="order-card" v-for="order in filteredOrders" :key="order.id" @click="viewOrder(order.id)">
        <div class="order-header">
          <span class="order-no">订单号: {{ order.orderNo }}</span>
          <span class="order-status" :class="order.statusClass">{{ order.statusText }}</span>
        </div>
        
        <div class="order-items">
          <img 
            v-for="(item, index) in order.items.slice(0, 3)" 
            :key="index"
            :src="item.image" 
            class="item-image"
          />
          <div class="more-items" v-if="order.items.length > 3">
            +{{ order.items.length - 3 }}
          </div>
        </div>
        
        <div class="order-footer">
          <div class="order-info">
            <span class="shop-name">{{ order.shopName }}</span>
            <span class="order-time">{{ order.createTime }}</span>
          </div>
          <div class="order-price">
            <span class="label">实付款</span>
            <span class="price">¥{{ formatPrice(order.payPrice) }}</span>
          </div>
        </div>
        
        <div class="order-actions" v-if="order.status === 'pending'">
          <button class="action-btn cancel" @click.stop="cancelOrder(order)">取消订单</button>
          <button class="action-btn primary" @click.stop="payOrder(order)">去支付</button>
        </div>
        <div class="order-actions" v-else-if="order.status === 'shipped'">
          <button class="action-btn primary" @click.stop="viewLogistics(order)">查看物流</button>
          <button class="action-btn" @click.stop="confirmReceive(order)">确认收货</button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else>
      <div class="empty-icon">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
        </svg>
      </div>
      <p class="empty-text">暂无订单</p>
      <button class="btn btn-primary" @click="$router.push('/')">去购物</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'

const router = useRouter()

const currentTab = ref('all')
const tabs = [
  { label: '全部', value: 'all' },
  { label: '待支付', value: 'pending' },
  { label: '待发货', value: 'processing' },
  { label: '待收货', value: 'shipped' },
  { label: '已完成', value: 'completed' }
]

const orders = ref([
  {
    id: 1,
    orderNo: '20240904001',
    shopName: 'Store',
    status: 'pending',
    statusText: '待支付',
    statusClass: 'pending',
    payPrice: 10898,
    createTime: '2024-09-04 15:30',
    items: [
      { image: 'https://picsum.photos/100/100?random=1', name: 'iPhone 16 Pro' },
      { image: 'https://picsum.photos/100/100?random=2', name: 'AirPods Pro' }
    ]
  },
  {
    id: 2,
    orderNo: '20240903001',
    shopName: 'Store',
    status: 'shipped',
    statusText: '配送中',
    statusClass: 'shipped',
    payPrice: 8999,
    createTime: '2024-09-03 10:20',
    items: [
      { image: 'https://picsum.photos/100/100?random=3', name: 'iPad Pro' }
    ]
  },
  {
    id: 3,
    orderNo: '20240902001',
    shopName: 'Store',
    status: 'completed',
    statusText: '已完成',
    statusClass: 'completed',
    payPrice: 15999,
    createTime: '2024-09-02 09:15',
    items: [
      { image: 'https://picsum.photos/100/100?random=4', name: 'MacBook Pro' },
      { image: 'https://picsum.photos/100/100?random=5', name: 'Magic Mouse' },
      { image: 'https://picsum.photos/100/100?random=6', name: 'Magic Keyboard' }
    ]
  }
])

const filteredOrders = computed(() => {
  if (currentTab.value === 'all') {
    return orders.value
  }
  return orders.value.filter(order => order.status === currentTab.value)
})

const formatPrice = (price) => {
  return Number(price || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const viewOrder = (id) => {
  router.push(`/order/${id}`)
}

const cancelOrder = (order) => {
  showToast('订单已取消')
  order.status = 'cancelled'
  order.statusText = '已取消'
}

const payOrder = (order) => {
  showToast('跳转支付...')
}

const viewLogistics = (order) => {
  router.push(`/express/${order.id}`)
}

const confirmReceive = (order) => {
  showToast('已确认收货')
  order.status = 'completed'
  order.statusText = '已完成'
}
</script>

<style lang="scss" scoped>
.orders-page {
  min-height: 100vh;
  background: var(--bg-secondary);
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
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.placeholder {
  width: 32px;
}

.order-tabs {
  display: flex;
  background: var(--bg-primary);
  padding: 0 16px;
  overflow-x: auto;
  
  &::-webkit-scrollbar {
    display: none;
  }
}

.tab-item {
  flex-shrink: 0;
  padding: 14px 16px;
  font-size: 15px;
  color: var(--text-secondary);
  border-bottom: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
  
  &.active {
    color: var(--apple-blue);
    border-bottom-color: var(--apple-blue);
    font-weight: 600;
  }
}

.orders-list {
  padding: 16px;
}

.order-card {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  padding: 16px;
  margin-bottom: 16px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.order-no {
  font-size: 13px;
  color: var(--text-secondary);
}

.order-status {
  font-size: 13px;
  font-weight: 600;
  
  &.pending { color: var(--apple-orange); }
  &.processing { color: var(--apple-blue); }
  &.shipped { color: var(--apple-purple); }
  &.completed { color: var(--apple-green); }
  &.cancelled { color: var(--text-tertiary); }
}

.order-items {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.item-image {
  width: 70px;
  height: 70px;
  border-radius: var(--radius-md);
  object-fit: cover;
  background: var(--bg-secondary);
}

.more-items {
  width: 70px;
  height: 70px;
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--text-tertiary);
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--bg-secondary);
}

.shop-name {
  font-size: 14px;
  font-weight: 600;
}

.order-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.order-price {
  text-align: right;
  
  .label {
    font-size: 12px;
    color: var(--text-secondary);
  }
  
  .price {
    font-size: 16px;
    font-weight: 700;
    color: var(--apple-red);
  }
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 12px;
}

.action-btn {
  padding: 8px 20px;
  border: 1px solid var(--text-tertiary);
  border-radius: var(--radius-full);
  background: transparent;
  font-size: 13px;
  cursor: pointer;
  
  &.cancel {
    color: var(--text-secondary);
  }
  
  &.primary {
    background: var(--apple-blue);
    border-color: var(--apple-blue);
    color: white;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 40px 20px;
}

.empty-icon {
  width: 100px;
  height: 100px;
  background: var(--bg-secondary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-tertiary);
  margin-bottom: 20px;
}

.empty-text {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}
</style>
