package com.chris.robot_server.enums;

public enum KeywordTypeEnum {
    WordLink((byte) 1, "WordLink"),// 文本连接

    Html((byte)2, "Html"),// HTML内容

    ;

    private Byte code;

    private String msg;

    KeywordTypeEnum(Byte code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Byte getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
