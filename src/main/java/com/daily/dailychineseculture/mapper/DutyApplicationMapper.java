package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.DutyApplication;
import org.apache.ibatis.annotations.*;

/**
 * 权限申请 Mapper
 */
@Mapper
public interface DutyApplicationMapper {

    /**
     * 插入权限申请记录
     *
     * @param application 权限申请实体
     * @return 影响行数
     */
    @Insert("INSERT INTO t_duty_application (user_id, camp_id, duty_type, apply_reason, status, create_time) " +
            "VALUES (#{userId}, #{campId}, #{dutyType}, #{applyReason}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "applyId")
    int insert(DutyApplication application);

    /**
     * 查询用户是否存在待审核的同类申请
     * 用于防重复申请校验
     *
     * @param userId   用户ID
     * @param dutyType 权限类型
     * @return 申请记录
     */
    @Select("SELECT * FROM t_duty_application " +
            "WHERE user_id = #{userId} " +
            "AND duty_type = #{dutyType} " +
            "AND status = 0 " +
            "LIMIT 1")
    DutyApplication selectPendingApplication(@Param("userId") Long userId, @Param("dutyType") String dutyType);

    /**
     * 根据ID查询申请记录
     *
     * @param applyId 申请ID
     * @return 申请记录
     */
    @Select("SELECT * FROM t_duty_application WHERE apply_id = #{applyId}")
    DutyApplication selectById(@Param("applyId") Integer applyId);
}
