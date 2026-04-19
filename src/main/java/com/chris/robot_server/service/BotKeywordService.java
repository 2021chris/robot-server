package com.chris.robot_server.service;

import java.util.List;

import com.chris.robot_server.model.BotKeyword;

public interface BotKeywordService {

    /**
     * 获取所有启用的关键词,先从redis获取,如果没有再从数据库获取并放入redis
     * @return
     */
    public List<BotKeyword> getAllEnabledKeywords();
}
