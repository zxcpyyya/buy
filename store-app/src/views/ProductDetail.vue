<template>
  <div class="product-detail-page safe-area-top">
    <!-- 顶部导航 -->
    <header class="header">
      <button class="back-btn" @click="$router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </button>
      <div class="header-actions">
        <button class="action-btn">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
          </svg>
        </button>
        <button class="action-btn">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="18" cy="5" r="3"/>
            <circle cx="6" cy="12" r="3"/>
            <circle cx="18" cy="19" r="3"/>
            <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
            <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
          </svg>
        </button>
      </div>
    </header>

    <!-- 商品图片轮播 -->
    <section class="product-images">
      <div class="images-container">
        <img :src="product.image || 'https://picsum.photos/400/400?random=product'" :alt="product.name" class="main-image" />
      </div>
      <div class="image-indicator">
        <span class="dot active"></span>
        <span class="dot"></span>
        <span class="dot"></span>
      </div>
    </section>

    <!-- 商品信息 -->
    <section class="product-info">
      <div class="price-section">
        <span class="current-price">¥{{ formatPrice(product.price) }}</span>
        <span class="original-price" v-if="product.originalPrice">¥{{ formatPrice(product.originalPrice) }}</span>
      </div>
      <h1 class="product-name">{{ product.name }}</h1>
      <p class="product-desc">{{ product.description }}</p>
      
      <div class="tags">
        <span class="tag" v-for="tag in product.tags" :key="tag">{{ tag }}</span>
      </div>
    </section>

    <!-- 商品特性 -->
    <section class="features">
      <div class="feature-item">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M5 12h14M12 5l7 7-7 7"/>
        </svg>
        <span>顺丰包邮</span>
      </div>
      <div class="feature-item">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
        </svg>
        <span>正品保证</span>
      </div>
      <div class="feature-item">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
        </svg>
        <span>7天退货</span>
      </div>
    </section>

    <!-- 商品详情 -->
    <section class="product-details">
      <h3 class="section-title">商品详情</h3>
      <div class="details-content">
        <div class="detail-item">
          <span class="detail-label">品牌</span>
          <span class="detail-value">Apple</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">型号</span>
          <span class="detail-value">{{ product.model || '官方标配' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">产地</span>
          <span class="detail-value">中国大陆</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">保修期</span>
          <span class="detail-value">一年官方保修</span>
        </div>
      </div>
      <div class="detail-images">
        <img v-for="i in 3" :key="i" :src="`https://picsum.photos/400/400?random=detail${i}`" alt="商品详情" />
      </div>
    </section>

    <!-- 底部操作栏 -->
    <div class="bottom-bar safe-area-bottom">
      <div class="action-icons">
        <button class="action-icon-btn">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
          </svg>
          <span>收藏</span>
        </button>
        <button class="action-icon-btn" @click="$router.push('/cart')">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="9" cy="21" r="1"/>
            <circle cx="20" cy="21" r="1"/>
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
          </svg>
          <span>购物车</span>
        </button>
      </div>
      <div class="buy-buttons">
        <button class="btn btn-add-cart" @click="addToCart">加入购物车</button>
        <button class="btn btn-buy-now" @click="buyNow">立即购买</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const product = ref({
  id: 1,
  name: 'iPhone 16 Pro 256GB 钛金属',
  description: 'A18 Pro 芯片，6.3 英寸超视网膜 XDR 显示屏，钛金属边框，5倍光学变焦，USB 3.0',
  price: 8999,
  originalPrice: 9999,
  image: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-16-pro-finish-select-202409-6-3inch-naturaltitanium?wid=600&hei=600&fmt=jpeg&qlt=95&.v=UXpVcGEvTEFDVEtVSTUu',
  tags: ['官方正品', '顺丰包邮', '7天无理由退换'],
  model: 'iPhone 16 Pro'
})

const formatPrice = (price) => {
  return Number(price || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const addToCart = () => {
  showToast('已加入购物车')
}

const buyNow = () => {
  router.push('/checkout')
}

onMounted(async () => {
  const productId = route.params.id
  if (productId) {
    try {
      const data = await request.get(`/product/detail/${productId}`)
      if (data) {
        product.value = data
      }
    } catch (e) {
      console.error('获取商品详情失败', e)
    }
  }
})
</script>

<style lang="scss" scoped>
.product-detail-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 70px;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  z-index: 100;
}

.back-btn, .action-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: saturate(180%) blur(20px);
  color: var(--text-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
}

.header-actions {
  display: flex;
  gap: 8px;
}

.product-images {
  position: relative;
  background: var(--bg-secondary);
}

.images-container {
  width: 100%;
  aspect-ratio: 1;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-indicator {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.2);
  
  &.active {
    background: var(--apple-blue);
  }
}

.product-info {
  padding: 20px 16px;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 12px;
}

.current-price {
  font-size: 28px;
  font-weight: 700;
  color: var(--apple-red);
}

.original-price {
  font-size: 16px;
  color: var(--text-tertiary);
  text-decoration: line-through;
}

.product-name {
  font-size: 20px;
  font-weight: 600;
  line-height: 1.4;
  margin-bottom: 8px;
}

.product-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 4px 10px;
  background: var(--bg-secondary);
  border-radius: var(--radius-full);
  font-size: 12px;
  color: var(--text-secondary);
}

.features {
  display: flex;
  justify-content: space-around;
  padding: 16px;
  background: var(--bg-secondary);
  margin: 0 16px;
  border-radius: var(--radius-lg);
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
  
  svg {
    color: var(--apple-green);
  }
}

.product-details {
  padding: 24px 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}

.details-content {
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: 16px;
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  
  &:last-child {
    border-bottom: none;
  }
}

.detail-label {
  width: 80px;
  font-size: 14px;
  color: var(--text-tertiary);
}

.detail-value {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
}

.detail-images {
  display: flex;
  flex-direction: column;
  gap: 8px;
  
  img {
    width: 100%;
    border-radius: var(--radius-lg);
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 8px 16px;
  background: var(--bg-primary);
  border-top: 1px solid var(--bg-secondary);
  z-index: 100;
}

.action-icons {
  display: flex;
  gap: 4px;
}

.action-icon-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 8px 12px;
  border: none;
  background: transparent;
  font-size: 10px;
  color: var(--text-secondary);
  cursor: pointer;
}

.buy-buttons {
  flex: 1;
  display: flex;
  gap: 12px;
  margin-left: 12px;
}

.btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: var(--radius-full);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-add-cart {
  background: var(--apple-orange);
  color: white;
}

.btn-buy-now {
  background: var(--apple-red);
  color: white;
}
</style>
