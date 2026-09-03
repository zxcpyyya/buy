<template>
  <div class="detail-page" v-loading="loading">
    <!-- 返回按钮 -->
    <button class="back-btn" @click="$router.back()">
      <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M15 18l-6-6 6-6"/>
      </svg>
      返回
    </button>
    
    <div v-if="product" class="detail-content">
      <!-- 左：产品大图 -->
      <div class="detail-gallery">
        <div class="gallery-main">
          <img :src="currentImage" :alt="product.name" />
        </div>
        <div v-if="product.images && product.images.length > 1" class="gallery-thumbs">
          <div 
            v-for="(img, idx) in product.images" 
            :key="idx"
            class="thumb"
            :class="{ active: currentImage === img }"
            @click="currentImage = img"
          >
            <img :src="img" :alt="`${product.name}-${idx}`" />
          </div>
        </div>
      </div>
      
      <!-- 右：产品信息 -->
      <div class="detail-info">
        <p class="detail-category">{{ product.categoryName }}</p>
        <h1 class="detail-title">{{ product.name }}</h1>
        <p class="detail-desc">{{ product.description }}</p>
        
        <div class="detail-price">
          <span class="price-symbol">¥</span>
          <span class="price-value">{{ formatPrice(product.price) }}</span>
          <span class="price-stock">库存 {{ product.stock }} 件</span>
        </div>
        
        <!-- 数量选择 -->
        <div class="detail-quantity">
          <label class="qty-label">数量</label>
          <div class="qty-control">
            <button class="qty-btn" :disabled="quantity <= 1" @click="changeQty(-1)">−</button>
            <span class="qty-value">{{ quantity }}</span>
            <button class="qty-btn" :disabled="quantity >= product.stock" @click="changeQty(1)">+</button>
          </div>
        </div>
        
        <!-- 行动按钮 -->
        <div class="detail-actions">
          <button class="btn-action btn-buy" @click="handleAddToCart(true)">
            立即购买
          </button>
          <button class="btn-action btn-cart" @click="handleAddToCart(false)">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M6 7h12l-1.5 12.5a1.5 1.5 0 01-1.5 1.4H9a1.5 1.5 0 01-1.5-1.4L6 7z"/>
              <path d="M9 7V5a3 3 0 016 0v2"/>
            </svg>
            加入购物袋
          </button>
        </div>
        
        <!-- 服务保障 -->
        <div class="detail-services">
          <div class="service-item">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M2 8h20v8H2z"/>
              <path d="M2 12h20"/>
            </svg>
            <span>满99元免运费</span>
          </div>
          <div class="service-item">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 4l4 4v6c0 4-4 6-4 6s-4-2-4-6V8l4-4z"/>
            </svg>
            <span>正品保障</span>
          </div>
          <div class="service-item">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="9"/>
              <path d="M12 8v4l3 2"/>
            </svg>
            <span>24小时发货</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const loading = ref(false)
const quantity = ref(1)
const currentImage = ref('')

const formatPrice = (price) => {
  return Number(price).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const changeQty = (delta) => {
  const newVal = quantity.value + delta
  if (newVal >= 1 && newVal <= product.value.stock) {
    quantity.value = newVal
  }
}

const fetchProduct = async (id) => {
  loading.value = true
  try {
    const data = await request.get(`/product/detail/${id}`)
    product.value = data
    currentImage.value = data.image
  } catch (e) {
    console.error('获取商品详情失败', e)
  } finally {
    loading.value = false
  }
}

const handleAddToCart = async (buyNow) => {
  if (!userStore.isLogin) {
    showToast('请先登录')
    setTimeout(() => router.push('/login'), 800)
    return
  }
  
  try {
    await request.post('/cart', {
      productId: product.value.id,
      quantity: quantity.value
    })
    
    showToast({
      message: '已加入购物袋',
      icon: 'success'
    })
    
    if (buyNow) {
      setTimeout(() => router.push('/cart'), 600)
    }
  } catch (e) {
    console.error('加入购物车失败', e)
  }
}

watch(() => route.params.id, (val) => {
  if (val) fetchProduct(val)
})

onMounted(() => {
  fetchProduct(route.params.id)
})
</script>

<style lang="scss" scoped>
.detail-page {
  padding: 80px 22px 80px;
  max-width: 1440px;
  margin: 0 auto;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  background: #f5f5f7;
  border-radius: 980px;
  font-size: 14px;
  color: #1d1d1f;
  margin-bottom: 32px;
  transition: background 0.2s;
  
  &:hover {
    background: #e8e8ed;
  }
}

.detail-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 64px;
  align-items: start;
  
  @media (max-width: 833px) {
    grid-template-columns: 1fr;
    gap: 32px;
  }
}

// 图片画廊
.detail-gallery {
  position: sticky;
  top: 80px;
}

.gallery-main {
  aspect-ratio: 1;
  background: #f5f5f7;
  border-radius: 18px;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.gallery-thumbs {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  
  .thumb {
    width: 64px;
    height: 64px;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.2s;
    
    &.active {
      border-color: #0071e3;
    }
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

// 信息区
.detail-info {
  padding: 32px 0;
}

.detail-category {
  font-size: 14px;
  color: #6e6e73;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-bottom: 12px;
}

.detail-title {
  font-size: 40px;
  font-weight: 600;
  line-height: 1.1;
  letter-spacing: -0.01em;
  color: #1d1d1f;
  margin-bottom: 16px;
  
  @media (max-width: 833px) {
    font-size: 28px;
  }
}

.detail-desc {
  font-size: 17px;
  color: #6e6e73;
  line-height: 1.5;
  margin-bottom: 32px;
}

.detail-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 24px 0;
  border-top: 1px solid #f5f5f7;
  border-bottom: 1px solid #f5f5f7;
  margin-bottom: 32px;
}

.price-symbol {
  font-size: 17px;
  color: #1d1d1f;
}

.price-value {
  font-size: 40px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.01em;
}

.price-stock {
  margin-left: auto;
  font-size: 14px;
  color: #6e6e73;
}

// 数量
.detail-quantity {
  margin-bottom: 32px;
}

.qty-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 12px;
}

.qty-control {
  display: inline-flex;
  align-items: center;
  border: 1px solid #d2d2d7;
  border-radius: 980px;
  overflow: hidden;
}

.qty-btn {
  width: 40px;
  height: 40px;
  font-size: 18px;
  color: #1d1d1f;
  transition: background 0.2s;
  
  &:hover:not(:disabled) {
    background: #f5f5f7;
  }
  
  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
}

.qty-value {
  min-width: 40px;
  text-align: center;
  font-size: 15px;
  font-weight: 500;
  color: #1d1d1f;
}

// 行动按钮
.detail-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 32px;
}

.btn-action {
  flex: 1;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border-radius: 980px;
  font-size: 17px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-buy {
  background: #0071e3;
  color: #fff;
  
  &:hover {
    background: #0077ed;
  }
  
  &:active {
    background: #006edb;
    transform: scale(0.98);
  }
}

.btn-cart {
  background: #f5f5f7;
  color: #1d1d1f;
  
  &:hover {
    background: #e8e8ed;
  }
}

// 服务
.detail-services {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 24px;
  background: #f5f5f7;
  border-radius: 18px;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #1d1d1f;
}
</style>
