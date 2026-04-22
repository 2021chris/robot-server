package com.chris.robot_server.dao;

import org.apache.ibatis.annotations.Mapper;

import com.chris.robot_server.model.Manager;

@Mapper
public interface ManagerMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Manager record);

    int insertSelective(Manager record);

    Manager selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Manager record);

    int updateByPrimaryKey(Manager record);

    Manager selectByUsername(String username);
}