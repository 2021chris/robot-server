package com.chris.robot_server.model;

import java.util.Date;

import lombok.Data;

@Data
public class Advertisement {
    private Long id;

    private Date createTime;

    private Date updateTime;

    private String seat;

    private Byte type;

    private String content;
}