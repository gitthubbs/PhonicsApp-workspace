package com.ray.phonicappserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ray.phonicappserver.common.Result;
import com.ray.phonicappserver.common.ResultCode;
import com.ray.phonicappserver.dto.request.LoginRequest;
import com.ray.phonicappserver.dto.request.RegisterRequest;
import com.ray.phonicappserver.dto.response.UserResponse;
import com.ray.phonicappserver.mapper.UserMapper;
import com.ray.phonicappserver.entity.User;
import com.ray.phonicappserver.security.JwtUtil;
import com.ray.phonicappserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;


    // 登录验证
    public Result<UserResponse> login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND); // 用户不存在
        }
        if (!user.getPassword().equals(request.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR); // 密码错误
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setAvatarUrl(user.getAvatarUrl());
        // 生成 JWT 令牌
        String token = jwtUtil.generateToken(user.getUsername());
        response.setToken(token);
        return Result.success(response);
    }

    // 获取用户信息
    public Result<UserResponse> getUserInfo(Long userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, userId));
        if (user != null) {
            UserResponse response = new UserResponse();
            response.setUsername(user.getUsername());
            response.setAvatarUrl(user.getAvatarUrl());
            return Result.success(response);
        }
        return Result.error(ResultCode.USER_NOT_FOUND);
    }

    @Override
    public Result<Boolean> register(RegisterRequest request) {
        // 检查用户名是否存在
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (existing != null) {
            return Result.error(ResultCode.USER_EXIST); // 用户名已存在
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setAvatarUrl(request.getAvatarUrl());

        int inserted = userMapper.insert(user);
        return Result.success(inserted > 0);
    }



}
