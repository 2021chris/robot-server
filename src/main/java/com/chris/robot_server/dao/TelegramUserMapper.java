package com.chris.robot_server.dao;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.chris.robot_server.model.TelegramUser;

public interface TelegramUserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TelegramUser record);

    int insertOrUpdate(TelegramUser record);

    int insertSelective(TelegramUser record);

    TelegramUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TelegramUser record);

    int updateByPrimaryKey(TelegramUser record);

    @Select("SELECT * FROM telegram_user")
    List<TelegramUser> selectAll();

    @Select("SELECT * FROM telegram_user WHERE user_id = #{userId} LIMIT 1")
    TelegramUser selectByUserId(Long userId);
}