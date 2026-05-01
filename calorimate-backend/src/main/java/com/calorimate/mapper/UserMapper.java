package com.calorimate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.calorimate.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
