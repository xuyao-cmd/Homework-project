import request from './request'

export const fileAPI = {
    // 获取文件列表
    getFileList(parentId = 0) {
        return request.get(`/file/list?parentId=${parentId}`)
    },
    
    // 上传文件（普通）
    uploadFile(file, parentId = 0) {
        const formData = new FormData()
        formData.append('file', file)
        formData.append('parentId', parentId)
        return request.post('/file/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        })
    },

    // 秒传检测
    checkMd5(md5) {
        return request.post('/file/check-md5', null, { params: { md5 } })
    },

    // ===== 分片上传 =====
    // 初始化分片上传
    initChunkUpload(fileName, fileSize, totalChunks, parentId = 0) {
        return request.post('/file/chunk/init', null, {
            params: { fileName, fileSize, totalChunks, parentId }
        })
    },
    // 上传分片
    uploadChunk(chunk, uploadId, chunkIndex) {
        const formData = new FormData()
        formData.append('chunk', chunk)
        formData.append('uploadId', uploadId)
        formData.append('chunkIndex', chunkIndex)
        return request.post('/file/chunk/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        })
    },
    // 合并分片
    mergeChunks(uploadId) {
        return request.post('/file/chunk/merge', null, { params: { uploadId } })
    },
    // 查询上传进度
    getUploadProgress(uploadId) {
        return request.get('/file/chunk/progress', { params: { uploadId } })
    },

    // 新建文件夹
    createFolder(folderName, parentId = 0) {
        return request.post('/file/folder', null, {
            params: { folderName, parentId }
        })
    },
    
    // 重命名
    rename(id, newName) {
        return request.put(`/file/rename?id=${id}&newName=${newName}`)
    },
    
    // 移动到回收站
    moveToRecycle(ids) {
        return request.delete('/file/move-to-recycle', { params: { ids: ids } })
    },
    
    // 获取回收站列表
    getRecycleList() {
        return request.get('/file/recycle/list')
    },
    
    // 还原文件
    restoreFromRecycle(ids) {
        return request.put('/file/recycle/restore', null, { params: { ids: ids } })
    },
    
    // 彻底删除
    permanentDelete(ids) {
        return request.delete('/file/recycle/permanent', { params: { ids: ids } })
    },
    
    // 清空回收站
    clearRecycle() {
        return request.delete('/file/recycle/clear')
    },
    
    // 创建分享
    createShare(fileId, expireHours = 24) {
        return request.post('/file/share/create', null, { params: { fileId, expireHours } })
    },
    
    // 获取分享信息
    getShareInfo(shareCode) {
        return request.get(`/file/share/info/${shareCode}`)
    },
    
    // 我的分享列表
    getMyShares() {
        return request.get('/file/share/my-shares')
    },
    
    // 取消分享
    cancelShare(shareId) {
        return request.delete(`/file/share/cancel/${shareId}`)
    },
    
    // 预览文件信息
    previewFile(userFileId) {
        return request.get(`/file/preview/${userFileId}`)
    },
    
    // 获取下载/预览 URL（带 token）
    getDownloadUrl(userFileId) {
        const token = localStorage.getItem('token')
        return `http://localhost:8080/file/download/${userFileId}?token=${token}`
    },
    
    // 移动文件
    moveFiles(ids, targetParentId) {
        return request.put('/file/move', null, { params: { ids: ids, targetParentId } })
    },

    // ===== 存储空间 =====
    // 获取当前用户存储空间使用情况
    getStorageUsage() {
        return request.get('/file/storage')
    },

    // ===== 管理员接口 =====
    // 管理员：全部文件列表
    getAdminFiles(page = 1, size = 10, keyword = '') {
        return request.get('/file/admin/files', { params: { page, size, keyword } })
    },
    // 管理员：文件统计
    getAdminStats() {
        return request.get('/file/admin/stats')
    },
    // 管理员：强制删除文件
    adminDeleteFile(userFileId) {
        return request.delete(`/file/admin/file/${userFileId}`)
    }
}
