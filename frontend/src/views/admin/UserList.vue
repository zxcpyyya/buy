<template>
  <div class="user-list">
    <div class="page-header">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-subtitle">查看和管理商城用户</p>
      </div>
    </div>

    <div class="filter-bar card">
      <div class="filter-row">
        <div class="filter-item">
          <label>用户ID/手机号</label>
          <input v-model="filters.keyword" type="text" class="form-input" placeholder="搜索用户..." />
        </div>
        <div class="filter-item">
          <label>注册时间</label>
          <div class="date-range">
            <input v-model="filters.startDate" type="date" class="form-input" />
            <span>-</span>
            <input v-model="filters.endDate" type="date" class="form-input" />
          </div>
        </div>
        <div class="filter-actions">
          <button class="btn btn-secondary" @click="resetFilters">重置</button>
          <button class="btn btn-primary" @click="searchUsers">搜索</button>
        </div>
      </div>
    </div>

    <div class="table-container">
      <table class="table">
        <thead>
          <tr>
            <th>用户</th>
            <th>手机号</th>
            <th>会员等级</th>
            <th>积分</th>
            <th>订单数</th>
            <th>注册时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>
              <div class="user-cell">
                <div class="user-avatar">{{ user.nickname?.charAt(0) || 'U' }}</div>
                <div class="user-info">
                  <div class="user-name">{{ user.nickname || user.username }}</div>
                  <div class="user-id">ID: {{ user.id }}</div>
                </div>
              </div>
            </td>
            <td>{{ user.phone }}</td>
            <td>
              <span class="level-badge" :class="'level-' + user.level">{{ user.levelName }}</span>
            </td>
            <td>{{ user.points?.toLocaleString() }}</td>
            <td>{{ user.orderCount }}</td>
            <td>{{ user.createTime }}</td>
            <td>
              <span class="status-badge" :class="user.status === 1 ? 'success' : 'danger'">
                {{ user.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>
              <button class="btn btn-ghost btn-sm" @click="viewUser(user)">详情</button>
              <button class="btn btn-ghost btn-sm" @click="toggleStatus(user)">
                {{ user.status === 1 ? '禁用' : '启用' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination-wrapper">
      <div class="pagination-info">共 {{ total }} 条记录</div>
      <div class="pagination">
        <button class="pagination-item" :disabled="filters.page <= 1" @click="changePage(filters.page - 1)">‹</button>
        <button v-for="p in visiblePages" :key="p" class="pagination-item" :class="{ active: p === filters.page }" @click="changePage(p)">{{ p }}</button>
        <button class="pagination-item" :disabled="filters.page >= totalPages" @click="changePage(filters.page + 1)">›</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

const filters = reactive({ keyword: '', startDate: '', endDate: '', page: 1, pageSize: 10 })
const total = ref(128)
const totalPages = computed(() => Math.ceil(total.value / filters.pageSize))
const visiblePages = computed(() => {
  const pages = []
  let start = Math.max(1, filters.page - 2)
  let end = Math.min(totalPages.value, start + 4)
  if (end - start < 4) start = Math.max(1, end - 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const users = ref([
  { id: 1001, username: 'user001', nickname: '张三', phone: '138****1234', level: 3, levelName: '黄金会员', points: 12580, orderCount: 45, createTime: '2024-01-15', status: 1 },
  { id: 1002, username: 'user002', nickname: '李四', phone: '139****5678', level: 2, levelName: '白银会员', points: 5680, orderCount: 23, createTime: '2024-02-20', status: 1 },
  { id: 1003, username: 'user003', nickname: '王五', phone: '137****9012', level: 4, levelName: '钻石会员', points: 45800, orderCount: 156, createTime: '2023-11-08', status: 1 },
  { id: 1004, username: 'user004', nickname: '赵六', phone: '136****3456', level: 1, levelName: '普通用户', points: 1200, orderCount: 5, createTime: '2024-08-01', status: 0 },
  { id: 1005, username: 'user005', nickname: '钱七', phone: '135****7890', level: 2, levelName: '白银会员', points: 8900, orderCount: 34, createTime: '2024-03-12', status: 1 }
])

const resetFilters = () => { filters.keyword = ''; filters.startDate = ''; filters.endDate = '' }
const searchUsers = () => { filters.page = 1 }
const changePage = (page) => { if (page >= 1 && page <= totalPages.value) filters.page = page }
const viewUser = (user) => { console.log('查看用户:', user) }
const toggleStatus = (user) => { user.status = user.status === 1 ? 0 : 1 }
</script>

<style scoped>
.user-list { max-width: 1200px; }

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

.date-range {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.date-range .form-input { width: 130px; }

.filter-actions {
  display: flex;
  gap: var(--spacing-sm);
  margin-left: auto;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-info));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
}

.user-name {
  font-weight: 500;
  color: var(--text-primary);
}

.user-id {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.level-badge {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: var(--font-size-xs);
  font-weight: 500;
}

.level-1 { background: var(--bg-tertiary); color: var(--text-secondary); }
.level-2 { background: #E8F8ED; color: var(--color-success); }
.level-3 { background: #FFF4E5; color: var(--color-warning); }
.level-4 { background: #F5E6FF; color: #9C27B0; }

.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg);
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  border: 1px solid var(--border-light);
  margin-top: var(--spacing-lg);
}

.pagination-info {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}
</style>
