package com.chris.robot_server.model;

import java.util.Date;

import lombok.Data;

/**
 * 老澳门
 */
@Data
public class LotteryHistoryLao {
    private Long id;

    private Date createTime;

    private Date updateTime;

    private String type;

    private String expect;

    private String openCode;

    private String zodiac;

    private String wave;

    private String openTime;

    private String fileId;
   
}