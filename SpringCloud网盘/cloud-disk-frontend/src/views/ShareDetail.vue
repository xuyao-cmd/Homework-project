<template>
    <div class="share-detail">
        <el-card class="share-card" v-loading="loading">
            <h2>分享文件</h2>
            <div v-if="shareInfo">
                <p><strong>文件名：</strong>{{ shareInfo.fileName }}</p>
                <p><strong>分享者：</strong>{{ shareInfo.username }}</p>
                <p><strong>过期时间：</strong>{{ shareInfo.expireTime }}</p>
                <el-button type="primary" @click="handleDownload">下载文件</el-button>
            </div>
            <el-empty v-else description="分享不存在或已过期" />
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fileAPI } from '@/api/file'

const route = useRoute()
const loading = ref(false)
const shareInfo = ref(null)

const loadShareInfo = async () => {
    loading.value = true
    try {
        const code = route.params.code
        const res = await fileAPI.getShareInfo(code)
        if (res.code === 200) {
            shareInfo.value = res.data
        }
    } catch {
        shareInfo.value = null
    } finally {
        loading.value = false
    }
}

const handleDownload = () => {
    if (shareInfo.value) {
        const url = fileAPI.getDownloadUrl(shareInfo.value.fileId)
        window.open(url)
    }
}

onMounted(() => loadShareInfo())
</script>

<style scoped>
.share-detail {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.share-card {
    width: 500px;
    padding: 20px;
}
</style>
