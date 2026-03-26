package com.chris.robot_server.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.chris.robot_server.model.LotteryPushExpect;

@Mapper
public interface LotteryPushExpectMapper {
    int deleteByPrimaryKey(Long id);

    int insert(LotteryPushExpect record);

    int insertSelective(LotteryPushExpect record);

    LotteryPushExpect selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(LotteryPushExpect record);

    int updateByPrimaryKey(LotteryPushExpect record);

    List<LotteryPushExpect> selectAll();
}