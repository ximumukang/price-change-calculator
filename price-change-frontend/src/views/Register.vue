<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, getPublicKey } from '../api/auth'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)

onMounted(async () => {
  try {
    await getPublicKey()
  } catch (e) {
    ElMessage.error('获取公钥失败，请检查后端服务是否启动')
  }
})

const handleRegister = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (username.value.length < 3 || username.value.length > 50) {
    ElMessage.warning('用户名长度需在3-50字符之间')
    return
  }
  if (password.value.length < 6) {
    ElMessage.warning('密码长度至少6位')
    return
  }
  if (password.value !== confirmPassword.value) {
    ElMessage.warning('两次密码输入不一致')
    return
  }

  loading.value = true
  try {
    const res = await register(username.value, password.value)
    authStore.setToken(res.token)
    authStore.setUsername(res.username)
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <h2>注册</h2>
      </template>
      <el-form :model="{ username, password, confirmPassword }" @submit.prevent="handleRegister">
        <el-form-item>
          <el-input v-model="username" placeholder="用户名（3-50字符）" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" type="password" placeholder="密码（至少6位）" show-password />
        </el-form-item>
        <el-form-item>
          <el-input v-model="confirmPassword" type="password" placeholder="确认密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="handleRegister">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="footer">
        <router-link to="/login">已有账号？去登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.register-card {
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
