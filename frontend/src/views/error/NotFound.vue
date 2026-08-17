<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1>页面不存在</h1>
      <p>抱歉，您访问的页面不存在或已被移除</p>
      <a-button type="primary" size="large" @click="$router.push('/dashboard')">
        返回首页
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
// 404 页面 — 无逻辑
</script>

<style scoped>
.not-found-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 64px);
  padding: 32px;
  background: #f5f5f5;
}

.not-found-content {
  text-align: center;
  max-width: 400px;
}

.error-code {
  font-size: 120px;
  font-weight: 900;
  color: #d9d9d9;
  line-height: 1;
  margin-bottom: 16px;
}

h1 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

p {
  font-size: 14px;
  color: #999;
  margin-bottom: 32px;
}
</style>
