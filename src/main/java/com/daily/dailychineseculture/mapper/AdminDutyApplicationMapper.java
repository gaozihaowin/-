package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.vo.AdminDutyApplicationListItemVO;
import com.daily.dailychineseculture.vo.AdminDutyApplicationStatsVO;
import com.daily.dailychineseculture.vo.ApplicationHistoryVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface AdminDutyApplicationMapper {

    @Select("<script>" +
            "SELECT " +
            "  COUNT(*) AS total, " +
            "  SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS pending, " +
            "  SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS passed, " +
            "  SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS rejected " +
            "FROM t_duty_application " +
            "WHERE status != 3 " +
            "<if test='dutyTypeFilter != null'>" +
            "  AND duty_type = #{dutyTypeFilter} " +
            "</if>" +
            "</script>")
    AdminDutyApplicationStatsVO selectStats(@Param("dutyTypeFilter") String dutyTypeFilter);

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

    @Select("SELECT " +
            "  a.apply_id AS applyId, " +
            "  a.user_id AS userId, " +
            "  u.nickname AS applicantName, " +
            "  a.duty_type AS dutyType, " +
            "  a.apply_reason AS applyReason, " +
            "  a.status AS status, " +
            "  a.create_time AS createTime " +
            "FROM t_duty_application a " +
            "LEFT JOIN t_user u ON a.user_id = u.user_id " +
            "WHERE a.user_id = #{userId} " +
            "ORDER BY a.create_time DESC")
    List<AdminDutyApplicationListItemVO> selectApplicationHistoryByUserId(@Param("userId") Long userId);

    @Select("SELECT " +
            "  apply_id AS applyId, " +
            "  duty_type AS dutyType, " +
            "  apply_reason AS applyReason, " +
            "  status, " +
            "  create_time AS createTime, " +
            "  review_time AS reviewTime, " +
            "  audit_remark AS auditRemark " +
            "FROM t_duty_application " +
            "WHERE user_id = #{userId} " +
            "ORDER BY create_time DESC")
    List<ApplicationHistoryVO> selectApplicationHistoryVOByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO t_duty_application " +
            "(user_id, duty_type, apply_reason, status, reviewer_id, review_time, audit_remark, create_time) " +
            "VALUES (#{userId}, #{dutyType}, #{applyReason}, #{status}, #{reviewerId}, #{reviewTime}, #{auditRemark}, #{createTime})")
    int insertApplicationAudit(@Param("userId") Long userId,
                                @Param("dutyType") String dutyType,
                                @Param("applyReason") String applyReason,
                                @Param("status") int status,
                                @Param("reviewerId") Long reviewerId,
                                @Param("reviewTime") Date reviewTime,
                                @Param("auditRemark") String auditRemark,
                                @Param("createTime") Date createTime);
}
