<template>
  <div class="express-page" v-loading="loading">
    <button class="back-btn" @click="$router.back()">
      <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M15 18l-6-6 6-6"/>
      </svg>
      返回
    </button>
    
    <div v-if="express" class="express-content">
      <!-- 物流状态横幅 -->
      <div class="status-banner" :class="`status-${express.status}`">
        <div class="status-info">
          <div class="status-icon">
            <svg v-if="express.status === 0" viewBox="0 0 48 48" width="48" height="48" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="8" y="24" width="32" height="16" rx="2"/>
              <path d="M8 28h32"/>
              <circle cx="16" cy="40" r="4"/>
              <circle cx="32" cy="40" r="4"/>
            </svg>
            <svg v-else-if="express.status === 3" viewBox="0 0 48 48" width="48" height="48" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="24" cy="24" r="20"/>
              <path d="M16 24l6 6 12-12"/>
            </svg>
            <svg v-else viewBox="0 0 48 48" width="48" height="48" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="4" y="12" width="40" height="24" rx="2"/>
              <path d="M4 20h40"/>
              <circle cx="14" cy="32" r="4"/>
              <circle cx="34" cy="32" r="4"/>
              <path d="M20 12V8a4 4 0 018 0v4"/>
            </svg>
          </div>
          <div class="status-text">
            <h2 class="status-name">{{ express.statusName }}</h2>
            <p class="status-desc">{{ getStatusDesc(express.status) }}</p>
          </div>
        </div>
      </div>
      
      <!-- 快递信息 -->
      <section class="express-info section">
        <div class="info-row">
          <span class="info-label">快递公司</span>
          <span class="info-value">{{ express.companyName }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">快递单号</span>
          <span class="info-value">
            {{ express.expressNo }}
            <button class="btn-copy" @click="copyExpressNo">复制</button>
          </span>
        </div>
        <div class="info-row" v-if="express.shipTime">
          <span class="info-label">发货时间</span>
          <span class="info-value">{{ formatTime(express.shipTime) }}</span>
        </div>
      </section>
      
      <!-- 收货信息 -->
      <section class="receiver-info section">
        <h3 class="section-title">收货信息</h3>
        <div class="receiver-card">
          <div class="receiver-name">{{ express.receiverName }}</div>
          <div class="receiver-phone">{{ express.receiverPhone }}</div>
          <div class="receiver-address">{{ express.receiverAddress }}</div>
        </div>
      </section>
      
      <!-- 物流轨迹 -->
      <section class="trace-section section">
        <h3 class="section-title">物流动态</h3>
        <div class="trace-timeline">
          <div 
            v-for="(trace, index) in express.traces" 
            :key="trace.id"
            class="trace-item"
            :class="{ active: index === express.traces.length - 1 }"
          >
            <div class="trace-dot"></div>
            <div class="trace-line" v-if="index < express.traces.length - 1"></div>
            <div class="trace-content">
              <div class="trace-message">{{ trace.message }}</div>
              <div class="trace-location" v-if="trace.location">{{ trace.location }}</div>
              <div class="trace-time">{{ formatTime(trace.traceTime) }}</div>
            </div>
          </div>
          
          <div v-if="!express.traces || express.traces.length === 0" class="trace-empty">
            暂无物流轨迹信息
          </div>
        </div>
      </section>
      
      <!-- 查询按钮 -->
      <div class="action-bar">
        <button class="btn-query" @click="openExpressQuery">
          在快递100查询
        </button>
      </div>
    </div>
    
    <div v-else class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 64 64" width="64" height="64" fill="none" stroke="currentColor" stroke-width="1.5">
          <rect x="8" y="24" width="48" height="24" rx="2"/>
          <path d="M8 32h48"/>
          <circle cx="20" cy="44" r="6"/>
          <circle cx="44" cy="44" r="6"/>
          <path d="M32 8v16"/>
        </svg>
      </div>
      <p class="empty-text">暂无物流信息</p>
      <p class="empty-desc">商家还未发货，请耐心等待</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import request from '@/utils/request'

const route = useRoute()

const express = ref(null)
const loading = ref(false)

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const getStatusDesc = (status) => {
  const map = {
    0: '商家正在准备商品',
    1: '商品运输中，请耐心等待',
    2: '商品正在派送中',
    3: '已签收，感谢购买',
    4: '包裹已被拒收或退回'
  }
  return map[status] || ''
}

const fetchExpress = async () => {
  loading.value = true
  try {
    express.value = await request.get(`/express/order/${route.params.orderId}`)
  } catch (e) {
    console.error('获取物流信息失败', e)
  } finally {
    loading.value = false
  }
}

const copyExpressNo = async () => {
  try {
    await navigator.clipboard.writeText(express.value.expressNo)
    showToast('复制成功')
  } catch (e) {
    showToast('复制失败')
  }
}

const openExpressQuery = () => {
  const url = `https://www.kuaidi100.com/?nu=${express.value.expressNo}`
  window.open(url, '_blank')
}

onMounted(fetchExpress)
</script>

<style lang="scss" scoped>
.express-page {
  padding: 80px 22px 120px;
  max-width: 600px;
  margin: 0 auto;
  background: #fbfbfd;
  min-height: 100vh;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  background: #fff;
  border-radius: 980px;
  font-size: 14px;
  color: #1d1d1f;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.status-banner {
  border-radius: 18px;
  padding: 24px;
  margin-bottom: 16px;
  color: #fff;
  
  &.status-0 { background: linear-gradient(135deg, #86868b 0%, #6e6e73 100%); }
  &.status-1 { background: linear-gradient(135deg, #0071e3 0%, #5856d6 100%); }
  &.status-2 { background: linear-gradient(135deg, #ff9500 0%, #ff6b00 100%); }
  &.status-3 { background: linear-gradient(135deg, #30d158 0%, #28a745 100%); }
  &.status-4 { background: linear-gradient(135deg, #ff3b30 0%, #dc3545 100%); }
}

.status-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-icon {
  opacity: 0.9;
}

.status-text {
  flex: 1;
}

.status-name {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.status-desc {
  font-size: 14px;
  opacity: 0.85;
}

.section {
  background: #fff;
  border-radius: 18px;
  padding: 24px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f7;
  
  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: 14px;
  color: #6e6e73;
}

.info-value {
  font-size: 14px;
  color: #1d1d1f;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-copy {
  padding: 2px 8px;
  font-size: 12px;
  background: #f5f5f7;
  color: #0071e3;
  border-radius: 4px;
  
  &:hover {
    background: #e8e8ed;
  }
}

.receiver-card {
  background: #f5f5f7;
  border-radius: 12px;
  padding: 16px;
}

.receiver-name {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.receiver-phone {
  font-size: 14px;
  color: #6e6e73;
  margin-bottom: 8px;
}

.receiver-address {
  font-size: 14px;
  color: #1d1d1f;
}

.trace-timeline {
  position: relative;
}

.trace-item {
  position: relative;
  padding-left: 32px;
  padding-bottom: 24px;
  
  &:last-child {
    padding-bottom: 0;
  }
}

.trace-dot {
  position: absolute;
  left: 0;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #d2d2d7;
  z-index: 1;
  
  .active & {
    background: #0071e3;
    box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.2);
  }
}

.trace-line {
  position: absolute;
  left: 5px;
  top: 16px;
  bottom: 0;
  width: 2px;
  background: #e8e8ed;
}

.trace-content {
  flex: 1;
}

.trace-message {
  font-size: 14px;
  color: #1d1d1f;
  margin-bottom: 4px;
  
  .active & {
    font-weight: 500;
    color: #0071e3;
  }
}

.trace-location {
  font-size: 13px;
  color: #6e6e73;
  margin-bottom: 4px;
}

.trace-time {
  font-size: 12px;
  color: #86868b;
}

.trace-empty {
  text-align: center;
  padding: 24px;
  color: #86868b;
  font-size: 14px;
}

.action-bar {
  text-align: center;
  margin-top: 24px;
}

.btn-query {
  padding: 12px 32px;
  background: #0071e3;
  color: #fff;
  border-radius: 980px;
  font-size: 15px;
  font-weight: 500;
  
  &:hover {
    background: #0077ed;
  }
}

.empty-state {
  text-align: center;
  padding: 80px 0;
}

.empty-icon {
  color: #d2d2d7;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 21px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #6e6e73;
}
</style>
