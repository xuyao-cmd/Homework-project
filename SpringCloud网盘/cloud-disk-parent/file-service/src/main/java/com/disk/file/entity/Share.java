package com.disk.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("share")
public class Share {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long fileId;
    private String shareCode;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}