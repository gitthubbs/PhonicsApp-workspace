package com.ray.phonicappserver.controller;

import com.ray.phonicappserver.common.Result;
import com.ray.phonicappserver.dto.request.LoginRequest;
import com.ray.phonicappserver.dto.request.RegisterRequest;
import com.ray.phonicappserver.dto.response.UserResponse;
import com.ray.phonicappserver.security.JwtUtil;
import com.ray.phonicappserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<UserResponse> login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/{userId}")
    public Result<UserResponse> getUserInfo(@PathVariable Long userId) {
        return userService.getUserInfo(userId);
    }

    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @GetMapping("/validate")
    public boolean validateToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            String username = jwtUtil.extractUsername(jwt);
            return jwtUtil.validateToken(jwt, username);
        }
        return false;
    }
}
