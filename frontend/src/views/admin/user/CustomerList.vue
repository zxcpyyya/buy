<template>
  <div class="customer-list">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
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

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="用户ID" width="100" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar">
              {{ row.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="账户余额" width="100">
          <template #default="{ row }">¥{{ row.balance || 0 }}</template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="100" />
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 1 && hasPermission('user:disable')"
              type="danger"
              link
              @click="handleDisable(row)"
            >
              禁用
            </el-button>
            <el-button
              v-else-if="row.status === 0"
              type="success"
              link
              @click="handleEnable(row)"
            >
              启用
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { customerApi } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  userId: null,
  username: '',
  phone: '',
  status: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const hasPermission = (perm) => userStore.hasPermission(perm)

async function loadData() {
  loading.value = true
  try {
    const res = await customerApi.list({
      ...searchForm,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error('加载数据失败', e)
    tableData.value = [
      {
        id: 1001,
        username: 'zhangsan',
        nickname: '张三',
        phone: '13800138001',
        email: 'zhangsan@example.com',
        avatar: '',
        status: 1,
        balance: 1000,
        points: 5000,
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
  searchForm.userId = null
  searchForm.username = ''
  searchForm.phone = ''
  searchForm.status = null
  handleSearch()
}

function handleDetail(row) {
  // 可以跳转到详情页
  console.log('查看用户详情', row)
}

async function handleDisable(row) {
  try {
    await ElMessageBox.confirm('确定要禁用该用户吗？禁用后该用户将无法登录', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await customerApi.disable(row.id)
    ElMessage.success('禁用成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('禁用失败')
    }
  }
}

async function handleEnable(row) {
  try {
    await customerApi.enable(row.id)
    ElMessage.success('启用成功')
    loadData()
  } catch (e) {
    ElMessage.error('启用失败')
  }
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
.customer-list {
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
