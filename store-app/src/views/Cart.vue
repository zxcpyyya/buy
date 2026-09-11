<template>
  <div class="cart-page safe-area-top">
    <!-- 顶部导航 -->
    <header class="header">
      <h1 class="title">购物车</h1>
      <span class="edit-btn" @click="isEditMode = !isEditMode">
        {{ isEditMode ? '完成' : '编辑' }}
      </span>
    </header>

    <!-- 购物车列表 -->
    <div class="cart-content" v-if="cartItems.length > 0">
      <!-- 店铺分组 -->
      <div class="store-group" v-for="store in cartStores" :key="store.id">
        <div class="store-header">
          <label class="checkbox" @click="toggleStoreSelected(store.id)">
            <input type="checkbox" :checked="isStoreSelected(store.id)" />
            <span class="checkmark"></span>
          </label>
          <span class="store-name">{{ store.name }}</span>
        </div>
        
        <div class="cart-items">
          <div class="cart-item" v-for="item in getStoreItems(store.id)" :key="item.id">
            <label class="checkbox" @click="toggleItemSelected(item)">
              <input type="checkbox" :checked="isItemSelected(item)" />
              <span class="checkmark"></span>
            </label>
            
            <div class="item-image" @click="$router.push(`/product/${item.productId}`)">
              <img :src="item.image" :alt="item.name" />
            </div>
            
            <div class="item-info">
              <h4 class="item-name">{{ item.name }}</h4>
              <p class="item-desc">{{ item.spec }}</p>
              <div class="item-bottom">
                <span class="item-price">¥{{ formatPrice(item.price) }}</span>
                <div class="quantity-control">
                  <button class="qty-btn" @click="decreaseQty(item)" :disabled="item.quantity <= 1">−</button>
                  <span class="qty-value">{{ item.quantity }}</span>
                  <button class="qty-btn" @click="increaseQty(item)">+</button>
                </div>
              </div>
            </div>
            
            <button class="delete-btn" @click="removeItem(item.id)" v-if="isEditMode">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- 底部结算栏 -->
      <div class="bottom-bar safe-area-bottom">
        <label class="checkbox" @click="toggleAllSelected">
          <input type="checkbox" :checked="isAllSelected" />
          <span class="checkmark"></span>
          <span class="check-label">全选</span>
        </label>
        
        <div class="total-section">
          <div class="total-info">
            <span class="total-label">合计</span>
            <span class="total-price">¥{{ formatPrice(totalPrice) }}</span>
          </div>
          <button class="checkout-btn" @click="goCheckout" :disabled="selectedItems.length === 0">
            结算 ({{ selectedItems.length }})
          </button>
        </div>
      </div>
    </div>

    <!-- 空购物车 -->
    <div class="empty-cart" v-else>
      <div class="empty-icon">
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
          <circle cx="9" cy="21" r="1"/>
          <circle cx="20" cy="21" r="1"/>
          <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
        </svg>
      </div>
      <p class="empty-text">购物车是空的</p>
      <p class="empty-hint">快去挑选心仪的商品吧</p>
      <button class="btn btn-primary" @click="$router.push('/category')">去逛逛</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import request from '@/utils/request'

const router = useRouter()

const isEditMode = ref(false)
const selectedItems = ref([])
const cartItems = ref([
  { id: 1, storeId: 1, productId: 1, name: 'iPhone 16 Pro 256GB', spec: '钛金属原色', price: 8999, quantity: 1, image: 'https://picsum.photos/100/100?random=1' },
  { id: 2, storeId: 1, productId: 2, name: 'AirPods Pro 2', spec: 'USB-C充电盒', price: 1899, quantity: 2, image: 'https://picsum.photos/100/100?random=2' },
])

const cartStores = computed(() => {
  const storeMap = new Map()
  cartItems.value.forEach(item => {
    if (!storeMap.has(item.storeId)) {
      storeMap.set(item.storeId, { id: item.storeId, name: 'Apple Store' })
    }
  })
  return Array.from(storeMap.values())
})

const getStoreItems = (storeId) => {
  return cartItems.value.filter(item => item.storeId === storeId)
}

const formatPrice = (price) => {
  return Number(price || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const totalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const isStoreSelected = (storeId) => {
  const storeItems = getStoreItems(storeId)
  return storeItems.every(item => selectedItems.value.some(s => s.id === item.id))
}

const isItemSelected = (item) => {
  return selectedItems.value.some(s => s.id === item.id)
}

const isAllSelected = computed(() => {
  return cartItems.value.length > 0 && cartItems.value.every(item => isItemSelected(item))
})

const toggleStoreSelected = (storeId) => {
  const storeItems = getStoreItems(storeId)
  const allSelected = isStoreSelected(storeId)
  
  if (allSelected) {
    selectedItems.value = selectedItems.value.filter(item => !storeItems.some(s => s.id === item.id))
  } else {
    storeItems.forEach(item => {
      if (!isItemSelected(item)) {
        selectedItems.value.push(item)
      }
    })
  }
}

const toggleItemSelected = (item) => {
  const index = selectedItems.value.findIndex(s => s.id === item.id)
  if (index > -1) {
    selectedItems.value.splice(index, 1)
  } else {
    selectedItems.value.push(item)
  }
}

const toggleAllSelected = () => {
  if (isAllSelected.value) {
    selectedItems.value = []
  } else {
    selectedItems.value = [...cartItems.value]
  }
}

const increaseQty = (item) => {
  item.quantity++
}

const decreaseQty = (item) => {
  if (item.quantity > 1) {
    item.quantity--
  }
}

const removeItem = (itemId) => {
  cartItems.value = cartItems.value.filter(item => item.id !== itemId)
  selectedItems.value = selectedItems.value.filter(item => item.id !== itemId)
  showToast('已删除')
}

const goCheckout = () => {
  if (selectedItems.value.length === 0) {
    showToast('请选择商品')
    return
  }
  // 存储选中商品到 localStorage
  localStorage.setItem('checkout_items', JSON.stringify(selectedItems.value))
  router.push('/checkout')
}
</script>

<style lang="scss" scoped>
.cart-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-bottom: 80px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: var(--bg-primary);
  position: sticky;
  top: 0;
  z-index: 100;
}

.title {
  font-size: 20px;
  font-weight: 600;
}

.edit-btn {
  font-size: 15px;
  color: var(--apple-blue);
  cursor: pointer;
}

.cart-content {
  padding: 16px;
}

.store-group {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  overflow: hidden;
  margin-bottom: 16px;
}

.store-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid var(--bg-secondary);
}

.store-name {
  font-size: 15px;
  font-weight: 600;
}

.cart-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid var(--bg-secondary);
  
  &:last-child {
    border-bottom: none;
  }
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  
  input {
    display: none;
  }
  
  .checkmark {
    width: 22px;
    height: 22px;
    border: 2px solid var(--text-tertiary);
    border-radius: 50%;
    transition: all 0.2s;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%) scale(0);
      width: 10px;
      height: 10px;
      background: var(--apple-blue);
      border-radius: 50%;
      transition: transform 0.2s;
    }
  }
  
  input:checked + .checkmark {
    border-color: var(--apple-blue);
    
    &::after {
      transform: translate(-50%, -50%) scale(1);
    }
  }
}

.check-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--bg-secondary);
  flex-shrink: 0;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-desc {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-bottom: 8px;
}

.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-price {
  font-size: 15px;
  font-weight: 600;
  color: var(--apple-red);
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 4px;
}

.qty-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: var(--bg-primary);
  border-radius: var(--radius-sm);
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
}

.qty-value {
  min-width: 24px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
}

.delete-btn {
  padding: 8px;
  border: none;
  background: transparent;
  color: var(--apple-red);
  cursor: pointer;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: var(--bg-primary);
  border-top: 1px solid var(--bg-secondary);
  z-index: 100;
}

.total-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.total-info {
  text-align: right;
}

.total-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.total-price {
  font-size: 20px;
  font-weight: 700;
  color: var(--apple-red);
}

.checkout-btn {
  padding: 12px 28px;
  border: none;
  background: var(--apple-blue);
  color: white;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-full);
  cursor: pointer;
  
  &:disabled {
    background: var(--text-tertiary);
    cursor: not-allowed;
  }
}

.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 120px);
  padding: 40px 20px;
}

.empty-icon {
  width: 100px;
  height: 100px;
  background: var(--bg-secondary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  color: var(--text-tertiary);
}

.empty-text {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 32px;
}
</style>
