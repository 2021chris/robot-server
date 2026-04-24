package com.chris.robot_server.model;

import java.util.Date;

import lombok.Data;

@Data
public class TelegramBots {
    private Long id;

    private Date createTime;

    private Date updateTime;

    private String token;

    private String botname;

    private String description;

    private Byte status;

    private Long botId;

    private Integer groupNumber;

}