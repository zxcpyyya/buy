<template>
  <div class="permission-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>权限列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 添加权限
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" row-key="id" default-expand-all :tree-props="{ children: 'children' }">
        <el-table-column prop="name" label="权限名称" width="200" />
        <el-table-column prop="code" label="权限标识" width="200" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.permissionType === 1 ? 'primary' : 'success'" size="small">
              {{ row.permissionType === 1 ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleAddChild(row)">添加子权限</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑权限' : '添加权限'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级权限" v-if="form.parentId">
          <el-input :value="getParentName(form.parentId)" disabled />
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限标识" prop="code">
          <el-input v-model="form.code" placeholder="请输入权限标识，如: product:add" />
        </el-form-item>
        <el-form-item label="权限类型">
          <el-radio-group v-model="form.permissionType">
            <el-radio :label="1">菜单</el-radio>
            <el-radio :label="2">按钮/操作</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="form.path" placeholder="请输入路由路径，如: /product/list" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { permissionApi } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  parentId: null,
  name: '',
  code: '',
  permissionType: 1,
  path: '',
  icon: '',
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入权限标识', trigger: 'blur' }]
}

function getParentName(parentId) {
  return findNode(tableData.value, parentId)?.name || ''
}

function findNode(nodes, id) {
  for (const node of nodes) {
    if (node.id === id) return node
    if (node.children) {
      const found = findNode(node.children, id)
      if (found) return found
    }
  }
  return null
}

async function loadData() {
  loading.value = true
  try {
    const res = await permissionApi.getTree()
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (e) {
    console.error('加载数据失败', e)
    tableData.value = [
      { id: 1, name: '系统管理', code: 'system', permissionType: 1, path: '/system', sort: 1, status: 1 },
      { id: 101, name: '管理员管理', code: 'system:admin', permissionType: 1, path: '/system/admin', sort: 1, status: 1, parentId: 1 },
      { id: 2, name: '商品管理', code: 'product', permissionType: 1, path: '/product', sort: 2, status: 1 }
    ]
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  form.id = null
  form.parentId = null
  form.name = ''
  form.code = ''
  form.permissionType = 1
  form.path = ''
  form.icon = ''
  form.sort = 0
  form.status = 1
  dialogVisible.value = true
}

function handleAddChild(row) {
  isEdit.value = false
  form.id = null
  form.parentId = row.id
  form.name = ''
  form.code = ''
  form.permissionType = 2
  form.path = ''
  form.icon = ''
  form.sort = 0
  form.status = 1
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.parentId = row.parentId
  form.name = row.name
  form.code = row.code
  form.permissionType = row.permissionType
  form.path = row.path || ''
  form.icon = row.icon || ''
  form.sort = row.sort || 0
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (isEdit.value) {
        await permissionApi.update(form)
        ElMessage.success('修改成功')
      } else {
        await permissionApi.add(form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  })
}

async function handleDelete(row) {
  if (row.children && row.children.length > 0) {
    ElMessage.warning('请先删除子权限')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除该权限吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await permissionApi.delete(row.id)
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
.permission-list {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
