package com.daily.dailychineseculture.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/**
 * 作业Mapper接口
 */
@Mapper
public interface HomeworkMapper {

    /**
     * 获取志愿者管理范围
     */
    @Select("SELECT ds.target_type, ds.target_id, " +
            "c.name as class_name, " +
            "bg.name as big_group_name, " +
            "sg.name as small_group_name " +
            "FROM t_duty_assignment da " +
            "LEFT JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
            "LEFT JOIN t_camp camp ON da.camp_id = camp.camp_id " +
            "LEFT JOIN t_class c ON ds.target_id = c.class_id AND ds.target_type = 'class' " +
            "LEFT JOIN t_big_group bg ON ds.target_id = bg.big_group_id AND ds.target_type = 'big_group' " +
            "LEFT JOIN t_small_group sg ON ds.target_id = sg.small_group_id AND ds.target_type = 'small_group' " +
            "WHERE da.user_id = #{userId} " +
            "AND da.duty_type IN ('检组', '学组', '检委', '学委', '学班', '检班') " +
            "AND ds.assignment_id IS NOT NULL " +
            "AND da.volunteer_end_time IS NULL " +
            "AND camp.end_time > NOW()")
    List<Map<String, Object>> getVolunteerScope(Long userId);

    /**
     * 检查志愿者权限（基础权限）
     */
    @Select("SELECT COUNT(*) as auth_count " +
            "FROM t_duty_scope ds " +
            "JOIN t_duty_assignment da ON ds.assignment_id = da.assignment_id " +
            "WHERE da.user_id = #{userId} " +
            "AND da.duty_type IN ('检组', '学组', '检委', '学委', '学班', '检班') " +
            "AND ds.target_type = #{targetType} " +
            "AND ds.target_id = #{targetId}")
    Integer checkVolunteerAuth(@Param("userId") Long userId,
                               @Param("targetType") String targetType,
                               @Param("targetId") Integer targetId);

    /**
     * 检查大组权限
     */
    @Select("SELECT COUNT(*) as auth_count " +
            "FROM t_duty_scope ds " +
            "JOIN t_duty_assignment da ON ds.assignment_id = da.assignment_id " +
            "WHERE da.user_id = #{userId} " +
            "AND da.duty_type IN ('检组', '学组', '检委', '学委', '学班', '检班') " +
            "AND ds.target_type = 'big_group' " +
            "AND ds.target_id = #{targetId}")
    Integer checkBigGroupAuth(@Param("userId") Long userId, @Param("targetId") Integer targetId);

    /**
     * 检查小组权限
     */
    @Select("SELECT COUNT(*) as auth_count " +
            "FROM t_duty_scope ds " +
            "JOIN t_duty_assignment da ON ds.assignment_id = da.assignment_id " +
            "WHERE da.user_id = #{userId} " +
            "AND da.duty_type IN ('检组', '学组', '检委', '学委', '学班', '检班') " +
            "AND ds.target_type = 'small_group' " +
            "AND ds.target_id = #{targetId}")
    Integer checkSmallGroupAuth(@Param("userId") Long userId, @Param("targetId") Integer targetId);

    /**
     * 获取指定范围的学员ID列表
     */
    @Select("<script>" +
            "SELECT user_id FROM t_camp_enrollment WHERE " +
            "<choose>" +
            "    <when test='type == \"class\"'>class_id = #{id}</when>" +
            "    <when test='type == \"bigGroup\" or type == \"big_group\"'>big_group_id = #{id}</when>" +
            "    <when test='type == \"smallGroup\" or type == \"small_group\"'>small_group_id = #{id}</when>" +
            "</choose>" +
            "</script>")
    List<Long> getStudentIdsByScope(@Param("type") String type, @Param("id") Integer id);

    /**
     * 获取作业列表
     */
    @Select("<script>" +
            "SELECT h.homework_id as homeworkId, " +
            "       h.user_id as userId, " +
            "       COALESCE(u.nickname, u.account, CONCAT('学员', h.user_id)) as name, " +
            "       h.is_excellent as isExcellent, " +
            "       h.submit_time, " +
            "       c.name as class_name, " +
            "       bg.name as big_group_name, " +
            "       sg.name as small_group_name, " +
            "       camp.name as camp_name " +
            "FROM t_homework h " +
            "LEFT JOIN t_user u ON h.user_id = u.user_id " +
            "LEFT JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
            "LEFT JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
            "LEFT JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
            "LEFT JOIN t_class c ON ce.class_id = c.class_id " +
            "LEFT JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id " +
            "LEFT JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id " +
            "WHERE h.homework_id IN (" +
            "    SELECT MAX(homework_id) " +
            "    FROM t_homework " +
            "    WHERE user_id IN " +
            "    <foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
            "        #{userId}" +
            "    </foreach>" +
            "    <if test='date != null and date != \"\"'> AND DATE(submit_time) = #{date}</if>" +
            "    GROUP BY user_id, plan_id " +  // 按用户ID和计划ID分组，取最新的作业
            ")" +
            "<if test='status == \"excellent\"'> AND h.is_excellent = true</if>" +
            "<if test='searchKeyword != null and searchKeyword != \"\"'> " +
            "    AND (" +
            "        camp.name LIKE CONCAT('%', #{searchKeyword}, '%') OR " +
            "        c.name LIKE CONCAT('%', #{searchKeyword}, '%') OR " +
            "        bg.name LIKE CONCAT('%', #{searchKeyword}, '%') OR " +
            "        sg.name LIKE CONCAT('%', #{searchKeyword}, '%') OR " +
            "        COALESCE(u.nickname, u.account) LIKE CONCAT('%', #{searchKeyword}, '%')" +
            "    )" +
            "</if>" +
            "ORDER BY h.submit_time DESC" +
            "</script>")
    List<Map<String, Object>> getHomeworkList(@Param("studentIds") List<Long> studentIds,
                                              @Param("status") String status,
                                              @Param("date") String date,
                                              @Param("searchKeyword") String searchKeyword);

    /**
     * 标记优秀作业
     */
    @Update("UPDATE t_homework SET is_excellent = #{isExcellent} WHERE homework_id = #{homeworkId}")
    int markExcellentHomework(@Param("homeworkId") Integer homeworkId,
                              @Param("isExcellent") Boolean isExcellent);

    /**
     * 获取作业详情
     */
    @Select("SELECT h.homework_id, h.user_id, h.content, h.submit_time, h.is_excellent as isExcellent, " +
            "       COALESCE(u.nickname, u.account) as name, " +  // 优先使用昵称，昵称空则使用账户名
            "       c.name as class_name, bg.name as big_group_name, sg.name as small_group_name, " +
            "       camp.name as camp_name " +
            "FROM t_homework h " +
            "LEFT JOIN t_user u ON h.user_id = u.user_id " +
            "LEFT JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
            "LEFT JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
            "LEFT JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
            "LEFT JOIN t_class c ON ce.class_id = c.class_id " +
            "LEFT JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id " +
            "LEFT JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id " +
            "WHERE h.homework_id = #{homeworkId}")
    Map<String, Object> getHomeworkDetail(@Param("homeworkId") Integer homeworkId);

    /**
     * 检查作业是否存在
     */
    @Select("SELECT COUNT(*) FROM t_homework WHERE homework_id = #{homeworkId}")
    int checkHomeworkExists(@Param("homeworkId") Integer homeworkId);

    /**
     * 获取已完成作业的人数
     */
    @Select("<script>" +
            "SELECT COUNT(DISTINCT h.user_id) FROM t_homework h " +
            "WHERE h.user_id IN " +
            "<foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
            "    #{userId}" +
            "</foreach>" +
            "<if test='date != null and date != \"\"'> AND DATE(h.submit_time) = #{date}</if>" +
            "</script>")
    int getCompletedHomeworkCount(@Param("studentIds") List<Long> studentIds, @Param("date") String date);

    /**
     * 获取指定日期的营期计划
     */
    @Select("SELECT plan_id FROM t_camp_plan WHERE plan_date = #{date}")
    Integer getPlanIdByDate(@Param("date") String date);

    /**
     * 获取已交作业的学生名单
     */
    @Select("<script>" +
            "SELECT u.user_id, COALESCE(u.nickname, u.account) as name, h.submit_time " +
            "FROM t_homework h " +
            "LEFT JOIN t_user u ON h.user_id = u.user_id " +
            "WHERE h.user_id IN " +
            "<foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
            "    #{userId}" +
            "</foreach>" +
            "AND DATE(h.submit_time) = #{date} " +
            "ORDER BY h.submit_time DESC" +
            "</script>")
    List<Map<String, Object>> getSubmittedHomeworkList(@Param("studentIds") List<Long> studentIds, @Param("date") String date);

    /**
     * 获取未交作业的学生名单
     */
    @Select("<script>" +
            "SELECT u.user_id, COALESCE(u.nickname, u.account) as name " +
            "FROM t_user u " +
            "WHERE u.user_id IN " +
            "<foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
            "    #{userId}" +
            "</foreach>" +
            "AND u.user_id NOT IN " +
            "(SELECT h.user_id FROM t_homework h WHERE DATE(h.submit_time) = #{date})" +
            "</script>")
    List<Map<String, Object>> getNotSubmittedHomeworkList(@Param("studentIds") List<Long> studentIds, @Param("date") String date);

    /**
     * 获取迟交作业的学生名单
     */
    @Select("<script>" +
            "SELECT u.user_id, COALESCE(u.nickname, u.account) as name, h.submit_time " +
            "FROM t_homework h " +
            "LEFT JOIN t_user u ON h.user_id = u.user_id " +
            "WHERE h.user_id IN " +
            "<foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
            "    #{userId}" +
            "</foreach>" +
            "AND DATE(h.submit_time) > #{date} " +
            "ORDER BY h.submit_time DESC" +
            "</script>")
    List<Map<String, Object>> getLateHomeworkList(@Param("studentIds") List<Long> studentIds, @Param("date") String date);

    /**
     * 根据搜索关键字过滤学员
     */
    @Select("<script>" +
            "SELECT u.user_id " +
            "FROM t_user u " +
            "LEFT JOIN t_camp_enrollment ce ON u.user_id = ce.user_id " +
            "LEFT JOIN t_class c ON ce.class_id = c.class_id " +
            "LEFT JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id " +
            "LEFT JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id " +
            "LEFT JOIN t_camp camp ON ce.camp_id = camp.camp_id " +
            "WHERE u.user_id IN " +
            "<foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
            "    #{userId}" +
            "</foreach>" +
            "AND (" +
            "    COALESCE(u.nickname, u.account) LIKE CONCAT('%', #{keyword}, '%') OR " +
            "    camp.name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "    c.name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "    bg.name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "    sg.name LIKE CONCAT('%', #{keyword}, '%') " +
            ")" +
            "</script>")
    List<Long> filterStudentsByKeyword(@Param("studentIds") List<Long> studentIds, @Param("keyword") String keyword);
}