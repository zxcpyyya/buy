<template>
  <div class="product-form">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑商品' : '添加商品' }}</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="product-form-body"
      >
        <el-form-item label="商品分类" prop="categoryId">
          <el-cascader
            v-model="form.categoryId"
            :options="categoryTree"
            :props="{ checkStrictly: true, label: 'name', value: 'id' }"
            placeholder="请选择商品分类"
            clearable
            style="width: 400px"
          />
        </el-form-item>

        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" style="width: 400px" />
        </el-form-item>

        <el-form-item label="副标题" prop="subtitle">
          <el-input v-model="form.subtitle" placeholder="请输入商品副标题" style="width: 600px" />
        </el-form-item>

        <el-form-item label="商品详情">
          <div class="editor-container">
            <WangEditor v-model="form.detail" height="400" />
          </div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品价格" prop="price">
              <el-input-number
                v-model="form.price"
                :precision="2"
                :min="0"
                :step="0.01"
                style="width: 200px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" style="width: 200px" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="划线价格">
              <el-input-number
                v-model="form.originalPrice"
                :precision="2"
                :min="0"
                :step="0.01"
                style="width: 200px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品重量">
              <el-input-number v-model="form.weight" :min="0" :precision="2" style="width: 200px" />
              <span class="unit">kg</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品主图" prop="mainImage">
          <el-upload
            class="image-uploader"
            :action="uploadUrl"
            :headers="{ Authorization: `Bearer ${userStore.token}` }"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload"
          >
            <img v-if="form.mainImage" :src="form.mainImage" class="preview-image" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
          <p class="upload-tip">建议尺寸：800x800像素，支持jpg、png格式</p>
        </el-form-item>

        <el-form-item label="商品图片">
          <div class="image-list">
            <div v-for="(img, index) in form.images" :key="index" class="image-item">
              <img :src="img" />
              <el-icon class="delete-icon" @click="removeImage(index)"><Close /></el-icon>
            </div>
            <el-upload
              v-if="form.images.length < 5"
              class="image-uploader small"
              :action="uploadUrl"
              :headers="{ Authorization: `Bearer ${userStore.token}` }"
              :show-file-list="false"
              :on-success="handleImagesSuccess"
            >
              <el-icon class="uploader-icon"><Plus /></el-icon>
            </el-upload>
          </div>
          <p class="upload-tip">最多上传5张图片，建议尺寸：800x800像素</p>
        </el-form-item>

        <el-form-item label="商品状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '立即创建' }}
          </el-button>
          <el-button @click="handleBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Close } from '@element-plus/icons-vue'
import { productApi, categoryApi } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const submitting = ref(false)
const categoryTree = ref([])
const isEdit = computed(() => !!route.params.id)

const form = reactive({
  categoryId: null,
  name: '',
  subtitle: '',
  detail: '',
  price: 0,
  stock: 0,
  originalPrice: 0,
  weight: 0,
  mainImage: '',
  images: [],
  status: 1
})

const rules = {
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入商品库存', trigger: 'blur' }],
  mainImage: [{ required: true, message: '请上传商品主图', trigger: 'change' }]
}

const uploadUrl = import.meta.env.VITE_API_BASE_URL + '/api/upload'

// 加载分类
async function loadCategories() {
  try {
    const res = await categoryApi.tree()
    if (res.code === 200) {
      categoryTree.value = res.data || []
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

// 加载商品详情
async function loadProduct() {
  if (!isEdit.value) return

  try {
    const res = await productApi.detail(route.params.id)
    if (res.code === 200) {
      Object.assign(form, res.data)
    }
  } catch (e) {
    ElMessage.error('加载商品信息失败')
  }
}

// 图片上传成功
function handleImageSuccess(res) {
  if (res.code === 200) {
    form.mainImage = res.data.url
  }
}

function handleImagesSuccess(res) {
  if (res.code === 200) {
    form.images.push(res.data.url)
  }
}

// 删除图片
function removeImage(index) {
  form.images.splice(index, 1)
}

// 上传前校验
function beforeImageUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传jpg、png格式的图片')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

// 提交
async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        await productApi.update(form)
        ElMessage.success('修改成功')
      } else {
        await productApi.add(form)
        ElMessage.success('添加成功')
      }
      router.push('/admin/product/list')
    } catch (e) {
      ElMessage.error(e.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 返回
function handleBack() {
  router.back()
}

onMounted(() => {
  loadCategories()
  if (isEdit.value) {
    loadProduct()
  }
})
</script>

<style scoped>
.product-form {
  padding: 0;
}

.product-form-body {
  max-width: 900px;
}

.unit {
  margin-left: 8px;
  color: #999;
}

.image-uploader {
  width: 150px;
  height: 150px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.2s;
}

.image-uploader:hover {
  border-color: #409eff;
}

.image-uploader.small {
  width: 100px;
  height: 100px;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.image-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.image-item {
  position: relative;
  width: 100px;
  height: 100px;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.image-item .delete-icon {
  position: absolute;
  top: -8px;
  right: -8px;
  font-size: 16px;
  color: #f56c6c;
  background: #fff;
  border-radius: 50%;
  cursor: pointer;
}

.editor-container {
  width: 100%;
}
</style>
