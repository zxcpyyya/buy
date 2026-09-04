<template>
  <div class="admin-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>管理员列表</span>
          <el-button type="primary" @click="handleAdd" v-if="hasPermission('system:admin')">
            <el-icon><Plus /></el-icon> 添加管理员
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column label="角色" width="200">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" class="role-tag">
              {{ getRoleName(role) }}
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
        <el-table-column prop="lastLoginTime" label="最后登录" width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleAssignRole(row)">分配角色</el-button>
            <el-button type="warning" link @click="handleResetPwd(row)">重置密码</el-button>
            <el-button
              v-if="row.id !== 1"
              type="danger"
              link
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑管理员' : '添加管理员'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
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

    <!-- 分配角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px">
      <el-form label-width="100px">
        <el-form-item label="用户名">{{ currentAdmin?.username }}</el-form-item>
        <el-form-item label="分配角色">
          <el-checkbox-group v-model="selectedRoles">
            <el-checkbox v-for="role in allRoles" :key="role.id" :label="role.code">
              {{ role.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssignRole">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { adminApi, roleApi } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const allRoles = ref([])
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const isEdit = ref(false)
const currentAdmin = ref(null)
const formRef = ref()
const selectedRoles = ref([])

const form = reactive({
  id: null,
  username: '',
  nickname: '',
  phone: '',
  email: '',
  password: '',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }]
}

const hasPermission = (perm) => userStore.hasPermission(perm)

function getRoleName(code) {
  const role = allRoles.value.find(r => r.code === code)
  return role?.name || code
}

async function loadData() {
  loading.value = true
  try {
    const res = await adminApi.list()
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (e) {
    console.error('加载数据失败', e)
    tableData.value = [
      { id: 1, username: 'superadmin', nickname: '超级管理员', phone: '13800000000', status: 1, roles: ['SUPER_ADMIN'], lastLoginTime: '2024-01-01 10:00:00' },
      { id: 2, username: 'operator', nickname: '运营人员', phone: '13800000001', status: 1, roles: ['OPERATION_ADMIN'], lastLoginTime: '2024-01-01 09:00:00' }
    ]
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  try {
    const res = await roleApi.list()
    if (res.code === 200) {
      allRoles.value = res.data || []
    }
  } catch (e) {
    allRoles.value = [
      { id: 1, code: 'SUPER_ADMIN', name: '超级管理员' },
      { id: 2, code: 'OPERATION_ADMIN', name: '运营管理员' },
      { id: 3, code: 'PRODUCT_ADMIN', name: '商品管理员' }
    ]
  }
}

function handleAdd() {
  isEdit.value = false
  form.id = null
  form.username = ''
  form.nickname = ''
  form.phone = ''
  form.email = ''
  form.password = ''
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
        await adminApi.update(form)
        ElMessage.success('修改成功')
      } else {
        await adminApi.add(form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  })
}

function handleAssignRole(row) {
  currentAdmin.value = row
  selectedRoles.value = [...(row.roles || [])]
  roleDialogVisible.value = true
}

async function confirmAssignRole() {
  try {
    await adminApi.assignRoles(currentAdmin.value.id, selectedRoles.value)
    ElMessage.success('分配成功')
    roleDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('分配失败')
  }
}

async function handleResetPwd(row) {
  try {
    await ElMessageBox.confirm('确定要重置该用户的密码吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await adminApi.resetPassword(row.id)
    ElMessage.success('密码已重置为: admin123')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('重置失败')
    }
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该管理员吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await adminApi.delete(row.id)
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
  loadRoles()
})
</script>

<style scoped>
.admin-list {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.role-tag {
  margin-right: 4px;
}
</style>
