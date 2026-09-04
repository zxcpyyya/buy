<template>
  <div class="merchant-list">
    <el-card>
      <template #header>
        <span>商家列表</span>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="merchantName" label="商家名称" width="150" />
        <el-table-column prop="merchantCode" label="商家编码" width="120" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { merchantApi } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])

async function loadData() {
  loading.value = true
  try {
    const res = await merchantApi.list()
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (e) {
    tableData.value = [
      { id: 1, merchantName: '测试商家001', merchantCode: 'MERCHANT001', contactName: '张经理', contactPhone: '13900000001', status: 1, createTime: '2024-01-01 10:00:00' }
    ]
  } finally {
    loading.value = false
  }
}

function getStatusType(status) {
  return { 1: 'success', 0: 'danger', 2: 'warning' }[status] || 'info'
}

function getStatusText(status) {
  return { 1: '启用', 0: '禁用', 2: '待审核' }[status] || '未知'
}

function handleEdit(row) {
  console.log('编辑商家', row)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.merchant-list {
  padding: 0;
}
</style>
