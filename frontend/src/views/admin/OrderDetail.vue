<template>
  <div class="order-detail">
    <div class="page-header">
      <div class="flex items-center gap-md">
        <button class="btn btn-ghost" @click="goBack">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/>
          </svg>
        </button>
        <div>
          <h1 class="page-title">订单详情</h1>
          <p class="page-subtitle">订单号：{{ order?.orderNo }}</p>
        </div>
      </div>
    </div>

    <div class="detail-container" v-if="order">
      <div class="detail-section card">
        <h3>订单状态</h3>
        <div class="status-info">
          <span class="status-badge" :class="'status-' + order.status">{{ getStatusText(order.status) }}</span>
        </div>
      </div>

      <div class="detail-section card">
        <h3>收货信息</h3>
        <div class="info-grid">
          <div class="info-item"><label>收货人：</label><span>{{ order.userName }}</span></div>
          <div class="info-item"><label>联系电话：</label><span>{{ order.userPhone }}</span></div>
          <div class="info-item full"><label>收货地址：</label><span>{{ order.address }}</span></div>
        </div>
      </div>

      <div class="detail-section card">
        <h3>商品信息</h3>
        <div class="product-list">
          <div class="product-item" v-for="item in order.items" :key="item.id">
            <img :src="item.image" class="product-img" />
            <div class="product-info">
              <div class="product-name">{{ item.name }}</div>
              <div class="product-spec">{{ item.spec }}</div>
            </div>
            <div class="product-price">¥{{ item.price }}</div>
            <div class="product-count">x{{ item.count }}</div>
          </div>
        </div>
        <div class="order-summary">
          <span>商品总额：¥{{ order.goodsAmount }}</span>
          <span>运费：¥{{ order.freight }}</span>
          <span class="total">实付：<strong>¥{{ order.totalAmount }}</strong></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const order = ref(null)

const getStatusText = (status) => ({ 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }[status] || '')

const goBack = () => router.back()

onMounted(() => {
  order.value = {
    orderNo: route.params.id || 'ORD202409040001',
    status: 2,
    userName: '张三',
    userPhone: '138****1234',
    address: '北京市朝阳区建国路88号SOHO现代城',
    goodsAmount: 9998,
    freight: 0,
    totalAmount: 9998,
    items: [
      { id: 1, name: 'iPhone 15 Pro Max 256GB', spec: '深空黑', price: 9999, count: 1, image: 'https://picsum.photos/80/80?random=1' }
    ]
  }
})
</script>

<style scoped>
.order-detail { max-width: 900px; }

.detail-container {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.detail-section {
  padding: var(--spacing-lg);
}

.detail-section h3 {
  font-size: var(--font-size-lg);
  font-weight: 600;
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--border-light);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.info-item {
  display: flex;
  gap: var(--spacing-sm);
}

.info-item.full {
  grid-column: span 2;
}

.info-item label {
  color: var(--text-secondary);
}

.product-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) 0;
  border-bottom: 1px solid var(--border-light);
}

.product-img {
  width: 60px;
  height: 60px;
  border-radius: var(--border-radius-sm);
  object-fit: cover;
}

.product-info { flex: 1; }

.product-name {
  font-weight: 500;
  color: var(--text-primary);
}

.product-spec {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.order-summary {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-lg);
  padding-top: var(--spacing-lg);
}

.order-summary .total {
  color: var(--color-danger);
  font-size: var(--font-size-lg);
}
</style>
