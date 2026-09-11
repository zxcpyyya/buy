<template>
  <div class="express-page safe-area-top">
    <header class="header">
      <button class="back-btn" @click="$router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </button>
      <h1 class="title">物流追踪</h1>
      <div class="placeholder"></div>
    </header>

    <!-- 物流信息 -->
    <div class="express-card">
      <div class="express-no">
        <span class="label">运单编号</span>
        <span class="value">{{ expressNo }}</span>
      </div>
      <div class="express-status">
        <div class="status-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="1" y="3" width="15" height="13"/>
            <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/>
            <circle cx="5.5" cy="18.5" r="2.5"/>
            <circle cx="18.5" cy="18.5" r="2.5"/>
          </svg>
        </div>
        <div class="status-info">
          <div class="status-text">{{ currentStatus }}</div>
          <div class="status-time">{{ currentTime }}</div>
        </div>
      </div>
    </div>

    <!-- 物流轨迹 -->
    <div class="timeline-section">
      <div class="timeline">
        <div class="timeline-item completed" v-for="(item, index) in tracks" :key="index">
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <div class="timeline-title">{{ item.status }}</div>
            <div class="timeline-desc">{{ item.desc }}</div>
            <div class="timeline-time">{{ item.time }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const expressNo = ref('SF1234567890')
const currentStatus = ref('包裹已到达深圳市南山区')
const currentTime = ref('2024-09-05 14:30')

const tracks = ref([
  { status: '已签收', desc: '已签收，感谢使用顺丰速运', time: '2024-09-05 14:30' },
  { status: '派送中', desc: '正在为您派送，配送员：张三 138****8888', time: '2024-09-05 10:00' },
  { status: '到达网点', desc: '快件到达【深圳南山区科技园分部】', time: '2024-09-05 08:00' },
  { status: '运输中', desc: '快件在途，前往深圳', time: '2024-09-04 20:00' },
  { status: '已发出', desc: '快件已从【广州白云航空枢纽】发出', time: '2024-09-04 18:00' },
  { status: '已揽收', desc: '商家已发货，快件已揽收', time: '2024-09-04 15:00' },
])
</script>

<style lang="scss" scoped>
.express-page { min-height: 100vh; background: var(--bg-secondary); }

.header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; background: var(--bg-primary); }
.back-btn { width: 32px; height: 32px; border: none; background: transparent; cursor: pointer; }
.title { font-size: 18px; font-weight: 600; }
.placeholder { width: 32px; }

.express-card {
  margin: 16px;
  padding: 20px;
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
}

.express-no { display: flex; justify-content: space-between; padding-bottom: 16px; border-bottom: 1px solid var(--bg-secondary); margin-bottom: 16px; }
.express-no .label { font-size: 14px; color: var(--text-secondary); }
.express-no .value { font-size: 14px; font-weight: 500; }

.express-status { display: flex; gap: 16px; align-items: center; }
.status-icon { width: 48px; height: 48px; background: var(--bg-secondary); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: var(--apple-blue); }
.status-info .status-text { font-size: 16px; font-weight: 600; margin-bottom: 4px; }
.status-info .status-time { font-size: 13px; color: var(--text-secondary); }

.timeline-section { background: var(--bg-primary); margin: 16px; border-radius: var(--radius-xl); padding: 20px; }
.timeline { position: relative; }

.timeline-item {
  position: relative;
  padding-left: 24px;
  padding-bottom: 24px;
  border-left: 2px solid var(--bg-secondary);
  &:last-child { border-left: none; padding-bottom: 0; }
  &.completed { border-left-color: var(--apple-blue); }
}

.timeline-dot {
  position: absolute;
  left: -7px;
  top: 0;
  width: 12px;
  height: 12px;
  background: var(--bg-secondary);
  border-radius: 50%;
  .completed & { background: var(--apple-blue); }
}

.timeline-content { }
.timeline-title { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.timeline-desc { font-size: 13px; color: var(--text-secondary); margin-bottom: 4px; }
.timeline-time { font-size: 12px; color: var(--text-tertiary); }
</style>
