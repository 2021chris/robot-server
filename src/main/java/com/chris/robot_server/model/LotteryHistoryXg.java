package com.chris.robot_server.model;

import java.util.Date;

import lombok.Data;
/**
 * 香港
 */
@Data
public class LotteryHistoryXg {
    private Long id;

    private Date createTime;

    private Date updateTime;

    private String type;

    private String expect;

    private String openCode;

    private String zodiac;

    private String wave;

    private String openTime;

    private String fileId; // telegram返回的图id
}