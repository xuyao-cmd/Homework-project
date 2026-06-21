package com.disk.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file")
public class File {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String md5;
    private String name;
    private Long size;
    private String path;
    private String type;
    private LocalDateTime createTime;
}