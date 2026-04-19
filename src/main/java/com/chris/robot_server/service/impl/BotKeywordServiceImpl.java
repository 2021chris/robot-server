package com.chris.robot_server.service.impl;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.chris.robot_server.dao.BotKeywordMapper;
import com.chris.robot_server.model.BotKeyword;
import com.chris.robot_server.service.BotKeywordService;

@Service
public class BotKeywordServiceImpl implements BotKeywordService {

    @Autowired
    @Qualifier("redisObjTemplate")
    private RedisTemplate<String,Object> redisObjTemplate;
    @Autowired
    private BotKeywordMapper botKeywordMapper;

    private static final String CACHE_KEY = "telegram:keywords";

    @Override
    public List<BotKeyword> getAllEnabledKeywords() {
        // 先从 Redis 取
        List<BotKeyword> cached = (List<BotKeyword>) redisObjTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            return cached;
        }
        // 如果 Redis 中没有，则从数据库获取
        List<BotKeyword> keywords = botKeywordMapper.selectAllEnabled();
        // 将结果放入 Redis
        redisObjTemplate.opsForValue().set(CACHE_KEY, keywords, Duration.ofHours(24)); // 设置过期时间为24小时
        return keywords;
    }

    /**
     * 当管理员在后台新增/修改/删除关键词时，调用此方法刷新缓存
     */
    public void refreshKeywordCache() {
        List<BotKeyword> keywords = botKeywordMapper.selectAllEnabled();
        redisObjTemplate.opsForValue().set(CACHE_KEY, keywords, Duration.ofHours(24));
    }

}
