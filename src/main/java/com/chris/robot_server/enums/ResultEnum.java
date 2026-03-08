package com.chris.robot_server.enums;

public enum ResultEnum {
    // token失效
    TOKEN_ERROR(-2, "Token expiration"),
    // 未知错误
    UNKONW_ERROR(-1, "Unknown error"),
    // 成功
    SUCESS(0, "Success"),
    // 登录失败, 登录信息不正确
    LOGIN_FAIL(1, "Login failed, incorrect login information"),
    // 退出登录成功
    LOGOUT_SUCCESS(2, "Logout successful"),
    // 数据插入错误
    DATA_UPDATE_FAIL(3, "Data insertion error"),
    // 参数错误
    PARAM_ERROR(4, "Parameter error"),
    ;

    private Integer code;

    private String msg;

    ResultEnum(Integer code, String msg) {
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
