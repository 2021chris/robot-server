package com.chris.robot_server.dao;

import org.apache.ibatis.annotations.Mapper;

import com.chris.robot_server.model.Advertisement;

@Mapper
public interface AdvertisementMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Advertisement record);

    int insertSelective(Advertisement record);

    Advertisement selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Advertisement record);

    int updateByPrimaryKey(Advertisement record);

    Advertisement selectBySeatAndType(Advertisement record);
}