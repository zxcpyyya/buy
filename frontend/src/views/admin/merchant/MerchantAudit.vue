<template>
  <div class="merchant-audit">
    <el-card>
      <template #header>
        <span>商家审核</span>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="merchantName" label="商家名称" width="150" />
        <el-table-column prop="merchantCode" label="商家编码" width="120" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="businessLicense" label="营业执照">
          <template #default="{ row }">
            <el-image v-if="row.businessLicense" :src="row.businessLicense" style="width: 60px; height: 60px" fit="cover" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag type="warning">待审核</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="success" link @click="handleAudit(row, 1)">通过</el-button>
            <el-button type="danger" link @click="handleAudit(row, 0)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { merchantApi } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])

async function loadData() {
  loading.value = true
  try {
    const res = await merchantApi.list({ status: 2 })
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (e) {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

async function handleAudit(row, status) {
  const action = status === 1 ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定要${action}该商家申请吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await merchantApi.audit(row.id, status)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.merchant-audit {
  padding: 0;
}
</style>
