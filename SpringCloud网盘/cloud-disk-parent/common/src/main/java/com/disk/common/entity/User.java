package com.disk.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String email;

    private String avatar;

    /** 第三方登录平台: github */
    private String oauthPlatform;

    /** 第三方登录唯一标识 */
    private String oauthOpenId;

    /** 用户角色: user-普通用户, admin-管理员 */
    private String role;

    /** 用户状态: 1-正常, 0-禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}