<template>
  <div class="coupon-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>优惠券列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 创建优惠券
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="优惠券名称">
          <el-input v-model="searchForm.name" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="未发布" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="优惠券名称" width="150" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            {{ row.couponType === 1 ? '满减券' : '折扣券' }}
          </template>
        </el-table-column>
        <el-table-column prop="discountValue" label="优惠值" width="100">
          <template #default="{ row }">
            {{ row.couponType === 1 ? `满${row.minAmount}减${row.discountValue}` : `${row.discountValue * 10}折` }}
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="发行量" width="100" />
        <el-table-column prop="receivedCount" label="已领取" width="100" />
        <el-table-column prop="usedCount" label="已使用" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="有效期" width="200">
          <template #default="{ row }">
            {{ row.startTime }} ~ {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="success" link @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.status === 1" type="warning" link @click="handleUnpublish(row)">撤回</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { couponApi } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  name: '',
  status: null
})

function getStatusType(status) {
  return { 0: 'info', 1: 'success', 2: '' }[status] || 'info'
}

function getStatusText(status) {
  return { 0: '未发布', 1: '进行中', 2: '已结束' }[status] || '未知'
}

async function loadData() {
  loading.value = true
  try {
    const res = await couponApi.list(searchForm)
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (e) {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadData()
}

function handleReset() {
  searchForm.name = ''
  searchForm.status = null
  loadData()
}

function handleAdd() {
  router.push('/admin/marketing/coupon/add')
}

function handleEdit(row) {
  console.log('编辑优惠券', row)
}

async function handlePublish(row) {
  try {
    await couponApi.publish(row.id)
    ElMessage.success('发布成功')
    loadData()
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

async function handleUnpublish(row) {
  try {
    await couponApi.unpublish(row.id)
    ElMessage.success('撤回成功')
    loadData()
  } catch (e) {
    ElMessage.error('撤回失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该优惠券吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await couponApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.coupon-list {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
}

.search-form {
  margin-bottom: 16px;
}
</style>
