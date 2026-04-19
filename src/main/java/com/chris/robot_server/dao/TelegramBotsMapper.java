package com.chris.robot_server.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.chris.robot_server.model.TelegramBots;

@Mapper
public interface TelegramBotsMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TelegramBots record);

    int insertSelective(TelegramBots record);

    TelegramBots selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TelegramBots record);

    int updateByPrimaryKey(TelegramBots record);

    List<TelegramBots> selectAllBots();

    TelegramBots findByToken(String token);
}