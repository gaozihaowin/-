package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.UserDailyRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户每日学习记录 Mapper
 */
@Mapper
public interface UserDailyRecordMapper {

    /**
     * 根据 ID 查询记录
     */
    @Select("SELECT record_id, user_id, camp_id, plan_id, completion_rate, is_all_completed FROM t_user_daily_record WHERE record_id = #{recordId}")
    UserDailyRecord selectById(Integer recordId);

    /**
     * 根据用户 ID 和计划 ID 查询记录（user_id + plan_id 联合唯一）
     */
    @Select("SELECT record_id, user_id, camp_id, plan_id, completion_rate, is_all_completed FROM t_user_daily_record WHERE user_id = #{userId} AND plan_id = #{planId}")
    UserDailyRecord selectByUserIdAndPlanId(@Param("userId") Long userId, @Param("planId") Integer planId);
    
    /**
     * 根据用户 ID 和营期 ID 查询所有打卡记录
     */
    @Select("SELECT record_id, user_id, camp_id, plan_id, completion_rate, is_all_completed FROM t_user_daily_record WHERE user_id = #{userId} AND camp_id = #{campId}")
    List<UserDailyRecord> selectByUserIdAndCampId(@Param("userId") Long userId, @Param("campId") Integer campId);

    /**
     * 插入或更新汇总记录
     */
    @Insert("INSERT INTO t_user_daily_record(user_id, camp_id, plan_id, completion_rate, is_all_completed) " +
            "VALUES(#{userId}, #{campId}, #{planId}, #{completionRate}, #{isAllCompleted}) " +
            "ON DUPLICATE KEY UPDATE completion_rate = #{completionRate}, is_all_completed = #{isAllCompleted}")
    int upsertSummary(@Param("userId") Long userId,
                      @Param("campId") Integer campId,
                      @Param("planId") Integer planId,
                      @Param("completionRate") Integer completionRate,
                      @Param("isAllCompleted") Integer isAllCompleted);
}
