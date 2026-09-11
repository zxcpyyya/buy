<template>
  <div class="history-page" v-loading="loading">
    <!-- 页面标题 -->
    <header class="page-header">
      <button class="back-btn" @click="$router.back()">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M15 18l-6-6 6-6"/>
        </svg>
        返回
      </button>
      <h1 class="page-title">浏览历史</h1>
    </header>

    <!-- 操作栏 -->
    <div class="action-bar" v-if="historyList.length > 0">
      <span class="history-count">共 {{ historyList.length }} 件商品</span>
      <button class="clear-btn" @click="handleClear">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="3 6 5 6 21 6"/>
          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
        </svg>
        清空
      </button>
    </div>

    <!-- 商品列表 -->
    <div class="product-list" v-if="!loading">
      <div v-if="historyList.length > 0" class="product-grid">
        <div 
          v-for="item in historyList" 
          :key="item.productId" 
          class="product-card"
          @click="goToProduct(item.productId)"
        >
          <div class="product-image">
            <img :src="item.productImage || 'https://picsum.photos/200/200?random=1'" :alt="item.productName" />
          </div>
          <div class="product-info">
            <p class="product-name">{{ item.productName }}</p>
            <p class="product-price">¥{{ formatPrice(item.price) }}</p>
            <p class="browse-time">{{ formatTime(item.browseTime) }}</p>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
        </div>
        <h3>暂无浏览记录</h3>
        <p>快去逛逛吧，看看有什么喜欢的</p>
        <button class="btn-primary" @click="$router.push('/')">去首页</button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else class="skeleton-list">
      <div v-for="i in 6" :key="i" class="skeleton-card">
        <div class="skeleton skeleton-image"></div>
        <div class="skeleton-card-content">
          <div class="skeleton skeleton-text" style="width: 80%"></div>
          <div class="skeleton skeleton-text skeleton-text-sm" style="width: 50%"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { formatPrice } from '@/utils'
import { getBrowseHistory, clearBrowseHistory } from '@/api/history'
import { showConfirmDialog, showToast } from 'vant'

const router = useRouter()
const loading = ref(false)
const historyList = ref([])

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await getBrowseHistory(50)
    historyList.value = res || []
  } catch (e) {
    console.error('获取浏览历史失败', e)
  } finally {
    loading.value = false
  }
}

const handleClear = async () => {
  try {
    await showConfirmDialog({
      title: '清空浏览历史',
      message: '确定要清空所有浏览记录吗？'
    })
    await clearBrowseHistory()
    historyList.value = []
    showToast({ message: '已清空', icon: 'success' })
  } catch (e) {
    // 用户取消
  }
}

const goToProduct = (productId) => {
  router.push(`/product/${productId}`)
}

onMounted(fetchHistory)
</script>

<style lang="scss" scoped>
.history-page {
  min-height: 100vh;
  background: #f5f5f7;
  padding: 80px 16px 32px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  background: #fff;
  border-radius: 980px;
  font-size: 14px;
  color: #1d1d1f;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 16px;
}

.history-count {
  font-size: 14px;
  color: #6e6e73;
}

.clear-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: #fff;
  border: 1px solid #d2d2d7;
  border-radius: 980px;
  font-size: 13px;
  color: #ff3b30;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  
  @media (min-width: 600px) {
    grid-template-columns: repeat(3, 1fr);
  }
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  
  &:active {
    transform: scale(0.98);
  }
}

.product-image {
  width: 100%;
  aspect-ratio: 1;
  background: #f5f5f7;
  
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
  color: #1d1d1f;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
}

.product-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff3b30;
  margin-bottom: 4px;
}

.browse-time {
  font-size: 12px;
  color: #86868b;
}

.empty-state {
  text-align: center;
  padding: 80px 32px;
  
  .empty-icon {
    color: #d2d2d7;
    margin-bottom: 24px;
  }
  
  h3 {
    font-size: 20px;
    font-weight: 600;
    color: #1d1d1f;
    margin-bottom: 8px;
  }
  
  p {
    font-size: 14px;
    color: #6e6e73;
    margin-bottom: 24px;
  }
}

.btn-primary {
  padding: 12px 32px;
  background: #0071e3;
  color: #fff;
  border-radius: 980px;
  font-size: 15px;
  font-weight: 500;
}

.skeleton-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.skeleton-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.skeleton-card-content {
  padding: 12px;
}
</style>
