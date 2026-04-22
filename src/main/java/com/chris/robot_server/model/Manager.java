package com.chris.robot_server.model;

import java.util.Date;

import lombok.Data;

@Data
public class Manager {
    private Long id;

    private Date createTime;

    private Date updateTime;

    private String username;

    private String password;

    private Byte status;

    private Integer level;

}