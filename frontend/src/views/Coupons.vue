<template>
  <div class="coupons-page">
    <section class="page-hero">
      <div class="container-large">
        <h1 class="page-title">我的优惠券</h1>
        <p class="page-subtitle">{{ totalCoupon }} 张优惠券</p>
      </div>
    </section>
    
    <!-- 状态筛选 -->
    <div class="coupon-tabs">
      <span 
        v-for="tab in tabs" 
        :key="tab.value"
        class="tab"
        :class="{ active: status === tab.value }"
        @click="changeStatus(tab.value)"
      >
        {{ tab.label }}
        <span v-if="tab.count" class="tab-count">{{ tab.count }}</span>
      </span>
    </div>
    
    <div class="container-large">
      <div v-loading="loading" class="coupon-list">
        <!-- 可领取优惠券 -->
        <section v-if="status === 'available'" class="section">
          <h2 class="section-title">可领取优惠券</h2>
          <div v-if="canReceiveTemplates.length > 0" class="template-list">
            <div 
              v-for="template in canReceiveTemplates" 
              :key="template.id"
              class="coupon-card coupon-template"
            >
              <div class="coupon-left">
                <div class="coupon-value">
                  <span v-if="template.couponType === 2" class="value-text">
                    {{ (template.discountValue * 10).toFixed(1) }}折
                  </span>
                  <span v-else class="value-text">
                    ¥{{ template.discountValue }}
                  </span>
                </div>
                <div class="coupon-condition">
                  <template v-if="template.minAmount > 0">
                    满{{ template.minAmount }}可用
                  </template>
                  <template v-else>
                    无门槛
                  </template>
                </div>
              </div>
              <div class="coupon-right">
                <div class="coupon-name">{{ template.name }}</div>
                <div class="coupon-desc">{{ template.description }}</div>
                <div class="coupon-time">
                  <template v-if="template.validType === 1">
                    {{ formatTime(template.startTime) }} - {{ formatTime(template.endTime) }}
                  </template>
                  <template v-else>
                    领取后{{ template.validDays }}天内有效
                  </template>
                </div>
                <button 
                  class="btn-receive" 
                  :disabled="!template.usable"
                  @click="handleReceive(template)"
                >
                  {{ template.usable ? '立即领取' : '已领取' }}
                </button>
              </div>
            </div>
          </div>
          <div v-else class="empty">
            <p>暂无可领取的优惠券</p>
          </div>
        </section>
        
        <!-- 用户优惠券 -->
        <section v-else class="section">
          <div v-if="coupons.length > 0" class="user-coupon-list">
            <div 
              v-for="coupon in coupons" 
              :key="coupon.id"
              class="coupon-card"
              :class="{ 
                'coupon-used': coupon.status === 1,
                'coupon-expired': coupon.status === 2 || !coupon.usable 
              }"
            >
              <div class="coupon-left">
                <div class="coupon-value">
                  <span v-if="coupon.couponType === 2" class="value-text">
                    {{ (coupon.discountValue * 10).toFixed(1) }}折
                  </span>
                  <span v-else class="value-text">
                    ¥{{ coupon.discountValue }}
                  </span>
                </div>
                <div class="coupon-condition">
                  <template v-if="coupon.minAmount > 0">
                    满{{ coupon.minAmount }}可用
                  </template>
                  <template v-else>
                    无门槛
                  </template>
                </div>
              </div>
              <div class="coupon-right">
                <div class="coupon-name">{{ coupon.name }}</div>
                <div class="coupon-time">
                  <template v-if="coupon.status === 0">
                    剩余 {{ coupon.remainDays }} 天
                  </template>
                  <template v-else-if="coupon.status === 1">
                    已使用
                  </template>
                  <template v-else>
                    已过期
                  </template>
                </div>
              </div>
              <div class="coupon-status-tag" v-if="coupon.status === 1">已使用</div>
              <div class="coupon-status-tag expired" v-if="coupon.status === 2 || (!coupon.usable && coupon.status === 0)">已过期</div>
            </div>
          </div>
          <div v-else class="empty">
            <div class="empty-icon">
              <svg viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="10" y="16" width="44" height="32" rx="2"/>
                <path d="M10 24h44M22 16V12a10 10 0 0120 0v4"/>
              </svg>
            </div>
            <p class="empty-text">暂无优惠券</p>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { showToast } from 'vant'
import request from '@/utils/request'

const coupons = ref([])
const availableTemplates = ref([])
const loading = ref(false)
const status = ref(0) // 0-未使用，1-已使用，2-已过期

// 只显示可领取的优惠券（过滤掉已领取的）
const canReceiveTemplates = computed(() => {
  return availableTemplates.value.filter(t => t.usable)
})

const tabs = computed(() => [
  { label: '未使用', value: 0, count: totalCoupon.value?.unused || 0 },
  { label: '已使用', value: 1, count: totalCoupon.value?.used || 0 },
  { label: '已过期', value: 2, count: totalCoupon.value?.expired || 0 },
  { label: '可领取', value: 'available', count: null }
])

const totalCoupon = ref({})

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleDateString('zh-CN')
}

const fetchCoupons = async () => {
  loading.value = true
  try {
    coupons.value = await request.get('/coupon', { params: { status: status.value } })
  } catch (e) {
    console.error('获取优惠券失败', e)
  } finally {
    loading.value = false
  }
}

const fetchAvailableTemplates = async () => {
  loading.value = true
  try {
    availableTemplates.value = await request.get('/coupon/templates')
  } catch (e) {
    console.error('获取优惠券模板失败', e)
  } finally {
    loading.value = false
  }
}

const fetchTotal = async () => {
  try {
    const [unused, used] = await Promise.all([
      request.get('/coupon', { params: { status: 0 } }),
      request.get('/coupon', { params: { status: 1 } })
    ])
    totalCoupon.value = {
      unused: unused?.length || 0,
      used: used?.length || 0
    }
  } catch (e) {
    console.error('获取统计失败', e)
  }
}

const changeStatus = async (val) => {
  status.value = val
  if (val === 'available') {
    await fetchAvailableTemplates()
  } else {
    await fetchCoupons()
  }
}

const handleReceive = async (template) => {
  try {
    await request.post('/coupon/receive', null, { params: { templateId: template.id } })
    showToast({ message: '领取成功', icon: 'success' })
    await fetchAvailableTemplates()
    await fetchTotal()
  } catch (e) {
    console.error('领取失败', e)
  }
}

onMounted(() => {
  fetchCoupons()
  fetchTotal()
})
</script>

<style lang="scss" scoped>
.coupons-page {
  padding-top: 44px;
  padding-bottom: 80px;
  background: #fbfbfd;
  min-height: 100vh;
}

.page-hero {
  padding: 80px 0 48px;
  text-align: center;
  background: linear-gradient(135deg, #ff9500 0%, #ff6b00 100%);
  color: #fff;
}

.page-title {
  font-size: 40px;
  font-weight: 600;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 17px;
  opacity: 0.9;
}

.coupon-tabs {
  display: flex;
  gap: 8px;
  padding: 16px 22px;
  background: #fff;
  border-bottom: 1px solid #f5f5f7;
}

.tab {
  padding: 8px 16px;
  font-size: 14px;
  color: #6e6e73;
  border-radius: 980px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
  
  &:hover {
    background: #f5f5f7;
  }
  
  &.active {
    background: #1d1d1f;
    color: #fff;
  }
}

.tab-count {
  font-size: 12px;
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 10px;
  
  .active & {
    background: rgba(255, 255, 255, 0.2);
  }
}

.section-title {
  font-size: 21px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 16px;
  padding-top: 24px;
}

.coupon-list {
  padding: 16px 0;
}

.coupon-card {
  display: flex;
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  margin-bottom: 12px;
  position: relative;
  
  &.coupon-template {
    border: 1px solid #e8e8ed;
  }
  
  &.coupon-used,
  &.coupon-expired {
    opacity: 0.6;
    
    .coupon-left {
      background: #86868b;
    }
  }
}

.coupon-left {
  width: 120px;
  padding: 24px 16px;
  background: linear-gradient(135deg, #ff9500 0%, #ff6b00 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.coupon-value {
  .value-text {
    font-size: 28px;
    font-weight: 700;
  }
}

.coupon-condition {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 4px;
}

.coupon-right {
  flex: 1;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.coupon-name {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 8px;
}

.coupon-desc {
  font-size: 13px;
  color: #6e6e73;
  margin-bottom: 8px;
}

.coupon-time {
  font-size: 12px;
  color: #86868b;
}

.btn-receive {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  padding: 8px 20px;
  background: #ff9500;
  color: #fff;
  border-radius: 980px;
  font-size: 14px;
  font-weight: 500;
  
  &:disabled {
    background: #d2d2d7;
  }
}

.coupon-status-tag {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%) rotate(-15deg);
  padding: 4px 12px;
  background: #30d158;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  border-radius: 4px;
  
  &.expired {
    background: #86868b;
  }
}

.empty {
  text-align: center;
  padding: 80px 0;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  color: #d2d2d7;
}

.empty-text {
  font-size: 17px;
  color: #6e6e73;
}
</style>
