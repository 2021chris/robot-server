package com.chris.robot_server.util;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class LotteryUtil {

    public static String getZodiacForNumber(int num) {
        // 基于2026年标准六合彩生肖映射（如果图片不匹配，可自定义调整数组）
        num = (num % 49 == 0) ? 49 : num % 49;

        if (contains(new int[] { 7, 19, 31, 43 }, num))
            return "鼠";
        if (contains(new int[] { 6, 18, 30, 42 }, num))
            return "牛";
        if (contains(new int[] { 5, 17, 29, 41 }, num))
            return "虎";
        if (contains(new int[] { 4, 16, 28, 40 }, num))
            return "兔";
        if (contains(new int[] { 3, 15, 27, 39 }, num))
            return "龙";
        if (contains(new int[] { 2, 14, 26, 38 }, num))
            return "蛇";
        if (contains(new int[] { 1, 13, 25, 37, 49 }, num))
            return "马";
        if (contains(new int[] { 12, 24, 36, 48 }, num))
            return "羊";
        if (contains(new int[] { 11, 23, 35, 47 }, num))
            return "猴";
        if (contains(new int[] { 10, 22, 34, 46 }, num))
            return "鸡";
        if (contains(new int[] { 9, 21, 33, 45 }, num))
            return "狗";
        if (contains(new int[] { 8, 20, 32, 44 }, num))
            return "猪";

        return "?"; // 默认未知
    }

    private static boolean contains(int[] array, int value) {
        for (int i : array) {
            if (i == value)
                return true;
        }
        return false;
    }

    // 示例颜色规则：根据号码模3分配红/蓝/绿（可自定义，根据你的图片逻辑调整）
    public static Color getColorForNumber(int num) {
        List<Integer> reds = Arrays.asList(1, 2, 7, 8, 12, 13, 18, 19, 23, 24, 29, 30, 34, 35, 40, 45, 46);
        List<Integer> blues = Arrays.asList(3, 4, 9, 10, 14, 15, 20, 25, 26, 31, 36, 37, 41, 42, 47, 48);
        List<Integer> greens = Arrays.asList(5, 6, 11, 16, 17, 21, 22, 27, 28, 32, 33, 38, 39, 43, 44, 49);

        // 使用提供的HEX颜色
        Color red = new Color(0xFE0000); // #FE0000
        Color green = new Color(0x008001); // #008001
        Color blue = new Color(0x0100FB); // #0100FB

        if (reds.contains(num))
            return red;
        if (blues.contains(num))
            return blue;
        if (greens.contains(num))
            return green;

        return Color.white;
    }

    static Set<Integer> RED = Set.of(1, 2, 7, 8, 12, 13, 18, 19, 23, 24, 29, 30, 34, 35, 40, 45, 46);
    static Set<Integer> BLUE = Set.of(3, 4, 9, 10, 14, 15, 20, 25, 26, 31, 36, 37, 41, 42, 47, 48);
    static Set<Integer> GREEN = Set.of(5, 6, 11, 16, 17, 21, 22, 27, 28, 32, 33, 38, 39, 43, 44, 49);

    public static String ballEmoji(int n) {
        if (RED.contains(n))
            return "🔴";
        if (BLUE.contains(n))
            return "🔵";
        return "🟢";
    }
}
