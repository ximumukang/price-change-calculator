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
  } catch (e: any) {
    console.error('[Register] Failed to get public key:', e)
    ElMessage.error('获取公钥失败，请检查后端服务是否启动')
  }
})

const handleRegister = async () => {
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
    const res = await register(username.value, password.value)
    authStore.setToken(res.token)
    authStore.setRefreshToken(res.refreshToken)
    authStore.setUsername(res.username)
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e: any) {
    console.error('[Register] Registration error:', e)
    const errorMsg = e.response?.data?.message || e.message || '注册失败'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>
    <div class="register-container">
      <div class="register-header">
        <h1>涨跌幅计算器</h1>
        <p>Create Account</p>
      </div>
      <el-card class="register-card">
        <h2>注册账号</h2>
        <el-form @submit.prevent="handleRegister">
          <el-form-item>
            <el-input
              v-model="username"
              placeholder="用户名（3-50字符）"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="password"
              type="password"
              placeholder="密码（至少8位，含大小写字母和数字）"
              size="large"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="confirmPassword"
              type="password"
              placeholder="确认密码"
              size="large"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              style="width: 100%"
              :loading="loading"
              @click="handleRegister"
            >
              注 册
            </el-button>
          </el-form-item>
        </el-form>
        <div class="footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script lang="ts">
import { User, Lock } from '@element-plus/icons-vue'
export default {
  components: { User, Lock }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
}

.register-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
}

.bg-shape-1 {
  width: 400px;
  height: 400px;
  background: #e94560;
  top: -100px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.bg-shape-2 {
  width: 300px;
  height: 300px;
  background: #0f3460;
  bottom: -50px;
  left: -50px;
  animation: float 10s ease-in-out infinite reverse;
}

.bg-shape-3 {
  width: 200px;
  height: 200px;
  background: #533483;
  top: 40%;
  left: 60%;
  animation: pulse 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-30px, 30px); }
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.3; }
  50% { transform: translate(-50%, -50%) scale(1.5); opacity: 0.5; }
}

.register-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;

  h1 {
    color: #fff;
    font-size: 32px;
    font-weight: 700;
    margin-bottom: 8px;
    text-shadow: 0 2px 10px rgba(0,0,0,0.3);
  }

  p {
    color: rgba(255,255,255,0.6);
    font-size: 14px;
    letter-spacing: 2px;
  }
}

.register-card {
  background: rgba(255,255,255,0.95);
  border: none;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  backdrop-filter: blur(10px);

  :deep(.el-card__body) {
    padding: 32px;
  }

  h2 {
    color: #1a1a2e;
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 28px;
    text-align: center;
  }

  :deep(.el-input__wrapper) {
    border-radius: 12px;
    padding: 4px 12px;
    box-shadow: 0 0 0 1px #e0e0e0 inset;

    &:hover {
      box-shadow: 0 0 0 1px #e94560 inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 2px rgba(233,69,96,0.2) inset;
    }
  }

  :deep(.el-button--primary) {
    background: linear-gradient(135deg, #e94560 0%, #c73e54 100%);
    border: none;
    border-radius: 12px;
    font-weight: 600;
    letter-spacing: 2px;
    height: 48px;

    &:hover {
      background: linear-gradient(135deg, #ff5a75 0%, #e94560 100%);
    }
  }
}

.footer {
  text-align: center;
  margin-top: 8px;
  color: #666;

  a {
    color: #e94560;
    font-weight: 600;
    margin-left: 8px;
  }
}
</style>