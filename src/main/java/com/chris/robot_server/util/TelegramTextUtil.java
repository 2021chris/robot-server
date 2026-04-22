package com.chris.robot_server.util;

import java.util.ArrayList;
import java.util.List;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;

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



    // 从 Update 中提取用户 ID
    public static Long getUserId(Update update) {
        if (update.message() != null && update.message().from() != null) {
            return update.message().from().id();
        }
        if (update.message() != null && update.message().newChatMembers() != null) {
            for (User u : update.message().newChatMembers()) {
                if (!u.isBot()) return u.id();
            }
        }
        return null;
    }

    // 获取聊天ID（群ID或私聊ID）
    public static Long getChatId(Update update) {
        if (update.message() != null && update.message().chat() != null) {
            return update.message().chat().id();
        }
        if (update.myChatMember() != null && update.myChatMember().chat() != null) {
            return update.myChatMember().chat().id();
        }
        return null;
    }

    // 从 Update 中提取 User 对象
    public static User extractUser(Update update) {
        if (update.message() != null && update.message().from() != null) {
            return update.message().from();
        }
        // newChatMembers 情况可类似处理
        return null;
    }
}
