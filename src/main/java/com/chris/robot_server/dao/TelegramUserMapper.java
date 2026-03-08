package com.chris.robot_server.dao;

import com.chris.robot_server.model.TelegramUser;

public interface TelegramUserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TelegramUser record);

    int insertSelective(TelegramUser record);

    TelegramUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TelegramUser record);

    int updateByPrimaryKey(TelegramUser record);
}