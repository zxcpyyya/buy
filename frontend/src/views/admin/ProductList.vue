<template>
  <div class="product-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ isMerchant ? '我的商品' : '商品管理' }}</h1>
        <p class="page-subtitle">
          <span v-if="isMerchant">管理您的店铺商品</span>
          <span v-else>管理所有商品库存和上下架状态</span>
        </p>
      </div>
      <button v-if="hasPermission('product:add')" class="btn btn-primary" @click="goToAdd">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"/>
          <line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        添加商品
      </button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar card">
      <div class="filter-row">
        <div class="filter-item">
          <label>商品名称</label>
          <input
            v-model="filters.keyword"
            type="text"
            class="form-input"
            placeholder="搜索商品名称..."
          />
        </div>
        <div class="filter-item">
          <label>商品分类</label>
          <select v-model="filters.categoryId" class="form-input form-select">
            <option value="">全部分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </option>
          </select>
        </div>
        <div class="filter-item">
          <label>商品状态</label>
          <select v-model="filters.status" class="form-input form-select">
            <option value="">全部状态</option>
            <option value="1">上架</option>
            <option value="0">下架</option>
          </select>
        </div>
        <div class="filter-actions">
          <button class="btn btn-secondary" @click="resetFilters">重置</button>
          <button class="btn btn-primary" @click="searchProducts">搜索</button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-mini" :class="{ active: filters.status === '' }" @click="filters.status = ''; searchProducts()">
        <span class="stat-num">{{ stats.total }}</span>
        <span class="stat-text">全部商品</span>
      </div>
      <div class="stat-mini" :class="{ active: filters.status === '1' }" @click="filters.status = '1'; searchProducts()">
        <span class="stat-num">{{ stats.onSale }}</span>
        <span class="stat-text">上架中</span>
      </div>
      <div class="stat-mini" :class="{ active: filters.status === '0' }" @click="filters.status = '0'; searchProducts()">
        <span class="stat-num">{{ stats.offSale }}</span>
        <span class="stat-text">已下架</span>
      </div>
      <div class="stat-mini warning">
        <span class="stat-num">{{ stats.lowStock }}</span>
        <span class="stat-text">库存预警</span>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="table-container">
      <table class="table">
        <thead>
          <tr>
            <th style="width: 60px">
              <input type="checkbox" @change="toggleSelectAll" />
            </th>
            <th style="width: 80px">图片</th>
            <th>商品名称</th>
            <th style="width: 100px" v-if="!isMerchant">商家</th>
            <th style="width: 100px">分类</th>
            <th style="width: 100px">价格</th>
            <th style="width: 80px">库存</th>
            <th style="width: 80px">销量</th>
            <th style="width: 80px">状态</th>
            <th style="width: 180px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in products" :key="product.id">
            <td>
              <input type="checkbox" v-model="product.selected" />
            </td>
            <td>
              <div class="product-image">
                <img :src="product.image" :alt="product.name" />
              </div>
            </td>
            <td>
              <div class="product-name">
                <span class="name">{{ product.name }}</span>
                <span class="desc">{{ product.description }}</span>
              </div>
            </td>
            <td v-if="!isMerchant">
              <span class="merchant-tag">{{ product.merchantName }}</span>
            </td>
            <td>{{ product.categoryName }}</td>
            <td class="price">¥{{ product.price }}</td>
            <td>
              <span :class="{ 'low-stock': product.stock < 10 }">
                {{ product.stock }}
              </span>
            </td>
            <td>{{ product.sales }}</td>
            <td>
              <span class="status-badge" :class="product.status === 1 ? 'success' : 'secondary'">
                {{ product.status === 1 ? '上架' : '下架' }}
              </span>
            </td>
            <td>
              <div class="action-buttons">
                <button class="btn btn-ghost btn-sm" @click="viewProduct(product)">查看</button>
                <button v-if="hasPermission('product:edit')" class="btn btn-ghost btn-sm" @click="editProduct(product)">编辑</button>
                <button v-if="hasPermission('product:publish')" class="btn btn-ghost btn-sm" @click="toggleStatus(product)">
                  {{ product.status === 1 ? '下架' : '上架' }}
                </button>
                <!-- 商家不能删除商品 -->
                <button v-if="hasPermission('product:delete') && !isMerchant" class="btn btn-ghost btn-sm text-danger" @click="deleteProduct(product)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 空状态 -->
      <div v-if="products.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
            <line x1="3" y1="6" x2="21" y2="6"/>
            <path d="M16 10a4 4 0 0 1-8 0"/>
          </svg>
        </div>
        <div class="empty-title">暂无商品</div>
        <div class="empty-description">点击"添加商品"按钮来创建您的第一个商品</div>
        <button v-if="hasPermission('product:add')" class="btn btn-primary" @click="goToAdd">添加商品</button>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <div class="pagination-info">
        共 {{ total }} 条记录
      </div>
      <div class="pagination">
        <button class="pagination-item" :disabled="filters.page <= 1" @click="changePage(filters.page - 1)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
        </button>
        <button v-for="page in visiblePages" :key="page" class="pagination-item" :class="{ active: page === filters.page }" @click="changePage(page)">
          {{ page }}
        </button>
        <button class="pagination-item" :disabled="filters.page >= totalPages" @click="changePage(filters.page + 1)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { hasPermission, isMerchant as checkMerchant } from '@/utils/permission'

const router = useRouter()

// 用户角色判断
const isMerchant = computed(() => checkMerchant())

// 权限检查
const hasPermissionCheck = (perm) => hasPermission(perm)

// 筛选条件
const filters = reactive({
  keyword: '',
  categoryId: '',
  status: '',
  page: 1,
  pageSize: 10
})

// 商品分类
const categories = ref([
  { id: 1, name: '手机数码' },
  { id: 2, name: '电脑办公' },
  { id: 3, name: '服装鞋包' },
  { id: 4, name: '食品饮料' },
  { id: 5, name: '美妆护肤' }
])

// 统计
const stats = reactive({
  total: 42,
  onSale: 35,
  offSale: 7,
  lowStock: 8
})

// 商品列表
const products = ref([
  {
    id: 1,
    name: 'iPhone 15 Pro Max 256GB 深空黑',
    description: '全新A17 Pro芯片，钛金属设计',
    categoryName: '手机数码',
    merchantName: 'Apple官方旗舰店',
    price: 9999,
    originalPrice: 10999,
    stock: 50,
    sales: 1234,
    status: 1,
    image: 'https://picsum.photos/200/200?random=1'
  },
  {
    id: 2,
    name: 'MacBook Pro 14英寸 M3 Pro',
    description: '全新M3 Pro芯片，性能大幅提升',
    categoryName: '电脑办公',
    merchantName: 'Apple官方旗舰店',
    price: 16999,
    stock: 25,
    sales: 567,
    status: 1,
    image: 'https://picsum.photos/200/200?random=4'
  },
  {
    id: 3,
    name: 'Nike Air Jordan 1 Retro High',
    description: '经典复刻，芝加哥配色',
    categoryName: '服装鞋包',
    merchantName: 'Nike官方店',
    price: 1499,
    stock: 0,
    sales: 89,
    status: 0,
    image: 'https://picsum.photos/200/200?random=6'
  },
  {
    id: 4,
    name: '商家自营商品A',
    description: '商家自有商品',
    categoryName: '手机数码',
    merchantName: '我的店铺',
    price: 599,
    stock: 36,
    sales: 456,
    status: 1,
    image: 'https://picsum.photos/200/200?random=7'
  }
])

// 总记录数
const total = ref(42)
const totalPages = computed(() => Math.ceil(total.value / filters.pageSize))

// 可见的页码
const visiblePages = computed(() => {
  const pages = []
  const current = filters.page
  const total = totalPages.value
  let start = Math.max(1, current - 2)
  let end = Math.min(total, start + 4)
  if (end - start < 4) {
    start = Math.max(1, end - 4)
  }
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// 方法
const searchProducts = () => {
  filters.page = 1
  console.log('搜索:', filters, '商家ID:', isMerchant.value ? JSON.parse(localStorage.getItem('admin_user') || '{}').merchantId : null)
}

const resetFilters = () => {
  filters.keyword = ''
  filters.categoryId = ''
  filters.status = ''
  filters.page = 1
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  filters.page = page
}

const toggleSelectAll = (e) => {
  products.value.forEach(p => p.selected = e.target.checked)
}

const goToAdd = () => {
  router.push('/admin/product-add')
}

const viewProduct = (product) => {
  console.log('查看商品:', product)
}

const editProduct = (product) => {
  router.push(`/admin/product-edit/${product.id}`)
}

const toggleStatus = (product) => {
  product.status = product.status === 1 ? 0 : 1
  stats.onSale = products.value.filter(p => p.status === 1).length
  stats.offSale = products.value.filter(p => p.status === 0).length
}

const deleteProduct = (product) => {
  if (confirm('确定删除该商品吗？')) {
    products.value = products.value.filter(p => p.id !== product.id)
    total.value--
  }
}

onMounted(() => {
  // 商家用户只能看到自己的商品
  if (isMerchant.value) {
    // 过滤只显示商家自己的商品
    const merchantName = '我的店铺'
    products.value = products.value.filter(p => p.merchantName === merchantName)
    stats.total = products.value.length
    stats.onSale = products.value.filter(p => p.status === 1).length
    stats.offSale = products.value.filter(p => p.status === 0).length
    stats.lowStock = products.value.filter(p => p.stock < 10).length
  }
})
</script>

<style scoped>
.product-list {
  max-width: 1400px;
}

/* 统计行 */
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

.stat-mini:hover {
  border-color: var(--color-primary);
}

.stat-mini.active {
  background: var(--color-primary-light);
  border-color: var(--color-primary);
}

.stat-mini.warning .stat-num {
  color: var(--color-warning);
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

/* 筛选栏 */
.filter-bar {
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

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

.filter-item .form-input {
  width: 160px;
}

.filter-actions {
  display: flex;
  gap: var(--spacing-sm);
  margin-left: auto;
}

/* 表格 */
.product-image {
  width: 60px;
  height: 60px;
  border-radius: var(--border-radius-sm);
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-name {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.product-name .name {
  font-weight: 500;
  color: var(--text-primary);
}

.product-name .desc {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.merchant-tag {
  font-size: var(--font-size-xs);
  padding: 2px 8px;
  background: var(--bg-tertiary);
  border-radius: 10px;
  color: var(--text-secondary);
}

.price {
  font-weight: 600;
  color: var(--color-danger);
}

.low-stock {
  color: var(--color-warning);
  font-weight: 500;
}

.text-danger {
  color: var(--color-danger) !important;
}

.action-buttons {
  display: flex;
  gap: var(--spacing-xs);
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

.pagination-info {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}
</style>
