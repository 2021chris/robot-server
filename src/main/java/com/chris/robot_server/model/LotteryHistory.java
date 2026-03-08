package com.chris.robot_server.model;

import java.util.Date;

import lombok.Data;
/**
 * 新澳门
 */
@Data
public class LotteryHistory {
    private Long id;

    private Date createTime;

    private Date updateTime;

    private String type;

    private String expect; // 期号

    private String openCode; // 开奖号码

    private String zodiac; // 生肖

    private String wave; // 红绿波数

    private String openTime; // 开奖时间

    private String fileId; // telegram返回的图id
}