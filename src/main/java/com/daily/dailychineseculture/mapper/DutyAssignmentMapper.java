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
    
    /**
     * 查询用户的所有任命记录（用于身份切换列表）
     * 
     * @param userId 用户 ID
     * @return 任命记录列表（包含营期信息）
     */
    @Select("SELECT da.assignment_id, da.user_id, da.camp_id, da.duty_type, " +
            "da.start_time, da.end_time, c.name as camp_name " +
            "FROM t_duty_assignment da " +
            "LEFT JOIN t_camp c ON da.camp_id = c.camp_id " +
            "WHERE da.user_id = #{userId} " +
            "ORDER BY da.start_time DESC")
    java.util.List<Map<String, Object>> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据 appointmentId 查询任命记录
     * 
     * @param assignmentId 任命记录 ID
     * @return 任命记录
     */
    @Select("SELECT da.assignment_id, da.user_id, da.camp_id, da.duty_type, " +
            "da.start_time, da.end_time, c.name as camp_name " +
            "FROM t_duty_assignment da " +
            "LEFT JOIN t_camp c ON da.camp_id = c.camp_id " +
            "WHERE da.assignment_id = #{assignmentId}")
    Map<String, Object> selectById(@Param("assignmentId") Integer assignmentId);
    
    /**
     * 校验用户是否已拥有某个权限（支持 campId 精准匹配）
     * 用于权限申请的防重复授权校验
     * 
     * @param userId 用户 ID
     * @param dutyType 权限类型
     * @param campId 营期 ID（可为 null）
     * @return 职位分配信息，如果存在则返回记录，否则返回 null
     */
    @Select("<script>" +
            "SELECT * FROM t_duty_assignment " +
            "WHERE user_id = #{userId} " +
            "AND duty_type = #{dutyType} " +
            "<if test='campId != null'>" +
            "AND camp_id = #{campId} " +
            "</if>" +
            "<if test='campId == null'>" +
            "AND camp_id IS NULL " +
            "</if>" +
            "AND (end_time IS NULL OR end_time > NOW()) " +
            "LIMIT 1" +
            "</script>")
    DutyAssignment selectByUserIdDutyTypeAndCampId(@Param("userId") Long userId, 
                                                    @Param("dutyType") String dutyType, 
                                                    @Param("campId") Integer campId);
}