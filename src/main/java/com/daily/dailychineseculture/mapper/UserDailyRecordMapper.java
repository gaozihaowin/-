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
    @Select("SELECT * FROM t_user_daily_record WHERE record_id= #{recordId}")
    UserDailyRecord selectById(Integer recordId);

    /**
     * 插入新记录
     */
    @Insert("INSERT INTO t_user_daily_record(user_id, camp_id, plan_id, is_read_done, is_video_done, is_homework_done, is_extra1_done, is_extra2_done, completion_rate) " +
            "VALUES(#{userId}, #{campId}, #{planId}, #{isReadDone}, #{isVideoDone}, #{isHomeworkDone}, #{isExtra1Done}, #{isExtra2Done}, #{completionRate})")
    int insert(UserDailyRecord userDailyRecord);

    /**
     * 更新记录
     */
    @Update("UPDATE t_user_daily_record SET " +
            "is_read_done = #{isReadDone}, " +
            "is_video_done = #{isVideoDone}, " +
            "is_homework_done = #{isHomeworkDone}, " +
            "is_extra1_done = #{isExtra1Done}, " +
            "is_extra2_done = #{isExtra2Done}, " +
            "completion_rate = #{completionRate} " +
            "WHERE record_id = #{recordId}")
    int update(UserDailyRecord userDailyRecord);

    /**
     * 删除记录
     */
    @Delete("DELETE FROM t_user_daily_record WHERE record_id = #{recordId}")
    int deleteById(Integer recordId);

    /**
     * 根据用户 ID 和计划 ID 查询记录（user_id + plan_id 联合唯一）
     */
    @Select("SELECT * FROM t_user_daily_record WHERE user_id = #{userId} AND plan_id = #{planId}")
    UserDailyRecord selectByUserIdAndPlanId(@Param("userId") Long userId, @Param("planId") Integer planId);
    
    /**
     * 根据用户 ID 和营期 ID 查询所有打卡记录
     */
    @Select("SELECT * FROM t_user_daily_record WHERE user_id = #{userId} AND camp_id = #{campId}")
    List<UserDailyRecord> selectByUserIdAndCampId(@Param("userId") Long userId, @Param("campId") Integer campId);
}