<template>
  <div class="coupon-form">
    <el-card>
      <template #header>
        <span>创建优惠券</span>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 600px">
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入优惠券名称" />
        </el-form-item>

        <el-form-item label="优惠券类型" prop="couponType">
          <el-radio-group v-model="form.couponType">
            <el-radio :label="1">满减券</el-radio>
            <el-radio :label="2">折扣券</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="form.couponType === 1" label="优惠金额" prop="discountValue">
          <el-input-number v-model="form.discountValue" :min="1" :precision="2" />
          <span style="margin-left: 8px">元</span>
        </el-form-item>

        <el-form-item v-if="form.couponType === 1" label="使用门槛" prop="minAmount">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" />
          <span style="margin-left: 8px">元（0表示无门槛）</span>
        </el-form-item>

        <el-form-item v-if="form.couponType === 2" label="折扣" prop="discountValue">
          <el-input-number v-model="form.discountValue" :min="0.1" :max="0.9" :precision="1" :step="0.1" />
          <span style="margin-left: 8px">折（如：0.8 = 8折）</span>
        </el-form-item>

        <el-form-item label="有效期类型" prop="validType">
          <el-radio-group v-model="form.validType">
            <el-radio :label="1">固定时间</el-radio>
            <el-radio :label="2">领券后N天</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="form.validType === 1" label="有效期" required>
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item v-if="form.validType === 2" label="有效天数" prop="validDays">
          <el-input-number v-model="form.validDays" :min="1" />
          <span style="margin-left: 8px">天</span>
        </el-form-item>

        <el-form-item label="发行量" prop="totalCount">
          <el-input-number v-model="form.totalCount" :min="0" />
          <span style="margin-left: 8px">张（0表示不限量）</span>
        </el-form-item>

        <el-form-item label="每人限领" prop="perUserLimit">
          <el-input-number v-model="form.perUserLimit" :min="1" />
          <span style="margin-left: 8px">张</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">创建</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { couponApi } from '@/api/admin'

const router = useRouter()
const formRef = ref()
const dateRange = ref([])

const form = reactive({
  name: '',
  couponType: 1,
  discountValue: 10,
  minAmount: 0,
  validType: 1,
  startTime: null,
  endTime: null,
  validDays: 7,
  totalCount: 100,
  perUserLimit: 1
})

const rules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  couponType: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }],
  discountValue: [{ required: true, message: '请输入优惠值', trigger: 'blur' }]
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (form.validType === 1 && dateRange.value) {
        form.startTime = dateRange.value[0]
        form.endTime = dateRange.value[1]
      }

      await couponApi.add(form)
      ElMessage.success('创建成功')
      router.push('/admin/marketing/coupon')
    } catch (e) {
      ElMessage.error('创建失败')
    }
  })
}
</script>

<style scoped>
.coupon-form {
  padding: 0;
}
</style>
