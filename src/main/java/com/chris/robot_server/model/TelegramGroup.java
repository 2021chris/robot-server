package com.chris.robot_server.model;

import java.util.Date;

import lombok.Data;

@Data
public class TelegramGroup {
    private Long id;

    private Long groupId;

    private String title;

    private String type;// 官方定义的类型，可能是private、group、supergroup或channel

    private Date createTime;

    private Date updateTime;

    private Byte status;// 区分什么彩的群1新澳，2香港，3快乐

    private Integer lastMessageId;

    private Long botId;// 关联的机器人ID，方便后续查询和管理

    private String token;// 机器人token，方便后续查询和管理

    public TelegramGroup(Long groupId, String title, String type, Long botId, String token) {
        this.groupId = groupId;
        this.title = title;
        this.type = type;
        this.botId = botId;
        this.token = token;
    }

    public TelegramGroup() {
    }

}