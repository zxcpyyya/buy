<template>
  <div class="profile">
    <div class="page-header"><h1 class="page-title">个人中心</h1><p class="page-subtitle">管理您的账户信息</p></div>
    <div class="profile-card card">
      <div class="avatar-section">
        <div class="avatar">{{ userInfo.nickname?.charAt(0) || 'A' }}</div>
        <button class="btn btn-ghost btn-sm">更换头像</button>
      </div>
      <div class="info-section">
        <div class="form-group"><label>用户名</label><input v-model="userInfo.username" class="form-input" disabled /></div>
        <div class="form-group"><label>昵称</label><input v-model="userInfo.nickname" class="form-input" /></div>
        <div class="form-group"><label>邮箱</label><input v-model="userInfo.email" type="email" class="form-input" /></div>
        <div class="form-group"><label>手机号</label><input v-model="userInfo.phone" class="form-input" /></div>
        <button class="btn btn-primary" @click="saveProfile">保存修改</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const userInfo = ref(JSON.parse(localStorage.getItem('admin_user') || '{}'))
const saveProfile = () => { alert('保存成功'); localStorage.setItem('admin_user', JSON.stringify(userInfo.value)) }
</script>

<style scoped>
.profile { max-width: 600px; }
.profile-card { padding: var(--spacing-xl); }
.avatar-section { text-align: center; margin-bottom: var(--spacing-xl); }
.avatar { width: 80px; height: 80px; border-radius: 50%; background: linear-gradient(135deg, var(--color-primary), var(--color-info)); display: flex; align-items: center; justify-content: center; color: white; font-size: 32px; font-weight: 600; margin: 0 auto var(--spacing-md); }
.info-section { display: flex; flex-direction: column; gap: var(--spacing-lg); }
.form-group label { display: block; font-size: var(--font-size-sm); font-weight: 500; color: var(--text-secondary); margin-bottom: var(--spacing-xs); }
</style>
