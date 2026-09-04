<template>
  <div class="category-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品分类</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 添加分类
          </el-button>
        </div>
      </template>

      <el-table
        :data="treeData"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        row-key="id"
        default-expand-all
      >
        <el-table-column prop="name" label="分类名称" width="200" />
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="{ row }">
            <el-icon><component :is="row.icon || 'Folder'" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleAddChild(row)" v-if="row.level < 3">
              添加子分类
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '添加分类'"
      width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级分类" v-if="form.parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="treeData"
            :props="{ label: 'name', value: 'id' }"
            placeholder="选择上级分类"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类图标">
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
import { categoryApi } from '@/api/admin'

const loading = ref(false)
const treeData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  parentId: null,
  name: '',
  icon: '',
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await categoryApi.tree()
    if (res.code === 200) {
      treeData.value = res.data || []
    }
  } catch (e) {
    console.error('加载分类失败', e)
    treeData.value = [
      { id: 1, name: '手机数码', sort: 1, status: 1, children: [
        { id: 11, name: '手机', sort: 1, status: 1 },
        { id: 12, name: '耳机', sort: 2, status: 1 }
      ]},
      { id: 2, name: '服装鞋包', sort: 2, status: 1, children: [] },
      { id: 3, name: '食品生鲜', sort: 3, status: 1, children: [] }
    ]
  } finally {
    loading.value = false
  }
}

// 添加顶级分类
function handleAdd() {
  isEdit.value = false
  form.id = null
  form.parentId = null
  form.name = ''
  form.icon = ''
  form.sort = 0
  form.status = 1
  dialogVisible.value = true
}

// 添加子分类
function handleAddChild(row) {
  isEdit.value = false
  form.id = null
  form.parentId = row.id
  form.name = ''
  form.icon = ''
  form.sort = 0
  form.status = 1
  dialogVisible.value = true
}

// 编辑
function handleEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.parentId = row.parentId
  form.name = row.name
  form.icon = row.icon || ''
  form.sort = row.sort || 0
  form.status = row.status
  dialogVisible.value = true
}

// 删除
async function handleDelete(row) {
  if (row.children && row.children.length > 0) {
    ElMessage.warning('请先删除子分类')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await categoryApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交
async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (isEdit.value) {
        await categoryApi.update(form)
        ElMessage.success('修改成功')
      } else {
        await categoryApi.add(form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.category-list {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
