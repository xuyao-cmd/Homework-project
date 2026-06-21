package com.disk.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.disk.common.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}