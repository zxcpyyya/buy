<template>
  <div class="admin-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">管理员管理</h1>
        <p class="page-subtitle">管理系统管理员账户</p>
      </div>
      <button class="btn btn-primary" @click="showAddModal = true">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"/>
          <line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        添加管理员
      </button>
    </div>

    <!-- 管理员列表 -->
    <div class="table-container">
      <table class="table">
        <thead>
          <tr>
            <th style="width: 80px">头像</th>
            <th>用户名</th>
            <th>昵称</th>
            <th>角色</th>
            <th>邮箱</th>
            <th>状态</th>
            <th>最后登录</th>
            <th style="width: 150px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="admin in admins" :key="admin.id">
            <td>
              <div class="admin-avatar">
                {{ getInitials(admin.nickname || admin.username) }}
              </div>
            </td>
            <td class="font-medium">{{ admin.username }}</td>
            <td>{{ admin.nickname || '-' }}</td>
            <td>
              <div class="role-tags">
                <span v-for="role in admin.roles" :key="role" class="role-tag">{{ getRoleName(role) }}</span>
              </div>
            </td>
            <td>{{ admin.email || '-' }}</td>
            <td>
              <span class="status-badge" :class="admin.status === 1 ? 'success' : 'secondary'">
                {{ admin.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="text-secondary">{{ admin.lastLoginTime || '从未登录' }}</td>
            <td>
              <div class="action-buttons">
                <button class="btn btn-ghost btn-sm" @click="editAdmin(admin)">编辑</button>
                <button class="btn btn-ghost btn-sm" @click="toggleStatus(admin)">
                  {{ admin.status === 1 ? '禁用' : '启用' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 空状态 -->
      <div v-if="admins.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <div class="empty-title">暂无管理员</div>
        <div class="empty-description">点击"添加管理员"按钮来创建管理员账户</div>
      </div>
    </div>

    <!-- 添加/编辑弹窗 -->
    <div v-if="showAddModal || showEditModal" class="modal-overlay active" @click.self="closeModal">
      <div class="modal" style="max-width: 500px;">
        <div class="modal-header">
          <h3 class="modal-title">{{ showEditModal ? '编辑管理员' : '添加管理员' }}</h3>
          <button class="modal-close" @click="closeModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label required">用户名</label>
            <input
              v-model="adminForm.username"
              type="text"
              class="form-input"
              placeholder="请输入用户名"
              :disabled="showEditModal"
            />
          </div>
          <div v-if="!showEditModal" class="form-group">
            <label class="form-label required">密码</label>
            <input
              v-model="adminForm.password"
              type="password"
              class="form-input"
              placeholder="请输入密码"
            />
          </div>
          <div class="form-group">
            <label class="form-label">昵称</label>
            <input
              v-model="adminForm.nickname"
              type="text"
              class="form-input"
              placeholder="请输入昵称"
            />
          </div>
          <div class="form-group">
            <label class="form-label">邮箱</label>
            <input
              v-model="adminForm.email"
              type="email"
              class="form-input"
              placeholder="请输入邮箱"
            />
          </div>
          <div class="form-group">
            <label class="form-label required">分配角色</label>
            <div class="role-checkboxes">
              <label v-for="role in allRoles" :key="role.id" class="role-checkbox">
                <input type="checkbox" v-model="adminForm.roleIds" :value="role.id" />
                <span>{{ role.name }}</span>
              </label>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">状态</label>
            <div class="switch-wrapper">
              <label class="switch">
                <input type="checkbox" v-model="adminForm.status" :checked="adminForm.status === 1" />
                <span class="slider"></span>
              </label>
              <span class="switch-label">{{ adminForm.status === 1 ? '启用' : '禁用' }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="saveAdmin">{{ showEditModal ? '保存' : '创建' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

// 角色列表
const allRoles = ref([
  { id: 1, name: '超级管理员', code: 'SUPER_ADMIN' },
  { id: 2, name: '运营管理员', code: 'OPERATION_ADMIN' },
  { id: 3, name: '商品管理员', code: 'PRODUCT_ADMIN' },
  { id: 4, name: '商家', code: 'MERCHANT' },
  { id: 5, name: '客服', code: 'CUSTOMER_SERVICE' }
])

// 管理员列表
const admins = ref([
  {
    id: 1,
    username: 'superadmin',
    nickname: '超级管理员',
    email: 'admin@example.com',
    roles: ['SUPER_ADMIN'],
    status: 1,
    lastLoginTime: '2024-09-04 10:30'
  },
  {
    id: 2,
    username: 'operator',
    nickname: '运营人员',
    email: 'operator@example.com',
    roles: ['OPERATION_ADMIN'],
    status: 1,
    lastLoginTime: '2024-09-04 09:15'
  },
  {
    id: 3,
    username: 'merchant001',
    nickname: '测试商家',
    email: 'merchant@example.com',
    roles: ['MERCHANT'],
    status: 1,
    lastLoginTime: '2024-09-03 14:20'
  }
])

// 弹窗状态
const showAddModal = ref(false)
const showEditModal = ref(false)
const selectedAdmin = ref(null)

// 管理员表单
const adminForm = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  roleIds: [],
  status: 1
})

// 方法
const getInitials = (name) => {
  return (name || 'A').charAt(0).toUpperCase()
}

const getRoleName = (code) => {
  const role = allRoles.value.find(r => r.code === code)
  return role?.name || code
}

const editAdmin = (admin) => {
  selectedAdmin.value = admin
  adminForm.username = admin.username
  adminForm.nickname = admin.nickname
  adminForm.email = admin.email
  adminForm.roleIds = admin.roles.map(code => {
    const role = allRoles.value.find(r => r.code === code)
    return role?.id
  }).filter(Boolean)
  adminForm.status = admin.status
  showEditModal.value = true
}

const toggleStatus = (admin) => {
  admin.status = admin.status === 1 ? 0 : 1
}

const saveAdmin = () => {
  if (!adminForm.username) {
    alert('请输入用户名')
    return
  }
  if (!showEditModal.value && !adminForm.password) {
    alert('请输入密码')
    return
  }
  if (adminForm.roleIds.length === 0) {
    alert('请选择至少一个角色')
    return
  }

  // 保存逻辑
  closeModal()
}

const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  adminForm.username = ''
  adminForm.password = ''
  adminForm.nickname = ''
  adminForm.email = ''
  adminForm.roleIds = []
  adminForm.status = 1
  selectedAdmin.value = null
}
</script>

<style scoped>
.admin-list {
  max-width: 1200px;
}

.admin-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-info));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.role-tag {
  font-size: 11px;
  padding: 2px 8px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: 10px;
}

.role-checkboxes {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.role-checkbox {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
  font-size: var(--font-size-base);
}

.role-checkbox input {
  width: 16px;
  height: 16px;
  accent-color: var(--color-primary);
}

/* 开关样式 */
.switch-wrapper {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.switch {
  position: relative;
  width: 44px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--border-color);
  transition: var(--transition-fast);
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 2px;
  bottom: 2px;
  background-color: white;
  transition: var(--transition-fast);
  border-radius: 50%;
}

.switch input:checked + .slider {
  background-color: var(--color-primary);
}

.switch input:checked + .slider:before {
  transform: translateX(20px);
}

.switch-label {
  font-size: var(--font-size-base);
  color: var(--text-primary);
}

.action-buttons {
  display: flex;
  gap: var(--spacing-xs);
}
</style>
