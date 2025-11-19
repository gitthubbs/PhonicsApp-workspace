package com.ray.phonicappserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ray.phonicappserver.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}