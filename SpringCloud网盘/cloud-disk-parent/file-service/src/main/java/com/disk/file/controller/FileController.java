package com.disk.file.controller;

import com.disk.common.Result;
import com.disk.file.service.FileService;
import com.disk.file.service.ShareService;
import com.disk.file.service.UserFileService;
import com.disk.file.vo.FilePreviewVo;
import com.disk.file.vo.ShareVo;
import com.disk.file.vo.UserFileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final UserFileService userFileService;
    private final ShareService shareService;

    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                return 1L;
            }
        }
        return 1L;
    }

    // ========== 秒传 ==========

    @PostMapping("/check-md5")
    public Result<Boolean> checkMd5(@RequestParam String md5, HttpServletRequest request) {
        return fileService.checkMd5(md5, getCurrentUserId(request));
    }

    // ========== 普通上传 ==========

    @PostMapping("/upload")
    public Result<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parentId", required = false) Long parentId,
            HttpServletRequest request) throws IOException {
        return fileService.uploadFile(file, getCurrentUserId(request), parentId);
    }

    // ========== 分片上传 ==========

    /**
     * 初始化分片上传
     * POST /file/chunk/init
     */
    @PostMapping("/chunk/init")
    public Result<Map<String, Object>> initChunkUpload(
            @RequestParam String fileName,
            @RequestParam Long fileSize,
            @RequestParam Integer totalChunks,
            @RequestParam(value = "parentId", required = false) Long parentId,
            HttpServletRequest request) {
        return fileService.initChunkUpload(fileName, fileSize, totalChunks, getCurrentUserId(request), parentId);
    }

    /**
     * 上传分片
     * POST /file/chunk/upload
     */
    @PostMapping("/chunk/upload")
    public Result<String> uploadChunk(
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam String uploadId,
            @RequestParam Integer chunkIndex,
            HttpServletRequest request) throws IOException {
        return fileService.uploadChunk(chunk, uploadId, chunkIndex, getCurrentUserId(request));
    }

    /**
     * 合并分片
     * POST /file/chunk/merge
     */
    @PostMapping("/chunk/merge")
    public Result<String> mergeChunks(
            @RequestParam String uploadId,
            HttpServletRequest request) throws IOException {
        return fileService.mergeChunks(uploadId, getCurrentUserId(request));
    }

    /**
     * 查询上传进度
     * GET /file/chunk/progress?uploadId=xxx
     */
    @GetMapping("/chunk/progress")
    public Result<Map<String, Object>> getUploadProgress(@RequestParam String uploadId) {
        return fileService.getUploadProgress(uploadId);
    }

    // ========== 文件列表 ==========

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("file-service is running");
    }

    @GetMapping("/list")
    public Result<List<UserFileVo>> getFileList(
            @RequestParam(value = "parentId", defaultValue = "0") Long parentId,
            HttpServletRequest request) {
        return userFileService.getFileList(parentId, getCurrentUserId(request));
    }

    // ========== 目录操作 ==========

    @PostMapping("/folder")
    public Result<String> createFolder(
            @RequestParam String folderName,
            @RequestParam(value = "parentId", defaultValue = "0") Long parentId,
            HttpServletRequest request) {
        return userFileService.createFolder(folderName, parentId, getCurrentUserId(request));
    }

    @PutMapping("/rename")
    public Result<String> rename(
            @RequestParam Long id,
            @RequestParam String newName,
            HttpServletRequest request) {
        return userFileService.rename(id, newName, getCurrentUserId(request));
    }

    // ========== 回收站 ==========

    @DeleteMapping("/move-to-recycle")
    public Result<String> moveToRecycle(@RequestParam List<Long> ids, HttpServletRequest request) {
        return userFileService.moveToRecycle(ids, getCurrentUserId(request));
    }

    @GetMapping("/recycle/list")
    public Result<List<UserFileVo>> getRecycleList(HttpServletRequest request) {
        return userFileService.getRecycleList(getCurrentUserId(request));
    }

    @PutMapping("/recycle/restore")
    public Result<String> restoreFromRecycle(@RequestParam List<Long> ids, HttpServletRequest request) {
        return userFileService.restoreFromRecycle(ids, getCurrentUserId(request));
    }

    @DeleteMapping("/recycle/permanent")
    public Result<String> permanentDelete(@RequestParam List<Long> ids, HttpServletRequest request) {
        return userFileService.permanentDelete(ids, getCurrentUserId(request));
    }

    @DeleteMapping("/recycle/clear")
    public Result<String> clearRecycle(HttpServletRequest request) {
        return userFileService.clearRecycle(getCurrentUserId(request));
    }

    // ========== 文件分享 ==========

    @PostMapping("/share/create")
    public Result<String> createShare(
            @RequestParam Long fileId,
            @RequestParam(required = false) Integer expireHours,
            HttpServletRequest request) {
        return shareService.createShare(fileId, getCurrentUserId(request), expireHours);
    }

    @GetMapping("/share/info/{shareCode}")
    public Result<ShareVo> getShareInfo(@PathVariable String shareCode) {
        return shareService.getShareInfo(shareCode);
    }

    @DeleteMapping("/share/cancel/{shareId}")
    public Result<String> cancelShare(@PathVariable Long shareId, HttpServletRequest request) {
        return shareService.cancelShare(shareId, getCurrentUserId(request));
    }

    @GetMapping("/share/my-shares")
    public Result<List<ShareVo>> getMyShares(HttpServletRequest request) {
        return shareService.getMyShares(getCurrentUserId(request));
    }

    // ========== 文件移动 ==========

    @PutMapping("/move")
    public Result<String> moveFiles(
            @RequestParam List<Long> ids,
            @RequestParam(required = false, defaultValue = "0") Long targetParentId,
            HttpServletRequest request) {
        return userFileService.moveFiles(ids, targetParentId, getCurrentUserId(request));
    }

    // ========== 文件预览/下载 ==========

    @GetMapping("/preview/{userFileId}")
    public Result<FilePreviewVo> previewFile(@PathVariable Long userFileId, HttpServletRequest request) {
        return fileService.previewFile(userFileId, getCurrentUserId(request));
    }

    @GetMapping("/download/{userFileId}")
    public void downloadFile(
            @PathVariable Long userFileId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        fileService.downloadFile(userFileId, getCurrentUserId(request), response);
    }

    // ========== 存储空间 ==========

    /**
     * 获取当前用户存储空间使用情况
     * GET /file/storage
     */
    @GetMapping("/storage")
    public Result<Map<String, Object>> getStorageUsage(HttpServletRequest request) {
        return fileService.getStorageUsage(getCurrentUserId(request));
    }

    // ========== 管理员 - 文件管理 ==========

    /**
     * 管理员：全部文件列表
     * GET /file/admin/files?page=1&size=10&keyword=xxx
     */
    @GetMapping("/admin/files")
    public Result<Map<String, Object>> getAllFiles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        return fileService.getAdminFileList(page, size, keyword, getCurrentUserId(request));
    }

    /**
     * 管理员：文件统计
     * GET /file/admin/stats
     */
    @GetMapping("/admin/stats")
    public Result<Map<String, Object>> getFileStats(HttpServletRequest request) {
        return fileService.getAdminFileStats(getCurrentUserId(request));
    }

    /**
     * 管理员：强制删除文件
     * DELETE /file/admin/file/{userFileId}
     */
    @DeleteMapping("/admin/file/{userFileId}")
    public Result<String> adminDeleteFile(
            @PathVariable Long userFileId,
            HttpServletRequest request) {
        return fileService.adminDeleteFile(userFileId, getCurrentUserId(request));
    }
}
