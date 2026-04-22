package com.chris.robot_server.model;

import java.util.Date;

import lombok.Data;

@Data
public class TelegramUser {
    private Long id;

    private Date createTime;

    private Date updateTime;

    private Long userId;

    private String userName;

    private String userGroup;

    private Long userGroupId;

    private String token;

    private String displayName;

}