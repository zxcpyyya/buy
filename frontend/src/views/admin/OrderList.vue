<template>
  <div class="order-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ isMerchant ? '我的订单' : '订单管理' }}</h1>
        <p class="page-subtitle">
          <span v-if="isMerchant">管理您的店铺订单</span>
          <span v-else>查看和处理所有订单</span>
        </p>
      </div>
      <!-- 导出按钮 -->
      <button v-if="hasPermission('order:export') && !isMerchant" class="btn btn-secondary">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
          <polyline points="7 10 12 15 17 10"/>
          <line x1="12" y1="15" x2="12" y2="3"/>
        </svg>
        导出订单
      </button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-mini" :class="{ active: statusFilter === '' }" @click="statusFilter = ''">
        <span class="stat-num">{{ stats.total }}</span>
        <span class="stat-text">全部订单</span>
      </div>
      <div class="stat-mini" :class="{ active: statusFilter === '0' }" @click="statusFilter = '0'">
        <span class="stat-num">{{ stats.pending }}</span>
        <span class="stat-text">待支付</span>
      </div>
      <div class="stat-mini" :class="{ active: statusFilter === '1' }" @click="statusFilter = '1'">
        <span class="stat-num">{{ stats.paid }}</span>
        <span class="stat-text">已支付</span>
      </div>
      <div class="stat-mini" :class="{ active: statusFilter === '2' }" @click="statusFilter = '2'">
        <span class="stat-num">{{ stats.shipped }}</span>
        <span class="stat-text">已发货</span>
      </div>
      <div class="stat-mini" :class="{ active: statusFilter === '3' }" @click="statusFilter = '3'">
        <span class="stat-num">{{ stats.completed }}</span>
        <span class="stat-text">已完成</span>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar card">
      <div class="filter-row">
        <div class="filter-item">
          <label>订单号</label>
          <input v-model="filters.orderNo" type="text" class="form-input" placeholder="请输入订单号" />
        </div>
        <div class="filter-item" v-if="!isMerchant">
          <label>商家</label>
          <select v-model="filters.merchantId" class="form-input form-select">
            <option value="">全部商家</option>
            <option v-for="m in merchants" :key="m.id" :value="m.id">{{ m.name }}</option>
          </select>
        </div>
        <div class="filter-item">
          <label>时间范围</label>
          <div class="date-range">
            <input v-model="filters.startDate" type="date" class="form-input" />
            <span>-</span>
            <input v-model="filters.endDate" type="date" class="form-input" />
          </div>
        </div>
        <div class="filter-item">
          <label>订单状态</label>
          <select v-model="filters.status" class="form-input form-select">
            <option value="">全部状态</option>
            <option value="0">待支付</option>
            <option value="1">已支付</option>
            <option value="2">已发货</option>
            <option value="3">已完成</option>
            <option value="4">已取消</option>
          </select>
        </div>
        <div class="filter-actions">
          <button class="btn btn-secondary" @click="resetFilters">重置</button>
          <button class="btn btn-primary" @click="searchOrders">搜索</button>
        </div>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="orders-container">
      <div v-for="order in orders" :key="order.id" class="order-card card">
        <!-- 订单头部 -->
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">{{ formatTime(order.createTime) }}</span>
            <span v-if="!isMerchant" class="order-merchant">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
              </svg>
              {{ order.merchantName }}
            </span>
          </div>
          <div class="order-status" :class="'status-' + order.status">
            {{ getStatusText(order.status) }}
          </div>
        </div>

        <!-- 订单商品 -->
        <div class="order-items">
          <div v-for="item in order.items" :key="item.id" class="order-item">
            <img :src="item.image" class="item-image" />
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div class="item-spec">{{ item.spec }}</div>
            </div>
            <div class="item-price">¥{{ item.price }}</div>
            <div class="item-count">x{{ item.count }}</div>
            <div class="item-subtotal">¥{{ item.subtotal }}</div>
          </div>
        </div>

        <!-- 订单底部 -->
        <div class="order-footer">
          <div class="order-summary">
            <span class="buyer">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              {{ order.userName }}
            </span>
            <span class="phone">{{ order.userPhone }}</span>
            <span class="total">
              共{{ order.totalCount }}件商品，实付
              <strong>¥{{ order.totalAmount }}</strong>
            </span>
          </div>
          <div class="order-actions">
            <button class="btn btn-ghost btn-sm" @click="viewOrder(order)">详情</button>

            <!-- 商家操作 -->
            <template v-if="isMerchant">
              <button v-if="order.status === 1" class="btn btn-primary btn-sm" @click="showShipModal(order)">
                发货
              </button>
              <button v-if="order.status === 2" class="btn btn-ghost btn-sm" @click="viewLogistics(order)">
                查看物流
              </button>
            </template>

            <!-- 管理员操作 -->
            <template v-else>
              <button v-if="order.status === 0" class="btn btn-ghost btn-sm" @click="handleOrder(order, 'cancel')">
                取消
              </button>
              <button v-if="order.status === 1" class="btn btn-primary btn-sm" @click="showShipModal(order)">
                发货
              </button>
              <button v-if="order.status === 2" class="btn btn-ghost btn-sm" @click="viewLogistics(order)">
                查看物流
              </button>
            </template>
          </div>
        </div>

        <!-- 收货信息 -->
        <div v-if="order.status >= 2" class="shipping-info">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="1" y="3" width="15" height="13"/>
            <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/>
            <circle cx="5.5" cy="18.5" r="2.5"/>
            <circle cx="18.5" cy="18.5" r="2.5"/>
          </svg>
          <span>{{ order.shippingInfo }}</span>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="orders.length === 0" class="empty-state card">
        <div class="empty-icon">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
        </div>
        <div class="empty-title">暂无订单</div>
        <div class="empty-description">当前筛选条件下没有找到订单</div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <div class="pagination-info">
        共 {{ total }} 条记录
      </div>
      <div class="pagination">
        <button class="pagination-item" :disabled="filters.page <= 1" @click="changePage(filters.page - 1)">‹</button>
        <button v-for="p in visiblePages" :key="p" class="pagination-item" :class="{ active: p === filters.page }" @click="changePage(p)">{{ p }}</button>
        <button class="pagination-item" :disabled="filters.page >= totalPages" @click="changePage(filters.page + 1)">›</button>
      </div>
    </div>

    <!-- 发货弹窗 -->
    <div v-if="showShipDialog" class="modal-overlay active" @click.self="showShipDialog = false">
      <div class="modal" style="max-width: 500px;">
        <div class="modal-header">
          <h3 class="modal-title">订单发货</h3>
          <button class="modal-close" @click="showShipDialog = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label required">物流公司</label>
            <select v-model="shippingForm.company" class="form-input form-select">
              <option value="">请选择物流公司</option>
              <option value="SF">顺丰速运</option>
              <option value="YTO">圆通速递</option>
              <option value="ZTO">中通快递</option>
              <option value="STO">申通快递</option>
              <option value="YD">韵达快递</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label required">运单号</label>
            <input v-model="shippingForm.trackingNo" type="text" class="form-input" placeholder="请输入运单号" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showShipDialog = false">取消</button>
          <button class="btn btn-primary" @click="confirmShip">确认发货</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { hasPermission, isMerchant as checkMerchant } from '@/utils/permission'

// 角色判断
const isMerchant = computed(() => checkMerchant())

// 权限检查
const hasPermissionCheck = (perm) => hasPermission(perm)

// 筛选
const statusFilter = ref('')
const filters = reactive({
  orderNo: '',
  merchantId: '',
  startDate: '',
  endDate: '',
  status: '',
  page: 1,
  pageSize: 10
})

// 商家列表
const merchants = ref([
  { id: 1, name: 'Apple官方旗舰店' },
  { id: 2, name: '小米商城' },
  { id: 3, name: 'Nike官方店' },
  { id: 4, name: '我的店铺' }
])

// 统计数据
const stats = reactive({
  total: 128,
  pending: 12,
  paid: 35,
  shipped: 28,
  completed: 48,
  cancelled: 5
})

// 订单列表
const orders = ref([
  {
    id: 1,
    orderNo: 'ORD202409040001',
    createTime: '2024-09-04 10:30:25',
    status: 1,
    merchantName: 'Apple官方旗舰店',
    userName: '张明',
    userPhone: '138****1234',
    address: '北京市朝阳区建国路88号',
    totalCount: 1,
    totalAmount: 9998,
    items: [
      { id: 1, name: 'iPhone 15 Pro Max 256GB', spec: '深空黑', price: 9998, count: 1, subtotal: 9998, image: 'https://picsum.photos/80/80?random=1' }
    ]
  },
  {
    id: 2,
    orderNo: 'ORD202409040002',
    createTime: '2024-09-04 09:15:00',
    status: 2,
    merchantName: '我的店铺',
    userName: '李华',
    userPhone: '139****5678',
    address: '上海市浦东新区世纪大道1000号',
    totalCount: 2,
    totalAmount: 2599,
    items: [
      { id: 2, name: '商家商品A', spec: '默认规格', price: 1299, count: 1, subtotal: 1299, image: 'https://picsum.photos/80/80?random=8' },
      { id: 3, name: '商家商品B', spec: '默认规格', price: 1300, count: 1, subtotal: 1300, image: 'https://picsum.photos/80/80?random=9' }
    ]
  },
  {
    id: 3,
    orderNo: 'ORD202409030015',
    createTime: '2024-09-03 14:22:10',
    status: 3,
    merchantName: 'Nike官方店',
    userName: '王丽',
    userPhone: '137****9012',
    address: '广州市天河区珠江新城',
    totalCount: 1,
    totalAmount: 1499,
    items: [
      { id: 3, name: 'Nike Air Jordan 1', spec: 'Chicago 42码', price: 1499, count: 1, subtotal: 1499, image: 'https://picsum.photos/80/80?random=3' }
    ],
    shippingInfo: '顺丰速运 SF1234567890'
  }
])

// 总记录数
const total = ref(128)
const totalPages = computed(() => Math.ceil(total.value / filters.pageSize))

// 可见的页码
const visiblePages = computed(() => {
  const pages = []
  const current = filters.page
  const total = totalPages.value
  let start = Math.max(1, current - 2)
  let end = Math.min(total, start + 4)
  if (end - start < 4) start = Math.max(1, end - 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

// 弹窗状态
const showShipDialog = ref(false)
const selectedOrder = ref(null)

// 发货表单
const shippingForm = reactive({
  company: '',
  trackingNo: ''
})

// 方法
const getStatusText = (status) => {
  const statusMap = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }
  return statusMap[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').slice(0, 19)
}

const searchOrders = () => { filters.page = 1 }

const resetFilters = () => {
  filters.orderNo = ''
  filters.merchantId = ''
  filters.startDate = ''
  filters.endDate = ''
  filters.status = ''
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  filters.page = page
}

const viewOrder = (order) => {
  console.log('查看订单:', order)
}

const showShipModal = (order) => {
  selectedOrder.value = order
  shippingForm.company = ''
  shippingForm.trackingNo = ''
  showShipDialog.value = true
}

const confirmShip = () => {
  if (!shippingForm.company || !shippingForm.trackingNo) {
    alert('请填写完整的物流信息')
    return
  }
  selectedOrder.value.status = 2
  selectedOrder.value.shippingInfo = `${getCompanyName(shippingForm.company)} ${shippingForm.trackingNo}`
  showShipDialog.value = false
}

const getCompanyName = (code) => {
  const map = { 'SF': '顺丰速运', 'YTO': '圆通速递', 'ZTO': '中通快递', 'STO': '申通快递', 'YD': '韵达快递' }
  return map[code] || code
}

const viewLogistics = (order) => {
  console.log('查看物流:', order)
}

const handleOrder = (order, action) => {
  if (action === 'cancel') {
    if (confirm('确定要取消该订单吗？')) {
      order.status = 4
    }
  }
}

onMounted(() => {
  // 商家用户只能看到自己店铺的订单
  if (isMerchant.value) {
    orders.value = orders.value.filter(o => o.merchantName === '我的店铺')
    stats.total = orders.value.length
    stats.pending = orders.value.filter(o => o.status === 0).length
    stats.paid = orders.value.filter(o => o.status === 1).length
    stats.shipped = orders.value.filter(o => o.status === 2).length
    stats.completed = orders.value.filter(o => o.status === 3).length
  }
})
</script>

<style scoped>
.order-list { max-width: 1200px; }

/* 统计行 */
.stats-row {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
  flex-wrap: wrap;
}

.stat-mini {
  flex: 1;
  min-width: 100px;
  padding: var(--spacing-md);
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  border-radius: var(--border-radius);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.stat-mini:hover { border-color: var(--color-primary); }
.stat-mini.active { background: var(--color-primary-light); border-color: var(--color-primary); }

.stat-num { display: block; font-size: var(--font-size-2xl); font-weight: 600; color: var(--text-primary); }
.stat-text { font-size: var(--font-size-sm); color: var(--text-secondary); }

/* 筛选栏 */
.filter-bar { padding: var(--spacing-lg); margin-bottom: var(--spacing-lg); }
.filter-row { display: flex; flex-wrap: wrap; gap: var(--spacing-lg); align-items: flex-end; }
.filter-item { display: flex; flex-direction: column; gap: var(--spacing-xs); }
.filter-item label { font-size: var(--font-size-sm); font-weight: 500; color: var(--text-secondary); }
.filter-item .form-input { width: 150px; }
.date-range { display: flex; align-items: center; gap: var(--spacing-sm); }
.date-range .form-input { width: 120px; }
.filter-actions { display: flex; gap: var(--spacing-sm); margin-left: auto; }

/* 订单卡片 */
.orders-container { display: flex; flex-direction: column; gap: var(--spacing-md); }

.order-card { padding: 0; overflow: hidden; }

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-light);
}

.order-info { display: flex; gap: var(--spacing-lg); align-items: center; }
.order-no { font-weight: 600; color: var(--text-primary); }
.order-time { font-size: var(--font-size-sm); color: var(--text-tertiary); }
.order-merchant { display: flex; align-items: center; gap: 4px; font-size: var(--font-size-sm); color: var(--text-secondary); }

.order-status {
  font-size: var(--font-size-sm);
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 12px;
}

.status-0 { background: #FFF4E5; color: var(--color-warning); }
.status-1 { background: var(--color-primary-light); color: var(--color-primary); }
.status-2 { background: #E5F1FB; color: var(--color-info); }
.status-3 { background: #E8F8ED; color: var(--color-success); }
.status-4 { background: #F5F5F7; color: var(--text-tertiary); }

/* 订单商品 */
.order-items { padding: var(--spacing-md) var(--spacing-lg); }

.order-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm) 0;
}

.order-item + .order-item {
  border-top: 1px dashed var(--border-light);
  padding-top: var(--spacing-md);
  margin-top: var(--spacing-sm);
}

.item-image { width: 60px; height: 60px; border-radius: var(--border-radius-sm); object-fit: cover; }
.item-info { flex: 1; }
.item-name { font-weight: 500; color: var(--text-primary); }
.item-spec { font-size: var(--font-size-sm); color: var(--text-tertiary); }
.item-price { font-size: var(--font-size-sm); color: var(--text-secondary); }
.item-count { font-size: var(--font-size-sm); color: var(--text-tertiary); width: 40px; text-align: center; }
.item-subtotal { font-weight: 600; color: var(--text-primary); width: 80px; text-align: right; }

/* 订单底部 */
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  border-top: 1px solid var(--border-light);
}

.order-summary { display: flex; align-items: center; gap: var(--spacing-lg); font-size: var(--font-size-sm); color: var(--text-secondary); }
.order-summary .buyer { display: flex; align-items: center; gap: 4px; }
.order-summary strong { color: var(--color-danger); font-size: var(--font-size-lg); }

.order-actions { display: flex; gap: var(--spacing-sm); }

/* 物流信息 */
.shipping-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-lg);
  background: #E5F1FB;
  font-size: var(--font-size-sm);
  color: var(--color-info);
}

/* 分页 */
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

.pagination-info { font-size: var(--font-size-sm); color: var(--text-secondary); }
</style>
