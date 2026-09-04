<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">仪表盘</h1>
      <p class="page-subtitle">欢迎回来，{{ userInfo.nickname || userInfo.username }}</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" @click="goToOrders">
        <div class="stat-icon primary">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.todayOrders }}</div>
          <div class="stat-label">今日订单</div>
          <div class="stat-change up">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="18 15 12 9 6 15"/>
            </svg>
            {{ stats.orderGrowth }}%
          </div>
        </div>
      </div>

      <div class="stat-card" @click="goToProducts">
        <div class="stat-icon success">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
            <line x1="3" y1="6" x2="21" y2="6"/>
            <path d="M16 10a4 4 0 0 1-8 0"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(stats.totalSales) }}</div>
          <div class="stat-label">今日销售额</div>
          <div class="stat-change up">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="18 15 12 9 6 15"/>
            </svg>
            {{ stats.salesGrowth }}%
          </div>
        </div>
      </div>

      <div class="stat-card" @click="goToUsers">
        <div class="stat-icon warning">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.newUsers }}</div>
          <div class="stat-label">新增用户</div>
          <div class="stat-change up">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="18 15 12 9 6 15"/>
            </svg>
            {{ stats.userGrowth }}%
          </div>
        </div>
      </div>

      <div class="stat-card" @click="goToProducts">
        <div class="stat-icon danger">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.lowStockProducts }}</div>
          <div class="stat-label">库存预警</div>
          <div class="stat-change down">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
            {{ stats.stockWarning }}
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 销售趋势 -->
      <div class="card chart-card">
        <div class="card-header">
          <h3 class="card-title">销售趋势</h3>
          <div class="chart-actions">
            <button
              v-for="period in periods"
              :key="period.value"
              class="chart-btn"
              :class="{ active: selectedPeriod === period.value }"
              @click="selectedPeriod = period.value"
            >
              {{ period.label }}
            </button>
          </div>
        </div>
        <div class="card-body">
          <div class="chart-container" ref="salesChartRef">
            <div v-if="loading" class="chart-loading">
              <div class="loading"></div>
            </div>
            <div v-else class="chart-placeholder">
              <svg class="chart-svg" viewBox="0 0 800 300" preserveAspectRatio="none">
                <!-- 渐变填充 -->
                <defs>
                  <linearGradient id="chartGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stop-color="#0071E3" stop-opacity="0.3"/>
                    <stop offset="100%" stop-color="#0071E3" stop-opacity="0"/>
                  </linearGradient>
                </defs>
                <!-- 网格线 -->
                <line x1="0" y1="75" x2="800" y2="75" stroke="#E5E5EA" stroke-width="1"/>
                <line x1="0" y1="150" x2="800" y2="150" stroke="#E5E5EA" stroke-width="1"/>
                <line x1="0" y1="225" x2="800" y2="225" stroke="#E5E5EA" stroke-width="1"/>
                <!-- 数据线 -->
                <path
                  d="M 0 250 Q 50 240 100 220 T 200 180 T 300 150 T 400 120 T 500 90 T 600 70 T 700 50 T 800 30"
                  fill="none"
                  stroke="#0071E3"
                  stroke-width="3"
                  stroke-linecap="round"
                />
                <!-- 填充区域 -->
                <path
                  d="M 0 250 Q 50 240 100 220 T 200 180 T 300 150 T 400 120 T 500 90 T 600 70 T 700 50 T 800 30 L 800 300 L 0 300 Z"
                  fill="url(#chartGradient)"
                />
                <!-- 数据点 -->
                <circle cx="100" cy="220" r="5" fill="#0071E3"/>
                <circle cx="200" cy="180" r="5" fill="#0071E3"/>
                <circle cx="300" cy="150" r="5" fill="#0071E3"/>
                <circle cx="400" cy="120" r="5" fill="#0071E3"/>
                <circle cx="500" cy="90" r="5" fill="#0071E3"/>
                <circle cx="600" cy="70" r="5" fill="#0071E3"/>
                <circle cx="700" cy="50" r="5" fill="#0071E3"/>
                <circle cx="800" cy="30" r="5" fill="#0071E3"/>
              </svg>
              <div class="chart-labels">
                <span>周一</span>
                <span>周二</span>
                <span>周三</span>
                <span>周四</span>
                <span>周五</span>
                <span>周六</span>
                <span>周日</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 订单状态分布 -->
      <div class="card chart-card">
        <div class="card-header">
          <h3 class="card-title">订单状态</h3>
        </div>
        <div class="card-body">
          <div class="order-stats">
            <div class="order-stat-item">
              <div class="order-stat-header">
                <span class="order-stat-dot pending"></span>
                <span class="order-stat-label">待支付</span>
              </div>
              <div class="order-stat-value">{{ orderStats.pending }}</div>
              <div class="order-stat-bar">
                <div class="order-stat-fill pending" :style="{ width: getPercentage(orderStats.pending) + '%' }"></div>
              </div>
            </div>

            <div class="order-stat-item">
              <div class="order-stat-header">
                <span class="order-stat-dot processing"></span>
                <span class="order-stat-label">处理中</span>
              </div>
              <div class="order-stat-value">{{ orderStats.processing }}</div>
              <div class="order-stat-bar">
                <div class="order-stat-fill processing" :style="{ width: getPercentage(orderStats.processing) + '%' }"></div>
              </div>
            </div>

            <div class="order-stat-item">
              <div class="order-stat-header">
                <span class="order-stat-dot shipped"></span>
                <span class="order-stat-label">已发货</span>
              </div>
              <div class="order-stat-value">{{ orderStats.shipped }}</div>
              <div class="order-stat-bar">
                <div class="order-stat-fill shipped" :style="{ width: getPercentage(orderStats.shipped) + '%' }"></div>
              </div>
            </div>

            <div class="order-stat-item">
              <div class="order-stat-header">
                <span class="order-stat-dot completed"></span>
                <span class="order-stat-label">已完成</span>
              </div>
              <div class="order-stat-value">{{ orderStats.completed }}</div>
              <div class="order-stat-bar">
                <div class="order-stat-fill completed" :style="{ width: getPercentage(orderStats.completed) + '%' }"></div>
              </div>
            </div>

            <div class="order-stat-item">
              <div class="order-stat-header">
                <span class="order-stat-dot cancelled"></span>
                <span class="order-stat-label">已取消</span>
              </div>
              <div class="order-stat-value">{{ orderStats.cancelled }}</div>
              <div class="order-stat-bar">
                <div class="order-stat-fill cancelled" :style="{ width: getPercentage(orderStats.cancelled) + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作和待办事项 -->
    <div class="bottom-grid">
      <!-- 快捷操作 -->
      <div class="card quick-actions-card">
        <div class="card-header">
          <h3 class="card-title">快捷操作</h3>
        </div>
        <div class="card-body">
          <div class="quick-actions">
            <div class="quick-action-item" @click="goToAddProduct">
              <div class="quick-action-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="12" y1="5" x2="12" y2="19"/>
                  <line x1="5" y1="12" x2="19" y2="12"/>
                </svg>
              </div>
              <span>添加商品</span>
            </div>
            <div class="quick-action-item" @click="goToOrders">
              <div class="quick-action-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                </svg>
              </div>
              <span>处理订单</span>
            </div>
            <div class="quick-action-item" @click="goToCoupons">
              <div class="quick-action-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                  <line x1="7" y1="7" x2="7.01" y2="7"/>
                </svg>
              </div>
              <span>创建优惠券</span>
            </div>
            <div class="quick-action-item" @click="goToUsers">
              <div class="quick-action-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
              </div>
              <span>用户管理</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 待办事项 -->
      <div class="card todo-card">
        <div class="card-header">
          <h3 class="card-title">待办事项</h3>
          <span class="todo-count">{{ todos.length }} 项</span>
        </div>
        <div class="card-body">
          <div class="todo-list">
            <div
              v-for="todo in todos"
              :key="todo.id"
              class="todo-item"
              :class="{ completed: todo.completed }"
            >
              <label class="todo-checkbox">
                <input type="checkbox" v-model="todo.completed" />
                <span class="checkmark"></span>
              </label>
              <div class="todo-content">
                <div class="todo-text">{{ todo.text }}</div>
                <div class="todo-meta">{{ todo.time }}</div>
              </div>
              <div class="todo-priority" :class="todo.priority">{{ todo.priorityText }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 最新订单 -->
      <div class="card recent-orders-card">
        <div class="card-header">
          <h3 class="card-title">最新订单</h3>
          <button class="btn btn-ghost btn-sm" @click="goToOrders">查看全部</button>
        </div>
        <div class="card-body">
          <div class="recent-orders-list">
            <div v-for="order in recentOrders" :key="order.id" class="recent-order-item">
              <div class="order-info">
                <div class="order-id">#{{ order.id }}</div>
                <div class="order-user">{{ order.user }}</div>
              </div>
              <div class="order-amount">¥{{ order.amount }}</div>
              <div class="order-status" :class="order.status">{{ order.statusText }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 用户信息
const userInfo = ref(JSON.parse(localStorage.getItem('admin_user') || '{}'))

// 加载状态
const loading = ref(false)

// 时间筛选
const periods = [
  { label: '今日', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '本年', value: 'year' }
]
const selectedPeriod = ref('week')

// 统计数据
const stats = reactive({
  todayOrders: 156,
  totalSales: 45680,
  newUsers: 42,
  lowStockProducts: 8,
  orderGrowth: 12.5,
  salesGrowth: 8.3,
  userGrowth: 15.2,
  stockWarning: 3
})

// 订单状态统计
const orderStats = reactive({
  pending: 23,
  processing: 45,
  shipped: 67,
  completed: 234,
  cancelled: 12
})

// 待办事项
const todos = ref([
  { id: 1, text: '审核新商家入驻申请', time: '2小时后', priority: 'high', priorityText: '紧急', completed: false },
  { id: 2, text: '处理积压订单 (15笔)', time: '今天内', priority: 'high', priorityText: '紧急', completed: false },
  { id: 3, text: '更新首页 Banner', time: '明天', priority: 'normal', priorityText: '普通', completed: false },
  { id: 4, text: '整理本周销售报告', time: '周五', priority: 'low', priorityText: '低', completed: false },
  { id: 5, text: '优化商品搜索算法', time: '下周', priority: 'low', priorityText: '低', completed: true }
])

// 最新订单
const recentOrders = ref([
  { id: '20240904001', user: '张*明', amount: '299.00', status: 'pending', statusText: '待支付' },
  { id: '20240904002', user: '李*华', amount: '1,299.00', status: 'processing', statusText: '处理中' },
  { id: '20240904003', user: '王*丽', amount: '599.00', status: 'shipped', statusText: '已发货' },
  { id: '20240904004', user: '赵*强', amount: '2,899.00', status: 'completed', statusText: '已完成' },
  { id: '20240904005', user: '刘*芳', amount: '129.00', status: 'completed', statusText: '已完成' }
])

// 方法
const formatNumber = (num) => {
  return num.toLocaleString('zh-CN')
}

const getPercentage = (value) => {
  const total = Object.values(orderStats).reduce((sum, v) => sum + v, 0)
  return total > 0 ? (value / total * 100).toFixed(1) : 0
}

const goToOrders = () => router.push('/admin/order-list')
const goToProducts = () => router.push('/admin/product-list')
const goToUsers = () => router.push('/admin/user-list')
const goToCoupons = () => router.push('/admin/coupon-list')
const goToAddProduct = () => router.push('/admin/product-add')

onMounted(() => {
  // 模拟加载数据
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  gap: var(--spacing-lg);
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.primary {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.stat-icon.success {
  background: #E8F8ED;
  color: var(--color-success);
}

.stat-icon.warning {
  background: #FFF4E5;
  color: var(--color-warning);
}

.stat-icon.danger {
  background: #FFEBE9;
  color: var(--color-danger);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-top: var(--spacing-xs);
}

.stat-change {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-sm);
  margin-top: var(--spacing-sm);
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 500;
}

.stat-change.up {
  color: var(--color-success);
  background: #E8F8ED;
}

.stat-change.down {
  color: var(--color-danger);
  background: #FFEBE9;
}

/* 图表区域 */
.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

@media (max-width: 1000px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  min-height: 380px;
}

.chart-actions {
  display: flex;
  gap: var(--spacing-xs);
}

.chart-btn {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: var(--font-size-sm);
  font-weight: 500;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.chart-btn:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.chart-btn.active {
  background: var(--color-primary);
  color: white;
}

.chart-container {
  height: 280px;
  position: relative;
}

.chart-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-svg {
  flex: 1;
  width: 100%;
}

.chart-labels {
  display: flex;
  justify-content: space-between;
  padding: 0 var(--spacing-lg);
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

/* 订单状态 */
.order-stats {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.order-stat-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.order-stat-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.order-stat-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.order-stat-dot.pending { background: var(--color-warning); }
.order-stat-dot.processing { background: var(--color-primary); }
.order-stat-dot.shipped { background: var(--color-info); }
.order-stat-dot.completed { background: var(--color-success); }
.order-stat-dot.cancelled { background: var(--text-tertiary); }

.order-stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  flex: 1;
}

.order-stat-value {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
}

.order-stat-bar {
  height: 6px;
  background: var(--bg-tertiary);
  border-radius: 3px;
  overflow: hidden;
}

.order-stat-fill {
  height: 100%;
  border-radius: 3px;
  transition: width var(--transition-normal);
}

.order-stat-fill.pending { background: var(--color-warning); }
.order-stat-fill.processing { background: var(--color-primary); }
.order-stat-fill.shipped { background: var(--color-info); }
.order-stat-fill.completed { background: var(--color-success); }
.order-stat-fill.cancelled { background: var(--text-tertiary); }

/* 底部网格 */
.bottom-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1.5fr;
  gap: var(--spacing-lg);
}

@media (max-width: 1200px) {
  .bottom-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 800px) {
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}

/* 快捷操作 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg);
  border-radius: var(--border-radius);
  background: var(--bg-tertiary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.quick-action-item:hover {
  background: var(--color-primary-light);
}

.quick-action-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.quick-action-item span:last-child {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-primary);
}

/* 待办事项 */
.todo-count {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.todo-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  border-radius: var(--border-radius-sm);
  background: var(--bg-tertiary);
  transition: all var(--transition-fast);
}

.todo-item:hover {
  background: var(--bg-hover);
}

.todo-item.completed {
  opacity: 0.6;
}

.todo-item.completed .todo-text {
  text-decoration: line-through;
}

.todo-checkbox {
  position: relative;
  cursor: pointer;
}

.todo-checkbox input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
}

.checkmark {
  width: 20px;
  height: 20px;
  border: 2px solid var(--border-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.todo-checkbox input:checked + .checkmark {
  background: var(--color-success);
  border-color: var(--color-success);
}

.todo-checkbox input:checked + .checkmark::after {
  content: '';
  width: 6px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg) translateY(-1px);
}

.todo-content {
  flex: 1;
}

.todo-text {
  font-size: var(--font-size-base);
  color: var(--text-primary);
  line-height: 1.4;
}

.todo-meta {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  margin-top: 4px;
}

.todo-priority {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.todo-priority.high {
  background: #FFEBE9;
  color: var(--color-danger);
}

.todo-priority.normal {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.todo-priority.low {
  background: #E8F8ED;
  color: var(--color-success);
}

/* 最新订单 */
.recent-orders-list {
  display: flex;
  flex-direction: column;
}

.recent-order-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-md) 0;
  border-bottom: 1px solid var(--border-light);
  gap: var(--spacing-md);
}

.recent-order-item:last-child {
  border-bottom: none;
}

.order-info {
  flex: 1;
}

.order-id {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.order-user {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.order-amount {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--text-primary);
}

.order-status {
  font-size: var(--font-size-xs);
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 12px;
}

.order-status.pending {
  background: #FFF4E5;
  color: var(--color-warning);
}

.order-status.processing {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.order-status.shipped {
  background: #E5F1FB;
  color: var(--color-info);
}

.order-status.completed {
  background: #E8F8ED;
  color: var(--color-success);
}
</style>
