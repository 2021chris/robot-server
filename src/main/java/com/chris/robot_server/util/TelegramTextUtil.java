package com.chris.robot_server.util;

import java.util.ArrayList;
import java.util.List;

public class TelegramTextUtil {
    public static String normalize(String text) {
        return text.replace("\uFEFF", "").replace("\u200B", "").trim();
    }

    public static String extractCommand(String text) {
        if (text == null)
            return null;

        String cmd = text.split("\\s+")[0];
        if (cmd.contains("@"))
            cmd = cmd.split("@")[0];

        return cmd.toLowerCase();
    }

    /**
     * 统计结果字符串中逗号分隔的数字的数量
     * 
     * @param str 结果字符串，例如 "1, 2, 3, 4, 5"
     * @return 数字的数量 38, , , , , , 返回1
     */
    public static int countResult(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        String[] parts = str.split(",");
        int count = 0;

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty() && trimmed.matches("\\d+")) { // 检查是否为纯数字
                count++;
            }
        }
        return count;
    }

    /**
     * 将结果字符串转换为整数数组
     * 
     * @param input 结果字符串，例如 "1, 2, 3, 4, 5"
     * @return 整数数组 [1, 2, 3, 4, 5]
     */
    public static List<Integer> convertStringToIntArray(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        String[] parts = input.split(",");
        List<Integer> numbers = new ArrayList<>();

        for (String part : parts) {
            String trimmed = part.trim(); // 去除前后空格
            if (!trimmed.isEmpty()) {
                try {
                    int num = Integer.parseInt(trimmed);
                    numbers.add(num);
                } catch (NumberFormatException e) {
                    // 忽略非数字部分
                }
            }
        }

        return numbers;
    }
}
