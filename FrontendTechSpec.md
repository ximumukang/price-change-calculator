# 前端技术规范 - Vue 3 + TypeScript

## 1. 项目结构

```
price-change-frontend/
├── src/
│   ├── api/       # API 调用
│   ├── router/    # 路由配置
│   ├── store/    # Pinia 状态
│   ├── utils/    # 工具函数
│   ├── views/    # 页面组件
│   ├── App.vue
│   └── main.ts
├── index.html
├── vite.config.ts
└── package.json
```

## 2. 核心依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| Vue | 3 | 框架 |
| TypeScript | ~5.9.3 | 语言 |
| Vite | 8.0.1 | 构建工具 |
| Element Plus | 2.13.6 | UI 组件库 |
| Pinia | 3.0.4 | 状态管理 |
| Vue Router | 4.6.4 | 路由 |
| Axios | 1.14.0 | HTTP 客户端 |
| jsencrypt | 3.5.4 | RSA 加密 |

## 3. 代码规范

### 3.1 组件

```vue
<template>
  <el-form :model="form" :rules="rules" ref="formRef">
    <el-form-item label="用户名" prop="username">
      <el-input v-model="form.username" />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

interface LoginForm {
  username: string
  password: string
}

const formRef = ref<FormInstance>()
const form = reactive<LoginForm>({
  username: '',
  password: ''
})

const rules = reactive<FormRules<LoginForm>>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})
</script>
```

- 必须: `<script setup lang="ts">`
- 表单: 使用 `reactive`
- DOM 引用: 使用 `ref<T>()`

### 3.2 API 调用

```typescript
// src/api/auth.ts
import request from '@/utils/request'
import type { LoginRequest, LoginResponse } from '@/types'

export function login(data: LoginRequest) {
  return request.post<LoginResponse>('/api/auth/login', data)
}

// src/utils/request.ts
import axios from 'axios'

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

instance.interceptors.response.use(
  response => response.data,
  error => Promise.reject(error)
)

export default instance
```

### 3.3 路由

```typescript
// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
    { path: '/register', name: 'Register', component: () => import('@/views/Register.vue') },
    { path: '/', name: 'Dashboard', component: () => import('@/views/Dashboard.vue') }
  ]
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (!authStore.token && to.path !== '/login') {
    next('/login')
  } else {
    next()
  }
})

export default router
```

### 3.4 状态管理

```typescript
// src/store/auth.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function logout() {
    token.value = ''
    localStorage.removeItem('token')
  }

  return { token, setToken, logout }
})
```

- 持久化: localStorage

### 3.5 RSA 加密

```typescript
// src/utils/crypto.ts
import JSEncrypt from 'jsencrypt'

const encrypt = new JSEncrypt()
encrypt.setPublicKey(PUBLIC_KEY)

export function encryptPassword(password: string): string {
  return encrypt.encrypt(password) || password
}
```

- 加密库: jsencrypt
- 填充模式: PKCS1Padding（与后端一致）

## 4. 注意事项

1. **Token 存储**: 使用 Pinia + localStorage
2. **类型推断**: 避免 `any`，优先 TypeScript 类型
3. **RSA 加密**: 前端 jsencrypt，后端 RSA/ECB/PKCS1Padding
4. **代理配置**: Vite `/api` 代理到 `http://localhost:8080`
5. **ID 类型必须用 string**: 后端所有表的 ID 均为雪花算法生成的 Long（如 `2045362233630998530`），超出 JS `Number.MAX_SAFE_INTEGER`（9007199254740991），后端已通过 `@JsonSerialize(using = ToStringSerializer.class)` 将 ID 序列化为字符串返回。前端接口定义、变量、参数中所有 ID（包括 `id`、`categoryId` 等）**必须声明为 `string`**，禁止使用 `number`，禁止 `Number()` 转换，否则会丢失末尾精度
6. **不要重复超轮子**: 优先使用Element Plus的组件，不要重复造轮子
7. **使用css嵌套的写法**: 使用css嵌套的写法，不要使用css预处理器，因为vite默认支持css嵌套的写法，不需要使用css预处理器  

## 5. Vite 代理配置

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

## 6. 页面路由

| 路径 | 组件 | 说明 |
|------|------|------|
| /login | Login.vue | 登录页 |
| /register | Register.vue | 注册页 |
| / | Dashboard.vue | 仪表盘 |