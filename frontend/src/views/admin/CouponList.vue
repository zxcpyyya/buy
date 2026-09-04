<template>
  <div class="coupon-list">
    <div class="page-header">
      <div>
        <h1 class="page-title">优惠券管理</h1>
        <p class="page-subtitle">创建和管理优惠券活动</p>
      </div>
      <button class="btn btn-primary" @click="showAddModal = true">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        创建优惠券
      </button>
    </div>

    <div class="filter-bar card">
      <div class="filter-row">
        <div class="filter-item">
          <label>优惠券名称</label>
          <input v-model="filters.name" type="text" class="form-input" placeholder="搜索优惠券..." />
        </div>
        <div class="filter-item">
          <label>状态</label>
          <select v-model="filters.status" class="form-input form-select">
            <option value="">全部</option>
            <option value="1">进行中</option>
            <option value="0">已结束</option>
          </select>
        </div>
        <div class="filter-actions">
          <button class="btn btn-secondary" @click="resetFilters">重置</button>
          <button class="btn btn-primary" @click="searchCoupons">搜索</button>
        </div>
      </div>
    </div>

    <div class="coupons-grid">
      <div v-for="coupon in coupons" :key="coupon.id" class="coupon-card card" :class="{ expired: coupon.status === 0 }">
        <div class="coupon-value">
          <span v-if="coupon.type === 1" class="price">
            <span class="unit">¥</span>{{ coupon.value }}
          </span>
          <span v-else class="discount">{{ (coupon.value * 10).toFixed(1) }}折</span>
          <span class="condition">{{ coupon.minAmount > 0 ? `满${coupon.minAmount}可用` : '无门槛' }}</span>
        </div>
        <div class="coupon-info">
          <div class="coupon-name">{{ coupon.name }}</div>
          <div class="coupon-time">{{ coupon.startTime }} - {{ coupon.endTime }}</div>
          <div class="coupon-stats">
            <span>已领取 {{ coupon.receivedCount }}/{{ coupon.totalCount }}</span>
          </div>
        </div>
        <div class="coupon-status">
          <span class="status-badge" :class="coupon.status === 1 ? 'success' : 'secondary'">
            {{ coupon.status === 1 ? '进行中' : '已结束' }}
          </span>
        </div>
        <div class="coupon-actions">
          <button class="btn btn-ghost btn-sm" @click="editCoupon(coupon)">编辑</button>
          <button class="btn btn-ghost btn-sm" @click="deleteCoupon(coupon)">删除</button>
        </div>
      </div>
    </div>

    <div v-if="showAddModal" class="modal-overlay active" @click.self="showAddModal = false">
      <div class="modal" style="max-width: 500px;">
        <div class="modal-header">
          <h3 class="modal-title">创建优惠券</h3>
          <button class="modal-close" @click="showAddModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label required">优惠券名称</label>
            <input v-model="couponForm.name" type="text" class="form-input" placeholder="如：新人专享券" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label required">优惠类型</label>
              <select v-model="couponForm.type" class="form-input form-select">
                <option value="1">满减券</option>
                <option value="2">折扣券</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label required">优惠金额</label>
              <input v-model="couponForm.value" type="number" class="form-input" placeholder="20" />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">使用门槛（满X元）</label>
            <input v-model="couponForm.minAmount" type="number" class="form-input" placeholder="0表示无门槛" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label required">开始时间</label>
              <input v-model="couponForm.startTime" type="date" class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label required">结束时间</label>
              <input v-model="couponForm.endTime" type="date" class="form-input" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label required">发行总量</label>
              <input v-model="couponForm.totalCount" type="number" class="form-input" placeholder="1000" />
            </div>
            <div class="form-group">
              <label class="form-label required">每人限领</label>
              <input v-model="couponForm.perUserLimit" type="number" class="form-input" placeholder="1" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showAddModal = false">取消</button>
          <button class="btn btn-primary" @click="saveCoupon">创建</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const showAddModal = ref(false)
const filters = reactive({ name: '', status: '' })
const couponForm = reactive({
  name: '', type: '1', value: '', minAmount: 0,
  startTime: '', endTime: '', totalCount: '', perUserLimit: 1
})

const coupons = ref([
  { id: 1, name: '新人专享券', type: 1, value: 20, minAmount: 100, startTime: '2024-09-01', endTime: '2024-09-30', totalCount: 10000, receivedCount: 5623, status: 1 },
  { id: 2, name: '满100减20', type: 1, value: 20, minAmount: 100, startTime: '2024-09-01', endTime: '2024-09-30', totalCount: 5000, receivedCount: 2341, status: 1 },
  { id: 3, name: '9折优惠券', type: 2, value: 0.9, minAmount: 200, startTime: '2024-08-01', endTime: '2024-08-31', totalCount: 3000, receivedCount: 3000, status: 0 },
  { id: 4, name: '无门槛10元券', type: 1, value: 10, minAmount: 0, startTime: '2024-09-01', endTime: '2024-09-15', totalCount: 2000, receivedCount: 456, status: 1 }
])

const resetFilters = () => { filters.name = ''; filters.status = '' }
const searchCoupons = () => {}
const editCoupon = (coupon) => { console.log('编辑:', coupon) }
const deleteCoupon = (coupon) => { if (confirm('确定删除？')) coupons.value = coupons.value.filter(c => c.id !== coupon.id) }
const saveCoupon = () => {
  coupons.value.push({ id: Date.now(), ...couponForm, receivedCount: 0, status: 1 })
  showAddModal.value = false
}
</script>

<style scoped>
.coupon-list { max-width: 1200px; }

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-lg);
  align-items: flex-end;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.filter-item label {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-secondary);
}

.filter-item .form-input { width: 160px; }

.filter-actions {
  display: flex;
  gap: var(--spacing-sm);
  margin-left: auto;
}

.coupons-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-lg);
  margin-top: var(--spacing-lg);
}

.coupon-card {
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  transition: all var(--transition-fast);
}

.coupon-card:hover {
  box-shadow: var(--shadow-md);
}

.coupon-card.expired {
  opacity: 0.6;
}

.coupon-value {
  text-align: center;
  padding: var(--spacing-lg);
  background: linear-gradient(135deg, var(--color-danger), #FF6B6B);
  border-radius: var(--border-radius);
  color: white;
}

.coupon-value .price {
  font-size: 36px;
  font-weight: 700;
}

.coupon-value .unit {
  font-size: 20px;
}

.coupon-value .discount {
  font-size: 32px;
  font-weight: 700;
}

.coupon-value .condition {
  display: block;
  font-size: var(--font-size-sm);
  margin-top: var(--spacing-xs);
  opacity: 0.9;
}

.coupon-name {
  font-weight: 600;
  color: var(--text-primary);
}

.coupon-time, .coupon-stats {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.coupon-stats {
  margin-top: var(--spacing-xs);
}

.coupon-actions {
  display: flex;
  gap: var(--spacing-sm);
  border-top: 1px solid var(--border-light);
  padding-top: var(--spacing-md);
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}
</style>
