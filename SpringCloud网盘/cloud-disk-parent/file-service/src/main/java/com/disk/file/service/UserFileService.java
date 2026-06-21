package com.disk.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disk.common.Result;
import com.disk.file.entity.File;
import com.disk.file.entity.UserFile;
import com.disk.file.mapper.FileMapper;
import com.disk.file.mapper.UserFileMapper;
import com.disk.file.vo.UserFileVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFileService extends ServiceImpl<UserFileMapper, UserFile> {

    private final UserFileMapper userFileMapper;
    private final FileMapper fileMapper;

    /**
     * 新建目录
     */
    public Result<String> createFolder(String folderName, Long parentId, Long userId) {
        LambdaQueryWrapper<UserFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFile::getUserId, userId)
                .eq(UserFile::getParentId, parentId)
                .eq(UserFile::getFileName, folderName)
                .eq(UserFile::getIsDir, 1)
                .eq(UserFile::getIsDeleted, 0);

        if (this.count(wrapper) > 0) {
            return Result.error("同名文件夹已存在");
        }

        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        userFile.setFileName(folderName);
        userFile.setParentId(parentId);
        userFile.setIsDir(1);
        userFile.setIsDeleted(0);
        userFile.setCreateTime(LocalDateTime.now());
        userFile.setUpdateTime(LocalDateTime.now());

        this.save(userFile);
        log.info("创建文件夹成功: {}, userId: {}", folderName, userId);
        return Result.success("创建成功");
    }

    /**
     * 获取文件列表
     */
    public Result<List<UserFileVo>> getFileList(Long parentId, Long userId) {
        LambdaQueryWrapper<UserFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFile::getUserId, userId)
                .eq(UserFile::getParentId, parentId)
                .eq(UserFile::getIsDeleted, 0)
                .orderByDesc(UserFile::getIsDir)
                .orderByDesc(UserFile::getCreateTime);

        List<UserFile> userFiles = this.list(wrapper);
        List<UserFileVo> voList = new ArrayList<>();

        for (UserFile userFile : userFiles) {
            UserFileVo vo = new UserFileVo();
            vo.setId(userFile.getId());
            vo.setFileId(userFile.getFileId());
            vo.setFileName(userFile.getFileName());
            vo.setParentId(userFile.getParentId());
            vo.setIsDir(userFile.getIsDir());
            vo.setCreateTime(userFile.getCreateTime());
            vo.setUpdateTime(userFile.getUpdateTime());

            if (userFile.getIsDir() == 0 && userFile.getFileId() != null) {
                File file = fileMapper.selectById(userFile.getFileId());
                if (file != null) {
                    vo.setSize(file.getSize());
                    vo.setFileType(file.getType());
                }
            }
            voList.add(vo);
        }
        return Result.success(voList);
    }

    /**
     * 重命名文件/文件夹
     */
    public Result<String> rename(Long id, String newName, Long userId) {
        UserFile userFile = this.getById(id);
        if (userFile == null) {
            return Result.error("文件不存在");
        }
        if (!userFile.getUserId().equals(userId)) {
            return Result.error("无权限操作");
        }

        LambdaQueryWrapper<UserFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFile::getUserId, userId)
                .eq(UserFile::getParentId, userFile.getParentId())
                .eq(UserFile::getFileName, newName)
                .eq(UserFile::getIsDeleted, 0);

        if (this.count(wrapper) > 0) {
            return Result.error("同名文件已存在");
        }

        userFile.setFileName(newName);
        userFile.setUpdateTime(LocalDateTime.now());
        this.updateById(userFile);
        log.info("重命名成功: id={}, newName={}", id, newName);
        return Result.success("重命名成功");
    }

    /**
     * 移动到回收站（软删除）
     */
    public Result<String> moveToRecycle(List<Long> ids, Long userId) {
        for (Long id : ids) {
            UserFile userFile = this.getById(id);
            if (userFile == null || !userFile.getUserId().equals(userId)) {
                continue;
            }
            userFile.setIsDeleted(1);
            userFile.setDeletedTime(LocalDateTime.now());
            this.updateById(userFile);
        }
        log.info("移动到回收站: ids={}, userId={}", ids, userId);
        return Result.success("已移动到回收站");
    }

    // ========== 回收站相关方法 ==========

    /**
     * 获取回收站列表
     */
    public Result<List<UserFileVo>> getRecycleList(Long userId) {
        LambdaQueryWrapper<UserFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDeleted, 1)
                .orderByDesc(UserFile::getDeletedTime);

        List<UserFile> userFiles = this.list(wrapper);
        List<UserFileVo> voList = new ArrayList<>();

        for (UserFile userFile : userFiles) {
            UserFileVo vo = new UserFileVo();
            vo.setId(userFile.getId());
            vo.setFileId(userFile.getFileId());
            vo.setFileName(userFile.getFileName());
            vo.setParentId(userFile.getParentId());
            vo.setIsDir(userFile.getIsDir());
            vo.setCreateTime(userFile.getCreateTime());
            vo.setUpdateTime(userFile.getUpdateTime());

            if (userFile.getIsDir() == 0 && userFile.getFileId() != null) {
                File file = fileMapper.selectById(userFile.getFileId());
                if (file != null) {
                    vo.setSize(file.getSize());
                    vo.setFileType(file.getType());
                }
            }
            voList.add(vo);
        }
        return Result.success(voList);
    }

    /**
     * 还原文件（从回收站恢复）
     */
    public Result<String> restoreFromRecycle(List<Long> ids, Long userId) {
        for (Long id : ids) {
            UserFile userFile = this.getById(id);
            if (userFile == null || !userFile.getUserId().equals(userId)) {
                continue;
            }
            userFile.setIsDeleted(0);
            userFile.setDeletedTime(null);
            this.updateById(userFile);
        }
        log.info("还原文件: ids={}, userId={}", ids, userId);
        return Result.success("还原成功");
    }

    /**
     * 彻底删除（物理删除）
     */
    public Result<String> permanentDelete(List<Long> ids, Long userId) {
        for (Long id : ids) {
            UserFile userFile = this.getById(id);
            if (userFile == null || !userFile.getUserId().equals(userId)) {
                continue;
            }

            if (userFile.getIsDir() == 0 && userFile.getFileId() != null) {
                LambdaQueryWrapper<UserFile> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(UserFile::getFileId, userFile.getFileId())
                        .eq(UserFile::getIsDeleted, 0);
                long count = this.count(wrapper);

                if (count <= 1) {
                    File file = fileMapper.selectById(userFile.getFileId());
                    if (file != null) {
                        try {
                            java.nio.file.Path filePath = java.nio.file.Paths.get(file.getPath());
                            java.nio.file.Files.deleteIfExists(filePath);
                        } catch (Exception e) {
                            log.error("删除物理文件失败: {}", e.getMessage());
                        }
                        fileMapper.deleteById(file.getId());
                    }
                }
            }
            this.removeById(id);
        }
        log.info("彻底删除: ids={}, userId={}", ids, userId);
        return Result.success("删除成功");
    }

    /**
     * 清空回收站
     */
    public Result<String> clearRecycle(Long userId) {
        LambdaQueryWrapper<UserFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFile::getUserId, userId).eq(UserFile::getIsDeleted, 1);

        List<UserFile> recycleFiles = this.list(wrapper);
        if (recycleFiles.isEmpty()) {
            return Result.success("回收站已空");
        }

        List<Long> ids = recycleFiles.stream().map(UserFile::getId).collect(Collectors.toList());
        return permanentDelete(ids, userId);
    }

    // ========== 文件移动相关方法 ==========

    /**
     * 移动文件/文件夹到目标目录
     */
    public Result<String> moveFiles(List<Long> ids, Long targetParentId, Long userId) {
        if (targetParentId != null && targetParentId != 0) {
            UserFile targetFolder = this.getById(targetParentId);
            if (targetFolder == null || targetFolder.getIsDir() != 1) {
                return Result.error("目标目录不存在或不是文件夹");
            }
            if (!targetFolder.getUserId().equals(userId)) {
                return Result.error("无权限操作目标目录");
            }
        }

        for (Long id : ids) {
            UserFile userFile = this.getById(id);
            if (userFile == null || !userFile.getUserId().equals(userId)) {
                continue;
            }

            LambdaQueryWrapper<UserFile> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserFile::getUserId, userId)
                    .eq(UserFile::getParentId, targetParentId != null ? targetParentId : 0L)
                    .eq(UserFile::getFileName, userFile.getFileName())
                    .eq(UserFile::getIsDeleted, 0);

            if (this.count(wrapper) > 0) {
                return Result.error("目标目录已存在同名文件：" + userFile.getFileName());
            }

            userFile.setParentId(targetParentId != null ? targetParentId : 0L);
            userFile.setUpdateTime(LocalDateTime.now());
            this.updateById(userFile);
        }

        log.info("移动文件成功: ids={}, targetParentId={}, userId={}", ids, targetParentId, userId);
        return Result.success("移动成功");
    }
}