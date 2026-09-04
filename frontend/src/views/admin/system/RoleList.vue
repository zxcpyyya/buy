<template>
  <div class="role-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色列表</span>
          <el-button type="primary" @click="handleAdd" v-if="hasPermission('system:role')">
            <el-icon><Plus /></el-icon> 添加角色
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="code" label="角色标识" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.roleType === 1 ? 'primary' : 'success'">
              {{ row.roleType === 1 ? '后台管理员' : '商家' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleAssignPerm(row)">分配权限</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-if="row.code !== 'SUPER_ADMIN'">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '添加角色'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色标识" prop="code">
          <el-input v-model="form.code" placeholder="请输入角色标识，如: ADMIN" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="角色类型">
          <el-radio-group v-model="form.roleType">
            <el-radio :label="1">后台管理员</el-radio>
            <el-radio :label="2">商家</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="600px">
      <el-form label-width="100px">
        <el-form-item label="角色">{{ currentRole?.name }}</el-form-item>
        <el-form-item label="权限列表">
          <el-tree
            ref="permTreeRef"
            :data="permissionTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            :default-checked-keys="selectedPerms"
            show-checkbox
            default-expand-all
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssignPerm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { roleApi, permissionApi } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const permissionTree = ref([])
const dialogVisible = ref(false)
const permDialogVisible = ref(false)
const isEdit = ref(false)
const currentRole = ref(null)
const formRef = ref()
const permTreeRef = ref()
const selectedPerms = ref([])

const form = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  roleType: 1,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}

const hasPermission = (perm) => userStore.hasPermission(perm)

async function loadData() {
  loading.value = true
  try {
    const res = await roleApi.list()
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (e) {
    console.error('加载数据失败', e)
    tableData.value = [
      { id: 1, name: '超级管理员', code: 'SUPER_ADMIN', description: '拥有所有权限', roleType: 1, status: 1 },
      { id: 2, name: '运营管理员', code: 'OPERATION_ADMIN', description: '负责日常运营', roleType: 1, status: 1 },
      { id: 4, name: '商家', code: 'MERCHANT', description: '商家端用户', roleType: 2, status: 1 }
    ]
  } finally {
    loading.value = false
  }
}

async function loadPermissions() {
  try {
    const res = await permissionApi.getTree()
    if (res.code === 200) {
      permissionTree.value = res.data || []
    }
  } catch (e) {
    console.error('加载权限失败', e)
  }
}

function handleAdd() {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.code = ''
  form.description = ''
  form.roleType = 1
  form.status = 1
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (isEdit.value) {
        await roleApi.update(form)
        ElMessage.success('修改成功')
      } else {
        await roleApi.add(form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  })
}

async function handleAssignPerm(row) {
  currentRole.value = row
  selectedPerms.value = []

  try {
    const res = await roleApi.getPermissions(row.id)
    if (res.code === 200) {
      selectedPerms.value = res.data || []
    }
  } catch (e) {
    console.error('加载角色权限失败', e)
  }

  permDialogVisible.value = true
}

async function confirmAssignPerm() {
  const checkedKeys = permTreeRef.value?.getCheckedKeys() || []
  const halfCheckedKeys = permTreeRef.value?.getHalfCheckedKeys() || []
  const allKeys = [...checkedKeys, ...halfCheckedKeys]

  try {
    await roleApi.assignPermissions(currentRole.value.id, allKeys)
    ElMessage.success('分配成功')
    permDialogVisible.value = false
  } catch (e) {
    ElMessage.error('分配失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await roleApi.delete(row.id)
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
  loadPermissions()
})
</script>

<style scoped>
.role-list {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
