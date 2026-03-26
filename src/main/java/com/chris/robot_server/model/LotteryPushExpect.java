package com.chris.robot_server.model;

import java.util.Date;

import lombok.Data;

@Data
public class LotteryPushExpect {
    private Long id;

    private Date createTime;

    private Date updateTime;

    private String type;

    private String expect;

   
}