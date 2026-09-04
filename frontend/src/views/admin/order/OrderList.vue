<template>
  <div class="order-list">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="用户">
          <el-input v-model="searchForm.userName" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待支付" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="待收货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
          <el-button type="success" @click="handleExport" v-if="hasPermission('order:export')">
            <el-icon><Download /></el-icon> 导出
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userName" label="用户" width="100" />
        <el-table-column label="商品信息" min-width="200">
          <template #default="{ row }">
            <div v-for="item in row.items" :key="item.id" class="order-item">
              <span>{{ item.productName }} x {{ item.quantity }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="订单金额" width="100">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">
              详情
            </el-button>
            <el-button
              type="success"
              link
              @click="handleShip(row)"
              v-if="row.status === 1 && hasPermission('order:ship')"
            >
              发货
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleCancel(row)"
              v-if="row.status === 0 && hasPermission('order:cancel')"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 发货对话框 -->
    <el-dialog v-model="shipDialogVisible" title="订单发货" width="500px">
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-width="100px">
        <el-form-item label="物流公司" prop="expressCompany">
          <el-select v-model="shipForm.expressCompany" placeholder="请选择物流公司">
            <el-option label="顺丰速运" value="SF" />
            <el-option label="中通快递" value="ZTO" />
            <el-option label="圆通速递" value="YTO" />
            <el-option label="韵达快递" value="YD" />
            <el-option label="申通快递" value="STO" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" prop="expressNo">
          <el-input v-model="shipForm.expressNo" placeholder="请输入快递单号" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="shipForm.remark" type="textarea" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip">确定发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { orderApi } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const shipDialogVisible = ref(false)
const shipFormRef = ref()
const currentOrder = ref(null)

const searchForm = reactive({
  orderNo: '',
  userName: '',
  status: null,
  dateRange: []
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const shipForm = reactive({
  expressCompany: '',
  expressNo: '',
  remark: ''
})

const shipRules = {
  expressCompany: [{ required: true, message: '请选择物流公司', trigger: 'change' }],
  expressNo: [{ required: true, message: '请输入快递单号', trigger: 'blur' }]
}

// 权限检查
const hasPermission = (perm) => userStore.hasPermission(perm)

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await orderApi.list({
      ...searchForm,
      page: pagination.page,
      pageSize: pagination.pageSize,
      startDate: searchForm.dateRange?.[0],
      endDate: searchForm.dateRange?.[1]
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error('加载数据失败', e)
    // 模拟数据
    tableData.value = [
      {
        id: 1,
        orderNo: 'ORD2024010100001',
        userName: '张三',
        items: [{ productName: 'iPhone 15', quantity: 1 }],
        totalAmount: 5999,
        status: 1,
        createTime: '2024-01-01 10:30:00'
      }
    ]
    pagination.total = 1
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  loadData()
}

// 重置
function handleReset() {
  searchForm.orderNo = ''
  searchForm.userName = ''
  searchForm.status = null
  searchForm.dateRange = []
  handleSearch()
}

// 导出
async function handleExport() {
  try {
    await orderApi.export(searchForm)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

// 详情
function handleDetail(row) {
  router.push(`/admin/order/detail/${row.id}`)
}

// 发货
function handleShip(row) {
  currentOrder.value = row
  shipForm.expressCompany = ''
  shipForm.expressNo = ''
  shipForm.remark = ''
  shipDialogVisible.value = true
}

async function confirmShip() {
  if (!shipFormRef.value) return

  await shipFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      await orderApi.ship(currentOrder.value.id, shipForm)
      ElMessage.success('发货成功')
      shipDialogVisible.value = false
      loadData()
    } catch (e) {
      ElMessage.error('发货失败')
    }
  })
}

// 取消
async function handleCancel(row) {
  try {
    await ElMessageBox.prompt('确定要取消该订单吗？请输入取消原因：', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '取消原因'
    })

    await orderApi.cancel(row.id, { reason: '用户取消' })
    ElMessage.success('订单已取消')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

// 分页
function handleSizeChange() {
  loadData()
}

function handlePageChange() {
  loadData()
}

// 状态相关
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
.order-list {
  padding: 0;
}

.search-form {
  margin-bottom: 16px;
}

.order-item {
  line-height: 1.5;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
