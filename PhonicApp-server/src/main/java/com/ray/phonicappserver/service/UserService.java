package com.ray.phonicappserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ray.phonicappserver.dto.request.LoginRequest;
import com.ray.phonicappserver.dto.request.RegisterRequest;
import com.ray.phonicappserver.dto.response.UserResponse;
import com.ray.phonicappserver.entity.User;

public interface UserService extends IService<User> {
    UserResponse login(LoginRequest request);

    UserResponse getUserInfo(String username);

    boolean register(RegisterRequest request);
}
