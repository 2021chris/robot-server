package com.chris.robot_server.util;

import java.util.Random;

/**
 * RandomUtil
 *
 * @author chris
 * 11/6/24
 */
public class RandomUtil {

    /**
     * 获得一个大于 x + 100 且小于 x + 200的整数
     * @param x
     * @return
     */
    public static String getRandomNumberInRange(int x) {
        // 定义随机数的下界和上界
        int lowerBound = x + 101; // 大于x + 100的最小整数
        int upperBound = x + 199; // 小于x + 200的最大整数

        // 创建一个Random对象
        Random random = new Random();

        // 获取一个范围在lowerBound（包含）和upperBound（不包含）之间的随机整数
        // 由于nextInt(int bound)返回的是从0（包含）到bound（不包含）之间的随机整数，
        // 因此我们需要加上lowerBound来得到正确的范围。
        Integer randomNumber = random.nextInt(upperBound - lowerBound + 1) + lowerBound;

        // 返回随机整数
        return randomNumber.toString();
    }


    /**
     * 获得一个大于 x - 150 且小于 x + 200的整数
     * @param x
     * @return
     */
    public static int getRandomOnlineNumber(int x) {
        // 定义随机数的下界和上界
        int lowerBound = x - 150; // 大于x + 100的最小整数
        int upperBound = x + 200; // 小于x + 200的最大整数

        // 创建一个Random对象
        Random random = new Random();

        // 获取一个范围在lowerBound（包含）和upperBound（不包含）之间的随机整数
        // 由于nextInt(int bound)返回的是从0（包含）到bound（不包含）之间的随机整数，
        // 因此我们需要加上lowerBound来得到正确的范围。
        return random.nextInt(upperBound - lowerBound + 1) + lowerBound;
    }
}
