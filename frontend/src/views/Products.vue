<template>
  <div class="products-page">
    <!-- 大标题区 -->
    <section class="page-hero">
      <div class="container-large">
        <h1 class="page-title">所有产品</h1>
        <p class="page-subtitle">探索每一件精心打造的精品</p>
      </div>
    </section>
    
    <!-- 工具栏：搜索 + 分类 + 排序 -->
    <section class="toolbar">
      <div class="container-large">
        <div class="toolbar-inner">
          <div class="toolbar-left">
            <div class="search-input">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="7"/>
                <path d="M21 21l-4.5-4.5"/>
              </svg>
              <input 
                v-model="query.keyword" 
                type="text" 
                placeholder="搜索产品"
                @keyup.enter="handleSearch"
              />
            </div>
            
            <div class="category-tabs">
              <span 
                class="tab" 
                :class="{ active: !query.categoryId }"
                @click="handleCategory(null)"
              >全部</span>
              <span 
                v-for="cat in categories" 
                :key="cat.id"
                class="tab"
                :class="{ active: query.categoryId === cat.id }"
                @click="handleCategory(cat.id)"
              >{{ cat.name }}</span>
            </div>
          </div>
          
          <div class="sort-dropdown">
            <select v-model="query.sortBy" @change="handleSearch">
              <option value="">默认排序</option>
              <option value="sales">销量优先</option>
              <option value="price">价格升序</option>
              <option value="createTime">最新上架</option>
            </select>
          </div>
        </div>
      </div>
    </section>
    
    <!-- 商品网格 -->
    <section class="products-content">
      <div class="container-large">
        <div v-loading="loading" class="products-grid" element-loading-text="加载中...">
          <div 
            v-for="product in products" 
            :key="product.id" 
            class="product-card card-hover"
            @click="$router.push(`/product/${product.id}`)"
          >
            <div class="product-image">
              <img :src="product.image" :alt="product.name" />
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-footer">
                <p class="product-price">¥{{ formatPrice(product.price) }}</p>
                <p class="product-sales">已售 {{ product.sales }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="!loading && products.length === 0" class="empty">
          <div class="empty-icon">
            <svg viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="28" cy="28" r="20"/>
              <path d="M44 44l12 12"/>
              <path d="M20 28h16M28 20v16"/>
            </svg>
          </div>
          <p class="empty-text">暂无相关商品</p>
        </div>
        
        <!-- 分页 -->
        <div v-if="total > pageSize" class="pagination">
          <button 
            class="page-btn" 
            :disabled="pageNum <= 1"
            @click="changePage(pageNum - 1)"
          >‹</button>
          <span class="page-info">{{ pageNum }} / {{ Math.ceil(total / pageSize) }}</span>
          <button 
            class="page-btn" 
            :disabled="pageNum >= Math.ceil(total / pageSize)"
            @click="changePage(pageNum + 1)"
          >›</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()

const products = ref([])
const categories = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)

const query = reactive({
  keyword: '',
  categoryId: null,
  sortBy: '',
  pageNum: 1,
  pageSize: 12
})

const formatPrice = (price) => {
  return Number(price).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const fetchCategories = async () => {
  try {
    const data = await request.get('/category/first')
    categories.value = data || []
  } catch (e) {
    console.error('获取分类失败', e)
  }
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = { ...query }
    const data = await request.get('/product/list', { params })
    products.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    console.error('获取商品失败', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  pageNum.value = 1
  fetchProducts()
}

const handleCategory = (id) => {
  query.categoryId = id
  handleSearch()
}

const changePage = (page) => {
  pageNum.value = page
  query.pageNum = page
  fetchProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(() => route.params.id, (val) => {
  if (val) {
    query.categoryId = Number(val)
    handleSearch()
  }
})

onMounted(() => {
  fetchCategories()
  // 如果是从分类页跳转过来
  if (route.params.id) {
    query.categoryId = Number(route.params.id)
  }
  fetchProducts()
})
</script>

<style lang="scss" scoped>
.products-page {
  padding-top: 44px; // 头部高度
}

.page-hero {
  padding: 80px 0 48px;
  text-align: center;
  background: linear-gradient(180deg, #fbfbfd 0%, #fff 100%);
}

.page-title {
  font-size: 56px;
  font-weight: 600;
  letter-spacing: -0.015em;
  color: #1d1d1f;
  margin-bottom: 8px;
  line-height: 1.07;
  
  @media (max-width: 833px) {
    font-size: 40px;
  }
}

.page-subtitle {
  font-size: 21px;
  color: #6e6e73;
  font-weight: 400;
}

// 工具栏
.toolbar {
  background: #fff;
  border-bottom: 1px solid #f5f5f7;
  padding: 16px 0;
  position: sticky;
  top: 44px;
  z-index: 100;
  backdrop-filter: saturate(180%) blur(20px);
  background: rgba(255, 255, 255, 0.92);
}

.toolbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 24px;
  flex: 1;
}

.search-input {
  display: flex;
  align-items: center;
  background: #f5f5f7;
  border-radius: 980px;
  padding: 8px 16px;
  gap: 8px;
  width: 240px;
  
  input {
    flex: 1;
    background: transparent;
    border: none;
    font-size: 14px;
    color: #1d1d1f;
    
    &::placeholder {
      color: #86868b;
    }
  }
}

.category-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  
  .tab {
    padding: 6px 14px;
    font-size: 14px;
    color: #1d1d1f;
    border-radius: 980px;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: #f5f5f7;
    }
    
    &.active {
      background: #1d1d1f;
      color: #fff;
    }
  }
}

.sort-dropdown {
  select {
    padding: 8px 16px;
    background: #fff;
    border: 1px solid #d2d2d7;
    border-radius: 8px;
    font-size: 14px;
    color: #1d1d1f;
    cursor: pointer;
  }
}

// 商品网格
.products-content {
  padding: 48px 0 80px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 400px;
  
  @media (max-width: 1170px) {
    grid-template-columns: repeat(3, 1fr);
  }
  
  @media (max-width: 833px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.product-card {
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.1, 0.25, 1);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08);
  }
}

.product-image {
  aspect-ratio: 1;
  background: #f5f5f7;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.6s cubic-bezier(0.25, 0.1, 0.25, 1);
  }
  
  .product-card:hover & img {
    transform: scale(1.05);
  }
}

.product-info {
  padding: 20px 24px 24px;
}

.product-name {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 13px;
  color: #6e6e73;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.product-price {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
}

.product-sales {
  font-size: 12px;
  color: #86868b;
}

// 空状态
.empty {
  text-align: center;
  padding: 80px 0;
  color: #86868b;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  color: #d2d2d7;
}

.empty-text {
  font-size: 17px;
}

// 分页
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 48px;
}

.page-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f5f5f7;
  color: #1d1d1f;
  font-size: 16px;
  transition: all 0.2s;
  
  &:hover:not(:disabled) {
    background: #e8e8ed;
  }
  
  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
}

.page-info {
  font-size: 14px;
  color: #6e6e73;
  min-width: 60px;
  text-align: center;
}
</style>
