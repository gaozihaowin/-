package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.DutyAssignment;
import com.daily.dailychineseculture.vo.AdminListItemVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
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
     * 校验用户是否已拥有某个权限（全局权限）
     * 用于权限申请的防重复授权校验
     * 
     * @param userId   用户 ID
     * @param dutyType 权限类型
     * @return 职位分配信息，如果存在则返回记录，否则返回 null
     */
    @Select("SELECT * FROM t_duty_assignment " +
            "WHERE user_id = #{userId} " +
            "AND duty_type = #{dutyType} " +
            "AND (end_time IS NULL OR end_time > NOW()) " +
            "LIMIT 1")
    DutyAssignment selectByUserIdAndDutyTypeForReview(@Param("userId") Long userId, 
                                                       @Param("dutyType") String dutyType);

    /**
     * 插入授权记录（发牌）
     * 用于审批通过后向 t_duty_assignment 表插入数据
     * 业务规则：所有管理员均为全局权限，不再维护 camp_id
     *
     * @param userId   用户ID
     * @param dutyType 权限类型
     * @return 影响行数
     */
    @Insert("INSERT INTO t_duty_assignment (user_id, duty_type) VALUES (#{userId}, #{dutyType})")
    int insertAssignment(@Param("userId") Long userId, @Param("dutyType") String dutyType);

    /**
     * 查询管理人员列表（带身份隔离）
     * JOIN t_user 表获取管理员的 nickname 和 account
     *
     * @param dutyTypeFilter 角色过滤条件（非 SUPER_ADMIN 时传入具体角色，SUPER_ADMIN 传 null）
     * @return 管理人员列表
     */
    @Select("<script>" +
            "SELECT " +
            "  da.user_id AS userId, " +
            "  u.nickname AS nickname, " +
            "  u.account AS account, " +
            "  da.duty_type AS dutyType, " +
            "  da.start_time AS assignTime " +
            "FROM t_duty_assignment da " +
            "LEFT JOIN t_user u ON da.user_id = u.user_id " +
            "WHERE (da.end_time IS NULL OR da.end_time > NOW()) " +
            "<if test='dutyTypeFilter != null'>" +
            "  AND da.duty_type = #{dutyTypeFilter} " +
            "</if>" +
            "ORDER BY da.start_time DESC " +
            "</script>")
    List<AdminListItemVO> selectAdminList(@Param("dutyTypeFilter") String dutyTypeFilter);

    @Select("SELECT COUNT(*) FROM t_duty_assignment WHERE user_id = #{userId}")
    Integer countByUserId(@Param("userId") Long userId);

    @Update("UPDATE t_duty_assignment SET user_id = #{newUserId} WHERE user_id = #{oldUserId}")
    int transferAllAssignments(@Param("oldUserId") Long oldUserId, @Param("newUserId") Long newUserId);
}