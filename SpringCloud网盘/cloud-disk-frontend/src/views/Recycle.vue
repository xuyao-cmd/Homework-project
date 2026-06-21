<template>
    <div class="recycle">
        <Sakura />
        <el-container>
            <el-header>
                <div class="header-content">
                    <div class="logo-area">
                        <el-icon size="24"><Delete /></el-icon>
                        <h2>回收站</h2>
                    </div>
                    <el-button @click="$router.push('/')" class="back-btn">
                        <el-icon><ArrowLeft /></el-icon>返回文件
                    </el-button>
                </div>
            </el-header>
            <el-main>
                <div class="toolbar">
                    <el-button type="primary" @click="restoreFiles" :disabled="selectedIds.length === 0" class="action-btn">
                        <el-icon><RefreshRight /></el-icon>还原
                    </el-button>
                    <el-button type="danger" plain @click="permanentDelete" :disabled="selectedIds.length === 0" class="action-btn-outline">
                        <el-icon><Delete /></el-icon>彻底删除
                    </el-button>
                    <el-button type="danger" @click="clearRecycle" class="action-btn-danger">
                        <el-icon><DeleteFilled /></el-icon>清空回收站
                    </el-button>
                </div>
                <div class="table-wrapper">
                    <el-table :data="fileList" @selection-change="handleSelectionChange" empty-text="回收站是空的~">
                        <el-table-column type="selection" width="50" />
                        <el-table-column label="名称" prop="fileName" min-width="300">
                            <template #default="{ row }">
                                <div class="file-name-cell">
                                    <el-icon size="20" color="#999"><Document /></el-icon>
                                    <span>{{ row.fileName }}</span>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column label="删除时间" prop="deletedTime" width="200" align="center">
                            <template #default="{ row }">
                                <span class="time-text">{{ row.deletedTime }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="180" align="center">
                            <template #default="{ row }">
                                <el-button link type="primary" @click="restoreFile(row.id)" class="op-btn">还原</el-button>
                                <el-button link type="danger" @click="deleteFile(row.id)" class="op-btn">彻底删除</el-button>
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

const fileList = ref([])
const selectedIds = ref([])

const loadRecycleList = async () => {
    const res = await fileAPI.getRecycleList()
    if (res.code === 200) fileList.value = res.data
}

const handleSelectionChange = (selection) => {
    selectedIds.value = selection.map(s => s.id)
}

const restoreFile = async (id) => {
    await fileAPI.restoreFromRecycle([id])
    ElMessage.success('还原成功')
    loadRecycleList()
}

const restoreFiles = async () => {
    await fileAPI.restoreFromRecycle(selectedIds.value)
    ElMessage.success('还原成功')
    selectedIds.value = []
    loadRecycleList()
}

const deleteFile = async (id) => {
    await fileAPI.permanentDelete([id])
    ElMessage.success('删除成功')
    loadRecycleList()
}

const permanentDelete = async () => {
    await fileAPI.permanentDelete(selectedIds.value)
    ElMessage.success('删除成功')
    selectedIds.value = []
    loadRecycleList()
}

const clearRecycle = async () => {
    ElMessageBox.confirm('确定要清空回收站吗？此操作不可撤销', '警告', { type: 'warning' }).then(async () => {
        await fileAPI.clearRecycle()
        ElMessage.success('清空成功')
        loadRecycleList()
    })
}

onMounted(() => loadRecycleList())
</script>

<style scoped>
.recycle { height: 100vh; background: #f5f0fa; }

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

.toolbar {
    margin-bottom: 20px;
    display: flex;
    gap: 10px;
}

.action-btn {
    border-radius: 10px;
    padding: 10px 20px;
}

.action-btn-outline {
    border-radius: 10px;
    padding: 10px 20px;
}

.action-btn-danger {
    border-radius: 10px;
    padding: 10px 20px;
    background: #f56c6c;
    border-color: #f56c6c;
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
