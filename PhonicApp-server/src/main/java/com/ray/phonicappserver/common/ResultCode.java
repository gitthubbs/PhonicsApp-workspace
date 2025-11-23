package com.ray.phonicappserver.common;

public enum ResultCode {
    SUCCESS(200, "Success"),
    USER_NOT_FOUND(1001, "User not found"),
    PASSWORD_ERROR(1002, "Password error"),
    USER_EXIST(1003, "User already exists"),
    ERROR(500, "Unknown Error");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
