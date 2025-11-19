package com.ray.phonicappserver.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String avatarUrl;
}
