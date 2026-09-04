<template>
  <div class="role-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">角色权限</h1>
        <p class="page-subtitle">管理系统角色和权限配置</p>
      </div>
      <button class="btn btn-primary" @click="showAddModal = true">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"/>
          <line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        添加角色
      </button>
    </div>

    <!-- 角色卡片列表 -->
    <div class="roles-grid">
      <div
        v-for="role in roles"
        :key="role.id"
        class="role-card card"
        :class="{ selected: selectedRole?.id === role.id }"
        @click="selectRole(role)"
      >
        <div class="role-header">
          <div class="role-icon" :class="role.iconClass">
            <svg v-if="role.code === 'SUPER_ADMIN'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
            </svg>
            <svg v-else-if="role.code === 'MERCHANT'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
              <polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
            <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <div class="role-info">
            <h3 class="role-name">{{ role.name }}</h3>
            <p class="role-desc">{{ role.description }}</p>
          </div>
          <div class="role-status">
            <span class="status-badge" :class="role.status === 1 ? 'success' : 'secondary'">
              {{ role.status === 1 ? '启用' : '禁用' }}
            </span>
          </div>
        </div>

        <div class="role-meta">
          <div class="meta-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
            </svg>
            <span>{{ role.userCount }} 个用户</span>
          </div>
          <div class="meta-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <span>{{ role.permissionCount }} 项权限</span>
          </div>
        </div>

        <div class="role-actions">
          <button
            v-if="role.code !== 'SUPER_ADMIN'"
            class="btn btn-ghost btn-sm"
            @click.stop="editRole(role)"
          >
            编辑
          </button>
          <button
            v-if="role.code !== 'SUPER_ADMIN'"
            class="btn btn-ghost btn-sm"
            @click.stop="toggleRoleStatus(role)"
          >
            {{ role.status === 1 ? '禁用' : '启用' }}
          </button>
          <button
            class="btn btn-ghost btn-sm"
            @click.stop="viewRoleDetail(role)"
          >
            权限配置
          </button>
        </div>
      </div>
    </div>

    <!-- 权限配置弹窗 -->
    <div v-if="showPermissionModal" class="modal-overlay active" @click.self="showPermissionModal = false">
      <div class="modal" style="max-width: 900px;">
        <div class="modal-header">
          <h3 class="modal-title">权限配置 - {{ selectedRole?.name }}</h3>
          <button class="modal-close" @click="showPermissionModal = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="permission-tree">
            <div v-for="module in permissionModules" :key="module.id" class="permission-module">
              <div class="module-header">
                <label class="module-checkbox">
                  <input
                    type="checkbox"
                    :checked="isModuleChecked(module)"
                    :indeterminate="isModuleIndeterminate(module)"
                    @change="toggleModule(module)"
                  />
                  <span class="module-name">{{ module.name }}</span>
                </label>
              </div>
              <div class="module-permissions">
                <div v-for="permission in module.permissions" :key="permission.id" class="permission-item">
                  <label class="permission-checkbox">
                    <input
                      type="checkbox"
                      v-model="selectedPermissions"
                      :value="permission.code"
                    />
                    <span class="permission-name">{{ permission.name }}</span>
                  </label>
                  <div v-if="permission.children?.length" class="permission-children">
                    <label
                      v-for="child in permission.children"
                      :key="child.id"
                      class="permission-checkbox child"
                    >
                      <input
                        type="checkbox"
                        v-model="selectedPermissions"
                        :value="child.code"
                      />
                      <span class="permission-name">{{ child.name }}</span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showPermissionModal = false">取消</button>
          <button class="btn btn-primary" @click="savePermissions">保存配置</button>
        </div>
      </div>
    </div>

    <!-- 添加/编辑角色弹窗 -->
    <div v-if="showAddModal || showEditModal" class="modal-overlay active" @click.self="closeModal">
      <div class="modal" style="max-width: 500px;">
        <div class="modal-header">
          <h3 class="modal-title">{{ showEditModal ? '编辑角色' : '添加角色' }}</h3>
          <button class="modal-close" @click="closeModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label required">角色名称</label>
            <input
              v-model="roleForm.name"
              type="text"
              class="form-input"
              placeholder="请输入角色名称"
            />
          </div>
          <div class="form-group">
            <label class="form-label required">角色标识</label>
            <input
              v-model="roleForm.code"
              type="text"
              class="form-input"
              placeholder="如: CUSTOM_ADMIN"
              :disabled="showEditModal"
            />
            <p class="form-hint">角色标识用于权限判断，建议使用大写下划线格式</p>
          </div>
          <div class="form-group">
            <label class="form-label">角色描述</label>
            <textarea
              v-model="roleForm.description"
              class="form-input form-textarea"
              placeholder="请输入角色描述"
              rows="3"
            ></textarea>
          </div>
          <div class="form-group">
            <label class="form-label">角色状态</label>
            <div class="switch-wrapper">
              <label class="switch">
                <input type="checkbox" v-model="roleForm.status" :checked="roleForm.status === 1" />
                <span class="slider"></span>
              </label>
              <span class="switch-label">{{ roleForm.status === 1 ? '启用' : '禁用' }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="saveRole">
            {{ showEditModal ? '保存' : '创建' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

// 角色列表
const roles = ref([
  {
    id: 1,
    name: '超级管理员',
    code: 'SUPER_ADMIN',
    description: '拥有系统所有权限，可进行所有操作',
    status: 1,
    userCount: 2,
    permissionCount: 45,
    iconClass: 'warning'
  },
  {
    id: 2,
    name: '运营管理员',
    code: 'OPERATION_ADMIN',
    description: '负责日常运营管理，包括商品、订单、用户等',
    status: 1,
    userCount: 5,
    permissionCount: 32,
    iconClass: 'primary'
  },
  {
    id: 3,
    name: '商品管理员',
    code: 'PRODUCT_ADMIN',
    description: '负责商品管理和订单处理',
    status: 1,
    userCount: 3,
    permissionCount: 18,
    iconClass: 'success'
  },
  {
    id: 4,
    name: '商家',
    code: 'MERCHANT',
    description: '商家端用户，管理自己的商品和订单',
    status: 1,
    userCount: 12,
    permissionCount: 15,
    iconClass: 'info'
  },
  {
    id: 5,
    name: '客服',
    code: 'CUSTOMER_SERVICE',
    description: '客服人员，处理用户咨询和订单问题',
    status: 1,
    userCount: 8,
    permissionCount: 8,
    iconClass: 'secondary'
  }
])

// 权限模块
const permissionModules = ref([
  {
    id: 1,
    name: '系统管理',
    permissions: [
      { id: 101, name: '管理员列表', code: 'system:admin:list', children: [] },
      { id: 102, name: '添加管理员', code: 'system:admin:add', children: [] },
      { id: 103, name: '编辑管理员', code: 'system:admin:edit', children: [] },
      { id: 104, name: '删除管理员', code: 'system:admin:delete', children: [] },
      { id: 105, name: '角色管理', code: 'system:role', children: [] },
      { id: 106, name: '权限管理', code: 'system:permission', children: [] },
      { id: 107, name: '操作日志', code: 'system:log', children: [] }
    ]
  },
  {
    id: 2,
    name: '商品管理',
    permissions: [
      { id: 201, name: '商品列表', code: 'product:list', children: [] },
      { id: 202, name: '添加商品', code: 'product:add', children: [] },
      { id: 203, name: '编辑商品', code: 'product:edit', children: [] },
      { id: 204, name: '删除商品', code: 'product:delete', children: [] },
      { id: 205, name: '上下架商品', code: 'product:publish', children: [] },
      { id: 206, name: '商品分类', code: 'product:category', children: [] }
    ]
  },
  {
    id: 3,
    name: '订单管理',
    permissions: [
      { id: 301, name: '订单列表', code: 'order:list', children: [] },
      { id: 302, name: '订单详情', code: 'order:detail', children: [] },
      { id: 303, name: '订单发货', code: 'order:ship', children: [] },
      { id: 304, name: '取消订单', code: 'order:cancel', children: [] },
      { id: 305, name: '导出订单', code: 'order:export', children: [] }
    ]
  },
  {
    id: 4,
    name: '用户管理',
    permissions: [
      { id: 401, name: '用户列表', code: 'user:list', children: [] },
      { id: 402, name: '用户详情', code: 'user:detail', children: [] },
      { id: 403, name: '禁用用户', code: 'user:disable', children: [] }
    ]
  },
  {
    id: 5,
    name: '营销管理',
    permissions: [
      { id: 501, name: '优惠券管理', code: 'marketing:coupon', children: [] },
      { id: 502, name: '创建优惠券', code: 'marketing:coupon:add', children: [] },
      { id: 503, name: '积分规则', code: 'marketing:points', children: [] }
    ]
  },
  {
    id: 6,
    name: '商家管理',
    permissions: [
      { id: 601, name: '商家列表', code: 'merchant:list', children: [] },
      { id: 602, name: '商家审核', code: 'merchant:audit', children: [] }
    ]
  },
  {
    id: 7,
    name: '数据统计',
    permissions: [
      { id: 701, name: '销售统计', code: 'statistics:sales', children: [] },
      { id: 702, name: '用户统计', code: 'statistics:user', children: [] }
    ]
  }
])

// 弹窗状态
const showAddModal = ref(false)
const showEditModal = ref(false)
const showPermissionModal = ref(false)
const selectedRole = ref(null)
const selectedPermissions = ref([])

// 角色表单
const roleForm = reactive({
  name: '',
  code: '',
  description: '',
  status: 1
})

// 方法
const selectRole = (role) => {
  selectedRole.value = role
}

const editRole = (role) => {
  selectedRole.value = role
  roleForm.name = role.name
  roleForm.code = role.code
  roleForm.description = role.description
  roleForm.status = role.status
  showEditModal.value = true
}

const viewRoleDetail = (role) => {
  selectedRole.value = role
  selectedPermissions.value = getRolePermissions(role)
  showPermissionModal.value = true
}

const getRolePermissions = (role) => {
  // 模拟获取角色已有权限
  if (role.code === 'SUPER_ADMIN') {
    return permissionModules.value.flatMap(m => m.permissions).map(p => p.code)
  }
  if (role.code === 'OPERATION_ADMIN') {
    return permissionModules.value.slice(0, 5).flatMap(m => m.permissions).map(p => p.code)
  }
  if (role.code === 'PRODUCT_ADMIN') {
    return permissionModules.value[1].permissions.map(p => p.code)
  }
  if (role.code === 'MERCHANT') {
    return [2, 3].flatMap(i => permissionModules.value[i].permissions).map(p => p.code)
  }
  if (role.code === 'CUSTOMER_SERVICE') {
    return permissionModules.value[2].permissions.slice(0, 3).map(p => p.code)
  }
  return []
}

const isModuleChecked = (module) => {
  const modulePerms = module.permissions.map(p => p.code)
  return modulePerms.every(p => selectedPermissions.value.includes(p))
}

const isModuleIndeterminate = (module) => {
  const modulePerms = module.permissions.map(p => p.code)
  const checked = modulePerms.filter(p => selectedPermissions.value.includes(p))
  return checked.length > 0 && checked.length < modulePerms.length
}

const toggleModule = (module) => {
  const modulePerms = module.permissions.map(p => p.code)
  const allChecked = isModuleChecked(module)

  if (allChecked) {
    selectedPermissions.value = selectedPermissions.value.filter(p => !modulePerms.includes(p))
  } else {
    const newPerms = new Set([...selectedPermissions.value, ...modulePerms])
    selectedPermissions.value = Array.from(newPerms)
  }
}

const toggleRoleStatus = (role) => {
  role.status = role.status === 1 ? 0 : 1
}

const saveRole = () => {
  if (!roleForm.name || !roleForm.code) {
    alert('请填写完整信息')
    return
  }

  if (showEditModal.value) {
    // 编辑
    const role = roles.value.find(r => r.id === selectedRole.value.id)
    if (role) {
      role.name = roleForm.name
      role.description = roleForm.description
      role.status = roleForm.status
    }
  } else {
    // 添加
    roles.value.push({
      id: Date.now(),
      name: roleForm.name,
      code: roleForm.code,
      description: roleForm.description,
      status: roleForm.status,
      userCount: 0,
      permissionCount: 0,
      iconClass: 'primary'
    })
  }

  closeModal()
}

const savePermissions = () => {
  if (selectedRole.value) {
    selectedRole.value.permissionCount = selectedPermissions.value.length
  }
  showPermissionModal.value = false
}

const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  roleForm.name = ''
  roleForm.code = ''
  roleForm.description = ''
  roleForm.status = 1
}
</script>

<style scoped>
.role-list {
  max-width: 1400px;
}

/* 角色网格 */
.roles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: var(--spacing-lg);
}

.role-card {
  padding: var(--spacing-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.role-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.role-card.selected {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}

.role-header {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.role-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.role-icon.warning {
  background: #FFF4E5;
  color: var(--color-warning);
}

.role-icon.primary {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.role-icon.success {
  background: #E8F8ED;
  color: var(--color-success);
}

.role-icon.info {
  background: #E5F1FB;
  color: var(--color-info);
}

.role-icon.secondary {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
}

.role-info {
  flex: 1;
}

.role-name {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.role-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.4;
}

.role-status {
  flex-shrink: 0;
}

/* 元信息 */
.role-meta {
  display: flex;
  gap: var(--spacing-lg);
  padding: var(--spacing-md) 0;
  border-top: 1px solid var(--border-light);
  border-bottom: 1px solid var(--border-light);
  margin-bottom: var(--spacing-md);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

/* 操作按钮 */
.role-actions {
  display: flex;
  gap: var(--spacing-sm);
}

/* 权限树 */
.permission-tree {
  max-height: 500px;
  overflow-y: auto;
}

.permission-module {
  margin-bottom: var(--spacing-lg);
}

.module-header {
  padding: var(--spacing-md);
  background: var(--bg-tertiary);
  border-radius: var(--border-radius-sm);
  margin-bottom: var(--spacing-sm);
}

.module-checkbox {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  cursor: pointer;
}

.module-checkbox input {
  width: 18px;
  height: 18px;
  accent-color: var(--color-primary);
}

.module-name {
  font-weight: 600;
  color: var(--text-primary);
}

.module-permissions {
  padding-left: var(--spacing-xl);
}

.permission-item {
  padding: var(--spacing-sm) 0;
}

.permission-checkbox {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  cursor: pointer;
}

.permission-checkbox input {
  width: 16px;
  height: 16px;
  accent-color: var(--color-primary);
}

.permission-name {
  font-size: var(--font-size-base);
  color: var(--text-primary);
}

.permission-children {
  padding-left: calc(var(--spacing-xl) + 20px);
  margin-top: var(--spacing-xs);
}

.permission-checkbox.child .permission-name {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

/* 表单 */
.form-hint {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  margin-top: var(--spacing-xs);
}

/* 开关 */
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
</style>
