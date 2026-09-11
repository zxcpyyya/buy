<template>
  <div class="addresses-page safe-area-top">
    <!-- 顶部 -->
    <header class="header">
      <button class="back-btn" @click="$router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </button>
      <h1 class="title">收货地址</h1>
      <button class="add-btn" @click="showAddModal = true">添加</button>
    </header>

    <!-- 地址列表 -->
    <div class="address-list" v-if="addresses.length > 0">
      <div 
        class="address-card" 
        v-for="addr in addresses" 
        :key="addr.id"
        :class="{ default: addr.isDefault }"
      >
        <div class="address-content" @click="selectAddress(addr)">
          <div class="contact-row">
            <span class="contact">{{ addr.contact }}</span>
            <span class="phone">{{ addr.phone }}</span>
            <span class="default-tag" v-if="addr.isDefault">默认</span>
          </div>
          <p class="address-text">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}</p>
        </div>
        <div class="address-actions">
          <button class="action-btn" @click.stop="editAddress(addr)">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
            </svg>
          </button>
          <button class="action-btn delete" @click.stop="deleteAddress(addr.id)">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else>
      <div class="empty-icon">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
          <circle cx="12" cy="10" r="3"/>
        </svg>
      </div>
      <p class="empty-text">暂无收货地址</p>
      <button class="btn btn-primary" @click="showAddModal = true">添加地址</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'

const router = useRouter()

const showAddModal = ref(false)
const addresses = ref([
  { id: 1, contact: '张三', phone: '138****8888', province: '广东省', city: '深圳市', district: '南山区', detail: '科技园南区A栋1201', isDefault: true },
  { id: 2, contact: '李四', phone: '139****6666', province: '广东省', city: '广州市', district: '天河区', detail: '珠江新城花城大道88号', isDefault: false }
])

const selectAddress = (addr) => {
  // 返回选中的地址
  router.back()
}

const editAddress = (addr) => {
  showToast('编辑地址')
}

const deleteAddress = (id) => {
  addresses.value = addresses.value.filter(a => a.id !== id)
  showToast('已删除')
}
</script>

<style lang="scss" scoped>
.addresses-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--bg-primary);
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  cursor: pointer;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.add-btn {
  font-size: 15px;
  color: var(--apple-blue);
  background: transparent;
  border: none;
  cursor: pointer;
}

.address-list {
  padding: 16px;
}

.address-card {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  
  &.default {
    border: 2px solid var(--apple-blue);
  }
}

.address-content {
  flex: 1;
  cursor: pointer;
}

.contact-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.contact {
  font-size: 17px;
  font-weight: 600;
}

.phone {
  font-size: 15px;
  color: var(--text-secondary);
}

.default-tag {
  padding: 2px 8px;
  background: var(--apple-blue);
  color: white;
  font-size: 11px;
  border-radius: var(--radius-full);
}

.address-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: var(--bg-secondary);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  
  &.delete {
    color: var(--apple-red);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
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
  color: var(--text-tertiary);
  margin-bottom: 20px;
}

.empty-text {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}
</style>
