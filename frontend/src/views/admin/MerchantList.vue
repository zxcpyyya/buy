<template>
  <div class="merchant-list">
    <div class="page-header">
      <div>
        <h1 class="page-title">商家管理</h1>
        <p class="page-subtitle">管理商家入驻和审核</p>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-mini">
        <span class="stat-num">{{ stats.total }}</span>
        <span class="stat-text">全部商家</span>
      </div>
      <div class="stat-mini active">
        <span class="stat-num">{{ stats.pending }}</span>
        <span class="stat-text">待审核</span>
      </div>
      <div class="stat-mini">
        <span class="stat-num">{{ stats.active }}</span>
        <span class="stat-text">已入驻</span>
      </div>
      <div class="stat-mini">
        <span class="stat-num">{{ stats.disabled }}</span>
        <span class="stat-text">已禁用</span>
      </div>
    </div>

    <div class="table-container">
      <table class="table">
        <thead>
          <tr>
            <th>商家信息</th>
            <th>联系人</th>
            <th>商品数</th>
            <th>订单数</th>
            <th>入驻时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="merchant in merchants" :key="merchant.id">
            <td>
              <div class="merchant-cell">
                <div class="merchant-logo">{{ merchant.name.charAt(0) }}</div>
                <div class="merchant-info">
                  <div class="merchant-name">{{ merchant.name }}</div>
                  <div class="merchant-code">{{ merchant.code }}</div>
                </div>
              </div>
            </td>
            <td>
              <div>{{ merchant.contact }}</div>
              <div class="text-secondary">{{ merchant.phone }}</div>
            </td>
            <td>{{ merchant.productCount }}</td>
            <td>{{ merchant.orderCount }}</td>
            <td>{{ merchant.createTime }}</td>
            <td>
              <span class="status-badge" :class="getStatusClass(merchant.status)">
                {{ getStatusText(merchant.status) }}
              </span>
            </td>
            <td>
              <button v-if="merchant.status === 2" class="btn btn-primary btn-sm" @click="auditMerchant(merchant, 1)">通过</button>
              <button v-if="merchant.status === 2" class="btn btn-danger btn-sm" @click="auditMerchant(merchant, 0)">拒绝</button>
              <button v-else class="btn btn-ghost btn-sm" @click="viewMerchant(merchant)">详情</button>
              <button v-if="merchant.status === 1" class="btn btn-ghost btn-sm" @click="toggleStatus(merchant)">禁用</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const stats = reactive({ total: 15, pending: 3, active: 11, disabled: 1 })

const merchants = ref([
  { id: 1, name: 'Apple官方旗舰店', code: 'MERCHANT001', contact: '张经理', phone: '139****0001', productCount: 45, orderCount: 1256, createTime: '2023-06-01', status: 1 },
  { id: 2, name: '小米商城', code: 'MERCHANT002', contact: '李经理', phone: '139****0002', productCount: 128, orderCount: 3456, createTime: '2023-08-15', status: 1 },
  { id: 3, name: 'Nike官方店', code: 'MERCHANT003', contact: '王经理', phone: '139****0003', productCount: 89, orderCount: 892, createTime: '2024-01-10', status: 1 },
  { id: 4, name: '雅诗兰黛官方旗舰店', code: 'MERCHANT004', contact: '赵经理', phone: '139****0004', productCount: 0, orderCount: 0, createTime: '2024-09-01', status: 2 },
  { id: 5, name: '联想官方店', code: 'MERCHANT005', contact: '钱经理', phone: '139****0005', productCount: 0, orderCount: 0, createTime: '2024-09-02', status: 2 }
])

const getStatusClass = (status) => ({ 0: 'danger', 1: 'success', 2: 'warning' }[status] || '')
const getStatusText = (status) => ({ 0: '已拒绝', 1: '已入驻', 2: '待审核' }[status] || '')

const auditMerchant = (merchant, status) => { merchant.status = status === 1 ? 1 : 0 }
const viewMerchant = (merchant) => { console.log('查看商家:', merchant) }
const toggleStatus = (merchant) => { merchant.status = merchant.status === 1 ? 0 : 1 }
</script>

<style scoped>
.merchant-list { max-width: 1200px; }

.stats-row {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.stat-mini {
  flex: 1;
  padding: var(--spacing-md);
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  border-radius: var(--border-radius);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.stat-mini:hover, .stat-mini.active {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}

.stat-num {
  display: block;
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--text-primary);
}

.stat-text {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.merchant-cell {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.merchant-logo {
  width: 40px;
  height: 40px;
  border-radius: var(--border-radius-sm);
  background: linear-gradient(135deg, #FF9500, #FF6B00);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
}

.merchant-name { font-weight: 500; color: var(--text-primary); }
.merchant-code { font-size: var(--font-size-xs); color: var(--text-tertiary); }
</style>
