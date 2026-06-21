package com.disk.file.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disk.common.Result;
import com.disk.file.entity.File;
import com.disk.file.entity.Share;
import com.disk.file.entity.UserFile;
import com.disk.file.mapper.FileMapper;
import com.disk.file.mapper.ShareMapper;
import com.disk.file.vo.ShareVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareService extends ServiceImpl<ShareMapper, Share> {

    private final ShareMapper shareMapper;
    private final UserFileService userFileService;
    private final FileMapper fileMapper;

    /**
     * 创建分享
     */
    public Result<String> createShare(Long fileId, Long userId, Integer expireHours) {
        // 检查文件是否存在且属于该用户
        UserFile userFile = userFileService.getById(fileId);
        if (userFile == null || !userFile.getUserId().equals(userId)) {
            return Result.error("文件不存在或无权限");
        }

        // 生成分享码
        String shareCode = RandomUtil.randomString(6).toUpperCase();

        // 计算过期时间
        LocalDateTime expireTime = null;
        if (expireHours != null && expireHours > 0) {
            expireTime = LocalDateTime.now().plusHours(expireHours);
        }

        Share share = new Share();
        share.setUserId(userId);
        share.setFileId(fileId);
        share.setShareCode(shareCode);
        share.setExpireTime(expireTime);
        share.setCreateTime(LocalDateTime.now());

        this.save(share);
        log.info("创建分享成功: shareCode={}, fileId={}", shareCode, fileId);

        return Result.success(shareCode);
    }

    /**
     * 获取分享信息
     */
    public Result<ShareVo> getShareInfo(String shareCode) {
        LambdaQueryWrapper<Share> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Share::getShareCode, shareCode);
        Share share = this.getOne(wrapper);

        if (share == null) {
            return Result.error("分享链接无效");
        }

        // 检查是否过期
        if (share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now())) {
            return Result.error("分享链接已过期");
        }

        // 获取文件信息
        UserFile userFile = userFileService.getById(share.getFileId());
        if (userFile == null) {
            return Result.error("文件不存在");
        }

        ShareVo vo = new ShareVo();
        vo.setShareCode(share.getShareCode());
        vo.setFileName(userFile.getFileName());
        vo.setIsDir(userFile.getIsDir());

        if (userFile.getIsDir() == 0 && userFile.getFileId() != null) {
            File file = fileMapper.selectById(userFile.getFileId());
            if (file != null) {
                vo.setFileSize(file.getSize());
                vo.setFileType(file.getType());
            }
        }

        vo.setExpireTime(share.getExpireTime());
        vo.setCreateTime(share.getCreateTime());

        return Result.success(vo);
    }

    /**
     * 取消分享
     */
    public Result<String> cancelShare(Long shareId, Long userId) {
        Share share = this.getById(shareId);
        if (share == null || !share.getUserId().equals(userId)) {
            return Result.error("分享不存在或无权限");
        }

        this.removeById(shareId);
        log.info("取消分享: shareId={}", shareId);
        return Result.success("已取消分享");
    }

    /**
     * 我的分享列表
     */
    public Result<List<ShareVo>> getMyShares(Long userId) {
        LambdaQueryWrapper<Share> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Share::getUserId, userId)
                .orderByDesc(Share::getCreateTime);

        List<Share> shares = this.list(wrapper);
        List<ShareVo> voList = new ArrayList<>();

        for (Share share : shares) {
            UserFile userFile = userFileService.getById(share.getFileId());
            if (userFile == null) continue;

            ShareVo vo = new ShareVo();
            vo.setId(share.getId());
            vo.setShareCode(share.getShareCode());
            vo.setFileName(userFile.getFileName());
            vo.setIsDir(userFile.getIsDir());
            vo.setExpireTime(share.getExpireTime());
            vo.setCreateTime(share.getCreateTime());
            voList.add(vo);
        }

        return Result.success(voList);
    }
}