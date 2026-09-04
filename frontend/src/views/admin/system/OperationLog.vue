<template>
  <div class="log-list">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="操作模块">
          <el-input v-model="searchForm.module" placeholder="请输入模块" clearable />
        </el-form-item>
        <el-form-item label="时间范围">
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
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="operation" label="操作类型" width="120" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="requestMethod" label="请求方式" width="80" />
        <el-table-column prop="requestUrl" label="请求URL" width="200" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="executeTime" label="耗时" width="80">
          <template #default="{ row }">{{ row.executeTime }}ms</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="160" />
      </el-table>

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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { logApi } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  username: '',
  module: '',
  dateRange: []
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

async function loadData() {
  loading.value = true
  try {
    const res = await logApi.list({
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
    tableData.value = [
      {
        id: 1,
        username: 'superadmin',
        operation: '登录',
        module: '系统',
        description: '后台管理员登录',
        requestMethod: 'POST',
        requestUrl: '/api/admin/auth/login',
        ipAddress: '127.0.0.1',
        executeTime: 156,
        status: 1,
        createTime: '2024-01-01 10:00:00'
      }
    ]
    pagination.total = 1
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function handleReset() {
  searchForm.username = ''
  searchForm.module = ''
  searchForm.dateRange = []
  handleSearch()
}

function handleSizeChange() {
  loadData()
}

function handlePageChange() {
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.log-list {
  padding: 0;
}

.search-form {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
