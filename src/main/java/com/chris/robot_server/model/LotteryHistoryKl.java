package com.chris.robot_server.model;

import java.util.Date;
import lombok.Data;
/**
 * 快乐8
 */
@Data
public class LotteryHistoryKl {
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