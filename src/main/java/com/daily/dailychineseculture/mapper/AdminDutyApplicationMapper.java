package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.vo.AdminDutyApplicationListItemVO;
import com.daily.dailychineseculture.vo.AdminDutyApplicationStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 管理端权限申请 Mapper
 * 所有 SQL 使用原生 MyBatis 注解编写
 */
@Mapper
public interface AdminDutyApplicationMapper {

    /**
     * 统计审批数据（支持角色数据隔离）
     * 使用 CASE WHEN 聚合一次性查出各状态数量
     *
     * @param dutyTypeFilter 角色过滤条件（非 SUPER_ADMIN 时传入具体角色，SUPER_ADMIN 传 null）
     * @return 统计结果
     */
    @Select("<script>" +
            "SELECT " +
            "  COUNT(*) AS total, " +
            "  SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS pending, " +
            "  SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS passed, " +
            "  SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS rejected " +
            "FROM t_duty_application " +
            "WHERE status != 3 " +  // 排除已撤销的
            "<if test='dutyTypeFilter != null'>" +
            "  AND duty_type = #{dutyTypeFilter} " +
            "</if>" +
            "</script>")
    AdminDutyApplicationStatsVO selectStats(@Param("dutyTypeFilter") String dutyTypeFilter);

    /**
     * 分页查询审批列表（支持角色数据隔离）
     * LEFT JOIN t_user 获取申请人姓名
     *
     * @param dutyTypeFilter   角色过滤条件（非 SUPER_ADMIN 时传入具体角色）
     * @param status           状态过滤（可选）
     * @param dutyType         权限类型过滤（可选，超级管理员用于筛选）
     * @return 申请列表
     */
    @Select("<script>" +
            "SELECT " +
            "  a.apply_id AS applyId, " +
            "  a.user_id AS userId, " +
            "  u.nickname AS applicantName, " +
            "  a.duty_type AS dutyType, " +
            "  a.apply_reason AS applyReason, " +
            "  a.status AS status, " +
            "  a.create_time AS createTime " +
            "FROM t_duty_application a " +
            "LEFT JOIN t_user u ON a.user_id = u.user_id " +
            "WHERE 1=1 " +
            "<if test='dutyTypeFilter != null'>" +
            "  AND a.duty_type = #{dutyTypeFilter} " +
            "</if>" +
            "<if test='status != null'>" +
            "  AND a.status = #{status} " +
            "</if>" +
            "<if test='dutyType != null and dutyType != \"\"'>" +
            "  AND a.duty_type = #{dutyType} " +
            "</if>" +
            "ORDER BY a.create_time DESC " +
            "</script>")
    List<AdminDutyApplicationListItemVO> selectApplicationList(
            @Param("dutyTypeFilter") String dutyTypeFilter,
            @Param("status") Integer status,
            @Param("dutyType") String dutyType);
}
