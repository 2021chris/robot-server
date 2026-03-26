package com.chris.robot_server.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.chris.robot_server.model.LotteryHistoryLao;

@Mapper
public interface LotteryHistoryLaoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(LotteryHistoryLao record);

    int insertSelective(LotteryHistoryLao record);

    LotteryHistoryLao selectByPrimaryKey(Long id);

    LotteryHistoryLao selectByExpect(String expect);

    List<LotteryHistoryLao> selectallList();

    List<LotteryHistoryLao> selectLatestList(Integer limit);

    LotteryHistoryLao selectLatest();


    int updateByPrimaryKeySelective(LotteryHistoryLao record);

    int updateByPrimaryKey(LotteryHistoryLao record);
}