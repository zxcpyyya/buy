<template>
  <div class="coupons-page safe-area-top">
    <header class="header">
      <button class="back-btn" @click="$router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </button>
      <h1 class="title">优惠券</h1>
      <div class="placeholder"></div>
    </header>

    <div class="tabs">
      <div v-for="tab in tabs" :key="tab.value" class="tab-item" :class="{ active: currentTab === tab.value }" @click="currentTab = tab.value">
        {{ tab.label }}
      </div>
    </div>

    <div class="coupon-list">
      <div class="coupon-card" v-for="coupon in filteredCoupons" :key="coupon.id" :class="{ disabled: coupon.status !== 0 }">
        <div class="coupon-left">
          <div class="coupon-value">
            <span class="currency">¥</span>
            <span class="amount">{{ coupon.value }}</span>
          </div>
          <div class="coupon-condition">{{ coupon.condition }}</div>
        </div>
        <div class="coupon-right">
          <div class="coupon-name">{{ coupon.name }}</div>
          <div class="coupon-time">{{ coupon.time }}</div>
          <button class="coupon-btn" v-if="coupon.status === 0" @click="useCoupon(coupon)">立即使用</button>
          <span class="coupon-status" v-else>{{ coupon.statusText }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const currentTab = ref('unused')
const tabs = [
  { label: '未使用', value: 'unused' },
  { label: '已使用', value: 'used' },
  { label: '已过期', value: 'expired' }
]

const coupons = ref([
  { id: 1, name: '新人专享券', value: 50, condition: '满200可用', time: '2024.09.01-2024.09.30', status: 0, statusText: '' },
  { id: 2, name: '限时折扣券', value: 100, condition: '满500可用', time: '2024.09.01-2024.09.30', status: 0, statusText: '' },
  { id: 3, name: '节日特惠券', value: 20, condition: '无门槛', time: '2024.08.01-2024.08.31', status: 1, statusText: '已使用' },
  { id: 4, name: 'VIP专属券', value: 200, condition: '满1000可用', time: '2024.07.01-2024.07.31', status: 2, statusText: '已过期' }
])

const filteredCoupons = computed(() => {
  if (currentTab.value === 'unused') return coupons.value.filter(c => c.status === 0)
  if (currentTab.value === 'used') return coupons.value.filter(c => c.status === 1)
  return coupons.value.filter(c => c.status === 2)
})

const useCoupon = (coupon) => {
  alert(`使用 ${coupon.name}`)
}
</script>

<style lang="scss" scoped>
.coupons-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--bg-primary);
}

.back-btn { width: 32px; height: 32px; border: none; background: transparent; cursor: pointer; }
.title { font-size: 18px; font-weight: 600; }
.placeholder { width: 32px; }

.tabs {
  display: flex;
  background: var(--bg-primary);
  padding: 0 16px;
  margin-bottom: 12px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 14px;
  font-size: 15px;
  color: var(--text-secondary);
  border-bottom: 2px solid transparent;
  cursor: pointer;
  &.active { color: var(--apple-blue); border-bottom-color: var(--apple-blue); font-weight: 600; }
}

.coupon-list { padding: 0 16px; }

.coupon-card {
  display: flex;
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  overflow: hidden;
  margin-bottom: 12px;
  &.disabled { opacity: 0.6; }
}

.coupon-left {
  width: 100px;
  background: linear-gradient(135deg, var(--apple-red) 0%, #ff6b6b 100%);
  color: white;
  padding: 20px 12px;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.coupon-value { .currency { font-size: 14px; } .amount { font-size: 32px; font-weight: 700; } }
.coupon-condition { font-size: 11px; margin-top: 4px; opacity: 0.9; }

.coupon-right {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.coupon-name { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.coupon-time { font-size: 12px; color: var(--text-tertiary); }
.coupon-btn {
  margin-top: 8px;
  padding: 6px 16px;
  background: var(--apple-red);
  color: white;
  border: none;
  border-radius: var(--radius-full);
  font-size: 12px;
  cursor: pointer;
  align-self: flex-start;
}
.coupon-status { font-size: 12px; color: var(--text-tertiary); margin-top: 8px; }
</style>
