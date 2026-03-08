package com.chris.robot_server.dao;

import com.chris.robot_server.model.LotteryHistory;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LotteryHistoryMapper {
    int deleteByPrimaryKey(Long id);

    int insert(LotteryHistory record);

    int insertSelective(LotteryHistory record);

    LotteryHistory selectByPrimaryKey(Long id);

    LotteryHistory selectByExpect(String expect);

    List<LotteryHistory> selectallList();

    List<LotteryHistory> selectLatestList(Integer limit);

    LotteryHistory selectLatest();

    int updateByPrimaryKeySelective(LotteryHistory record);

    int updateByPrimaryKey(LotteryHistory record);
}