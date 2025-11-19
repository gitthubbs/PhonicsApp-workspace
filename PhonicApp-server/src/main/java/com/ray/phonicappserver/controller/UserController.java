package com.ray.phonicappserver.controller;

import com.ray.phonicappserver.dto.request.LoginRequest;
import com.ray.phonicappserver.dto.request.RegisterRequest;
import com.ray.phonicappserver.dto.response.UserResponse;
import com.ray.phonicappserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/{username}")
    public UserResponse getUserInfo(@PathVariable String username) {
        UserResponse response = userService.getUserInfo(username);
        if (response != null) {
            return response;
        }
        throw new RuntimeException("用户不存在");
    }

    @PostMapping("/register")
    public boolean register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }
}
