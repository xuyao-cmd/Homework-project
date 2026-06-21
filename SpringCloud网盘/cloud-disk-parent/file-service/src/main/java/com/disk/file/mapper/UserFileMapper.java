package com.disk.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.disk.file.entity.UserFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserFileMapper extends BaseMapper<UserFile> {
}