package com.disk.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_file")
public class UserFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long fileId;
    private Long parentId;
    private String fileName;
    private Integer isDeleted;
    private LocalDateTime deletedTime;
    private Integer isDir;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}