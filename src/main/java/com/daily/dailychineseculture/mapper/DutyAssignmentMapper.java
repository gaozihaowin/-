package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.DutyAssignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 职位分配 Mapper
 */
@Mapper
public interface DutyAssignmentMapper {
    
    /**
     * 查询用户的职位权限信息
     * 
     * @param userId 用户 ID
     * @param dutyType 职位类型
     * @return 职位分配信息
     */
    @Select("SELECT * FROM t_duty_assignment " +
            "WHERE user_id = #{userId} " +
            "AND duty_type = #{dutyType} " +
            "AND (end_time IS NULL OR end_time > NOW())")
    DutyAssignment selectByUserIdAndDutyType(@Param("userId") Long userId, @Param("dutyType") String dutyType);
    
    /**
     * 查询用户的职位权限信息（返回 Map，包含营期信息）
     * 
     * @param userId 用户 ID
     * @param dutyType 职位类型
     * @return Map 包含 assignment_id, camp_id, duty_type 等
     */
    @Select("SELECT da.*, c.name as camp_name " +
            "FROM t_duty_assignment da " +
            "LEFT JOIN t_camp c ON da.camp_id = c.camp_id " +
            "WHERE da.user_id = #{userId} " +
            "AND da.duty_type = #{dutyType} " +
            "AND (da.end_time IS NULL OR da.end_time > NOW())")
    Map<String, Object> selectWithCampInfo(@Param("userId") Long userId, @Param("dutyType") String dutyType);
}
