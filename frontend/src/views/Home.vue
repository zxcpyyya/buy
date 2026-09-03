<template>
  <div class="home">
    <!-- Hero区 - 苹果风格的"巨幅标题" -->
    <section class="hero">
      <div class="hero-content">
        <p class="hero-eyebrow">新品发布</p>
        <h1 class="hero-title">iPhone 15 Pro</h1>
        <p class="hero-subtitle">钛金属。超强。超轻。超 Pro。</p>
        <div class="hero-actions">
          <router-link to="/products" class="btn btn-primary">立即购买</router-link>
          <router-link to="/products" class="btn-link">了解更多 ›</router-link>
        </div>
      </div>
    </section>
    
    <!-- 第二Hero - 倒置色块 -->
    <section class="hero hero-dark">
      <div class="hero-content">
        <h2 class="hero-title">MacBook Pro</h2>
        <p class="hero-subtitle">Mind-blowing. Head-turning.</p>
        <div class="hero-actions">
          <router-link to="/products" class="btn btn-primary">立即购买</router-link>
          <router-link to="/products" class="btn-link btn-link-light">了解更多 ›</router-link>
        </div>
      </div>
    </section>
    
    <!-- 分类导航 - 4宫格 -->
    <section class="categories">
      <div class="container-large">
        <div class="categories-grid">
          <router-link to="/category/1" class="category-card">
            <div class="category-icon">
              <svg viewBox="0 0 48 48" fill="none">
                <rect x="8" y="12" width="32" height="24" rx="3" stroke="currentColor" stroke-width="1.5"/>
                <path d="M8 18h32M8 30h32" stroke="currentColor" stroke-width="1.5"/>
              </svg>
            </div>
            <h3>数码电子</h3>
            <p>探索 ›</p>
          </router-link>
          <router-link to="/category/2" class="category-card">
            <div class="category-icon">
              <svg viewBox="0 0 48 48" fill="none">
                <path d="M24 8l4 12h12l-10 7 4 12-10-7-10 7 4-12-10-7h12l4-12z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
              </svg>
            </div>
            <h3>服装鞋包</h3>
            <p>探索 ›</p>
          </router-link>
          <router-link to="/category/3" class="category-card">
            <div class="category-icon">
              <svg viewBox="0 0 48 48" fill="none">
                <circle cx="24" cy="24" r="16" stroke="currentColor" stroke-width="1.5"/>
                <path d="M24 16v16M16 24h16" stroke="currentColor" stroke-width="1.5"/>
              </svg>
            </div>
            <h3>食品生鲜</h3>
            <p>探索 ›</p>
          </router-link>
          <router-link to="/category/4" class="category-card">
            <div class="category-icon">
              <svg viewBox="0 0 48 48" fill="none">
                <path d="M12 20l12-12 12 12v20a2 2 0 01-2 2H14a2 2 0 01-2-2V20z" stroke="currentColor" stroke-width="1.5"/>
                <path d="M20 42V28h8v14" stroke="currentColor" stroke-width="1.5"/>
              </svg>
            </div>
            <h3>家居百货</h3>
            <p>探索 ›</p>
          </router-link>
        </div>
      </div>
    </section>
    
    <!-- 热门商品 -->
    <section class="products-section">
      <div class="container-large">
        <h2 class="section-title">热销爆款</h2>
        <p class="section-subtitle">大家都在买的精选好物</p>
        
        <div class="products-grid" v-loading="loadingHot">
          <div 
            v-for="product in hotProducts" 
            :key="product.id" 
            class="product-card"
            @click="$router.push(`/product/${product.id}`)"
          >
            <div class="product-image">
              <img :src="product.image" :alt="product.name" />
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              <p class="product-price">¥{{ formatPrice(product.price) }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
    
    <!-- 新品上市 -->
    <section class="products-section products-section-light">
      <div class="container-large">
        <h2 class="section-title">新品上市</h2>
        <p class="section-subtitle">最新上架的精选商品</p>
        
        <div class="products-grid" v-loading="loadingNew">
          <div 
            v-for="product in newProducts" 
            :key="product.id" 
            class="product-card"
            @click="$router.push(`/product/${product.id}`)"
          >
            <span class="badge-new">NEW</span>
            <div class="product-image">
              <img :src="product.image" :alt="product.name" />
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              <p class="product-price">¥{{ formatPrice(product.price) }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
    
    <!-- 价值主张 -->
    <section class="values">
      <div class="container-large">
        <div class="values-grid">
          <div class="value-item">
            <div class="value-icon">
              <svg viewBox="0 0 32 32" fill="none">
                <path d="M2 10h28v12H2z" stroke="currentColor" stroke-width="1.5"/>
                <path d="M2 14h28" stroke="currentColor" stroke-width="1.5"/>
              </svg>
            </div>
            <h4>免运费</h4>
            <p>满99元免运费</p>
          </div>
          <div class="value-item">
            <div class="value-icon">
              <svg viewBox="0 0 32 32" fill="none">
                <path d="M16 4l8 4v8c0 6-4 10-8 12-4-2-8-6-8-12V8l8-4z" stroke="currentColor" stroke-width="1.5"/>
              </svg>
            </div>
            <h4>正品保障</h4>
            <p>100%原厂正品</p>
          </div>
          <div class="value-item">
            <div class="value-icon">
              <svg viewBox="0 0 32 32" fill="none">
                <path d="M6 16l6 6 14-14" stroke="currentColor" stroke-width="2"/>
              </svg>
            </div>
            <h4>七天无理由</h4>
            <p>随时退换无忧</p>
          </div>
          <div class="value-item">
            <div class="value-icon">
              <svg viewBox="0 0 32 32" fill="none">
                <circle cx="16" cy="16" r="12" stroke="currentColor" stroke-width="1.5"/>
                <path d="M16 10v6l4 4" stroke="currentColor" stroke-width="1.5"/>
              </svg>
            </div>
            <h4>24小时发货</h4>
            <p>极速配送到家</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const hotProducts = ref([])
const newProducts = ref([])
const loadingHot = ref(true)
const loadingNew = ref(true)

const formatPrice = (price) => {
  return Number(price).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const fetchHotProducts = async () => {
  try {
    const data = await request.get('/product/hot', { params: { limit: 8 } })
    hotProducts.value = data || []
  } catch (e) {
    console.error('获取热门商品失败', e)
  } finally {
    loadingHot.value = false
  }
}

const fetchNewProducts = async () => {
  try {
    const data = await request.get('/product/new', { params: { limit: 8 } })
    newProducts.value = data || []
  } catch (e) {
    console.error('获取新品失败', e)
  } finally {
    loadingNew.value = false
  }
}

onMounted(() => {
  fetchHotProducts()
  fetchNewProducts()
})
</script>

<style lang="scss" scoped>
.home {
  // Hero区 - 苹果官方风格
  .hero {
    text-align: center;
    padding: 120px 22px 80px;
    background: #fbfbfd;
    
    &-dark {
      background: #000;
      color: #f5f5f7;
    }
  }
  
  .hero-eyebrow {
    font-size: 21px;
    font-weight: 400;
    color: #86868b;
    margin-bottom: 8px;
  }
  
  .hero-title {
    font-size: 64px;
    font-weight: 600;
    line-height: 1.07;
    letter-spacing: -0.015em;
    margin-bottom: 8px;
    
    @media (max-width: 833px) {
      font-size: 48px;
    }
  }
  
  .hero-subtitle {
    font-size: 24px;
    font-weight: 400;
    line-height: 1.33;
    color: inherit;
    opacity: 0.85;
    margin-bottom: 24px;
    
    @media (max-width: 833px) {
      font-size: 19px;
    }
  }
  
  .hero-actions {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 28px;
  }
  
  .btn-link {
    font-size: 17px;
    color: #06c;
    
    &:hover {
      text-decoration: underline;
    }
    
    &-light {
      color: #2997ff;
    }
  }
  
  // 分类网格
  .categories {
    padding: 80px 0;
    background: #fff;
  }
  
  .categories-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    
    @media (max-width: 833px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }
  
  .category-card {
    background: #f5f5f7;
    border-radius: 18px;
    padding: 48px 24px;
    text-align: center;
    transition: all 0.4s cubic-bezier(0.25, 0.1, 0.25, 1);
    
    &:hover {
      transform: translateY(-4px);
      background: #fff;
      box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08);
    }
    
    h3 {
      font-size: 21px;
      font-weight: 600;
      color: #1d1d1f;
      margin-bottom: 8px;
    }
    
    p {
      font-size: 14px;
      color: #06c;
    }
  }
  
  .category-icon {
    width: 64px;
    height: 64px;
    margin: 0 auto 16px;
    color: #1d1d1f;
  }
  
  // 产品区
  .products-section {
    padding: 80px 0;
    
    &-light {
      background: #f5f5f7;
    }
  }
  
  .section-title {
    font-size: 40px;
    font-weight: 600;
    text-align: center;
    color: #1d1d1f;
    margin-bottom: 8px;
    line-height: 1.1;
    
    @media (max-width: 833px) {
      font-size: 32px;
    }
  }
  
  .section-subtitle {
    font-size: 17px;
    text-align: center;
    color: #6e6e73;
    margin-bottom: 48px;
  }
  
  .products-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    
    @media (max-width: 1170px) {
      grid-template-columns: repeat(3, 1fr);
    }
    
    @media (max-width: 833px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }
  
  .product-card {
    position: relative;
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
    overflow: hidden;
    background: #fbfbfd;
    
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
  
  .product-price {
    font-size: 17px;
    font-weight: 600;
    color: #1d1d1f;
  }
  
  .badge-new {
    position: absolute;
    top: 12px;
    right: 12px;
    z-index: 1;
    padding: 4px 10px;
    background: #ff3b30;
    color: #fff;
    font-size: 11px;
    font-weight: 600;
    border-radius: 980px;
    letter-spacing: 0.04em;
  }
  
  // 价值主张
  .values {
    padding: 80px 0;
    background: #fff;
  }
  
  .values-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 32px;
    
    @media (max-width: 833px) {
      grid-template-columns: repeat(2, 1fr);
      gap: 24px;
    }
  }
  
  .value-item {
    text-align: center;
    
    h4 {
      font-size: 17px;
      font-weight: 600;
      color: #1d1d1f;
      margin: 12px 0 4px;
    }
    
    p {
      font-size: 14px;
      color: #6e6e73;
    }
  }
  
  .value-icon {
    width: 48px;
    height: 48px;
    margin: 0 auto;
    color: #1d1d1f;
  }
}
</style>
