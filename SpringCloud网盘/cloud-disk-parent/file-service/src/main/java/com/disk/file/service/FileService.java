package com.disk.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disk.common.Result;
import com.disk.file.entity.File;
import com.disk.file.entity.User;
import com.disk.file.entity.UserFile;
import com.disk.file.mapper.FileMapper;
import com.disk.file.mapper.ShareMapper;
import com.disk.file.mapper.UserFileMapper;
import com.disk.file.mapper.UserMapper;
import com.disk.file.vo.FilePreviewVo;
import com.disk.file.vo.UserFileVo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService extends ServiceImpl<FileMapper, File> {

    private final FileMapper fileMapper;
    private final UserFileMapper userFileMapper;
    private final UserFileService userFileService;
    private final ShareMapper shareMapper;
    private final UserMapper userMapper;

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    // 分片上传信息缓存: uploadId -> UploadInfo
    private final Map<String, UploadInfo> chunkUploadCache = new ConcurrentHashMap<>();

    @Data
    static class UploadInfo {
        String fileName;
        Long fileSize;
        Integer totalChunks;
        Long userId;
        Long parentId;
        String tempDir;
        Set<Integer> uploadedChunks = new HashSet<>();
        LocalDateTime createTime = LocalDateTime.now();
    }

    // ========== 秒传 ==========

    public Result<Boolean> checkMd5(String md5, Long userId) {
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getMd5, md5);
        File file = fileMapper.selectOne(wrapper);

        if (file != null) {
            UserFile userFile = new UserFile();
            userFile.setUserId(userId);
            userFile.setFileId(file.getId());
            userFile.setFileName(file.getName());
            userFile.setParentId(0L);
            userFile.setIsDir(0);
            userFile.setIsDeleted(0);
            userFile.setCreateTime(LocalDateTime.now());
            userFileMapper.insert(userFile);
            return Result.success(true);
        }
        return Result.success(false);
    }

    // ========== 普通上传 ==========

    public Result<String> uploadFile(MultipartFile file, Long userId, Long parentId) throws IOException {
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String md5 = org.springframework.util.DigestUtils.md5DigestAsHex(file.getBytes());

        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getMd5, md5);
        File existFile = fileMapper.selectOne(wrapper);

        Long fileId;
        if (existFile != null) {
            fileId = existFile.getId();
        } else {
            String originalFilename = file.getOriginalFilename();
            String saveFileName = UUID.randomUUID() + "_" + originalFilename;
            Path filePath = uploadDir.resolve(saveFileName);
            file.transferTo(filePath.toFile());

            File newFile = new File();
            newFile.setMd5(md5);
            newFile.setName(originalFilename);
            newFile.setSize(file.getSize());
            newFile.setPath(filePath.toString());
            newFile.setType(getFileType(originalFilename));
            newFile.setCreateTime(LocalDateTime.now());
            fileMapper.insert(newFile);
            fileId = newFile.getId();
        }

        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        userFile.setFileId(fileId);
        userFile.setFileName(file.getOriginalFilename());
        userFile.setParentId(parentId != null ? parentId : 0L);
        userFile.setIsDir(0);
        userFile.setIsDeleted(0);
        userFile.setCreateTime(LocalDateTime.now());
        userFileMapper.insert(userFile);

        log.info("文件上传成功: {}, userId: {}", file.getOriginalFilename(), userId);
        return Result.success("上传成功");
    }

    // ========== 分片上传 ==========

    /**
     * 初始化分片上传
     */
    public Result<Map<String, Object>> initChunkUpload(String fileName, Long fileSize, Integer totalChunks,
                                                        Long userId, Long parentId) {
        String uploadId = UUID.randomUUID().toString().replace("-", "");
        String tempDir = uploadPath + "/chunks/" + uploadId;

        UploadInfo info = new UploadInfo();
        info.setFileName(fileName);
        info.setFileSize(fileSize);
        info.setTotalChunks(totalChunks);
        info.setUserId(userId);
        info.setParentId(parentId != null ? parentId : 0L);
        info.setTempDir(tempDir);
        chunkUploadCache.put(uploadId, info);

        // 创建临时目录
        try {
            Files.createDirectories(Paths.get(tempDir));
        } catch (IOException e) {
            log.error("创建分片临时目录失败: {}", e.getMessage());
            return Result.error("初始化上传失败");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("uploadId", uploadId);
        log.info("初始化分片上传: uploadId={}, fileName={}, totalChunks={}", uploadId, fileName, totalChunks);
        return Result.success(result);
    }

    /**
     * 上传单个分片
     */
    public Result<String> uploadChunk(MultipartFile chunk, String uploadId, Integer chunkIndex, Long userId) throws IOException {
        UploadInfo info = chunkUploadCache.get(uploadId);
        if (info == null) {
            return Result.error("上传会话不存在或已过期，请重新初始化");
        }

        if (!info.getUserId().equals(userId)) {
            return Result.error("无权限");
        }

        // 保存分片
        Path chunkPath = Paths.get(info.getTempDir(), "chunk_" + chunkIndex);
        chunk.transferTo(chunkPath.toFile());

        info.getUploadedChunks().add(chunkIndex);
        log.debug("分片上传成功: uploadId={}, chunkIndex={}", uploadId, chunkIndex);
        return Result.success("分片 " + chunkIndex + " 上传成功");
    }

    /**
     * 合并分片
     */
    public Result<String> mergeChunks(String uploadId, Long userId) throws IOException {
        UploadInfo info = chunkUploadCache.get(uploadId);
        if (info == null) {
            return Result.error("上传会话不存在或已过期");
        }

        if (!info.getUserId().equals(userId)) {
            return Result.error("无权限");
        }

        // 检查是否所有分片都已上传
        if (info.getUploadedChunks().size() < info.getTotalChunks()) {
            return Result.error("分片未全部上传，已上传: " + info.getUploadedChunks().size() + "/" + info.getTotalChunks());
        }

        // 确保上传目录存在
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String saveFileName = UUID.randomUUID() + "_" + info.getFileName();
        Path mergedFile = uploadDir.resolve(saveFileName);

        // 按顺序合并分片
        try (FileOutputStream fos = new FileOutputStream(mergedFile.toFile());
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            for (int i = 0; i < info.getTotalChunks(); i++) {
                Path chunkPath = Paths.get(info.getTempDir(), "chunk_" + i);
                Files.copy(chunkPath, bos);
            }
            bos.flush();
        }

        // 计算 MD5
        String md5;
        try (InputStream is = new FileInputStream(mergedFile.toFile())) {
            md5 = org.springframework.util.DigestUtils.md5DigestAsHex(is);
        }

        // 检查是否已存在（秒传）
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getMd5, md5);
        File existFile = fileMapper.selectOne(wrapper);

        Long fileId;
        if (existFile != null) {
            // 已存在，删除合并的文件
            Files.deleteIfExists(mergedFile);
            fileId = existFile.getId();
        } else {
            File newFile = new File();
            newFile.setMd5(md5);
            newFile.setName(info.getFileName());
            newFile.setSize(info.getFileSize());
            newFile.setPath(mergedFile.toString());
            newFile.setType(getFileType(info.getFileName()));
            newFile.setCreateTime(LocalDateTime.now());
            fileMapper.insert(newFile);
            fileId = newFile.getId();
        }

        // 创建用户文件记录
        UserFile userFile = new UserFile();
        userFile.setUserId(info.getUserId());
        userFile.setFileId(fileId);
        userFile.setFileName(info.getFileName());
        userFile.setParentId(info.getParentId());
        userFile.setIsDir(0);
        userFile.setIsDeleted(0);
        userFile.setCreateTime(LocalDateTime.now());
        userFileMapper.insert(userFile);

        // 清理临时文件和缓存
        deleteTempDir(info.getTempDir());
        chunkUploadCache.remove(uploadId);

        log.info("分片合并成功: uploadId={}, fileName={}", uploadId, info.getFileName());
        return Result.success("上传成功");
    }

    /**
     * 查询上传进度
     */
    public Result<Map<String, Object>> getUploadProgress(String uploadId) {
        UploadInfo info = chunkUploadCache.get(uploadId);
        if (info == null) {
            return Result.error("上传会话不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("uploadId", uploadId);
        result.put("totalChunks", info.getTotalChunks());
        result.put("uploadedChunks", info.getUploadedChunks().size());
        result.put("progress", (double) info.getUploadedChunks().size() / info.getTotalChunks() * 100);
        return Result.success(result);
    }

    private void deleteTempDir(String tempDir) {
        try {
            Path dir = Paths.get(tempDir);
            if (Files.exists(dir)) {
                Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try { Files.deleteIfExists(path); } catch (Exception ignored) {}
                        });
            }
        } catch (Exception e) {
            log.warn("清理临时目录失败: {}", e.getMessage());
        }
    }

    // ========== 存储空间 ==========

    /**
     * 获取当前用户的存储空间使用情况
     */
    public Result<Map<String, Object>> getStorageUsage(Long userId) {
        // 统计用户所有未删除文件的大小总和
        LambdaQueryWrapper<UserFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFile::getUserId, userId)
               .eq(UserFile::getIsDeleted, 0)
               .eq(UserFile::getIsDir, 0);

        List<UserFile> userFiles = userFileService.list(wrapper);

        long usedSpace = 0L;
        for (UserFile uf : userFiles) {
            if (uf.getFileId() != null) {
                File file = fileMapper.selectById(uf.getFileId());
                if (file != null && file.getSize() != null) {
                    usedSpace += file.getSize();
                }
            }
        }

        // 默认总空间 10GB
        long totalSpace = 10L * 1024 * 1024 * 1024;

        Map<String, Object> result = new HashMap<>();
        result.put("usedSpace", usedSpace);
        result.put("totalSpace", totalSpace);

        return Result.success(result);
    }

    // ========== 管理员 - 文件管理 ==========

    /**
     * 管理员：全量文件列表
     */
    public Result<Map<String, Object>> getAdminFileList(Integer page, Integer size, String keyword, Long userId) {
        Page<UserFile> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 10);
        LambdaQueryWrapper<UserFile> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(UserFile::getFileName, keyword);
        }
        wrapper.orderByDesc(UserFile::getCreateTime);
        Page<UserFile> result = userFileService.page(pageParam, wrapper);

        List<UserFileVo> voList = new ArrayList<>();
        for (UserFile uf : result.getRecords()) {
            UserFileVo vo = new UserFileVo();
            vo.setId(uf.getId());
            vo.setFileId(uf.getFileId());
            vo.setUserId(uf.getUserId());
            vo.setFileName(uf.getFileName());
            vo.setParentId(uf.getParentId());
            vo.setIsDir(uf.getIsDir());
            vo.setCreateTime(uf.getCreateTime());
            vo.setUpdateTime(uf.getUpdateTime());

            // 查用户名
            if (uf.getUserId() != null) {
                User user = userMapper.selectById(uf.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername());
                }
            }

            if (uf.getIsDir() == 0 && uf.getFileId() != null) {
                File file = fileMapper.selectById(uf.getFileId());
                if (file != null) {
                    vo.setSize(file.getSize());
                    vo.setFileType(file.getType());
                }
            }
            voList.add(vo);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("list", voList);
        return Result.success(map);
    }

    /**
     * 管理员：文件统计
     */
    public Result<Map<String, Object>> getAdminFileStats(Long userId) {
        long totalFiles = userFileService.count(new LambdaQueryWrapper<UserFile>().eq(UserFile::getIsDir, 0));
        long totalFolders = userFileService.count(new LambdaQueryWrapper<UserFile>().eq(UserFile::getIsDir, 1));
        long totalShares = shareServiceCount();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFiles", totalFiles);
        stats.put("totalFolders", totalFolders);
        stats.put("totalShares", totalShares);
        return Result.success(stats);
    }

    private long shareServiceCount() {
        try {
            return shareMapper.selectCount(null);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 管理员：强制删除文件
     */
    public Result<String> adminDeleteFile(Long userFileId, Long userId) {
        UserFile userFile = userFileService.getById(userFileId);
        if (userFile == null) {
            return Result.error("文件不存在");
        }

        if (userFile.getIsDir() == 0 && userFile.getFileId() != null) {
            File file = fileMapper.selectById(userFile.getFileId());
            if (file != null) {
                try {
                    Files.deleteIfExists(Paths.get(file.getPath()));
                } catch (Exception e) {
                    log.error("删除物理文件失败: {}", e.getMessage());
                }
                fileMapper.deleteById(file.getId());
            }
        }
        userFileService.removeById(userFileId);
        log.info("管理员强制删除文件: userFileId={}, adminId={}", userFileId, userId);
        return Result.success("已删除");
    }

    // ========== 工具方法 ==========

    private String getFileType(String fileName) {
        if (fileName == null) return "other";
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == -1) return "other";
        return fileName.substring(lastDot + 1).toLowerCase();
    }

    // ========== 文件预览/下载 ==========

    public Result<FilePreviewVo> previewFile(Long userFileId, Long userId) {
        UserFile userFile = userFileService.getById(userFileId);
        if (userFile == null || !userFile.getUserId().equals(userId)) {
            return Result.error("文件不存在或无权限");
        }

        if (userFile.getIsDir() == 1) {
            return Result.error("文件夹无法预览");
        }

        File file = fileMapper.selectById(userFile.getFileId());
        if (file == null) {
            return Result.error("物理文件不存在");
        }

        FilePreviewVo vo = new FilePreviewVo();
        vo.setFileName(userFile.getFileName());
        vo.setFileSize(file.getSize());
        vo.setFileType(file.getType());

        List<String> previewTypes = Arrays.asList("jpg", "jpeg", "png", "gif", "pdf", "txt", "md", "mp4", "mp3", "html", "webp", "bmp");
        if (previewTypes.contains(file.getType().toLowerCase())) {
            vo.setSupportPreview(true);
            vo.setPreviewUrl("/file/download/" + userFileId);
        } else {
            vo.setSupportPreview(false);
        }

        return Result.success(vo);
    }

    public void downloadFile(Long userFileId, Long userId, HttpServletResponse response) throws IOException {
        UserFile userFile = userFileService.getById(userFileId);
        if (userFile == null || !userFile.getUserId().equals(userId)) {
            response.setStatus(403);
            response.getWriter().write("无权限访问");
            return;
        }

        if (userFile.getIsDir() == 1) {
            response.setStatus(400);
            response.getWriter().write("无法下载文件夹");
            return;
        }

        File file = fileMapper.selectById(userFile.getFileId());
        if (file == null) {
            response.setStatus(404);
            response.getWriter().write("文件不存在");
            return;
        }

        Path filePath = Paths.get(file.getPath());
        if (!Files.exists(filePath)) {
            response.setStatus(404);
            response.getWriter().write("物理文件不存在");
            return;
        }

        response.setContentType(getContentType(file.getType()));
        response.setHeader("Content-Disposition", "inline; filename=" + URLEncoder.encode(userFile.getFileName(), "UTF-8"));
        Files.copy(filePath, response.getOutputStream());
    }

    private String getContentType(String fileType) {
        if (fileType == null) return "application/octet-stream";
        switch (fileType.toLowerCase()) {
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "bmp": return "image/bmp";
            case "webp": return "image/webp";
            case "pdf": return "application/pdf";
            case "txt": return "text/plain";
            case "md": return "text/markdown";
            case "html": case "htm": return "text/html";
            case "mp4": return "video/mp4";
            case "mp3": return "audio/mpeg";
            case "json": return "application/json";
            default: return "application/octet-stream";
        }
    }
}
