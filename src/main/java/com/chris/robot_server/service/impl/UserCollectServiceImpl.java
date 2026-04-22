package com.chris.robot_server.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.chris.robot_server.dao.TelegramUserMapper;
import com.chris.robot_server.model.TelegramUser;
import com.chris.robot_server.service.UserCollectService;
import com.chris.robot_server.util.TelegramTextUtil;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCollectServiceImpl implements UserCollectService {

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String,String> redisTemplate;

    private final TelegramUserMapper telegramUserMapper;   // MyBatis Mapper

    private static final String USER_KEY_PREFIX = "tg:user:";
    private static final String BOT_USERS_KEY_PREFIX = "bot:users:";
    // private static final String GROUP_USERS_KEY_PREFIX = "group:users:";

    @Override
    public void collectUser(Update update, long botId, String token) {
        Long userId = TelegramTextUtil.getUserId(update);
        if (userId == null || userId < 0) return;

        Long groupId = TelegramTextUtil.getChatId(update);

        String userKey = USER_KEY_PREFIX + userId;

        // 1. 判断是否已采集（Redis 判断，非常快）
        if (Boolean.TRUE.equals(redisTemplate.hasKey(userKey))) {
            // 已采集过，只更新最后活跃时间（可选）
            // redisTemplate.opsForHash().put(userKey, "last_active_time", System.currentTimeMillis());
            return;
        }

        // 2. 第一次采集
        User telegramUser = TelegramTextUtil.extractUser(update);

        // 保存到 Redis（Hash）
        Map<String, Object> userData = new HashMap<>();
        userData.put("collected", "1");
        userData.put("bot_id", botId);
        userData.put("bot_token", token);
        userData.put("first_group", groupId);
        userData.put("username", telegramUser.username());
        String displayName = telegramUser.firstName();
        if (telegramUser.lastName() != null) {
            displayName += " " + telegramUser.lastName();
        }
        userData.put("displayName", displayName);
        userData.put("collected_time", System.currentTimeMillis());
        redisTemplate.opsForHash().putAll(userKey, userData);

        // 3. 写入机器人用户集合
        String botUsersKey = BOT_USERS_KEY_PREFIX + botId;
        redisTemplate.opsForSet().add(botUsersKey, userId.toString());

        // 4. 写入数据库（异步执行，避免阻塞）
        TelegramUser user = new TelegramUser();
        user.setUserId(userId);
        user.setUserName(telegramUser.username());
        user.setDisplayName(displayName);
        user.setToken(token);
        user.setUserGroupId(groupId);
        user.setCreateTime(new Date());
        telegramUserMapper.insert(user);
    }

}
