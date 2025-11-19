package com.ray.phonicappserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ray.phonicappserver.dto.request.LoginRequest;
import com.ray.phonicappserver.dto.request.RegisterRequest;
import com.ray.phonicappserver.dto.response.UserResponse;
import com.ray.phonicappserver.mapper.UserMapper;
import com.ray.phonicappserver.entity.User;
import com.ray.phonicappserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;


    // 登录验证
    public UserResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user != null && user.getPassword().equals(request.getPassword())) { // ★ 实际应使用哈希
            UserResponse response = new UserResponse();
            response.setUsername(user.getUsername());
            response.setAvatarUrl(user.getAvatarUrl());
            return response;
        }
        return null;
    }

    // 获取用户信息
    public UserResponse getUserInfo(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user != null) {
            UserResponse response = new UserResponse();
            response.setUsername(user.getUsername());
            response.setAvatarUrl(user.getAvatarUrl());
            return response;
        }
        return null;
    }

    @Override
    public boolean register(RegisterRequest request) {
        // 检查用户名是否存在
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (existing != null) {
            return false; // 用户名已存在
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setAvatarUrl(request.getAvatarUrl());

        int inserted = userMapper.insert(user);
        return inserted > 0;
    }

}
