import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  // 检查 token 是否存在且不为空
  const hasValidToken = token && token.trim() !== ''
  
  if (to.meta.requiresAuth && !hasValidToken) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && hasValidToken) {
    next('/')
  } else {
    next()
  }
})

export default router
