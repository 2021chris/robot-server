package com.chris.robot_server.model;

import java.util.Date;

import lombok.Data;

@Data
public class TelegramGroup {
    private Long id;

    private Long groupId;

    private String title;

    private String type;

    private Date createTime;

    private Date updateTime;

    private Byte status;

    private Integer lastMessageId;

    public TelegramGroup(Long groupId, String title, String type) {
        this.groupId = groupId;
        this.title = title;
        this.type = type;
    }

    public TelegramGroup() {
    }

}