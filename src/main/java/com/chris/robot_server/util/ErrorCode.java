package com.chris.robot_server.util;

/**
 * ErrorCode
 *
 * @author chris
 * 11/2/24
 */
public class ErrorCode {
    // public static final Integer Email_Exist = 10001;// 邮箱已存在
    // public static final Integer Email_NOT_Exist = 10002;

    // public static final Integer No_Lover = 10003;// 用户不存在

    // public static final Integer Money_Not_Enough = 20001;// 账户余额不足
    // public static final Integer Word_Not_Enough = 20002;// 用户字数不够，需要转换

    public static final Integer No_Manager = 1001;// 操作员不存在
    public static final Integer Error_Password = 1002; // 密码不对
    public static final Integer Token_Invalid = 1003; // Token 无效
    public static final Integer Permission_Denied = 1004; // 权限 denied

}
