<template>
  <div class="settings">
    <div class="page-header"><h1 class="page-title">账户设置</h1><p class="page-subtitle">修改密码和安全设置</p></div>
    <div class="card">
      <div class="card-header"><h3 class="card-title">修改密码</h3></div>
      <div class="card-body">
        <div class="form-group"><label class="form-label required">原密码</label><input v-model="pwdForm.oldPassword" type="password" class="form-input" placeholder="请输入原密码" /></div>
        <div class="form-group"><label class="form-label required">新密码</label><input v-model="pwdForm.newPassword" type="password" class="form-input" placeholder="请输入新密码" /></div>
        <div class="form-group"><label class="form-label required">确认密码</label><input v-model="pwdForm.confirmPassword" type="password" class="form-input" placeholder="请再次输入新密码" /></div>
        <button class="btn btn-primary" @click="changePassword">修改密码</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const changePassword = () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) { alert('请填写完整'); return }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { alert('两次密码不一致'); return }
  alert('密码修改成功')
}
</script>

<style scoped>
.settings { max-width: 600px; }
.card { padding: var(--spacing-lg); }
.card-header { margin-bottom: var(--spacing-lg); }
.card-title { font-size: var(--font-size-lg); font-weight: 600; }
.form-group { margin-bottom: var(--spacing-lg); }
.form-label.required::after { content: ' *'; color: var(--color-danger); }
.form-label { display: block; font-size: var(--font-size-sm); font-weight: 500; color: var(--text-secondary); margin-bottom: var(--spacing-xs); }
</style>
