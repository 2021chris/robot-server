package com.chris.robot_server.model;

import java.util.Date;

import lombok.Data;

@Data
public class BotKeyword {
    private Long id;

    private Date createTime;

    private Date updateTime;

    private String keyWord;

    private String keyValue;

    private Byte enable;

    private Byte type;

}