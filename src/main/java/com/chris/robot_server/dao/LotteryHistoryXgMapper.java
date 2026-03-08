package com.chris.robot_server.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.chris.robot_server.model.LotteryHistoryXg;

@Mapper
public interface LotteryHistoryXgMapper {
    int deleteByPrimaryKey(Long id);

    int insert(LotteryHistoryXg record);

    int insertSelective(LotteryHistoryXg record);

    LotteryHistoryXg selectByPrimaryKey(Long id);

    LotteryHistoryXg selectByExpect(String expect);

    int updateByPrimaryKeySelective(LotteryHistoryXg record);

    int updateByPrimaryKey(LotteryHistoryXg record);

    List<LotteryHistoryXg> selectallList();

    List<LotteryHistoryXg> selectLatestList(Integer limit);

    LotteryHistoryXg selectLatest();
}