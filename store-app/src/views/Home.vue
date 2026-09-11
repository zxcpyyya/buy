<template>
  <div class="home">
    <!-- 顶部导航 -->
    <header class="header safe-area-top">
      <div class="header-content">
        <h1 class="logo">Apple Store</h1>
        <div class="header-actions">
          <button class="icon-btn" @click="$router.push('/cart')">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="9" cy="21" r="1"/>
              <circle cx="20" cy="21" r="1"/>
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
            </svg>
            <span v-if="cartCount > 0" class="badge">{{ cartCount }}</span>
          </button>
        </div>
      </div>
    </header>

    <!-- Hero Section - 苹果风格巨幅展示 -->
    <section class="hero">
      <div class="hero-content">
        <p class="hero-eyebrow">{{ heroProduct.tag }}</p>
        <h2 class="hero-title">{{ heroProduct.name }}</h2>
        <p class="hero-subtitle">{{ heroProduct.slogan }}</p>
        <div class="hero-actions">
          <button class="btn btn-primary btn-lg" @click="buyNow">立即购买</button>
          <button class="btn-text" @click="learnMore">了解更多 ›</button>
        </div>
      </div>
      <div class="hero-image">
        <img :src="heroProduct.image" :alt="heroProduct.name" />
      </div>
    </section>

    <!-- 分类导航 -->
    <section class="categories">
      <div class="container">
        <h3 class="section-eyebrow">探索</h3>
        <h2 class="section-title">浏览各款产品</h2>
        <div class="category-grid">
          <div 
            v-for="category in categories" 
            :key="category.id"
            class="category-card"
            @click="$router.push(`/category/${category.id}`)"
          >
            <div class="category-icon">
              <img :src="category.icon" :alt="category.name" />
            </div>
            <h4 class="category-name">{{ category.name }}</h4>
            <p class="category-desc">{{ category.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门商品 -->
    <section class="products-section bg-secondary">
      <div class="container">
        <div class="section-header">
          <div>
            <h3 class="section-eyebrow">热门精选</h3>
            <h2 class="section-title">热销产品</h2>
          </div>
          <button class="btn-text" @click="$router.push('/category')">查看全部 ›</button>
        </div>
        
        <div class="products-scroll">
          <div 
            v-for="product in hotProducts" 
            :key="product.id"
            class="product-card"
            @click="$router.push(`/product/${product.id}`)"
          >
            <div class="product-image">
              <img :src="product.image || 'https://picsum.photos/300/300?random=' + product.id" :alt="product.name" />
            </div>
            <div class="product-info">
              <h4 class="product-name">{{ product.name }}</h4>
              <p class="product-desc">{{ product.description }}</p>
              <p class="product-price">¥{{ formatPrice(product.price) }}</p>
            </div>
            <button class="product-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"/>
                <line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 价值主张 -->
    <section class="values">
      <div class="container">
        <div class="values-grid">
          <div class="value-item">
            <div class="value-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="1" y="3" width="22" height="18" rx="2"/>
                <path d="M1 9h22"/>
              </svg>
            </div>
            <h4>免费送货</h4>
            <p>免费送货，无最低消费</p>
          </div>
          <div class="value-item">
            <div class="value-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                <path d="M9 12l2 2 4-4"/>
              </svg>
            </div>
            <h4>安全支付</h4>
            <p>安全加密，保护隐私</p>
          </div>
          <div class="value-item">
            <div class="value-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                <polyline points="9 22 9 12 15 12 15 22"/>
              </svg>
            </div>
            <h4>轻松退货</h4>
            <p>14天无理由退货</p>
          </div>
          <div class="value-item">
            <div class="value-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
            </div>
            <h4>专家支持</h4>
            <p>24/7 在线客服</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'

const hotProducts = ref([])
const cartCount = ref(0)

const heroProduct = ref({
  tag: '新品上市',
  name: 'iPhone 16 Pro',
  slogan: '钛金属设计，强得难以置信。',
  image: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-16-pro-finish-select-202409-6-3inch-naturaltitanium?wid=600&hei=600&fmt=jpeg&qlt=95&.v=UXpVcGEvTEFDVEtVSTUu'
})

const categories = ref([
  { id: 1, name: 'iPhone', desc: '卓越性能，极致体验', icon: 'https://via.placeholder.com/80x80/000000/FFFFFF?text=iPhone' },
  { id: 2, name: 'Mac', desc: '创意工作，无限可能', icon: 'https://via.placeholder.com/80x80/333333/FFFFFF?text=Mac' },
  { id: 3, name: 'iPad', desc: '强大功能，轻薄设计', icon: 'https://via.placeholder.com/80x80/666666/FFFFFF?text=iPad' },
  { id: 4, name: 'Watch', desc: '健康生活，智慧相伴', icon: 'https://via.placeholder.com/80x80/999999/FFFFFF?text=Watch' },
  { id: 5, name: 'AirPods', desc: '沉浸音效，无线自由', icon: 'https://via.placeholder.com/80x80/cccccc/333333?text=AirPods' },
  { id: 6, name: '配件', desc: '完美搭配，提升体验', icon: 'https://via.placeholder.com/80x80/eeeeee/333333?text=配件' },
])

const formatPrice = (price) => {
  return Number(price || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const buyNow = () => {
  if (hotProducts.value.length > 0) {
    window.location.href = `/product/${hotProducts.value[0].id}`
  }
}

const learnMore = () => {
  window.location.href = '/category'
}

const fetchHotProducts = async () => {
  try {
    const data = await request.get('/product/hot', { params: { limit: 10 } })
    hotProducts.value = data || []
  } catch (e) {
    console.error('获取热门商品失败', e)
    // 使用示例数据
    hotProducts.value = [
      { id: 1, name: 'iPhone 15 Pro Max', description: '钛金属边框，A17 Pro芯片', price: 9999, image: 'https://picsum.photos/300/300?random=1' },
      { id: 2, name: 'MacBook Pro 14', description: 'M3 Pro芯片，专业级性能', price: 15999, image: 'https://picsum.photos/300/300?random=2' },
      { id: 3, name: 'iPad Pro', description: 'M2芯片，超薄设计', price: 7999, image: 'https://picsum.photos/300/300?random=3' },
      { id: 4, name: 'Apple Watch Ultra 2', description: '钛金属表壳，精准定位', price: 6499, image: 'https://picsum.photos/300/300?random=4' },
    ]
  }
}

const updateCartCount = () => {
  try {
    const cart = JSON.parse(localStorage.getItem('cart') || '{}')
    cartCount.value = cart.items?.length || 0
  } catch {
    cartCount.value = 0
  }
}

onMounted(() => {
  fetchHotProducts()
  updateCartCount()
})
</script>

<style lang="scss" scoped>
.home {
  padding-bottom: 70px;
}

// 头部
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 0.5px solid rgba(0, 0, 0, 0.05);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  max-width: 980px;
  margin: 0 auto;
}

.logo {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.icon-btn {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: var(--text-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  
  &:hover {
    background: var(--bg-secondary);
  }
}

.badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: var(--apple-red);
  color: white;
  font-size: 10px;
  font-weight: 600;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

// Hero
.hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 60px 20px 80px;
  background: var(--bg-secondary);
}

.hero-content {
  max-width: 600px;
}

.hero-eyebrow {
  font-size: 14px;
  font-weight: 500;
  color: var(--apple-orange);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: 8px;
}

.hero-title {
  font-size: 48px;
  font-weight: 600;
  line-height: 1.05;
  letter-spacing: -0.02em;
  margin-bottom: 12px;
  
  @media (max-width: 600px) {
    font-size: 36px;
  }
}

.hero-subtitle {
  font-size: 19px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.hero-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
}

.hero-image {
  margin-top: 40px;
  width: 100%;
  max-width: 400px;
  
  img {
    width: 100%;
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-xl);
  }
}

// 分类
.categories {
  padding: 80px 0;
  background: var(--bg-primary);
}

.section-eyebrow {
  font-size: 14px;
  font-weight: 500;
  color: var(--apple-blue);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 8px;
}

.section-title {
  font-size: 32px;
  font-weight: 600;
  letter-spacing: -0.01em;
  margin-bottom: 40px;
  
  @media (max-width: 600px) {
    font-size: 24px;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  
  @media (max-width: 600px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 32px 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg);
  }
}

.category-icon {
  width: 60px;
  height: 60px;
  margin-bottom: 16px;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: var(--radius-lg);
  }
}

.category-name {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 4px;
}

.category-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

// 产品区
.products-section {
  padding: 60px 0;
}

.products-scroll {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 16px;
  margin: 0 -20px;
  padding-left: 20px;
  padding-right: 20px;
  
  // 隐藏滚动条但保留功能
  scrollbar-width: none;
  -ms-overflow-style: none;
  &::-webkit-scrollbar {
    display: none;
  }
}

.product-card {
  flex-shrink: 0;
  width: 200px;
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg);
    
    .product-btn {
      opacity: 1;
      transform: scale(1);
    }
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
  padding: 16px;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 13px;
  color: var(--text-secondary);
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

.product-btn {
  position: absolute;
  bottom: 70px;
  right: 12px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: var(--apple-blue);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.2s ease;
  box-shadow: var(--shadow-md);
}

// 价值主张
.values {
  padding: 60px 0;
  background: var(--bg-primary);
}

.values-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
  
  @media (max-width: 800px) {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
  }
  
  @media (max-width: 480px) {
    grid-template-columns: 1fr;
  }
}

.value-item {
  text-align: center;
}

.value-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 12px;
  color: var(--apple-blue);
  
  svg {
    width: 100%;
    height: 100%;
  }
}

.value-item h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.value-item p {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
