package com.chris.robot_server.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.chris.robot_server.model.TelegramGroup;

@Mapper
public interface TelegramGroupMapper {

    @Select("SELECT * FROM telegram_group")
    List<TelegramGroup> findAll();

    @Select("SELECT COUNT(*) FROM telegram_group")
    int count();

    void deleteByGroupIdAndToken(@Param("groupId") Long groupId, @Param("token") String token);

    int deleteByPrimaryKey(Long id);

    int insert(TelegramGroup record);

    int insertSelective(TelegramGroup record);

    TelegramGroup selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TelegramGroup record);

    int updateByPrimaryKey(TelegramGroup record);

    boolean existsByGroupIdAndToken(@Param("groupId") Long groupId, @Param("token") String token);

    int updateStatusByGroupIdAndToken(@Param("groupId") Long groupId, @Param("token") String token, @Param("status") Byte status);

    List<TelegramGroup> findByStatus(Byte status);

    List<TelegramGroup> selectAllGroups();
}