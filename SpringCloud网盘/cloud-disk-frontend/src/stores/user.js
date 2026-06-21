import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userId = ref(localStorage.getItem('userId') || '')
    const username = ref(localStorage.getItem('username') || '')
    const role = ref(localStorage.getItem('role') || 'user')

    // 存储空间
    const usedSpace = ref(0)
    const totalSpace = ref(10 * 1024 * 1024 * 1024) // 默认 10GB

    // 计算属性：是否是管理员
    const isAdmin = computed(() => role.value === 'admin')

    // 计算属性：存储空间使用百分比
    const storagePercent = computed(() => {
        if (totalSpace.value === 0) return 0
        return Math.round((usedSpace.value / totalSpace.value) * 100)
    })

    const setUserInfo = (data) => {
        token.value = data.token
        userId.value = data.userId
        username.value = data.username
        role.value = data.role || 'user'
        localStorage.setItem('token', data.token)
        localStorage.setItem('userId', data.userId)
        localStorage.setItem('username', data.username)
        localStorage.setItem('role', data.role || 'user')
    }

    const setStorageUsage = (used, total) => {
        usedSpace.value = used || 0
        totalSpace.value = total || 10 * 1024 * 1024 * 1024
    }

    const logout = () => {
        token.value = ''
        userId.value = ''
        username.value = ''
        role.value = 'user'
        usedSpace.value = 0
        totalSpace.value = 10 * 1024 * 1024 * 1024
        localStorage.clear()
    }

    return { token, userId, username, role, usedSpace, totalSpace, isAdmin, storagePercent, setUserInfo, setStorageUsage, logout }
})
