<template>
  <div class="order-detail" v-loading="loading">
    <el-card v-if="order">
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <!-- 订单状态 -->
      <el-steps :active="getStepActive(order.status)" finish-status="success" class="order-steps">
        <el-step title="提交订单" :description="order.createTime" />
        <el-step title="支付成功" :description="order.payTime" />
        <el-step title="商家发货" :description="order.shipTime" />
        <el-step title="确认收货" :description="order.receiveTime" />
        <el-step title="完成评价" />
      </el-steps>

      <!-- 基本信息 -->
      <div class="section">
        <h3 class="section-title">订单信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ order.payMethod || '在线支付' }}</el-descriptions-item>
          <el-descriptions-item label="订单备注">{{ order.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 用户信息 -->
      <div class="section">
        <h3 class="section-title">用户信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ order.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ order.userName }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ order.receiverAddress }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 商品信息 -->
      <div class="section">
        <h3 class="section-title">商品信息</h3>
        <el-table :data="order.items" border>
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="specs" label="规格" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="subtotal" label="小计" width="100">
            <template #default="{ row }">¥{{ row.subtotal }}</template>
          </el-table-column>
        </el-table>

        <div class="order-summary">
          <div class="summary-item">
            <span>商品总价：</span>
            <span>¥{{ order.goodsAmount }}</span>
          </div>
          <div class="summary-item">
            <span>运费：</span>
            <span>¥{{ order.freightAmount || 0 }}</span>
          </div>
          <div class="summary-item">
            <span>优惠券：</span>
            <span>-¥{{ order.couponAmount || 0 }}</span>
          </div>
          <div class="summary-item total">
            <span>实付金额：</span>
            <span>¥{{ order.totalAmount }}</span>
          </div>
        </div>
      </div>

      <!-- 物流信息 -->
      <div class="section" v-if="order.expressNo">
        <h3 class="section-title">物流信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="物流公司">{{ order.expressCompany }}</el-descriptions-item>
          <el-descriptions-item label="快递单号">{{ order.expressNo }}</el-descriptions-item>
        </el-descriptions>

        <el-timeline class="express-timeline" v-if="order.expressTraces">
          <el-timeline-item
            v-for="(trace, index) in order.expressTraces"
            :key="index"
            :timestamp="trace.time"
            :type="index === 0 ? 'primary' : ''"
          >
            {{ trace.content }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { orderApi } from '@/api/admin'

const route = useRoute()
const loading = ref(false)
const order = ref(null)

async function loadData() {
  loading.value = true
  try {
    const res = await orderApi.detail(route.params.id)
    if (res.code === 200) {
      order.value = res.data
    }
  } catch (e) {
    ElMessage.error('加载订单详情失败')
    // 模拟数据
    order.value = {
      id: 1,
      orderNo: 'ORD2024010100001',
      status: 2,
      createTime: '2024-01-01 10:30:00',
      payTime: '2024-01-01 10:35:00',
      shipTime: '2024-01-01 15:00:00',
      receiveTime: null,
      userId: 1001,
      userName: '张三',
      receiverName: '张三',
      receiverPhone: '13800138000',
      receiverAddress: '北京市朝阳区某某街道某某小区1号楼101',
      remark: '请尽快发货',
      goodsAmount: 5999,
      freightAmount: 0,
      couponAmount: 50,
      totalAmount: 5949,
      expressCompany: '顺丰速运',
      expressNo: 'SF1234567890',
      expressTraces: [
        { time: '2024-01-01 15:30:00', content: '商品已发货' },
        { time: '2024-01-01 18:00:00', content: '商品已到达北京分拨中心' }
      ],
      items: [
        {
          id: 1,
          productName: 'iPhone 15 Pro Max',
          specs: '256GB 深空黑',
          price: 5999,
          quantity: 1,
          subtotal: 5999
        }
      ]
    }
  } finally {
    loading.value = false
  }
}

function getStepActive(status) {
  const map = { 0: 0, 1: 1, 2: 2, 3: 3, 4: 0 }
  return map[status] ?? 0
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
  return texts[status] || '未知'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.order-detail {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-steps {
  margin-bottom: 30px;
}

.section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.order-summary {
  margin-top: 16px;
  text-align: right;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.summary-item {
  line-height: 2;
  color: #666;
}

.summary-item.total {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.express-timeline {
  margin-top: 20px;
}
</style>
