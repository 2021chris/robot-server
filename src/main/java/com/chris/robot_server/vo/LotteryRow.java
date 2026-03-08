package com.chris.robot_server.vo;

import lombok.Data;

@Data
public class LotteryRow {

    private String period; // 期数，如 "2026037"
    private String openCode;
    private int[] numbers; // 正码数组，如 [34, 42, 39, 13, 44, 45]
    private int specialNumber; // 特码，如 43

    public LotteryRow(String period, String openCode) {
        this.openCode = openCode;
        this.period = period;
        String[] codes = openCode.split(",");
        this.numbers = new int[codes.length - 1];
        for (int i = 0; i < codes.length - 1; i++) {
            this.numbers[i] = Integer.parseInt(codes[i]);
        }
        this.specialNumber = Integer.parseInt(codes[codes.length - 1]);
    }
}
