<template>
  <div class="user-stat">
    <div class="page-header">
      <div><h1 class="page-title">用户统计</h1><p class="page-subtitle">用户数据分析</p></div>
    </div>
    <div class="stats-grid">
      <div class="stat-card"><div class="stat-icon primary"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg></div><div class="stat-value">{{ totalUsers }}</div><div class="stat-label">总用户数</div></div>
      <div class="stat-card"><div class="stat-icon success"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg></div><div class="stat-value">{{ newUsers }}</div><div class="stat-label">本月新增</div></div>
      <div class="stat-card"><div class="stat-icon warning"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div><div class="stat-value">{{ activeUsers }}</div><div class="stat-label">活跃用户</div></div>
      <div class="stat-card"><div class="stat-icon danger"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></div><div class="stat-value">¥{{ avgAmount.toFixed(2) }}</div><div class="stat-label">人均消费</div></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const totalUsers = ref(12345)
const newUsers = ref(456)
const activeUsers = ref(2345)
const avgAmount = ref(568.90)
</script>

<style scoped>
.user-stat { max-width: 1200px; }
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-xl); }
.stat-card { background: var(--bg-secondary); border-radius: var(--border-radius); padding: var(--spacing-lg); box-shadow: var(--shadow-card); border: 1px solid var(--border-light); text-align: center; }
.stat-icon { width: 48px; height: 48px; border-radius: var(--border-radius); display: flex; align-items: center; justify-content: center; margin: 0 auto var(--spacing-md); }
.stat-icon.primary { background: var(--color-primary-light); color: var(--color-primary); }
.stat-icon.success { background: #E8F8ED; color: var(--color-success); }
.stat-icon.warning { background: #FFF4E5; color: var(--color-warning); }
.stat-icon.danger { background: #FFEBE9; color: var(--color-danger); }
.stat-value { font-size: 24px; font-weight: 600; color: var(--text-primary); }
.stat-label { font-size: var(--font-size-sm); color: var(--text-secondary); margin-top: var(--spacing-xs); }
</style>
