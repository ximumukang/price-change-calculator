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

// 组件挂载时获取 RSA 公钥
onMounted(async () => {
  try {
    await getPublicKey()
  } catch (e: any) {
    console.error('[Register] Failed to get public key:', e)
    ElMessage.error('获取公钥失败，请检查后端服务是否启动')
  }
})

const handleRegister = async () => {
  // 前端表单验证
  if (!username.value || !password.value || !confirmPassword.value) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (username.value.length < 3 || username.value.length > 50) {
    ElMessage.warning('用户名长度需在3-50字符之间')
    return
  }
  if (password.value.length < 8) {
    ElMessage.warning('密码长度至少8位')
    return
  }
  if (!/[a-z]/.test(password.value)) {
    ElMessage.warning('密码必须包含小写字母')
    return
  }
  if (!/[A-Z]/.test(password.value)) {
    ElMessage.warning('密码必须包含大写字母')
    return
  }
  if (!/\d/.test(password.value)) {
    ElMessage.warning('密码必须包含数字')
    return
  }
  if (password.value !== confirmPassword.value) {
    ElMessage.warning('两次密码输入不一致')
    return
  }

  loading.value = true
  try {
    // 调用注册接口（用户名和密码会自动 RSA 加密）
    const res = await register(username.value, password.value)
    
    // 保存认证信息到 store 和 localStorage
    authStore.setToken(res.token)
    authStore.setRefreshToken(res.refreshToken)
    authStore.setUsername(res.username)
    
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e: any) {
    console.error('[Register] Registration error:', e)
    // 优先显示后端返回的错误信息
    const errorMsg = e.response?.data?.message || e.message || '注册失败'
    ElMessage.error(errorMsg)
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
          <el-input v-model="password" type="password" placeholder="密码（至少8位，含大小写字母和数字）" show-password />
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
