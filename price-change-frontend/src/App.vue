<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()

onMounted(() => {
  // 页面加载时初始化认证信息（恢复 token 并获取公钥）
  authStore.initAuth()
})
</script>

<template>
  <router-view />
</template>
