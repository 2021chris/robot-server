package com.chris.robot_server.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.chris.robot_server.model.TelegramLink;

@Mapper
public interface TelegramLinkMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TelegramLink record);

    int insertSelective(TelegramLink record);

    TelegramLink selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TelegramLink record);

    int updateByPrimaryKey(TelegramLink record);

    List<TelegramLink> selectByType(String type);
}