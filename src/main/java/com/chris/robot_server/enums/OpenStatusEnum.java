package com.chris.robot_server.enums;

public enum OpenStatusEnum {

    Xin_Aomen((byte)1, "Xin_Aomen"),
    Hongkong((byte)2, "Hongkong"),
    KL8((byte)3, "KL8"),
    LaoAo((byte)4, "LaoAo");

    private Byte code;

    private String msg;

    OpenStatusEnum(Byte code, String msg) {
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
