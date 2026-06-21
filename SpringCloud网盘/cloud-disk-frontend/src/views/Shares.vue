<template>
    <div class="shares">
        <Sakura />
        <el-container>
            <el-header>
                <div class="header-content">
                    <div class="logo-area">
                        <el-icon size="24"><Share /></el-icon>
                        <h2>我的分享</h2>
                    </div>
                    <el-button @click="$router.push('/')" class="back-btn">
                        <el-icon><ArrowLeft /></el-icon>返回文件
                    </el-button>
                </div>
            </el-header>
            <el-main>
                <div class="table-wrapper">
                    <el-table :data="shareList" v-loading="loading" empty-text="暂无分享记录~">
                        <el-table-column label="文件名" prop="fileName" min-width="200">
                            <template #default="{ row }">
                                <div class="file-name-cell">
                                    <el-icon size="20" color="#a18cd1"><Document /></el-icon>
                                    <span>{{ row.fileName }}</span>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column label="分享码" prop="shareCode" width="160" align="center">
                            <template #default="{ row }">
                                <el-tag type="info" size="small" effect="plain">{{ row.shareCode }}</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="创建时间" prop="createTime" width="180" align="center">
                            <template #default="{ row }">
                                <span class="time-text">{{ row.createTime }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column label="过期时间" prop="expireTime" width="180" align="center">
                            <template #default="{ row }">
                                <span class="time-text">{{ row.expireTime }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="180" align="center">
                            <template #default="{ row }">
                                <el-button link type="primary" @click="copyShareLink(row.shareCode)" class="op-btn">复制链接</el-button>
                                <el-button link type="danger" @click="handleCancelShare(row.id)" class="op-btn">取消分享</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-main>
        </el-container>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fileAPI } from '@/api/file'
import Sakura from '@/components/Sakura.vue'

const shareList = ref([])
const loading = ref(false)

const loadShares = async () => {
    loading.value = true
    try {
        const res = await fileAPI.getMyShares()
        if (res.code === 200) shareList.value = res.data
    } finally {
        loading.value = false
    }
}

const copyShareLink = (shareCode) => {
    const url = `http://localhost:5173/share/${shareCode}`
    navigator.clipboard.writeText(url)
    ElMessage.success('链接已复制')
}

const handleCancelShare = async (shareId) => {
    ElMessageBox.confirm('确定取消分享吗？', '提示').then(async () => {
        await fileAPI.cancelShare(shareId)
        ElMessage.success('已取消分享')
        loadShares()
    })
}

onMounted(() => loadShares())
</script>

<style scoped>
.shares { height: 100vh; background: #f5f0fa; }

.el-header {
    background: linear-gradient(135deg, #a18cd1 0%, #f5a6b5 100%);
    color: white;
    height: 60px;
    box-shadow: 0 2px 12px rgba(161, 140, 209, 0.3);
}

.header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;
}

.logo-area {
    display: flex;
    align-items: center;
    gap: 10px;
}

.logo-area h2 {
    font-size: 20px;
    font-weight: 700;
    margin: 0;
}

.back-btn {
    color: white;
    background: rgba(255, 255, 255, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 20px;
    padding: 6px 18px;
    transition: all 0.3s;
}

.back-btn:hover {
    background: rgba(255, 255, 255, 0.25);
    border-color: white;
}

.el-main {
    padding: 24px;
    max-width: 1200px;
    margin: 0 auto;
}

.table-wrapper {
    background: white;
    border-radius: 16px;
    padding: 8px;
    box-shadow: 0 2px 20px rgba(161, 140, 209, 0.08);
}

.file-name-cell {
    display: flex;
    align-items: center;
    gap: 10px;
}

.time-text {
    color: #999;
    font-size: 13px;
}

.op-btn {
    font-size: 13px;
    padding: 4px 10px;
    border-radius: 6px;
}

.op-btn:hover {
    background: #f8f0ff;
}

:deep(.el-table__header th) {
    background: #faf6ff;
}
</style>
