<template>
  <div class="points-page safe-area-top">
    <header class="header">
      <button class="back-btn" @click="$router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </button>
      <h1 class="title">积分中心</h1>
      <div class="placeholder"></div>
    </header>

    <!-- 积分卡片 -->
    <div class="points-card">
      <div class="points-value">
        <span class="label">我的积分</span>
        <span class="value">{{ points }}</span>
      </div>
      <div class="points-tip">每消费1元 = 1积分</div>
    </div>

    <!-- 积分记录 -->
    <div class="section">
      <h3 class="section-title">积分明细</h3>
      <div class="records-list">
        <div class="record-item" v-for="record in records" :key="record.id">
          <div class="record-left">
            <div class="record-type">{{ record.type }}</div>
            <div class="record-time">{{ record.time }}</div>
          </div>
          <div class="record-value" :class="{ positive: record.value > 0 }">
            {{ record.value > 0 ? '+' : '' }}{{ record.value }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const points = ref(1280)
const records = ref([
  { id: 1, type: '订单完成奖励', time: '2024-09-04 15:30', value: 10898 },
  { id: 2, type: '积分商城兑换', time: '2024-09-02 10:20', value: -500 },
  { id: 3, type: '订单完成奖励', time: '2024-09-01 09:15', value: 8999 },
  { id: 4, type: '新手注册奖励', time: '2024-08-28 20:00', value: 100 },
])
</script>

<style lang="scss" scoped>
.points-page { min-height: 100vh; background: var(--bg-secondary); }

.header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; background: var(--bg-primary); }
.back-btn { width: 32px; height: 32px; border: none; background: transparent; cursor: pointer; }
.title { font-size: 18px; font-weight: 600; }
.placeholder { width: 32px; }

.points-card {
  margin: 16px;
  padding: 24px;
  background: linear-gradient(135deg, #ffd700 0%, #ff8c00 100%);
  border-radius: var(--radius-xl);
  color: #1d1d1f;
}

.points-value { text-align: center; margin-bottom: 8px; }
.points-value .label { font-size: 14px; opacity: 0.8; display: block; margin-bottom: 4px; }
.points-value .value { font-size: 48px; font-weight: 700; }
.points-tip { text-align: center; font-size: 13px; opacity: 0.8; }

.section { background: var(--bg-primary); margin: 16px; border-radius: var(--radius-xl); padding: 16px; }
.section-title { font-size: 16px; font-weight: 600; margin-bottom: 16px; }

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--bg-secondary);
  &:last-child { border-bottom: none; }
}

.record-left .record-type { font-size: 14px; font-weight: 500; margin-bottom: 4px; }
.record-left .record-time { font-size: 12px; color: var(--text-tertiary); }
.record-value { font-size: 16px; font-weight: 600; &.positive { color: var(--apple-green); } }
</style>
