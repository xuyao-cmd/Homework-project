package com.disk.file.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserFileVo {
    private Long id;
    private Long fileId;
    private Long userId;        // 所属用户ID（管理员查看时使用）
    private String username;    // 所属用户名（管理员查看时使用）
    private String fileName;
    private Long parentId;
    private Integer isDir;      // 0=文件 1=文件夹
    private Long size;          // 文件大小（文件夹为0）
    private String fileType;    // 文件类型
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
