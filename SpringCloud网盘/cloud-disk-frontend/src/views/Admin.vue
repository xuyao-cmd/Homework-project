<template>
    <div class="admin">
        <Sakura />
        <el-container>
            <el-header>
                <div class="header-content">
                    <div class="logo-area">
                        <span class="logo-icon">⚙️</span>
                        <h2>管理后台</h2>
                    </div>
                    <div class="user-info">
                        <el-avatar :size="32" class="user-avatar">{{ userStore.username?.charAt(0)?.toUpperCase() }}</el-avatar>
                        <span class="username">{{ userStore.username }}</span>
                        <el-button @click="handleLogout" class="logout-btn">退出</el-button>
                    </div>
                </div>
            </el-header>
            <el-container class="main-container">
                <el-aside width="220px" class="sidebar">
                    <el-menu :default-active="activeTab" @select="handleMenuSelect" class="side-menu">
                        <el-menu-item index="/">
                            <el-icon><Folder /></el-icon>
                            <span>我的文件</span>
                        </el-menu-item>
                        <el-menu-item index="/recycle">
                            <el-icon><Delete /></el-icon>
                            <span>回收站</span>
                        </el-menu-item>
                        <el-menu-item index="/shares">
                            <el-icon><Share /></el-icon>
                            <span>我的分享</span>
                        </el-menu-item>
                        <el-menu-item index="admin" class="admin-menu-item">
                            <el-icon><Setting /></el-icon>
                            <span>管理后台</span>
                        </el-menu-item>
                    </el-menu>
                </el-aside>
                <el-main class="main-content">
                    <el-tabs v-model="activeTab" class="admin-tabs">
                        <el-tab-pane label="文件管理" name="files">
                            <div class="toolbar">
                                <div class="toolbar-left">
                                    <el-input v-model="searchKeyword" placeholder="搜索文件名..." class="search-input" clearable @clear="loadAdminFiles" @keyup.enter="loadAdminFiles">
                                        <template #prefix><el-icon><Search /></el-icon></template>
                                    </el-input>
                                    <el-button type="primary" @click="loadAdminFiles" class="action-btn">搜索</el-button>
                                </div>
                                <div class="toolbar-right">
                                    <span class="stat-text" v-if="stats">
                                        文件总数：<b>{{ stats.totalFiles }}</b> | 总大小：<b>{{ formatSize(stats.totalSize) }}</b>
                                    </span>
                                </div>
                            </div>
                            <div class="file-table-wrapper">
                                <el-table :data="adminFiles" v-loading="loading" class="file-table" empty-text="暂无文件">
                                    <el-table-column prop="id" label="ID" width="80" />
                                    <el-table-column label="文件名" min-width="200">
                                        <template #default="{ row }">
                                            <div class="file-name-cell">
                                                <el-icon v-if="row.isDir === 1" size="20" color="#a18cd1"><Folder /></el-icon>
                                                <el-icon v-else size="20" color="#909399"><Document /></el-icon>
                                                <span>{{ row.fileName }}</span>
                                            </div>
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="类型" width="80" align="center">
                                        <template #default="{ row }">
                                            <el-tag :type="row.isDir === 1 ? 'warning' : 'info'" size="small">{{ row.isDir === 1 ? '文件夹' : '文件' }}</el-tag>
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="大小" width="100" align="center">
                                        <template #default="{ row }">
                                            {{ row.isDir === 1 ? '-' : formatSize(row.size) }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="所属用户" width="140" align="center">
                                        <template #default="{ row }">
                                            <el-tooltip :content="'用户ID: ' + row.userId" placement="top">
                                                <span class="file-owner">{{ row.username || ('用户' + row.userId) }}</span>
                                            </el-tooltip>
                                        </template>
                                    </el-table-column>
                                    <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
                                    <el-table-column label="操作" width="120" align="center">
                                        <template #default="{ row }">
                                            <el-button link type="danger" @click="handleAdminDelete(row)">强制删除</el-button>
                                        </template>
                                    </el-table-column>
                                </el-table>
                            </div>
                            <div class="pagination-wrapper">
                                <el-pagination
                                    v-model:current-page="currentPage"
                                    v-model:page-size="pageSize"
                                    :page-sizes="[10, 20, 50]"
                                    layout="total, sizes, prev, pager, next"
                                    :total="totalFiles"
                                    @size-change="loadAdminFiles"
                                    @current-change="loadAdminFiles"
                                />
                            </div>
                        </el-tab-pane>

                        <el-tab-pane label="用户管理" name="users">
                            <div class="toolbar">
                                <div class="toolbar-left">
                                    <el-input v-model="userSearchKeyword" placeholder="搜索用户名或邮箱..." class="search-input" clearable @clear="loadUsers" @keyup.enter="loadUsers">
                                        <template #prefix><el-icon><Search /></el-icon></template>
                                    </el-input>
                                    <el-button type="primary" @click="loadUsers" class="action-btn">搜索</el-button>
                                </div>
                                <div class="toolbar-right">
                                    <span class="stat-text" v-if="userStats">
                                        总用户：<b>{{ userStats.totalUsers }}</b> | 正常：<b style="color:#67C23A">{{ userStats.activeUsers }}</b> | 禁用：<b style="color:#F56C6C">{{ userStats.disabledUsers }}</b>
                                    </span>
                                </div>
                            </div>
                            <div class="file-table-wrapper">
                                <el-table :data="userList" v-loading="userLoading" class="file-table" empty-text="暂无用户">
                                    <el-table-column prop="id" label="ID" width="80" />
                                    <el-table-column prop="username" label="用户名" min-width="140" />
                                    <el-table-column prop="email" label="邮箱" min-width="180" />
                                    <el-table-column label="角色" width="100" align="center">
                                        <template #default="{ row }">
                                            <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small">{{ row.role === 'admin' ? '管理员' : '普通用户' }}</el-tag>
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="来源" width="100" align="center">
                                        <template #default="{ row }">
                                            <el-tag v-if="row.oauthPlatform" type="warning" size="small">{{ row.oauthPlatform === 'github' ? 'GitHub' : row.oauthPlatform === 'wechat' ? '微信' : row.oauthPlatform === 'qq' ? 'QQ' : row.oauthPlatform }}</el-tag>
                                            <el-tag v-else type="" size="small">注册</el-tag>
                                        </template>
                                    </el-table-column>
                                    <el-table-column prop="createTime" label="注册时间" width="170" align="center" />
                                    <el-table-column label="状态" width="100" align="center">
                                        <template #default="{ row }">
                                            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="操作" width="160" align="center" fixed="right">
                                        <template #default="{ row }">
                                            <el-button link type="primary" @click="toggleUserStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
                                            <el-button link type="danger" @click="deleteUser(row)" :disabled="row.role === 'admin'">删除</el-button>
                                        </template>
                                    </el-table-column>
                                </el-table>
                            </div>
                            <div class="pagination-wrapper">
                                <el-pagination
                                    v-model:current-page="userCurrentPage"
                                    v-model:page-size="userPageSize"
                                    :page-sizes="[10, 20, 50]"
                                    layout="total, sizes, prev, pager, next"
                                    :total="totalUsers"
                                    @size-change="loadUsers"
                                    @current-change="loadUsers"
                                />
                            </div>
                        </el-tab-pane>
                    </el-tabs>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fileAPI } from '@/api/file'
import { authAPI } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import Sakura from '@/components/Sakura.vue'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('files')
const loading = ref(false)
const userLoading = ref(false)

// 文件管理
const adminFiles = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalFiles = ref(0)
const stats = ref(null)

// 用户管理
const userList = ref([])
const userSearchKeyword = ref('')
const userCurrentPage = ref(1)
const userPageSize = ref(10)
const totalUsers = ref(0)
const userStats = ref(null)

const loadAdminFiles = async () => {
    loading.value = true
    try {
        const res = await fileAPI.getAdminFiles(currentPage.value, pageSize.value, searchKeyword.value)
        if (res.code === 200) {
            // 后端返回 { code: 200, data: { total: xxx, list: [...] } }
            adminFiles.value = res.data.list || []
            totalFiles.value = res.data.total || 0
        }
    } catch (e) {
        ElMessage.error('获取文件列表失败，请确认管理员权限')
        console.error('管理员文件接口错误', e)
    } finally {
        loading.value = false
    }
}

const loadStats = async () => {
    try {
        const res = await fileAPI.getAdminStats()
        if (res.code === 200) {
            stats.value = res.data
        }
    } catch (e) {
        // 统计接口可能暂未实现或需要管理员权限
        console.warn('统计接口未实现或权限不足', e)
    }
}

const loadUsers = async () => {
    userLoading.value = true
    try {
        const res = await authAPI.getUsers({
            page: userCurrentPage.value,
            size: userPageSize.value,
            keyword: userSearchKeyword.value || undefined
        })
        if (res.code === 200) {
            // 后端返回 { code: 200, data: { total: xxx, list: [...] } }
            userList.value = res.data.list || []
            totalUsers.value = res.data.total || 0
        }
    } catch (e) {
        console.warn('用户管理接口未实现或权限不足', e)
    } finally {
        userLoading.value = false
    }
}

const loadUserStats = async () => {
    try {
        const res = await authAPI.getUserStats()
        if (res.code === 200) {
            userStats.value = res.data
        }
    } catch (e) {
        console.warn('用户统计接口未实现或权限不足', e)
    }
}

const handleAdminDelete = (row) => {
    ElMessageBox.confirm(`确定要强制删除 "${row.fileName}" 吗？此操作不可恢复！`, '警告', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        try {
            await fileAPI.adminDeleteFile(row.id)
            ElMessage.success('已删除')
            loadAdminFiles()
        } catch (e) {
            ElMessage.warning('管理员删除接口暂未实现或权限不足')
        }
    }).catch(() => {})
}

const toggleUserStatus = async (row) => {
    const action = row.status === 1 ? '禁用' : '启用'
    try {
        const newStatus = row.status === 1 ? 0 : 1
        await authAPI.toggleUserStatus(row.id, newStatus)
        ElMessage.success(`${action}成功`)
        loadUsers()
    } catch (e) {
        ElMessage.error(`${action}失败`)
    }
}

const deleteUser = (row) => {
    ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？此操作不可恢复！`, '警告', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        try {
            await authAPI.deleteUser(row.id)
            ElMessage.success('删除成功')
            loadUsers()
        } catch (e) {
            ElMessage.error('删除失败')
        }
    }).catch(() => {})
}

const handleMenuSelect = (index) => {
    if (index === 'admin') return
    router.push(index)
}

const handleLogout = () => {
    userStore.logout()
    router.push('/login')
}

const formatSize = (bytes) => {
    if (!bytes) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

// Tab 切换时自动加载对应数据
watch(activeTab, (tab) => {
    if (tab === 'users') {
        loadUsers()
        loadUserStats()
    } else if (tab === 'files') {
        loadAdminFiles()
        loadStats()
    }
})

onMounted(() => {
    loadAdminFiles()
    loadStats()
})
</script>

<style scoped>
.admin { height: 100vh; background: #f5f0fa; }
.el-header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; height: 60px; padding: 0 24px; box-shadow: 0 2px 12px rgba(102, 126, 234, 0.3); }
.header-content { display: flex; justify-content: space-between; align-items: center; height: 100%; }
.logo-area { display: flex; align-items: center; gap: 10px; }
.logo-icon { font-size: 28px; animation: float 3s ease-in-out infinite; }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-5px); } }
.logo-area h2 { font-size: 20px; font-weight: 700; margin: 0; letter-spacing: 2px; }
.user-info { display: flex; align-items: center; gap: 12px; }
.user-avatar { background: linear-gradient(135deg, #fbc2c8, #a18cd1); color: white; }
.username { font-size: 14px; font-weight: 500; }
.logout-btn { color: rgba(255,255,255,0.9); border: 1px solid rgba(255,255,255,0.4); background: transparent; padding: 4px 16px; border-radius: 20px; font-size: 13px; transition: all 0.3s; }
.logout-btn:hover { background: rgba(255,255,255,0.2); color: white; border-color: white; }
.main-container { height: calc(100vh - 60px); }
.sidebar { background: white; border-right: 1px solid #f0e6f6; padding-top: 12px; display: flex; flex-direction: column; }
.side-menu { border-right: none; flex: 1; }
.side-menu .el-menu-item { margin: 4px 12px; border-radius: 10px; font-size: 14px; height: 44px; line-height: 44px; transition: all 0.3s; }
.side-menu .el-menu-item:hover { background: #f8f0ff; color: #a18cd1; }
.side-menu .el-menu-item.is-active { background: linear-gradient(135deg, #a18cd1, #f5a6b5); color: white; }
.admin-menu-item.is-active { background: linear-gradient(135deg, #667eea, #764ba2) !important; color: white !important; }
.main-content { background: #f5f0fa; padding: 24px; }
.admin-tabs :deep(.el-tabs__header) { margin-bottom: 20px; }
.admin-tabs :deep(.el-tabs__item) { font-size: 15px; font-weight: 500; }
.admin-tabs :deep(.el-tabs__item.is-active) { color: #667eea; }
.admin-tabs :deep(.el-tabs__active-bar) { background: linear-gradient(135deg, #667eea, #764ba2); }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.toolbar-left { display: flex; gap: 10px; align-items: center; }
.toolbar-right { display: flex; align-items: center; }
.search-input { width: 260px; }
.action-btn { border-radius: 10px; background: linear-gradient(135deg, #667eea, #764ba2); border: none; padding: 10px 20px; font-size: 14px; transition: all 0.3s; }
.action-btn:hover { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4); }
.stat-text { font-size: 14px; color: #666; }
.stat-text b { color: #667eea; }
.file-table-wrapper { background: white; border-radius: 16px; padding: 8px; box-shadow: 0 2px 20px rgba(161, 140, 209, 0.08); }
.file-table :deep(.el-table__header th) { background: #faf6ff; color: #666; font-weight: 600; border-bottom: 2px solid #f0e6f6; }
.file-name-cell { display: flex; align-items: center; gap: 8px; }
.file-owner { color: #667eea; font-weight: 500; cursor: pointer; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
