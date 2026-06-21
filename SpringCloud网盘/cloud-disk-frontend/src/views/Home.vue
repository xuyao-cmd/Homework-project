<template>
    <div class="home">
        <Sakura />
        <el-container>
            <el-header>
                <div class="header-content">
                    <div class="logo-area">
                        <span class="logo-icon">☁️</span>
                        <h2>Cloud Disk</h2>
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
                    <el-menu router :default-active="$route.path" class="side-menu">
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
                        <el-menu-item v-if="userStore.isAdmin" index="/admin">
                            <el-icon><Setting /></el-icon>
                            <span>管理后台</span>
                        </el-menu-item>
                    </el-menu>
                    <div class="sidebar-footer">
                        <p>存储空间</p>
                        <el-progress :percentage="userStore.storagePercent" :stroke-width="8" color="#a18cd1" />
                        <p class="usage-text">已使用 {{ formatSize(userStore.usedSpace) }} / {{ formatSize(userStore.totalSpace) }}</p>
                    </div>
                </el-aside>
                <el-main class="main-content">
                    <div class="toolbar">
                        <div class="toolbar-left">
                            <el-button type="primary" @click="handleUpload" class="action-btn">
                                <el-icon><Upload /></el-icon>上传文件
                            </el-button>
                            <el-button @click="showCreateFolder = true" class="action-btn-outline">
                                <el-icon><FolderAdd /></el-icon>新建文件夹
                            </el-button>
                            <el-button type="danger" plain @click="batchDelete" :disabled="selectedIds.length === 0" class="action-btn-outline">
                                <el-icon><Delete /></el-icon>批量删除
                            </el-button>
                            <el-button @click="showMoveDialog = true" :disabled="selectedIds.length === 0" class="action-btn-outline">
                                <el-icon><Switch /></el-icon>移动
                            </el-button>
                            <el-button @click="goBack" :disabled="breadcrumb.length === 0" class="action-btn-outline">
                                <el-icon><Back /></el-icon>返回上级
                            </el-button>
                        </div>
                        <el-breadcrumb separator="›" class="breadcrumb">
                            <el-breadcrumb-item :to="{ path: '/' }">根目录</el-breadcrumb-item>
                            <el-breadcrumb-item v-for="crumb in breadcrumb" :key="crumb.id">{{ crumb.name }}</el-breadcrumb-item>
                        </el-breadcrumb>
                    </div>
                    <div class="file-table-wrapper">
                        <el-table
                            :data="fileList"
                            @selection-change="handleSelectionChange"
                            @row-dblclick="handleRowDbClick"
                            v-loading="loading"
                            class="file-table"
                            empty-text="暂无文件，上传或新建吧~"
                            highlight-current-row
                        >
                            <el-table-column type="selection" width="50" />
                            <el-table-column label="名称" min-width="260">
                                <template #default="{ row }">
                                    <div class="file-name-cell">
                                        <el-icon v-if="row.isDir === 1" size="22" color="#a18cd1"><Folder /></el-icon>
                                        <el-icon v-else size="22" :color="getFileIconColor(row.fileType)"><Document /></el-icon>
                                        <span class="file-name">{{ row.fileName }}</span>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column label="大小" width="120" align="center">
                                <template #default="{ row }">
                                    <span class="file-size">{{ row.isDir === 1 ? '-' : formatSize(row.size) }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column label="创建时间" width="180" align="center">
                                <template #default="{ row }">
                                    <span class="file-time">{{ row.createTime }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column label="操作" width="320" align="center">
                                <template #default="{ row }">
                                    <template v-if="row.isDir === 1">
                                        <el-button link type="primary" @click="enterFolder(row)" class="op-btn">打开</el-button>
                                        <el-button link type="primary" @click="handleRename(row)" class="op-btn">重命名</el-button>
                                        <el-button link type="danger" @click="handleDelete(row.id)" class="op-btn">删除</el-button>
                                    </template>
                                    <template v-else>
                                        <el-button link type="primary" @click="handlePreview(row)" class="op-btn">预览</el-button>
                                        <el-button link type="success" @click="handleDownload(row)" class="op-btn">下载</el-button>
                                        <el-button link type="primary" @click="handleRename(row)" class="op-btn">重命名</el-button>
                                        <el-button link type="primary" @click="handleShare(row)" class="op-btn">分享</el-button>
                                        <el-button link type="danger" @click="handleDelete(row.id)" class="op-btn">删除</el-button>
                                    </template>
                                </template>
                            </el-table-column>
                        </el-table>
                    </div>
                </el-main>
            </el-container>
        </el-container>

        <!-- 上传对话框 -->
        <el-dialog v-model="showUpload" title="上传文件" width="520px" class="custom-dialog" :close-on-click-modal="false">
            <el-upload drag :auto-upload="false" :on-change="handleFileChange" multiple class="upload-area">
                <el-icon size="48" color="#a18cd1"><Upload /></el-icon>
                <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
                <template #tip>
                    <div class="el-upload__tip">支持大文件分片上传（单文件最大5GB）</div>
                </template>
            </el-upload>
            <div v-if="uploadFiles.length > 0" class="upload-file-list">
                <div v-for="(f, idx) in uploadFiles" :key="idx" class="upload-file-item">
                    <div class="ufi-info">
                        <el-icon size="20"><Document /></el-icon>
                        <span class="ufi-name">{{ f.name }}</span>
                        <span class="ufi-size">{{ formatSize(f.size) }}</span>
                    </div>
                    <el-progress v-if="uploadProgress[idx]" :percentage="uploadProgress[idx]" :stroke-width="8" :color="uploadProgress[idx] === 100 ? '#67C23A' : '#a18cd1'" />
                    <el-tag v-if="uploadStatus[idx] === 'sec_upload'" type="success" size="small">秒传成功</el-tag>
                    <el-tag v-else-if="uploadStatus[idx] === 'done'" type="success" size="small">上传完成</el-tag>
                    <el-tag v-else-if="uploadStatus[idx] === 'error'" type="danger" size="small">上传失败</el-tag>
                </div>
            </div>
            <template #footer>
                <el-button @click="showUpload = false" size="large">取消</el-button>
                <el-button type="primary" @click="confirmUpload" :loading="uploading" :disabled="uploadFiles.length === 0" size="large">
                    {{ uploading ? '上传中...' : '开始上传' }}
                </el-button>
            </template>
        </el-dialog>

        <!-- 新建文件夹对话框 -->
        <el-dialog v-model="showCreateFolder" title="新建文件夹" width="380px" class="custom-dialog">
            <el-input v-model="folderName" placeholder="请输入文件夹名称" size="large" />
            <template #footer>
                <el-button @click="showCreateFolder = false" size="large">取消</el-button>
                <el-button type="primary" @click="createFolder" size="large">确定</el-button>
            </template>
        </el-dialog>

        <!-- 重命名对话框 -->
        <el-dialog v-model="showRename" title="重命名" width="380px" class="custom-dialog">
            <el-input v-model="newName" placeholder="请输入新名称" size="large" />
            <template #footer>
                <el-button @click="showRename = false" size="large">取消</el-button>
                <el-button type="primary" @click="confirmRename" size="large">确定</el-button>
            </template>
        </el-dialog>

        <!-- 分享对话框 -->
        <el-dialog v-model="showShare" title="创建分享" width="400px" class="custom-dialog">
            <div v-if="shareCode" class="share-result">
                <el-icon size="36" color="#67C23A"><CircleCheck /></el-icon>
                <p>分享链接已生成</p>
                <el-input :value="shareUrl" readonly size="large">
                    <template #append><el-button @click="copyShareUrl">复制</el-button></template>
                </el-input>
            </div>
            <div v-else>
                <p class="share-label">分享有效时间：</p>
                <el-radio-group v-model="expireHours" size="large">
                    <el-radio-button :label="24">24小时</el-radio-button>
                    <el-radio-button :label="168">7天</el-radio-button>
                    <el-radio-button :label="720">30天</el-radio-button>
                </el-radio-group>
                <div style="margin-top: 24px; text-align: center;">
                    <el-button type="primary" @click="doCreateShare" size="large">生成分享链接</el-button>
                </div>
            </div>
        </el-dialog>

        <!-- 移动对话框 -->
        <el-dialog v-model="showMoveDialog" title="移动到" width="380px" class="custom-dialog">
            <p class="share-label">选择目标目录（0 为根目录）：</p>
            <el-input-number v-model="targetParentId" :min="0" size="large" style="width: 100%" />
            <template #footer>
                <el-button @click="showMoveDialog = false" size="large">取消</el-button>
                <el-button type="primary" @click="confirmMove" size="large">确定移动</el-button>
            </template>
        </el-dialog>

        <!-- 预览对话框 -->
        <el-dialog v-model="showPreview" :title="'预览：' + previewFileName" width="70%" class="custom-dialog" destroy-on-close>
            <div v-if="previewLoading" style="text-align:center; padding:60px;">
                <el-icon class="is-loading" size="40"><Loading /></el-icon>
                <p>加载中...</p>
            </div>
            <div v-else-if="previewInfo && previewInfo.supportPreview" class="preview-container">
                <img v-if="isImage(previewInfo.fileType)" :src="previewUrl" style="max-width:100%; max-height:70vh; border-radius:8px;" />
                <video v-else-if="isVideo(previewInfo.fileType)" :src="previewUrl" controls style="max-width:100%; max-height:70vh; border-radius:8px;" />
                <audio v-else-if="isAudio(previewInfo.fileType)" :src="previewUrl" controls style="width:100%;" />
                <iframe v-else-if="isPdf(previewInfo.fileType)" :src="previewUrl" style="width:100%; height:70vh; border:none; border-radius:8px;" />
                <div v-else class="preview-fallback">
                    <el-icon size="48"><Document /></el-icon>
                    <p>暂不支持在线预览此类型文件</p>
                    <p>文件类型：{{ previewInfo.fileType }}</p>
                    <p>文件大小：{{ formatSize(previewInfo.fileSize) }}</p>
                    <el-button type="primary" @click="downloadPreviewFile">下载文件</el-button>
                </div>
            </div>
            <div v-else class="preview-fallback">
                <el-icon size="48"><Document /></el-icon>
                <p>暂不支持在线预览此类型文件</p>
                <p v-if="previewInfo">文件大小：{{ formatSize(previewInfo.fileSize) }}</p>
                <el-button type="primary" @click="downloadPreviewFile">下载文件</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fileAPI } from '@/api/file'
import { useUserStore } from '@/stores/user'
import Sakura from '@/components/Sakura.vue'
import SparkMD5 from 'spark-md5'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const fileList = ref([])
const selectedIds = ref([])
const currentParentId = ref(0)
const breadcrumb = ref([])

// UI 状态
const showUpload = ref(false)
const showCreateFolder = ref(false)
const showRename = ref(false)
const showShare = ref(false)
const showMoveDialog = ref(false)
const showPreview = ref(false)
const uploading = ref(false)
const previewLoading = ref(false)
const folderName = ref('')
const renameId = ref(null)
const newName = ref('')
const uploadFiles = ref([])
const uploadProgress = ref([])
const uploadStatus = ref([])
const shareFileId = ref(null)
const shareCode = ref('')
const expireHours = ref(24)
const targetParentId = ref(0)
const previewInfo = ref(null)
const previewFileName = ref('')
const previewFileId = ref(null)

const CHUNK_SIZE = 5 * 1024 * 1024 // 5MB 每片

const shareUrl = computed(() => `http://localhost:5173/share/${shareCode.value}`)
const previewUrl = computed(() => fileAPI.getDownloadUrl(previewFileId.value))

// 加载文件列表
const loadFileList = async () => {
    loading.value = true
    try {
        const res = await fileAPI.getFileList(currentParentId.value)
        if (res.code === 200) {
            fileList.value = res.data
        }
    } finally {
        loading.value = false
    }
}

// 进入文件夹
const enterFolder = (row) => {
    breadcrumb.value.push({ id: row.id, name: row.fileName })
    currentParentId.value = row.id
    loadFileList()
}

// 返回上级
const goBack = () => {
    breadcrumb.value.pop()
    currentParentId.value = breadcrumb.value.length > 0 ? breadcrumb.value[breadcrumb.value.length - 1].id : 0
    loadFileList()
}

// 双击行：文件夹进入，文件预览
const handleRowDbClick = (row) => {
    if (row.isDir === 1) {
        enterFolder(row)
    } else {
        handlePreview(row)
    }
}

// 计算文件 MD5
const computeMD5 = (file) => {
    return new Promise((resolve, reject) => {
        const chunkSize = 2 * 1024 * 1024 // 2MB 每片计算MD5
        const chunks = Math.ceil(file.size / chunkSize)
        const spark = new SparkMD5.ArrayBuffer()
        const reader = new FileReader()
        let currentChunk = 0

        reader.onload = (e) => {
            spark.append(e.target.result)
            currentChunk++
            if (currentChunk < chunks) {
                loadNext()
            } else {
                resolve(spark.end())
            }
        }
        reader.onerror = () => reject(new Error('MD5 计算失败'))

        const loadNext = () => {
            const start = currentChunk * chunkSize
            const end = Math.min(start + chunkSize, file.size)
            reader.readAsArrayBuffer(file.slice(start, end))
        }
        loadNext()
    })
}

// 上传文件（支持秒传+分片）
const uploadSingleFile = async (file, index) => {
    uploadStatus.value[index] = 'computing'
    try {
        // 1. 计算 MD5
        const md5 = await computeMD5(file)
        
        // 2. 秒传检测
        try {
            const checkRes = await fileAPI.checkMd5(md5)
            if (checkRes.code === 200 && checkRes.data === true) {
                uploadStatus.value[index] = 'sec_upload'
                uploadProgress.value[index] = 100
                ElMessage.success(`${file.name} 秒传成功`)
                return
            }
        } catch (e) {
            // 秒传接口可能未实现，继续正常上传
        }

        uploadStatus.value[index] = 'uploading'
        
        // 3. 小文件直接上传
        if (file.size <= CHUNK_SIZE) {
            await fileAPI.uploadFile(file, currentParentId.value)
            uploadProgress.value[index] = 100
            uploadStatus.value[index] = 'done'
            return
        }

        // 4. 大文件分片上传
        const totalChunks = Math.ceil(file.size / CHUNK_SIZE)
        let uploadId = null

        // 初始化分片上传
        try {
            const initRes = await fileAPI.initChunkUpload(file.name, file.size, totalChunks, currentParentId.value)
            if (initRes.code === 200) {
                uploadId = initRes.data
            }
        } catch (e) {
            // 分片接口可能未实现，回退到普通上传
        }

        if (uploadId) {
            // 分片上传
            for (let i = 0; i < totalChunks; i++) {
                const start = i * CHUNK_SIZE
                const end = Math.min(start + CHUNK_SIZE, file.size)
                const chunk = file.slice(start, end)
                await fileAPI.uploadChunk(chunk, uploadId, i)
                uploadProgress.value[index] = Math.round(((i + 1) / totalChunks) * 100)
            }
            // 合并分片
            await fileAPI.mergeChunks(uploadId)
        } else {
            // 回退普通上传
            await fileAPI.uploadFile(file, currentParentId.value)
        }

        uploadProgress.value[index] = 100
        uploadStatus.value[index] = 'done'
    } catch (e) {
        uploadStatus.value[index] = 'error'
        ElMessage.error(`${file.name} 上传失败`)
    }
}

const handleFileChange = (file) => {
    uploadFiles.value.push(file.raw)
    uploadProgress.value.push(0)
    uploadStatus.value.push('pending')
}

const confirmUpload = async () => {
    if (uploadFiles.value.length === 0) return
    uploading.value = true
    try {
        const tasks = uploadFiles.value.map((file, idx) => uploadSingleFile(file, idx))
        await Promise.all(tasks)
        ElMessage.success('上传完成')
        loadFileList()
    } catch (e) {
        ElMessage.error('部分文件上传失败')
    } finally {
        uploading.value = false
    }
}

// 新建文件夹
const createFolder = async () => {
    if (!folderName.value) return
    await fileAPI.createFolder(folderName.value, currentParentId.value)
    ElMessage.success('创建成功')
    showCreateFolder.value = false
    folderName.value = ''
    loadFileList()
}

// 重命名
const handleRename = (row) => {
    renameId.value = row.id
    newName.value = row.fileName
    showRename.value = true
}
const confirmRename = async () => {
    await fileAPI.rename(renameId.value, newName.value)
    ElMessage.success('重命名成功')
    showRename.value = false
    loadFileList()
}

// 删除
const handleDelete = (id) => {
    ElMessageBox.confirm('确定移动到回收站吗？', '提示').then(async () => {
        await fileAPI.moveToRecycle([id])
        ElMessage.success('已移动到回收站')
        loadFileList()
    })
}
const batchDelete = () => {
    ElMessageBox.confirm(`确定删除 ${selectedIds.value.length} 个文件吗？`, '提示').then(async () => {
        await fileAPI.moveToRecycle(selectedIds.value)
        ElMessage.success('已移动到回收站')
        selectedIds.value = []
        loadFileList()
    })
}

// 分享
const handleShare = (row) => {
    shareFileId.value = row.id
    shareCode.value = ''
    showShare.value = true
}
const doCreateShare = async () => {
    const res = await fileAPI.createShare(shareFileId.value, expireHours.value)
    shareCode.value = res.data
}
const copyShareUrl = () => {
    navigator.clipboard.writeText(shareUrl.value)
    ElMessage.success('链接已复制')
}

// 移动
const confirmMove = async () => {
    await fileAPI.moveFiles(selectedIds.value, targetParentId.value)
    ElMessage.success('移动成功')
    showMoveDialog.value = false
    selectedIds.value = []
    targetParentId.value = 0
    loadFileList()
}

// 预览
const handlePreview = async (row) => {
    previewFileName.value = row.fileName
    previewFileId.value = row.id
    previewInfo.value = null
    previewLoading.value = true
    showPreview.value = true
    try {
        const res = await fileAPI.previewFile(row.id)
        if (res.code === 200) {
            previewInfo.value = res.data
        }
    } catch (e) {
        previewInfo.value = null
    } finally {
        previewLoading.value = false
    }
}

// 下载
const handleDownload = (row) => {
    const url = fileAPI.getDownloadUrl(row.id)
    window.open(url, '_blank')
}
const downloadPreviewFile = () => {
    if (previewFileId.value) {
        window.open(fileAPI.getDownloadUrl(previewFileId.value), '_blank')
    }
}

// 文件类型判断
const isImage = (type) => ['jpg','jpeg','png','gif','bmp','webp','svg'].includes(type?.toLowerCase())
const isVideo = (type) => ['mp4','avi','mov','wmv','flv','mkv'].includes(type?.toLowerCase())
const isAudio = (type) => ['mp3','wav','flac','aac','ogg'].includes(type?.toLowerCase())
const isPdf = (type) => type?.toLowerCase() === 'pdf'
const getFileIconColor = (type) => {
    if (isImage(type)) return '#67C23A'
    if (isVideo(type)) return '#E6A23C'
    if (isAudio(type)) return '#F56C6C'
    if (isPdf(type)) return '#E74C3C'
    return '#909399'
}

// 其他
const handleSelectionChange = (selection) => {
    selectedIds.value = selection.map(s => s.id)
}
const handleUpload = () => {
    showUpload.value = true
    uploadFiles.value = []
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

// 加载存储空间使用情况
const loadStorageUsage = async () => {
    try {
        const res = await fileAPI.getStorageUsage()
        if (res.code === 200) {
            userStore.setStorageUsage(res.data.usedSpace, res.data.totalSpace)
        }
    } catch (e) {
        // 后端接口可能未实现，使用默认值
        console.warn('存储空间接口暂未实现')
    }
}

onMounted(() => {
    loadFileList()
    loadStorageUsage()
})
</script>

<style scoped>
.home { height: 100vh; background: #f5f0fa; }
.el-header { background: linear-gradient(135deg, #a18cd1 0%, #f5a6b5 100%); color: white; height: 60px; padding: 0 24px; box-shadow: 0 2px 12px rgba(161, 140, 209, 0.3); }
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
.sidebar-footer { padding: 20px 20px; border-top: 1px solid #f0e6f6; }
.sidebar-footer p { font-size: 13px; color: #999; margin-bottom: 8px; }
.usage-text { margin-top: 6px !important; color: #a18cd1 !important; }
.main-content { background: #f5f0fa; padding: 24px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.toolbar-left { display: flex; gap: 10px; }
.action-btn { border-radius: 10px; background: linear-gradient(135deg, #a18cd1, #f5a6b5); border: none; padding: 10px 20px; font-size: 14px; transition: all 0.3s; }
.action-btn:hover { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(161, 140, 209, 0.4); }
.action-btn-outline { border-radius: 10px; padding: 10px 20px; font-size: 14px; transition: all 0.3s; }
.action-btn-outline:hover { border-color: #a18cd1; color: #a18cd1; }
.breadcrumb { font-size: 14px; }
.file-table-wrapper { background: white; border-radius: 16px; padding: 8px; box-shadow: 0 2px 20px rgba(161, 140, 209, 0.08); }
.file-table :deep(.el-table__header th) { background: #faf6ff; color: #666; font-weight: 600; border-bottom: 2px solid #f0e6f6; }
.file-table :deep(.el-table__row) { transition: background 0.2s; }
.file-table :deep(.el-table__row:hover) { background: #fdf6ff; }
.file-name-cell { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.file-name { font-weight: 500; color: #333; }
.file-size, .file-time { color: #999; font-size: 13px; }
.op-btn { font-size: 12px; padding: 2px 6px; border-radius: 6px; transition: all 0.2s; }
.op-btn:hover { background: #f8f0ff; }
.custom-dialog :deep(.el-dialog) { border-radius: 16px; }
.custom-dialog :deep(.el-dialog__header) { border-bottom: 1px solid #f0e6f6; padding-bottom: 16px; }
.upload-area :deep(.el-upload-dragger) { border-radius: 16px; border: 2px dashed #d5c8e8; background: #fdf6ff; padding: 40px; }
.upload-file-list { margin-top: 16px; max-height: 240px; overflow-y: auto; }
.upload-file-item { background: #faf6ff; border-radius: 10px; padding: 10px 14px; margin-bottom: 8px; }
.ufi-info { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.ufi-name { flex: 1; font-size: 13px; font-weight: 500; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ufi-size { font-size: 12px; color: #999; flex-shrink: 0; }
.share-label { font-size: 15px; color: #666; margin-bottom: 16px; }
.share-result { text-align: center; padding: 20px 0; }
.share-result p { color: #666; margin: 12px 0 20px; }
.preview-container { text-align: center; }
.preview-fallback { text-align: center; padding: 40px 0; color: #999; }
.preview-fallback p { margin: 8px 0; }
</style>
