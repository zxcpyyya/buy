<template>
  <div class="points-rule">
    <div class="page-header">
      <div>
        <h1 class="page-title">积分规则</h1>
        <p class="page-subtitle">配置用户积分获取和消耗规则</p>
      </div>
    </div>

    <div class="rules-container">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">积分获取规则</h3>
        </div>
        <div class="card-body">
          <div class="rule-item" v-for="rule in earnRules" :key="rule.id">
            <div class="rule-info">
              <div class="rule-name">{{ rule.name }}</div>
              <div class="rule-desc">{{ rule.description }}</div>
            </div>
            <div class="rule-value">
              <input type="number" v-model="rule.value" class="form-input" style="width: 80px;" />
              <span class="unit">积分</span>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <h3 class="card-title">积分兑换规则</h3>
        </div>
        <div class="card-body">
          <div class="rule-item">
            <div class="rule-info">
              <div class="rule-name">积分抵扣比例</div>
              <div class="rule-desc">每X积分可抵扣1元</div>
            </div>
            <div class="rule-value">
              <input type="number" v-model="exchangeRate" class="form-input" style="width: 80px;" />
              <span class="unit">: 1元</span>
            </div>
          </div>
          <div class="rule-item">
            <div class="rule-info">
              <div class="rule-name">单次最高抵扣</div>
              <div class="rule-desc">订单最多使用积分抵扣金额</div>
            </div>
            <div class="rule-value">
              <input type="number" v-model="maxDeduct" class="form-input" style="width: 80px;" />
              <span class="unit">元</span>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <h3 class="card-title">会员等级积分阈值</h3>
        </div>
        <div class="card-body">
          <div class="level-item" v-for="level in levels" :key="level.id">
            <div class="level-info">
              <span class="level-name" :style="{ color: level.color }">{{ level.name }}</span>
            </div>
            <div class="level-threshold">
              <span>累计积分达到</span>
              <input type="number" v-model="level.threshold" class="form-input" style="width: 100px;" />
              <span>即可升级</span>
            </div>
          </div>
        </div>
      </div>

      <div class="form-actions">
        <button class="btn btn-primary" @click="saveRules">保存配置</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const earnRules = ref([
  { id: 1, name: '消费返积分', description: '每消费1元返积分', value: 1 },
  { id: 2, name: '订单完成奖励', description: '每完成一笔订单额外奖励', value: 50 },
  { id: 3, name: '新手首次下单', description: '首次下单奖励', value: 200 },
  { id: 4, name: '每日签到', description: '连续签到额外奖励', value: 10 },
  { id: 5, name: '商品评价', description: '评价已购买的商品', value: 5 }
])

const exchangeRate = ref(100)
const maxDeduct = ref(50)

const levels = ref([
  { id: 1, name: '普通用户', color: '#86868B', threshold: 0 },
  { id: 2, name: '白银会员', color: '#C0C0C0', threshold: 1000 },
  { id: 3, name: '黄金会员', color: '#FFD700', threshold: 5000 },
  { id: 4, name: '钻石会员', color: '#9C27B0', threshold: 20000 }
])

const saveRules = () => {
  alert('保存成功')
}
</script>

<style scoped>
.points-rule { max-width: 800px; }

.rules-container {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.rule-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) 0;
  border-bottom: 1px solid var(--border-light);
}

.rule-item:last-child {
  border-bottom: none;
}

.rule-name {
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.rule-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.rule-value {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.unit {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.level-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) 0;
  border-bottom: 1px solid var(--border-light);
}

.level-item:last-child {
  border-bottom: none;
}

.level-name {
  font-weight: 600;
  font-size: var(--font-size-lg);
}

.level-threshold {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: var(--spacing-lg);
}
</style>
