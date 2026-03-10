package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.UserDailyRecord;
import org.apache.ibatis.annotations.*;

import java.util.Date;

/**
 * 用户每日学习记录Mapper
 */
@Mapper
public interface UserDailyRecordMapper {

    /**
     * 根据ID查询记录
     */
    @Select("SELECT * FROM t_user_daily_record WHERE record_id = #{recordId}")
    UserDailyRecord selectById(Long recordId);

    /**
     * 插入新记录
     */
    @Insert("INSERT INTO t_user_daily_record(user_id, date, learning_duration, check_in_status, create_time) " +
            "VALUES(#{userId}, #{date}, #{learningDuration}, #{checkInStatus}, #{createTime})")
    int insert(UserDailyRecord userDailyRecord);

    /**
     * 更新记录
     */
    @Update("UPDATE t_user_daily_record SET learning_duration = #{learningDuration}, " +
            "check_in_status = #{checkInStatus} WHERE record_id = #{recordId}")
    int update(UserDailyRecord userDailyRecord);

    /**
     * 删除记录
     */
    @Delete("DELETE FROM t_user_daily_record WHERE record_id = #{recordId}")
    int deleteById(Long recordId);

    /**
     * 根据用户 ID 和计划 ID 查询记录（user_id + plan_id 联合唯一）
     */
    @Select("SELECT * FROM t_user_daily_record WHERE user_id = #{userId} AND plan_id = #{planId}")
    UserDailyRecord selectByUserIdAndPlanId(@Param("userId") Long userId, @Param("planId") Integer planId);
}