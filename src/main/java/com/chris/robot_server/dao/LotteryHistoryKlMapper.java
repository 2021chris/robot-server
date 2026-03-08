package com.chris.robot_server.dao;

import java.util.List;

import com.chris.robot_server.model.LotteryHistoryKl;

public interface LotteryHistoryKlMapper {
    int deleteByPrimaryKey(Long id);

    int insert(LotteryHistoryKl record);

    int insertSelective(LotteryHistoryKl record);

    LotteryHistoryKl selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(LotteryHistoryKl record);

    int updateByPrimaryKey(LotteryHistoryKl record);

    LotteryHistoryKl selectByExpect(String expect);

    /**
     * 获取最新N条记录
     * @param limit
     * @return
     */
    List<LotteryHistoryKl> selectLatestList(int limit);

    /**
     * 获取最新一条记录
     * @return
     */
    LotteryHistoryKl selectLatest();
}