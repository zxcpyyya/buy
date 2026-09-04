<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #409eff">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-value">{{ stats.todayOrders || 0 }}</p>
            <p class="stat-label">今日订单</p>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #67c23a">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-value">¥{{ formatMoney(stats.todaySales || 0) }}</p>
            <p class="stat-label">今日销售额</p>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #e6a23c">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-value">{{ stats.pendingOrders || 0 }}</p>
            <p class="stat-label">待处理订单</p>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #f56c6c">
            <el-icon><Goods /></el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-value">{{ stats.lowStockProducts || 0 }}</p>
            <p class="stat-label">低库存商品</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <span>销售趋势</span>
          </template>
          <div ref="salesChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <span>销售占比</span>
          </template>
          <div ref="categoryChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新订单 -->
    <el-card class="order-card">
      <template #header>
        <div class="card-header">
          <span>最新订单</span>
          <el-button type="primary" link @click="$router.push('/admin/order/list')">
            查看更多
          </el-button>
        </div>
      </template>
      <el-table :data="recentOrders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userName" label="用户" width="100" />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row.status)">
              {{ getOrderStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="$router.push(`/admin/order/detail/${row.id}`)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Money, ShoppingCart, Goods } from '@element-plus/icons-vue'
import { statisticsApi } from '@/api/admin'
import * as echarts from 'echarts'

const stats = ref({})
const recentOrders = ref([])
const salesChartRef = ref()
const categoryChartRef = ref()

// 加载数据
async function loadData() {
  try {
    const res = await statisticsApi.dashboard()
    if (res.code === 200) {
      stats.value = res.data.stats || {}
      recentOrders.value = res.data.recentOrders || []
      initSalesChart(res.data.salesTrend)
      initCategoryChart(res.data.categorySales)
    }
  } catch (e) {
    console.error('加载数据失败', e)
    // 使用模拟数据
    stats.value = {
      todayOrders: 128,
      todaySales: 35680,
      pendingOrders: 23,
      lowStockProducts: 5
    }
    recentOrders.value = []
    initMockChart()
  }
}

// 初始化销售趋势图
function initSalesChart(data) {
  if (!salesChartRef.value) return
  const chart = echarts.init(salesChartRef.value)

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售额', '订单数'] },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: [
      { type: 'value', name: '销售额', axisLabel: { formatter: '¥{value}' } },
      { type: 'value', name: '订单数' }
    ],
    series: [
      {
        name: '销售额',
        type: 'bar',
        data: [32000, 45000, 38000, 52000, 48000, 35000, 42000]
      },
      {
        name: '订单数',
        type: 'line',
        yAxisIndex: 1,
        data: [45, 62, 55, 78, 68, 52, 65]
      }
    ]
  }

  chart.setOption(option)
}

// 初始化分类销售图
function initCategoryChart(data) {
  if (!categoryChartRef.value) return
  const chart = echarts.init(categoryChartRef.value)

  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: '60%',
        data: [
          { value: 35, name: '数码产品' },
          { value: 25, name: '服装鞋包' },
          { value: 20, name: '食品生鲜' },
          { value: 12, name: '家居用品' },
          { value: 8, name: '其他' }
        ]
      }
    ]
  }

  chart.setOption(option)
}

// 模拟图表数据
function initMockChart() {
  initSalesChart()
  initCategoryChart()
}

function formatMoney(value) {
  if (!value) return '0.00'
  return (value / 100).toFixed(2)
}

function getOrderStatusType(status) {
  const types = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
  return types[status] || 'info'
}

function getOrderStatusText(status) {
  const texts = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
  return texts[status] || '未知'
}

onMounted(() => {
  loadData()

  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (salesChartRef.value) {
      echarts.getInstanceByDom(salesChartRef.value)?.resize()
    }
    if (categoryChartRef.value) {
      echarts.getInstanceByDom(categoryChartRef.value)?.resize()
    }
  })
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin: 4px 0 0;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 350px;
}

.chart {
  height: 260px;
}

.order-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
