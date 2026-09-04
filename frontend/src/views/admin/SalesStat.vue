<template>
  <div class="sales-stat">
    <div class="page-header">
      <div>
        <h1 class="page-title">销售统计</h1>
        <p class="page-subtitle">销售数据分析</p>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card"><div class="stat-icon primary"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></div><div class="stat-value">¥{{ totalSales.toLocaleString() }}</div><div class="stat-label">总销售额</div></div>
      <div class="stat-card"><div class="stat-icon success"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div><div class="stat-value">{{ totalOrders }}</div><div class="stat-label">总订单数</div></div>
      <div class="stat-card"><div class="stat-icon warning"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg></div><div class="stat-value">{{ totalUsers }}</div><div class="stat-label">支付用户数</div></div>
      <div class="stat-card"><div class="stat-icon danger"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></div><div class="stat-value">¥{{ avgOrderAmount.toFixed(2) }}</div><div class="stat-label">客单价</div></div>
    </div>

    <div class="card chart-card">
      <div class="card-header"><h3 class="card-title">销售趋势</h3></div>
      <div class="card-body">
        <div class="chart-placeholder">
          <svg class="chart-svg" viewBox="0 0 800 300" preserveAspectRatio="none">
            <defs><linearGradient id="grad" x1="0%" y1="0%" x2="0%" y2="100%"><stop offset="0%" stop-color="#0071E3" stop-opacity="0.3"/><stop offset="100%" stop-color="#0071E3" stop-opacity="0"/></linearGradient></defs>
            <path d="M 0 250 Q 100 220 200 180 T 400 120 T 600 80 T 800 40" fill="none" stroke="#0071E3" stroke-width="3"/>
            <path d="M 0 250 Q 100 220 200 180 T 400 120 T 600 80 T 800 40 L 800 300 L 0 300 Z" fill="url(#grad)"/>
          </svg>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const totalSales = ref(1256890)
const totalOrders = ref(3456)
const totalUsers = ref(1234)
const avgOrderAmount = ref(363.68)
</script>

<style scoped>
.sales-stat { max-width: 1200px; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

@media (max-width: 900px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }

.stat-card {
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  text-align: center;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--spacing-md);
}

.stat-icon.primary { background: var(--color-primary-light); color: var(--color-primary); }
.stat-icon.success { background: #E8F8ED; color: var(--color-success); }
.stat-icon.warning { background: #FFF4E5; color: var(--color-warning); }
.stat-icon.danger { background: #FFEBE9; color: var(--color-danger); }

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-top: var(--spacing-xs);
}

.chart-placeholder { height: 300px; }
.chart-svg { width: 100%; height: 100%; }
</style>
