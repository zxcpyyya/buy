import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const totalCount = ref(0)
  const totalPrice = ref(0)
  
  const fetchCart = async () => {
    try {
      const data = await request.get('/cart')
      items.value = data.items || []
      totalCount.value = data.totalCount || 0
      totalPrice.value = data.totalPrice || 0
    } catch (e) {
      console.error('获取购物车失败', e)
    }
  }
  
  const fetchCartCount = async () => {
    try {
      const count = await request.get('/cart/count')
      totalCount.value = count || 0
    } catch (e) {
      // 未登录时不处理
    }
  }
  
  const clearCart = () => {
    items.value = []
    totalCount.value = 0
    totalPrice.value = 0
  }
  
  return {
    items,
    totalCount,
    totalPrice,
    fetchCart,
    fetchCartCount,
    clearCart
  }
})
