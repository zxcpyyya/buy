<template>
  <div class="addresses-page">
    <section class="page-hero">
      <div class="container-large">
        <h1 class="page-title">收货地址</h1>
        <p class="page-subtitle">管理您的收货地址</p>
      </div>
    </section>
    
    <div class="container-large">
      <!-- 新增按钮 -->
      <button class="btn-add" @click="openDialog()">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M12 5v14M5 12h14"/>
        </svg>
        新增地址
      </button>
      
      <!-- 地址列表 -->
      <div v-loading="loading" class="address-list" element-loading-text="加载中...">
        <div 
          v-for="addr in addresses" 
          :key="addr.id"
          class="address-card"
        >
          <div class="card-header">
            <div class="card-tags">
              <span class="tag-name">{{ addr.consignee }}</span>
              <span class="tag-phone">{{ addr.phone }}</span>
              <span v-if="addr.isDefault" class="tag-default">默认</span>
            </div>
            <div class="card-actions">
              <button class="action-btn" @click="openDialog(addr)">编辑</button>
              <button class="action-btn danger" @click="handleDelete(addr)">删除</button>
            </div>
          </div>
          <p class="card-address">{{ addr.fullAddress }}</p>
          <p v-if="addr.label" class="card-label">{{ addr.label }}</p>
          <button v-if="!addr.isDefault" class="btn-set-default" @click="setDefault(addr)">
            设为默认地址
          </button>
        </div>
        
        <div v-if="!loading && addresses.length === 0" class="empty">
          <div class="empty-icon">
            <svg viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M32 8c-8 0-16 6-16 16 0 12 16 32 16 32s16-20 16-32c0-10-8-16-16-16z"/>
              <circle cx="32" cy="24" r="6"/>
            </svg>
          </div>
          <h2 class="empty-title">暂无收货地址</h2>
          <p class="empty-desc">添加您的第一个收货地址</p>
        </div>
      </div>
    </div>
    
    <!-- 编辑弹窗 -->
    <van-dialog
      v-model:show="dialogVisible"
      :title="editing ? '编辑地址' : '新增地址'"
      :show-cancel-button="false"
      :show-confirm-button="false"
      close-on-click-overlay
    >
      <div class="dialog-form">
        <div class="form-item">
          <label>收货人</label>
          <input v-model="form.consignee" type="text" placeholder="请输入收货人姓名" />
        </div>
        <div class="form-item">
          <label>手机号</label>
          <input v-model="form.phone" type="text" placeholder="请输入手机号" maxlength="11" />
        </div>
        <div class="form-item">
          <label>省份</label>
          <input v-model="form.province" type="text" placeholder="如：广东省" />
        </div>
        <div class="form-item">
          <label>城市</label>
          <input v-model="form.city" type="text" placeholder="如：深圳市" />
        </div>
        <div class="form-item">
          <label>区县</label>
          <input v-model="form.district" type="text" placeholder="如：南山区" />
        </div>
        <div class="form-item">
          <label>详细地址</label>
          <textarea v-model="form.detailAddress" placeholder="街道、楼栋、门牌号" rows="2"></textarea>
        </div>
        <div class="form-item">
          <label>标签</label>
          <input v-model="form.label" type="text" placeholder="如：家、公司（选填）" />
        </div>
        <div class="form-item">
          <label>设为默认</label>
          <van-switch v-model="isDefault" />
        </div>
        <div class="form-actions">
          <button class="btn-cancel" @click="dialogVisible = false">取消</button>
          <button class="btn-save" @click="handleSave">保存</button>
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import request from '@/utils/request'

const addresses = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(null)
const isDefault = ref(false)

const form = reactive({
  consignee: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  label: ''
})

const fetchAddresses = async () => {
  loading.value = true
  try {
    addresses.value = await request.get('/address') || []
  } catch (e) {
    console.error('获取地址失败', e)
  } finally {
    loading.value = false
  }
}

const openDialog = (addr) => {
  if (addr) {
    editing.value = addr
    Object.assign(form, {
      consignee: addr.consignee,
      phone: addr.phone,
      province: addr.province,
      city: addr.city,
      district: addr.district,
      detailAddress: addr.detailAddress,
      label: addr.label
    })
    isDefault.value = addr.isDefault === 1
  } else {
    editing.value = null
    Object.keys(form).forEach(k => form[k] = '')
    isDefault.value = false
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  // 校验
  if (!form.consignee || !form.phone || !form.province || 
      !form.city || !form.district || !form.detailAddress) {
    showToast('请填写完整信息')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    showToast('手机号格式不正确')
    return
  }
  
  const data = { ...form, isDefault: isDefault.value ? 1 : 0 }
  
  try {
    if (editing.value) {
      await request.put(`/address/${editing.value.id}`, data)
      showToast('地址已更新')
    } else {
      await request.post('/address', data)
      showToast('地址已添加')
    }
    dialogVisible.value = false
    fetchAddresses()
  } catch (e) {
    console.error('保存失败', e)
  }
}

const handleDelete = async (addr) => {
  try {
    await showConfirmDialog({ title: '删除地址', message: '确认删除该地址吗？' })
  } catch { return }
  
  try {
    await request.delete(`/address/${addr.id}`)
    showToast('已删除')
    fetchAddresses()
  } catch (e) {
    console.error('删除失败', e)
  }
}

const setDefault = async (addr) => {
  try {
    await request.put(`/address/${addr.id}/default`)
    showToast('已设为默认')
    fetchAddresses()
  } catch (e) {
    console.error('设置失败', e)
  }
}

onMounted(fetchAddresses)
</script>

<style lang="scss" scoped>
.addresses-page {
  padding-top: 44px;
  padding-bottom: 80px;
  background: #fbfbfd;
  min-height: 100vh;
}

.page-hero {
  padding: 80px 0 48px;
  text-align: center;
  background: #fff;
}

.page-title {
  font-size: 48px;
  font-weight: 600;
  letter-spacing: -0.015em;
  color: #1d1d1f;
  margin-bottom: 8px;
  
  @media (max-width: 833px) {
    font-size: 40px;
  }
}

.page-subtitle {
  font-size: 21px;
  color: #6e6e73;
}

.btn-add {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  max-width: 400px;
  height: 48px;
  margin: 0 auto 24px;
  background: #fff;
  color: #1d1d1f;
  border-radius: 980px;
  font-size: 15px;
  font-weight: 500;
  transition: background 0.2s;
  
  &:hover {
    background: #f5f5f7;
  }
}

.address-list {
  max-width: 800px;
  margin: 0 auto;
}

.address-card {
  background: #fff;
  border-radius: 18px;
  padding: 24px;
  margin-bottom: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-tags {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-name {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
}

.tag-phone {
  font-size: 14px;
  color: #6e6e73;
}

.tag-default {
  padding: 2px 8px;
  background: #1d1d1f;
  color: #fff;
  font-size: 11px;
  border-radius: 4px;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 4px 12px;
  font-size: 13px;
  color: #6e6e73;
  border-radius: 980px;
  transition: all 0.2s;
  
  &:hover {
    background: #f5f5f7;
    color: #1d1d1f;
  }
  
  &.danger:hover {
    color: #ff3b30;
  }
}

.card-address {
  font-size: 14px;
  color: #1d1d1f;
  line-height: 1.5;
  margin-bottom: 8px;
}

.card-label {
  display: inline-block;
  padding: 2px 8px;
  background: #f5f5f7;
  color: #6e6e73;
  font-size: 12px;
  border-radius: 4px;
}

.btn-set-default {
  margin-top: 12px;
  padding: 6px 14px;
  background: transparent;
  color: #0071e3;
  border: 1px solid #0071e3;
  border-radius: 980px;
  font-size: 12px;
  transition: all 0.2s;
  
  &:hover {
    background: rgba(0, 113, 227, 0.08);
  }
}

// 空状态
.empty {
  text-align: center;
  padding: 80px 0;
  background: #fff;
  border-radius: 18px;
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  color: #d2d2d7;
}

.empty-title {
  font-size: 21px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #6e6e73;
}

// 弹窗表单
.dialog-form {
  padding: 8px 16px;
}

.form-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f7;
  
  label {
    flex-shrink: 0;
    width: 80px;
    font-size: 14px;
    color: #1d1d1f;
    padding-top: 8px;
  }
  
  input, textarea {
    flex: 1;
    padding: 8px 0;
    border: none;
    font-size: 14px;
    color: #1d1d1f;
    background: transparent;
    
    &::placeholder {
      color: #86868b;
    }
  }
}

.form-actions {
  display: flex;
  gap: 8px;
  padding: 16px 0;
  
  button {
    flex: 1;
    height: 44px;
    border-radius: 980px;
    font-size: 15px;
  }
}

.btn-cancel {
  background: #f5f5f7;
  color: #1d1d1f;
}

.btn-save {
  background: #0071e3;
  color: #fff;
}
</style>
