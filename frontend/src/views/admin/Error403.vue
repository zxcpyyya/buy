<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">403</h1>
      <p class="error-message">抱歉，您没有权限访问该页面</p>
      <el-button type="primary" @click="$router.push('/admin')">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.error-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}

.error-content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #e6a23c;
  margin: 0;
}

.error-message {
  font-size: 20px;
  color: #666;
  margin: 20px 0 30px;
}
</style>
