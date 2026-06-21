package com.disk.file.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShareVo {
    private Long id;
    private String shareCode;
    private String fileName;
    private Integer isDir;
    private Long fileSize;
    private String fileType;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}