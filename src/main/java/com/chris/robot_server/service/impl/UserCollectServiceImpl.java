package com.chris.robot_server.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.chris.robot_server.config.StaticConfig;
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

    // private static final String GROUP_USERS_KEY_PREFIX = "group:users:";

    @Override
    public void collectUser(Update update, long botId, String token) {
        Long userId = TelegramTextUtil.getUserId(update);
        if (userId == null || userId < 0) return;

        Long groupId = TelegramTextUtil.getChatId(update);

        String userKey = StaticConfig.USER_KEY_PREFIX + userId;

        // 1. 判断是否已采集（Redis 判断，非常快）
        if (Boolean.TRUE.equals(redisTemplate.hasKey(userKey))) {
            // 已采集过，只更新最后活跃时间（可选）
            // redisTemplate.opsForHash().put(userKey, "last_active_time", System.currentTimeMillis());
            return;
        }

        TelegramUser existingUser = telegramUserMapper.selectByUserId(userId);
        if (existingUser != null) {
            // TODO 待测试：数据库已有，补入 Redis 缓存
            refreshUserToRedis(existingUser);
            return;
        }
        // 2. 第一次采集
        User user = TelegramTextUtil.extractUser(update);

        // 保存到 Redis（Hash）
        Map<String, Object> userData = new HashMap<>();
        userData.put("collected", "1");
        // userData.put("bot_id", botId);
        userData.put("bot_token", token);
        userData.put("first_group", groupId);
        userData.put("username", user.username());
        String displayName = user.firstName();
        if (user.lastName() != null) {
            displayName += " " + user.lastName();
        }
        userData.put("displayName", displayName);
        userData.put("collected_time", System.currentTimeMillis());
        redisTemplate.opsForHash().putAll(userKey, userData);

        // 3. 写入机器人用户集合
        String botUsersKey = StaticConfig.BOT_USERS_KEY_PREFIX + botId;
        redisTemplate.opsForSet().add(botUsersKey, userId.toString());

        // 4. 写入数据库（异步执行，避免阻塞）
        TelegramUser telegramUser = new TelegramUser();
        telegramUser.setUserId(userId);
        telegramUser.setUserName(user.username());
        telegramUser.setDisplayName(displayName);
        telegramUser.setToken(token);
        telegramUser.setUserGroupId(groupId);
        telegramUser.setCreateTime(new Date());
        telegramUserMapper.insert(telegramUser);
    }

    /**
     * 把数据库用户刷新到 Redis
     */
    private void refreshUserToRedis(TelegramUser user) {
        String userKey = StaticConfig.USER_KEY_PREFIX + user.getUserId();

        Map<String, Object> userData = new HashMap<>();
        userData.put("collected", "1");
        userData.put("bot_token", user.getToken());
        userData.put("first_group", user.getUserGroupId());
        userData.put("username", user.getUserName());
        userData.put("displayName", user.getDisplayName());
        userData.put("collected_time", user.getCreateTime() != null ? 
                    user.getCreateTime().getTime() : System.currentTimeMillis());
        userData.put("last_active_time", System.currentTimeMillis());

        redisTemplate.opsForHash().putAll(userKey, userData);
    }

}
