package com.chris.robot_server.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.chris.robot_server.model.BotKeyword;

@Mapper
public interface BotKeywordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(BotKeyword record);

    int insertSelective(BotKeyword record);

    BotKeyword selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(BotKeyword record);

    int updateByPrimaryKey(BotKeyword record);

    List<BotKeyword> selectAllEnabled();
}