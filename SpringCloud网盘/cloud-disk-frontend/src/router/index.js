import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue')
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/Register.vue')
    },
    {
        path: '/',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/recycle',
        name: 'Recycle',
        component: () => import('@/views/Recycle.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/shares',
        name: 'Shares',
        component: () => import('@/views/Shares.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/share/:code',
        name: 'ShareDetail',
        component: () => import('@/views/ShareDetail.vue')
    },
    {
        path: '/admin',
        name: 'Admin',
        component: () => import('@/views/Admin.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
        path: '/oauth/callback',
        name: 'OAuthCallback',
        component: () => import('@/views/OAuthCallback.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const role = localStorage.getItem('role') || 'user'
    
    // 需要登录但未登录 → 跳登录页
    if (to.meta.requiresAuth && !token) {
        next('/login')
        return
    }
    
    // 需要管理员但非管理员 → 跳首页
    if (to.meta.requiresAdmin && role !== 'admin') {
        next('/')
        return
    }
    
    // 已登录访问登录/注册页 → 跳首页
    if ((to.path === '/login' || to.path === '/register') && token) {
        next('/')
        return
    }
    
    next()
})

export default router
