package com.chris.robot_server.model;

import java.util.Date;

import lombok.Data;
/**
 * 相关链接
 */
@Data
public class TelegramLink {
    private Long id;

    private Date createTime;

    private Date updateTime;

    private String type;

    private String title;

    private String address;

}