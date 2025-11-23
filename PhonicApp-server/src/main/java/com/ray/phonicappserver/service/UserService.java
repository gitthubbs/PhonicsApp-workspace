package com.ray.phonicappserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ray.phonicappserver.dto.request.LoginRequest;
import com.ray.phonicappserver.dto.request.RegisterRequest;
import com.ray.phonicappserver.dto.response.UserResponse;
import com.ray.phonicappserver.common.Result;
import com.ray.phonicappserver.entity.User;

public interface UserService extends IService<User> {
    Result<UserResponse> login(LoginRequest request);

    Result<UserResponse> getUserInfo(Long userId);

    Result<Boolean> register(RegisterRequest request);
}
