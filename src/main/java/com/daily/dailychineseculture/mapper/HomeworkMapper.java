package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.dto.MyHomeworkDTO;
import com.daily.dailychineseculture.dto.ExcellentShowcaseDTO;
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
            "AND ds.target_type = #{type} " +
            "AND ds.target_id = #{id}")
    Integer checkVolunteerAuth(@Param("userId") Long userId,
                               @Param("type") String type,
                               @Param("id") Integer id);

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
            "SELECT h.homework_id as homeworkId, h.user_id as userId, " +
            "       COALESCE(u.nickname, u.account, CONCAT('学员', h.user_id)) as name, " +
            "       h.is_small_group_excellent as isSmallGroupExcellent, " +
            "       h.is_big_group_excellent as isBigGroupExcellent, " +
            "       h.submit_time as submitTime, " +
            "       c.name as className, " +
            "       bg.name as bigGroupName, " +
            "       sg.name as smallGroupName, " +
            "       camp.name as campName " +
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
            "    FROM t_homework h " +
            "    JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
            "    WHERE user_id IN " +
            "    <foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
            "        #{userId}" +
            "    </foreach>" +
            "    <if test='date != null and date != \"\"'> AND DATE(cp.plan_date) = #{date}</if>" +
            "    GROUP BY user_id, h.plan_id " +  // 明确指定使用 h.plan_id
            ")" +
            "<if test='status == \"excellent\"'> AND (h.is_small_group_excellent = 1 OR h.is_big_group_excellent = 1)</if>" +
            "<if test='status == \"small_group_excellent\"'> AND h.is_small_group_excellent = 1</if>" +
            "<if test='status == \"big_group_excellent\"'> AND h.is_big_group_excellent = 1</if>" +
            "ORDER BY h.submit_time DESC" +
            "</script>")
    List<Map<String, Object>> getHomeworkList(@Param("studentIds") List<Long> studentIds,
                                              @Param("status") String status,
                                              @Param("date") String date);

    /**
     * 标记小组优秀作业
     */
    @Update("UPDATE t_homework SET is_small_group_excellent = #{isSmallGroupExcellent} WHERE homework_id = #{homeworkId}")
    int markSmallGroupExcellent(@Param("homeworkId") Integer homeworkId,
                                @Param("isSmallGroupExcellent") Integer isSmallGroupExcellent);

    /**
     * 标记大组优秀作业
     */
    @Update("UPDATE t_homework SET is_big_group_excellent = #{isBigGroupExcellent} WHERE homework_id = #{homeworkId}")
    int markBigGroupExcellent(@Param("homeworkId") Integer homeworkId,
                              @Param("isBigGroupExcellent") Integer isBigGroupExcellent);

    /**
     * 检查作业是否为小组优秀
     */
    @Select("SELECT is_small_group_excellent FROM t_homework WHERE homework_id = #{homeworkId}")
    Integer checkSmallGroupExcellent(@Param("homeworkId") Integer homeworkId);

    /**
     * 检查作业是否为大组优秀
     */
    @Select("SELECT is_big_group_excellent FROM t_homework WHERE homework_id = #{homeworkId}")
    Integer checkBigGroupExcellent(@Param("homeworkId") Integer homeworkId);

    /**
     * 根据作业ID获取小组ID
     */
    @Select("SELECT ce.small_group_id FROM t_homework h " +
            "LEFT JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
            "LEFT JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
            "LEFT JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
            "WHERE h.homework_id = #{homeworkId}")
    Integer getSmallGroupIdByHomeworkId(@Param("homeworkId") Integer homeworkId);

    /**
     * 根据作业ID获取大组ID
     */
    @Select("SELECT ce.big_group_id FROM t_homework h " +
            "LEFT JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
            "LEFT JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
            "LEFT JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
            "WHERE h.homework_id = #{homeworkId}")
    Integer getBigGroupIdByHomeworkId(@Param("homeworkId") Integer homeworkId);

    /**
     * 获取小组优秀作业数量
     */
    @Select("SELECT COUNT(*) FROM t_homework h " +
            "LEFT JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
            "LEFT JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
            "LEFT JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
            "WHERE ce.small_group_id = #{smallGroupId} " +
            "AND h.is_small_group_excellent = 1")
    int getSmallGroupExcellentCount(@Param("smallGroupId") Integer smallGroupId);

    /**
     * 获取大组优秀作业数量
     */
    @Select("SELECT COUNT(*) FROM t_homework h " +
            "LEFT JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
            "LEFT JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
            "LEFT JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
            "WHERE ce.big_group_id = #{bigGroupId} " +
            "AND h.is_big_group_excellent = 1")
    int getBigGroupExcellentCount(@Param("bigGroupId") Integer bigGroupId);

    /**
     * 获取作业详情
     */
    @Select("SELECT h.homework_id as homeworkId, h.user_id as userId, h.content, h.submit_time as submitTime, " +
            "h.is_small_group_excellent as isSmallGroupExcellent, " +
            "h.is_big_group_excellent as isBigGroupExcellent, " +
            "       COALESCE(u.nickname, u.account) as name, " +  // 优先使用昵称，昵称空则使用账户名
            "       c.name as className, bg.name as bigGroupName, sg.name as smallGroupName, " +
            "       camp.name as campName " +
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
            "JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
            "WHERE h.user_id IN " +
            "<foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
            "    #{userId}" +
            "</foreach>" +
            "<if test='date != null and date != \"\"'> AND DATE(cp.plan_date) = #{date}</if>" +
            "</script>")
    int getCompletedHomeworkCount(@Param("studentIds") List<Long> studentIds, @Param("date") String date);

    /**
     * 获取指定日期的营期计划
     */
    @Select("SELECT plan_id FROM t_camp_plan WHERE DATE(plan_date) = #{date} LIMIT 1")
    Integer getPlanIdByDate(@Param("date") String date);

    /**
     * 获取已交作业的学生名单
     */
    @Select("<script>" +
            "SELECT COALESCE(u.nickname, u.account) as name, u.phone " +
            "FROM t_homework h " +
            "JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
            "LEFT JOIN t_user u ON h.user_id = u.user_id " +
            "WHERE h.user_id IN " +
            "<foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
            "    #{userId}" +
            "</foreach>" +
            "AND DATE(cp.plan_date) = #{date} " +
            "AND DATE(h.submit_time) = DATE(cp.plan_date) " +
            "ORDER BY COALESCE(u.nickname, u.account)" +
            "</script>")
    List<Map<String, Object>> getSubmittedHomeworkList(@Param("studentIds") List<Long> studentIds, @Param("date") String date);

    /**
     * 获取迟交作业的学生名单
     */
    @Select("<script>" +
            "SELECT COALESCE(u.nickname, u.account) as name, u.phone " +
            "FROM t_homework h " +
            "JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
            "LEFT JOIN t_user u ON h.user_id = u.user_id " +
            "WHERE h.user_id IN " +
            "<foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
            "    #{userId}" +
            "</foreach>" +
            "AND DATE(cp.plan_date) = #{date} " +
            "AND DATE(h.submit_time) > DATE(cp.plan_date) " +
            "ORDER BY COALESCE(u.nickname, u.account)" +
            "</script>")
    List<Map<String, Object>> getLateHomeworkList(@Param("studentIds") List<Long> studentIds, @Param("date") String date);

    /**
     * 获取未交作业的学生名单
     */
    @Select("<script>" +
            "SELECT COALESCE(u.nickname, u.account) as name, u.phone " +
            "FROM t_user u " +
            "WHERE u.user_id IN " +
            "<foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
            "    #{userId}" +
            "</foreach>" +
            "AND u.user_id NOT IN (" +
            "    SELECT DISTINCT h.user_id FROM t_homework h " +
            "    JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
            "    WHERE DATE(cp.plan_date) = #{date}" +
            ")" +
            "ORDER BY COALESCE(u.nickname, u.account)" +
            "</script>")
    List<Map<String, Object>> getPendingHomeworkList(@Param("studentIds") List<Long> studentIds, @Param("date") String date);


    /**
     * 根据小组ID获取大组ID
     */
    @Select("SELECT big_group_id as bigGroupId FROM t_small_group WHERE small_group_id = #{smallGroupId}")
    Map<String, Object> getBigGroupIdBySmallGroupId(@Param("smallGroupId") Integer smallGroupId);
    /**
     * 根据大组ID获取班级ID
     */
    @Select("SELECT class_id as classId FROM t_big_group WHERE big_group_id = #{bigGroupId}")
    Map<String, Object> getClassIdByBigGroupId(@Param("bigGroupId") Integer bigGroupId);

    /**
     * 获取大组下的小组列表
     */
    @Select("SELECT small_group_id as smallGroupId, name as smallGroupName " +
            "FROM t_small_group " +
            "WHERE big_group_id = #{bigGroupId} " +
            "ORDER BY name")
    List<Map<String, Object>> getSmallGroupsByBigGroup(@Param("bigGroupId") Integer bigGroupId);

    /**
     * 获取班级下的大组列表
     */
    @Select("SELECT big_group_id as bigGroupId, name as bigGroupName " +
            "FROM t_big_group " +
            "WHERE class_id = #{classId} " +
            "ORDER BY name")
    List<Map<String, Object>> getBigGroupsByClass(@Param("classId") Integer classId);

    /**
     * 获取小组下的成员列表
     */
    @Select("SELECT u.user_id as userId, COALESCE(u.nickname, u.account) as name " +
            "FROM t_camp_enrollment ce " +
            "JOIN t_user u ON ce.user_id = u.user_id " +
            "WHERE ce.small_group_id = #{smallGroupId} " +
            "ORDER BY COALESCE(u.nickname, u.account)")
    List<Map<String, Object>> getMembersBySmallGroup(@Param("smallGroupId") Integer smallGroupId);

    /**
     * 获取组内成员数量
     */
    @Select("<script>" +
            "SELECT COUNT(DISTINCT ce.user_id) FROM t_camp_enrollment ce " +
            "JOIN t_camp_plan cp ON ce.camp_id = cp.camp_id " +
            "WHERE " +
            "<choose>" +
            "    <when test='groupType == \"class\"'>ce.class_id = #{groupId}</when>" +
            "    <when test='groupType == \"bigGroup\"'>ce.big_group_id = #{groupId}</when>" +
            "    <when test='groupType == \"smallGroup\"'>ce.small_group_id = #{groupId}</when>" +
            "</choose>" +
            "AND DATE(cp.plan_date) = #{date} " +
            "</script>")
    Integer getMemberCountByGroup(@Param("groupType") String groupType, @Param("groupId") Integer groupId, @Param("date") String date);

    /**
     * 获取组内已提交作业数量
     */
    @Select("<script>" +
            "SELECT COUNT(DISTINCT h.user_id) FROM t_homework h " +
            "JOIN t_camp_enrollment ce ON h.user_id = ce.user_id " +
            "JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
            "WHERE " +
            "<choose>" +
            "    <when test='groupType == \"class\"'>ce.class_id = #{groupId}</when>" +
            "    <when test='groupType == \"bigGroup\"'>ce.big_group_id = #{groupId}</when>" +
            "    <when test='groupType == \"smallGroup\"'>ce.small_group_id = #{groupId}</when>" +
            "</choose>" +
            "AND DATE(cp.plan_date) = #{date} " +
            "AND DATE(h.submit_time) = DATE(cp.plan_date) " +
            "</script>")
    Integer getSubmittedCountByGroup(@Param("groupType") String groupType, @Param("groupId") Integer groupId, @Param("date") String date);
    /**
     * 获取组内优秀作业数量
     */
    @Select("<script>" +
            "SELECT COUNT(DISTINCT h.user_id) FROM t_homework h " +
            "JOIN t_camp_enrollment ce ON h.user_id = ce.user_id " +
            "WHERE " +
            "<choose>" +
            "    <when test='status == \"small_group_excellent\"'>h.is_small_group_excellent = 1</when>" +
            "    <when test='status == \"big_group_excellent\"'>h.is_big_group_excellent = 1</when>" +
            "    <otherwise>(h.is_small_group_excellent = 1 OR h.is_big_group_excellent = 1)</otherwise>" +
            "</choose>" +
            "<choose>" +
            "    <when test='groupType == \"class\"'>ce.class_id = #{groupId}</when>" +
            "    <when test='groupType == \"bigGroup\"'>ce.big_group_id = #{groupId}</when>" +
            "    <when test='groupType == \"smallGroup\"'>ce.small_group_id = #{groupId}</when>" +
            "</choose>" +
            "<if test='date != null and date != \"\"'> AND DATE(h.submit_time) = #{date}</if>" +
            "</script>")
    Integer getExcellentCountByGroup(@Param("groupType") String groupType, @Param("groupId") Integer groupId,
                                     @Param("date") String date, @Param("status") String status);

    /**
     * 检查成员是否已提交作业
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_homework " +
            "WHERE user_id = #{userId} " +
            "<if test='date != null and date != \"\"'> AND DATE(submit_time) = #{date}</if>" +
            "</script>")
    Integer checkMemberSubmitted(@Param("userId") Long userId, @Param("date") String date);

    /**
     * 检查成员是否有优秀作业
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_homework " +
            "WHERE user_id = #{userId} " +
            "AND " +
            "<choose>" +
            "    <when test='status == \"small_group_excellent\"'>is_small_group_excellent = 1</when>" +
            "    <when test='status == \"big_group_excellent\"'>is_big_group_excellent = 1</when>" +
            "    <otherwise>(is_small_group_excellent = 1 OR is_big_group_excellent = 1)</otherwise>" +
            "</choose>" +
            "<if test='date != null and date != \"\"'> AND DATE(submit_time) = #{date}</if>" +
            "</script>")
    Integer checkMemberExcellent(@Param("userId") Long userId, @Param("date") String date, @Param("status") String status);

    /**
     * 获取班级信息
     */
    @Select("SELECT class_id as classId, name FROM t_class WHERE class_id = #{classId}")
    Map<String, Object> getClassInfo(@Param("classId") Integer classId);

    /**
     * 获取大组信息
     */
    @Select("SELECT big_group_id as bigGroupId, name FROM t_big_group WHERE big_group_id = #{bigGroupId}")
    Map<String, Object> getBigGroupInfo(@Param("bigGroupId") Integer bigGroupId);

    /**
     * 获取小组信息
     */
    @Select("SELECT small_group_id as smallGroupId, name FROM t_small_group WHERE small_group_id = #{smallGroupId}")
    Map<String, Object> getSmallGroupInfo(@Param("smallGroupId") Integer smallGroupId);

    /**
     * 获取组内迟交作业数量
     */
    @Select("<script>" +
            "SELECT COUNT(DISTINCT h.user_id) FROM t_homework h " +
            "JOIN t_camp_enrollment ce ON h.user_id = ce.user_id " +
            "JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
            "WHERE " +
            "<choose>" +
            "    <when test='groupType == \"class\"'>ce.class_id = #{groupId}</when>" +
            "    <when test='groupType == \"bigGroup\"'>ce.big_group_id = #{groupId}</when>" +
            "    <when test='groupType == \"smallGroup\"'>ce.small_group_id = #{groupId}</when>" +
            "</choose>" +
            "AND DATE(cp.plan_date) = #{date} " +
            "AND DATE(h.submit_time) > DATE(cp.plan_date) " +
            "</script>")
    Integer getLateCountByGroup(@Param("groupType") String groupType, @Param("groupId") Integer groupId, @Param("date") String date);

    List<MyHomeworkDTO> selectMyHomeworkList(@Param("userId") Long userId);

    List<ExcellentShowcaseDTO> selectExcellentShowcaseList();
}