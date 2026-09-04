<template>
  <div class="points-page">
    <section class="page-hero">
      <div class="hero-content">
        <div class="points-balance">
          <span class="balance-label">我的积分</span>
          <span class="balance-value">{{ pointsInfo.balance || 0 }}</span>
        </div>
        <div class="points-tips">
          <span class="tips-icon">💡</span>
          <span>每消费1元可获得1积分</span>
        </div>
      </div>
    </section>
    
    <div class="container-large">
      <section class="section">
        <h2 class="section-title">积分记录</h2>
        
        <div v-loading="loading" class="record-list">
          <div 
            v-for="record in records" 
            :key="record.id"
            class="record-item"
            :class="{ positive: record.points > 0, negative: record.points < 0 }"
          >
            <div class="record-left">
              <div class="record-type">{{ record.typeName }}</div>
              <div class="record-desc">{{ record.description || '-' }}</div>
              <div class="record-time">{{ formatTime(record.createTime) }}</div>
            </div>
            <div class="record-right">
              <span class="record-points">
                {{ record.points > 0 ? '+' : '' }}{{ record.points }}
              </span>
              <span class="record-balance">余额: {{ record.balance }}</span>
            </div>
          </div>
          
          <div v-if="!loading && records.length === 0" class="empty">
            <div class="empty-icon">
              <svg viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="32" cy="32" r="24"/>
                <path d="M32 20v12l8 4"/>
              </svg>
            </div>
            <p class="empty-text">暂无积分记录</p>
          </div>
        </div>
        
        <!-- 分页 -->
        <div v-if="total > pageSize" class="pagination">
          <button 
            class="page-btn" 
            :disabled="pageNum <= 1"
            @click="changePage(pageNum - 1)"
          >‹</button>
          <span class="page-info">{{ pageNum }} / {{ Math.ceil(total / pageSize) }}</span>
          <button 
            class="page-btn" 
            :disabled="pageNum >= Math.ceil(total / pageSize)"
            @click="changePage(pageNum + 1)"
          >›</button>
        </div>
      </section>
      
      <!-- 积分规则 -->
      <section class="section rules-section">
        <h2 class="section-title">积分规则</h2>
        <div class="rules-list">
          <div class="rule-item">
            <div class="rule-icon">🛒</div>
            <div class="rule-content">
              <h3>订单获取</h3>
              <p>每消费1元可获得1积分，订单完成后自动到账</p>
            </div>
          </div>
          <div class="rule-item">
            <div class="rule-icon">🎁</div>
            <div class="rule-content">
              <h3>活动赠送</h3>
              <p>参与平台活动可获得额外积分奖励</p>
            </div>
          </div>
          <div class="rule-item">
            <div class="rule-icon">⚠️</div>
            <div class="rule-content">
              <h3>积分扣除</h3>
              <p>订单取消时，积分将自动退还到账户</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import request from '@/utils/request'

const pointsInfo = ref({})
const records = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const fetchPointsInfo = async () => {
  try {
    pointsInfo.value = await request.get('/points')
  } catch (e) {
    console.error('获取积分信息失败', e)
  }
}

const fetchRecords = async () => {
  loading.value = true
  try {
    const data = await request.get('/points/records', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value }
    })
    records.value = data || []
    total.value = data?.length || 0
  } catch (e) {
    console.error('获取积分记录失败', e)
  } finally {
    loading.value = false
  }
}

const changePage = (page) => {
  pageNum.value = page
  fetchRecords()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  fetchPointsInfo()
  fetchRecords()
})
</script>

<style lang="scss" scoped>
.points-page {
  padding-top: 44px;
  padding-bottom: 80px;
  background: #fbfbfd;
  min-height: 100vh;
}

.page-hero {
  background: linear-gradient(135deg, #0071e3 0%, #5856d6 100%);
  padding: 80px 22px;
  color: #fff;
}

.hero-content {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}

.points-balance {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 24px;
}

.balance-label {
  font-size: 17px;
  opacity: 0.85;
}

.balance-value {
  font-size: 64px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.points-tips {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 980px;
  font-size: 14px;
}

.section {
  padding: 24px 0;
}

.section-title {
  font-size: 21px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 16px;
}

.record-list {
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f7;
  
  &:last-child {
    border-bottom: none;
  }
  
  &.positive .record-points {
    color: #30d158;
  }
  
  &.negative .record-points {
    color: #ff3b30;
  }
}

.record-left {
  flex: 1;
}

.record-type {
  font-size: 15px;
  font-weight: 500;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.record-desc {
  font-size: 13px;
  color: #6e6e73;
  margin-bottom: 4px;
}

.record-time {
  font-size: 12px;
  color: #86868b;
}

.record-right {
  text-align: right;
}

.record-points {
  font-size: 20px;
  font-weight: 600;
  display: block;
  margin-bottom: 4px;
}

.record-balance {
  font-size: 12px;
  color: #86868b;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

.page-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f5f5f7;
  color: #1d1d1f;
  font-size: 16px;
  
  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
}

.page-info {
  font-size: 14px;
  color: #6e6e73;
}

.rules-section {
  margin-top: 16px;
}

.rules-list {
  background: #fff;
  border-radius: 18px;
  padding: 8px 0;
}

.rule-item {
  display: flex;
  gap: 16px;
  padding: 20px 24px;
  border-bottom: 1px solid #f5f5f7;
  
  &:last-child {
    border-bottom: none;
  }
}

.rule-icon {
  font-size: 32px;
}

.rule-content {
  flex: 1;
  
  h3 {
    font-size: 15px;
    font-weight: 600;
    color: #1d1d1f;
    margin-bottom: 4px;
  }
  
  p {
    font-size: 13px;
    color: #6e6e73;
  }
}

.empty {
  text-align: center;
  padding: 60px 0;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  color: #d2d2d7;
}

.empty-text {
  font-size: 15px;
  color: #6e6e73;
}
</style>
