<template>
  <div class="products-page safe-area-top">
    <header class="header">
      <button class="back-btn" @click="$router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </button>
      <h1 class="title">{{ categoryName }}</h1>
      <button class="filter-btn" @click="showFilter = true">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="4" y1="21" x2="4" y2="14"/>
          <line x1="4" y1="10" x2="4" y2="3"/>
          <line x1="12" y1="21" x2="12" y2="12"/>
          <line x1="12" y1="8" x2="12" y2="3"/>
          <line x1="20" y1="21" x2="20" y2="16"/>
          <line x1="20" y1="12" x2="20" y2="3"/>
          <line x1="1" y1="14" x2="7" y2="14"/>
          <line x1="9" y1="8" x2="15" y2="8"/>
          <line x1="17" y1="16" x2="23" y2="16"/>
        </svg>
      </button>
    </header>

    <div class="product-grid">
      <div class="product-card" v-for="product in products" :key="product.id" @click="$router.push(`/product/${product.id}`)">
        <div class="product-image">
          <img :src="product.image || `https://picsum.photos/200/200?random=${product.id}`" :alt="product.name" />
        </div>
        <div class="product-info">
          <h3 class="product-name">{{ product.name }}</h3>
          <p class="product-desc">{{ product.description }}</p>
          <div class="product-bottom">
            <span class="product-price">¥{{ formatPrice(product.price) }}</span>
            <span class="product-sales">{{ product.sales || 0 }}人付款</span>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-state" v-if="products.length === 0">
      <p>暂无商品</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const categoryName = ref('商品列表')
const showFilter = ref(false)
const products = ref([])

const formatPrice = (price) => Number(price || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })

onMounted(() => {
  products.value = [
    { id: 1, name: 'iPhone 16 Pro Max 256GB', description: 'A18 Pro芯片，钛金属设计', price: 9999, sales: 5200, image: 'https://picsum.photos/200/200?random=1' },
    { id: 2, name: 'iPhone 16 Pro 128GB', description: 'A18 Pro芯片，极致性能', price: 7999, sales: 3800, image: 'https://picsum.photos/200/200?random=2' },
    { id: 3, name: 'MacBook Pro 14寸 M3', description: 'M3 Pro芯片，专业级性能', price: 15999, sales: 1200, image: 'https://picsum.photos/200/200?random=3' },
    { id: 4, name: 'iPad Pro 12.9寸 M2', description: '超视网膜XDR显示屏', price: 9299, sales: 2800, image: 'https://picsum.photos/200/200?random=4' },
    { id: 5, name: 'Apple Watch Ultra 2', description: '钛金属表壳，极限运动', price: 6499, sales: 1500, image: 'https://picsum.photos/200/200?random=5' },
    { id: 6, name: 'AirPods Pro 2', description: '自适应音频，个性化空间音频', price: 1899, sales: 8900, image: 'https://picsum.photos/200/200?random=6' },
  ]
})
</script>

<style lang="scss" scoped>
.products-page { min-height: 100vh; background: var(--bg-secondary); }

.header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; background: var(--bg-primary); }
.back-btn { width: 32px; height: 32px; border: none; background: transparent; cursor: pointer; }
.title { font-size: 18px; font-weight: 600; }
.filter-btn { width: 32px; height: 32px; border: none; background: transparent; cursor: pointer; }

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 16px;
}

.product-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.product-image {
  aspect-ratio: 1;
  background: var(--bg-secondary);
  img { width: 100%; height: 100%; object-fit: cover; }
}

.product-info { padding: 12px; }
.product-name { font-size: 14px; font-weight: 600; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-desc { font-size: 12px; color: var(--text-tertiary); margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-bottom { display: flex; justify-content: space-between; align-items: center; }
.product-price { font-size: 16px; font-weight: 700; color: var(--apple-red); }
.product-sales { font-size: 11px; color: var(--text-tertiary); }

.empty-state { display: flex; align-items: center; justify-content: center; min-height: 50vh; font-size: 14px; color: var(--text-tertiary); }
</style>
