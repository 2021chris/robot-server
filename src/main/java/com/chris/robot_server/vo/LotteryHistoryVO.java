package com.chris.robot_server.vo;

import java.util.List;

import lombok.Data;

/**
 * telegram开奖数据模型
 */
@Data
public class LotteryHistoryVO {
    /** 期号 */
    private String expect;

    /** 开奖号码（已拆分） */
    private List<Integer> numbers;

    /** 开奖时间 */
    private String openTime;

    /** 彩种标识：1新澳 2香港 3快乐8 */
    private Byte lotteryType;
}
