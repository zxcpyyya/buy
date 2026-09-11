<template>
  <div class="category-page safe-area-top">
    <!-- 顶部搜索栏 -->
    <header class="header">
      <div class="search-bar">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/>
          <path d="m21 21-4.35-4.35"/>
        </svg>
        <input type="text" placeholder="搜索商品" v-model="searchKeyword" @keyup.enter="handleSearch" />
      </div>
    </header>

    <div class="category-container">
      <!-- 左侧分类导航 -->
      <aside class="category-nav">
        <div 
          v-for="category in categories" 
          :key="category.id"
          class="nav-item"
          :class="{ active: selectedCategory === category.id }"
          @click="selectCategory(category.id)"
        >
          <span class="nav-icon">{{ category.icon }}</span>
          <span class="nav-text">{{ category.name }}</span>
        </div>
      </aside>

      <!-- 右侧子分类和商品 -->
      <main class="category-content">
        <!-- 子分类 -->
        <section class="sub-categories" v-if="currentSubCategories.length > 0">
          <div class="sub-category-grid">
            <div 
              v-for="sub in currentSubCategories" 
              :key="sub.id"
              class="sub-category-item"
              @click="$router.push(`/category/${sub.id}`)"
            >
              <img :src="sub.image" :alt="sub.name" />
              <span>{{ sub.name }}</span>
            </div>
          </div>
        </section>

        <!-- 热门品牌 -->
        <section class="brands" v-if="currentBrands.length > 0">
          <h3 class="section-title">热门品牌</h3>
          <div class="brand-grid">
            <div v-for="brand in currentBrands" :key="brand.id" class="brand-item">
              <img :src="brand.logo" :alt="brand.name" />
              <span>{{ brand.name }}</span>
            </div>
          </div>
        </section>

        <!-- 推荐商品 -->
        <section class="recommend-products">
          <h3 class="section-title">为你推荐</h3>
          <div class="product-grid">
            <div 
              v-for="product in products" 
              :key="product.id"
              class="product-card"
              @click="$router.push(`/product/${product.id}`)"
            >
              <div class="product-image">
                <img :src="product.image || `https://picsum.photos/200/200?random=${product.id}`" :alt="product.name" />
              </div>
              <div class="product-info">
                <h4 class="product-name">{{ product.name }}</h4>
                <p class="product-desc">{{ product.description }}</p>
                <p class="product-price">¥{{ formatPrice(product.price) }}</p>
              </div>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-if="products.length === 0 && !loading" class="empty-state">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
              <rect x="3" y="3" width="18" height="18" rx="2"/>
              <path d="M3 9h18M9 21V9"/>
            </svg>
            <p>暂无相关商品</p>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const searchKeyword = ref('')
const selectedCategory = ref(1)
const loading = ref(false)
const products = ref([])

const categories = ref([
  { id: 1, name: 'iPhone', icon: '📱' },
  { id: 2, name: 'Mac', icon: '💻' },
  { id: 3, name: 'iPad', icon: '📲' },
  { id: 4, name: 'Watch', icon: '⌚' },
  { id: 5, name: 'AirPods', icon: '🎧' },
  { id: 6, name: '配件', icon: '🔌' },
  { id: 7, name: 'App', icon: '📲' },
  { id: 8, name: '服务', icon: '🎁' },
])

const subCategories = {
  1: [
    { id: 11, name: 'iPhone 16 Pro', image: 'https://picsum.photos/100/100?random=11' },
    { id: 12, name: 'iPhone 16', image: 'https://picsum.photos/100/100?random=12' },
    { id: 13, name: 'iPhone 15', image: 'https://picsum.photos/100/100?random=13' },
  ],
  2: [
    { id: 21, name: 'MacBook Pro', image: 'https://picsum.photos/100/100?random=21' },
    { id: 22, name: 'MacBook Air', image: 'https://picsum.photos/100/100?random=22' },
    { id: 23, name: 'iMac', image: 'https://picsum.photos/100/100?random=23' },
  ],
}

const brands = {
  1: [
    { id: 1, name: 'Apple', logo: 'https://picsum.photos/60/60?random=b1' },
    { id: 2, name: 'Belkin', logo: 'https://picsum.photos/60/60?random=b2' },
  ],
  2: [
    { id: 3, name: 'Apple', logo: 'https://picsum.photos/60/60?random=b3' },
  ],
}

const currentSubCategories = computed(() => subCategories[selectedCategory.value] || [])
const currentBrands = computed(() => brands[selectedCategory.value] || [])

const selectCategory = (id) => {
  selectedCategory.value = id
  fetchProducts()
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/category', query: { keyword: searchKeyword.value } })
  }
}

const formatPrice = (price) => {
  return Number(price || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const data = await request.get('/product/list', { 
      params: { pageNum: 1, pageSize: 20 } 
    })
    products.value = data?.records || []
  } catch (e) {
    console.error('获取商品失败', e)
    // 使用示例数据
    products.value = [
      { id: 1, name: 'iPhone 16 Pro Max 256GB', description: '钛金属设计，A18 Pro芯片', price: 9999, image: 'https://picsum.photos/200/200?random=101' },
      { id: 2, name: 'MacBook Pro 14寸 M3', description: 'M3 Pro芯片，18GB内存', price: 15999, image: 'https://picsum.photos/200/200?random=102' },
      { id: 3, name: 'iPad Pro 12.9寸', description: 'M2芯片，超视网膜XDR', price: 9299, image: 'https://picsum.photos/200/200?random=103' },
      { id: 4, name: 'Apple Watch Ultra 2', description: '钛金属表壳，精密双频GPS', price: 6499, image: 'https://picsum.photos/200/200?random=104' },
      { id: 5, name: 'AirPods Pro 2', description: '自适应音频，个性化空间音频', price: 1899, image: 'https://picsum.photos/200/200?random=105' },
      { id: 6, name: 'MagSafe充电器', description: '磁吸配件，无线快充', price: 329, image: 'https://picsum.photos/200/200?random=106' },
    ]
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, (newId) => {
  if (newId) {
    selectedCategory.value = parseInt(newId) || 1
    fetchProducts()
  }
}, { immediate: true })

onMounted(() => {
  fetchProducts()
})
</script>

<style lang="scss" scoped>
.category-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: saturate(180%) blur(20px);
  padding: 12px 16px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-full);
  color: var(--text-tertiary);
  
  input {
    flex: 1;
    border: none;
    background: transparent;
    font-size: 15px;
    outline: none;
    
    &::placeholder {
      color: var(--text-tertiary);
    }
  }
}

.category-container {
  display: flex;
  min-height: calc(100vh - 60px);
}

.category-nav {
  width: 90px;
  background: var(--bg-primary);
  overflow-y: auto;
  padding: 8px 0;
  position: sticky;
  top: 60px;
  height: calc(100vh - 60px);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 8px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 3px solid transparent;
  
  &.active {
    background: var(--bg-secondary);
    border-left-color: var(--apple-blue);
    
    .nav-text {
      color: var(--apple-blue);
      font-weight: 600;
    }
  }
  
  &:hover:not(.active) {
    background: var(--bg-secondary);
  }
}

.nav-icon {
  font-size: 24px;
}

.nav-text {
  font-size: 12px;
  color: var(--text-secondary);
  text-align: center;
}

.category-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary);
}

// 子分类
.sub-categories {
  margin-bottom: 24px;
}

.sub-category-grid {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 8px;
  
  &::-webkit-scrollbar {
    display: none;
  }
}

.sub-category-item {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.2s;
  
  img {
    width: 56px;
    height: 56px;
    border-radius: var(--radius-md);
    object-fit: cover;
  }
  
  span {
    font-size: 12px;
    color: var(--text-primary);
  }
  
  &:hover {
    transform: scale(1.02);
  }
}

// 品牌
.brands {
  margin-bottom: 24px;
}

.brand-grid {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 8px;
  
  &::-webkit-scrollbar {
    display: none;
  }
}

.brand-item {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  min-width: 80px;
  
  img {
    width: 40px;
    height: 40px;
    border-radius: var(--radius-sm);
    object-fit: cover;
  }
  
  span {
    font-size: 11px;
    color: var(--text-secondary);
  }
}

// 商品
.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.product-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }
}

.product-image {
  aspect-ratio: 1;
  background: var(--bg-secondary);
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.product-info {
  padding: 12px;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  font-size: 15px;
  font-weight: 600;
  color: var(--apple-red);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--text-tertiary);
  
  svg {
    margin-bottom: 16px;
    opacity: 0.5;
  }
  
  p {
    font-size: 15px;
  }
}
</style>
