import request from './request'

export const authAPI = {
    // 登录
    login(data) {
        return request.post('/auth/login', data)
    },
    // 注册
    register(data) {
        return request.post('/auth/register', data)
    },
    // 健康检查
    health() {
        return request.get('/auth/health')
    },
    // 管理员：用户列表
    getUsers(params) {
        return request.get('/auth/admin/users', { params })
    },
    // 管理员：切换用户状态
    toggleUserStatus(userId, status) {
        return request.put(`/auth/admin/user/${userId}/status`, { status })
    },
    // 管理员：删除用户
    deleteUser(userId) {
        return request.delete(`/auth/admin/user/${userId}`)
    },
    // 管理员：用户统计
    getUserStats() {
        return request.get('/auth/admin/stats')
    },
    // 第三方登录回调处理
    oauthCallback(platform, code) {
        return request.get(`/oauth/${platform}/callback`, { params: { code } })
    }
}
