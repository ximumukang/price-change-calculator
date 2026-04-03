<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, getPublicKey } from '../api/auth'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)

onMounted(async () => {
  try {
    await getPublicKey()
  } catch (e) {
    ElMessage.error('获取公钥失败，请检查后端服务是否启动')
  }
})

const handleLogin = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = await login(username.value, password.value)
    authStore.setToken(res.token)
    authStore.setUsername(res.username)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>登录</h2>
      </template>
      <el-form :model="{ username, password }" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="username" placeholder="用户名" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="footer">
        <router-link to="/register">还没有账号？去注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 400px;
}
.footer {
  text-align: center;
  margin-top: 16px;
}
a {
  color: #409eff;
  text-decoration: none;
}
</style>
