<template>
  <div class="product-form">
    <div class="page-header">
      <div class="flex items-center gap-md">
        <button class="btn btn-ghost" @click="goBack">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12"/>
            <polyline points="12 19 5 12 12 5"/>
          </svg>
        </button>
        <div>
          <h1 class="page-title">{{ isEdit ? '编辑商品' : '添加商品' }}</h1>
          <p class="page-subtitle">{{ isEdit ? '修改商品信息' : '创建新的商品' }}</p>
        </div>
      </div>
    </div>

    <div class="form-container">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">基本信息</h3>
        </div>
        <div class="card-body">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label required">商品名称</label>
              <input v-model="form.name" type="text" class="form-input" placeholder="请输入商品名称" />
            </div>
            <div class="form-group">
              <label class="form-label required">商品分类</label>
              <select v-model="form.categoryId" class="form-input form-select">
                <option value="">请选择分类</option>
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label required">商品价格</label>
              <input v-model="form.price" type="number" class="form-input" placeholder="0.00" />
            </div>
            <div class="form-group">
              <label class="form-label">原价</label>
              <input v-model="form.originalPrice" type="number" class="form-input" placeholder="划线价" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label required">商品库存</label>
              <input v-model="form.stock" type="number" class="form-input" placeholder="库存数量" />
            </div>
            <div class="form-group">
              <label class="form-label">商品编码</label>
              <input v-model="form.sku" type="text" class="form-input" placeholder="SKU编码" />
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <h3 class="card-title">商品图片</h3>
        </div>
        <div class="card-body">
          <div class="image-upload-area">
            <div class="image-list">
              <div v-for="(img, index) in form.images" :key="index" class="image-item">
                <img :src="img" alt="" />
                <button class="remove-btn" @click="removeImage(index)">×</button>
              </div>
              <div v-if="form.images.length < 5" class="upload-btn" @click="triggerUpload">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="12" y1="5" x2="12" y2="19"/>
                  <line x1="5" y1="12" x2="19" y2="12"/>
                </svg>
                <span>上传图片</span>
              </div>
            </div>
            <p class="image-hint">最多上传5张图片，建议尺寸 800x800</p>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <h3 class="card-title">商品详情</h3>
        </div>
        <div class="card-body">
          <div class="form-group">
            <label class="form-label">商品描述</label>
            <textarea v-model="form.description" class="form-input form-textarea" rows="5" placeholder="请输入商品详细描述"></textarea>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <h3 class="card-title">其他设置</h3>
        </div>
        <div class="card-body">
          <div class="form-group">
            <label class="form-label">商品状态</label>
            <div class="switch-wrapper">
              <label class="switch">
                <input type="checkbox" v-model="form.status" :checked="form.status === 1" />
                <span class="slider"></span>
              </label>
              <span class="switch-label">{{ form.status === 1 ? '上架' : '下架' }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="form-actions">
        <button class="btn btn-secondary" @click="goBack">取消</button>
        <button class="btn btn-primary" @click="saveProduct">
          {{ isEdit ? '保存修改' : '创建商品' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)

const categories = ref([
  { id: 1, name: '手机数码' },
  { id: 2, name: '电脑办公' },
  { id: 3, name: '服装鞋包' },
  { id: 4, name: '食品饮料' },
  { id: 5, name: '美妆护肤' }
])

const form = reactive({
  name: '',
  categoryId: '',
  price: '',
  originalPrice: '',
  stock: '',
  sku: '',
  description: '',
  images: [],
  status: 1
})

const goBack = () => {
  router.back()
}

const removeImage = (index) => {
  form.images.splice(index, 1)
}

const triggerUpload = () => {
  // 模拟上传
  const randomId = Math.random().toString(36).substr(2, 9)
  form.images.push(`https://picsum.photos/200/200?random=${randomId}`)
}

const saveProduct = () => {
  if (!form.name || !form.categoryId || !form.price || !form.stock) {
    alert('请填写必填项')
    return
  }
  alert(isEdit.value ? '保存成功' : '创建成功')
  router.back()
}

onMounted(() => {
  if (isEdit.value) {
    // 模拟加载数据
    form.name = 'iPhone 15 Pro Max 256GB 深空黑'
    form.categoryId = 1
    form.price = 9999
    form.originalPrice = 10999
    form.stock = 50
    form.description = '全新A17 Pro芯片，钛金属设计'
    form.images = ['https://picsum.photos/200/200?random=1']
  }
})
</script>

<style scoped>
.product-form {
  max-width: 900px;
}

.form-container {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
}

@media (max-width: 600px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.form-group {
  margin-bottom: var(--spacing-md);
}

/* 图片上传 */
.image-upload-area {
  padding: var(--spacing-lg);
  background: var(--bg-tertiary);
  border-radius: var(--border-radius);
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.image-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: var(--border-radius);
  overflow: hidden;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-item .remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
}

.upload-btn {
  width: 100px;
  height: 100px;
  border: 2px dashed var(--border-color);
  border-radius: var(--border-radius);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  cursor: pointer;
  color: var(--text-tertiary);
  transition: all var(--transition-fast);
}

.upload-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.upload-btn span {
  font-size: var(--font-size-xs);
}

.image-hint {
  margin-top: var(--spacing-md);
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
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

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
  padding-top: var(--spacing-lg);
}
</style>
