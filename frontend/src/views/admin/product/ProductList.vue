<template>
  <div class="product-list">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
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

      <!-- 操作按钮 -->
      <div class="table-operations">
        <el-button type="primary" @click="handleAdd" v-if="hasPermission('product:add')">
          <el-icon><Plus /></el-icon> 添加商品
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image
              :src="row.mainImage"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px"
            >
              <template #error>
                <div class="image-placeholder">暂无图片</div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="200">
          <template #default="{ row }">
            <div class="product-name">
              <p class="name">{{ row.name }}</p>
              <p class="sub-name">{{ row.subtitle }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" v-if="hasPermission('product:edit')">
              编辑
            </el-button>
            <el-button type="success" link @click="handlePublish(row)" v-if="hasPermission('product:publish')">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="hasPermission('product:delete')">
              删除
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { productApi } from '@/api/admin'
import { categoryApi } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const categories = ref([])

const searchForm = reactive({
  name: '',
  categoryId: null,
  status: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 权限检查
const hasPermission = (perm) => userStore.hasPermission(perm)

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await productApi.list({
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
    // 模拟数据
    tableData.value = [
      {
        id: 1,
        name: 'iPhone 15 Pro Max',
        subtitle: '苹果旗舰手机',
        mainImage: '',
        categoryName: '手机数码',
        price: 9999,
        stock: 100,
        status: 1,
        sales: 520
      }
    ]
    pagination.total = 1
  } finally {
    loading.value = false
  }
}

// 加载分类
async function loadCategories() {
  try {
    const res = await categoryApi.list()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  loadData()
}

// 重置
function handleReset() {
  searchForm.name = ''
  searchForm.categoryId = null
  searchForm.status = null
  handleSearch()
}

// 添加
function handleAdd() {
  router.push('/admin/product/add')
}

// 编辑
function handleEdit(row) {
  router.push(`/admin/product/edit/${row.id}`)
}

// 上下架
async function handlePublish(row) {
  const action = row.status === 1 ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确定要${action}该商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    if (row.status === 1) {
      await productApi.unpublish(row.id)
    } else {
      await productApi.publish(row.id)
    }
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 删除
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？删除后不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await productApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
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

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<style scoped>
.product-list {
  padding: 0;
}

.search-form {
  margin-bottom: 16px;
}

.table-operations {
  margin-bottom: 16px;
}

.product-name .name {
  margin: 0;
  font-weight: 500;
}

.product-name .sub-name {
  margin: 4px 0 0;
  font-size: 12px;
  color: #999;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.image-placeholder {
  width: 60px;
  height: 60px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
