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

        @Select("SELECT ds.target_type, ds.target_id, " +
                        "c.name as class_name, " +
                        "bg.name as big_group_name, " +
                        "sg.name as small_group_name " +
                        "FROM t_duty_assignment da " +
                        "JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
                        "JOIN t_camp camp ON da.camp_id = camp.camp_id " +
                        "LEFT JOIN t_class c ON ds.target_id = c.class_id AND ds.target_type = 'class' " +
                        "LEFT JOIN t_big_group bg ON ds.target_id = bg.big_group_id AND ds.target_type = 'big_group' " +
                        "LEFT JOIN t_small_group sg ON ds.target_id = sg.small_group_id AND ds.target_type = 'small_group' "
                        +
                        "WHERE da.user_id = #{userId} " +
                        "AND da.duty_type IN ('检组', '学组', '检委', '学委', '学班', '检班') " +
                        "AND da.end_time IS NULL " +
                        "AND camp.end_time > NOW() " +
                        "ORDER BY ds.target_type, ds.target_id")
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
                        "SELECT ce.user_id FROM t_camp_enrollment ce " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id AND bg.class_id = c.class_id " +
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id "
                        +
                        "WHERE ce.is_completed = 0 " +
                        "<choose>" +
                        "    <when test='type == \"class\"'>" +
                        "        AND ce.class_id = #{id} " +
                        "    </when>" +
                        "    <when test='type == \"bigGroup\" or type == \"big_group\"'>" +
                        "        AND ce.big_group_id = #{id} " +
                        "    </when>" +
                        "    <when test='type == \"smallGroup\" or type == \"small_group\"'>" +
                        "        AND ce.small_group_id = #{id} " +
                        "    </when>" +
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
                        "       ct.level_name as campName " +
                        "FROM t_homework h " +
                        "INNER JOIN t_user u ON h.user_id = u.user_id " +
                        "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
                        "INNER JOIN t_camp_type ct ON camp.type_id = ct.type_id " +
                        "INNER JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id AND bg.class_id = c.class_id " +
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id "
                        +
                        "WHERE ce.is_completed = 0 " +
                        "<choose>" +
                        "    <when test='type == \"class\"'>" +
                        "        AND ce.class_id = #{id} " +
                        "    </when>" +
                        "    <when test='type == \"bigGroup\" or type == \"big_group\"'>" +
                        "        AND ce.big_group_id = #{id} " +
                        "    </when>" +
                        "    <when test='type == \"smallGroup\" or type == \"small_group\"'>" +
                        "        AND ce.small_group_id = #{id} " +
                        "    </when>" +
                        "</choose>" +
                        "<if test='date != null and date != \"\"'> AND DATE(cp.plan_date) = #{date}</if>" +
                        "<if test='status == \"excellent\"'> AND (h.is_small_group_excellent = 1 OR h.is_big_group_excellent = 1)</if>"
                        +
                        "<if test='status == \"small_group_excellent\"'> AND h.is_small_group_excellent = 1</if>" +
                        "<if test='status == \"big_group_excellent\"'> AND h.is_big_group_excellent = 1</if>" +
                        "ORDER BY h.submit_time DESC" +
                        "</script>")
        List<Map<String, Object>> getHomeworkList(@Param("status") String status,
                        @Param("date") String date,
                        @Param("type") String type,
                        @Param("id") Integer id);

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
         * 根据作业ID获取学生ID
         */
        @Select("SELECT user_id FROM t_homework WHERE homework_id = #{homeworkId}")
        Long getStudentIdByHomeworkId(@Param("homeworkId") Integer homeworkId);

        /**
         * 根据作业ID获取小组ID
         */
        @Select("SELECT ce.small_group_id FROM t_homework h " +
                        "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
                        "INNER JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
                        "WHERE h.homework_id = #{homeworkId}")
        Integer getSmallGroupIdByHomeworkId(@Param("homeworkId") Integer homeworkId);

        /**
         * 根据作业ID获取大组ID
         */
        @Select("SELECT ce.big_group_id FROM t_homework h " +
                        "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
                        "INNER JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
                        "WHERE h.homework_id = #{homeworkId}")
        Integer getBigGroupIdByHomeworkId(@Param("homeworkId") Integer homeworkId);

        /**
         * 获取小组优秀作业数量
         */
        @Select("SELECT COUNT(*) FROM t_homework h " +
                        "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
                        "INNER JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
                        "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id " +
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id "
                        +
                        "WHERE ce.small_group_id = #{smallGroupId} " +
                        "AND h.is_small_group_excellent = 1")
        int getSmallGroupExcellentCount(@Param("smallGroupId") Integer smallGroupId);

        /**
         * 获取大组优秀作业数量
         */
        @Select("SELECT COUNT(*) FROM t_homework h " +
                        "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
                        "INNER JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id AND bg.class_id = c.class_id " +
                        "WHERE ce.big_group_id = #{bigGroupId} " +
                        "AND h.is_big_group_excellent = 1")
        int getBigGroupExcellentCount(@Param("bigGroupId") Integer bigGroupId);

        /**
         * 获取作业详情
         */
        @Select("SELECT h.homework_id as homeworkId, h.user_id as userId, h.content, h.submit_time as submitTime, " +
                        "h.is_small_group_excellent as isSmallGroupExcellent, " +
                        "h.is_big_group_excellent as isBigGroupExcellent, " +
                        "       COALESCE(u.nickname, u.account) as name, " +
                        "       c.name as className, bg.name as bigGroupName, sg.name as smallGroupName, " +
                        "       CONCAT('第', camp.term, '期', camp.name) as campName, " +
                        "       camp.camp_id as campId, cp.plan_id as planId " +
                        "FROM t_homework h " +
                        "INNER JOIN t_user u ON h.user_id = u.user_id " +
                        "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
                        "INNER JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id AND bg.class_id = c.class_id " +
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id "
                        +
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
         * 根据日期和营期ID获取计划ID
         */
        @Select("SELECT plan_id FROM t_camp_plan WHERE DATE(plan_date) = #{date} AND camp_id = #{campId} LIMIT 1")
        Integer getPlanIdByDateAndCamp(@Param("date") String date, @Param("campId") Integer campId);

        /**
         * 获取已交作业的学生名单
         */
        @Select("<script>" +
                        "SELECT COALESCE(u.nickname, u.account) as name, u.phone " +
                        "FROM t_homework h " +
                        "JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_user u ON h.user_id = u.user_id " +
                        "WHERE h.user_id IN " +
                        "<foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
                        "    #{userId}" +
                        "</foreach>" +
                        "AND DATE(cp.plan_date) = #{date} " +
                        "AND DATE(h.submit_time) = DATE(cp.plan_date) " +
                        "ORDER BY COALESCE(u.nickname, u.account)" +
                        "</script>")
        List<Map<String, Object>> getSubmittedHomeworkList(@Param("studentIds") List<Long> studentIds,
                        @Param("date") String date);

        /**
         * 获取迟交作业的学生名单
         */
        @Select("<script>" +
                        "SELECT COALESCE(u.nickname, u.account) as name, u.phone " +
                        "FROM t_homework h " +
                        "JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_user u ON h.user_id = u.user_id " +
                        "WHERE h.user_id IN " +
                        "<foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
                        "    #{userId}" +
                        "</foreach>" +
                        "AND DATE(cp.plan_date) = #{date} " +
                        "AND DATE(h.submit_time) > DATE(cp.plan_date) " +
                        "ORDER BY COALESCE(u.nickname, u.account)" +
                        "</script>")
        List<Map<String, Object>> getLateHomeworkList(@Param("studentIds") List<Long> studentIds,
                        @Param("date") String date);

        /**
         * 获取未交作业的学生名单
         */
        @Select("<script>" +
                        "SELECT DISTINCT COALESCE(u.nickname, u.account) as name, u.phone " +
                        "FROM t_user u " +
                        "JOIN t_camp_enrollment ce ON u.user_id = ce.user_id " +
                        "WHERE u.user_id IN " +
                        "<foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
                        "    #{userId}" +
                        "</foreach>" +
                        "AND ce.is_completed = 0 " +
                        "AND u.user_id NOT IN (" +
                        "    SELECT DISTINCT h.user_id FROM t_homework h " +
                        "    JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "    WHERE DATE(cp.plan_date) = #{date} " +
                        "    AND h.user_id IN " +
                        "    <foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
                        "        #{userId}" +
                        "    </foreach>" +
                        ")" +
                        "ORDER BY COALESCE(u.nickname, u.account)" +
                        "</script>")
        List<Map<String, Object>> getPendingHomeworkList(@Param("studentIds") List<Long> studentIds,
                        @Param("date") String date);

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
        @Select("SELECT sg.small_group_id as smallGroupId, sg.name as smallGroupName " +
                        "FROM t_small_group sg " +
                        "JOIN t_big_group bg ON sg.big_group_id = bg.big_group_id " +
                        "WHERE sg.big_group_id = #{bigGroupId} " +
                        "ORDER BY sg.name")
        List<Map<String, Object>> getSmallGroupsByBigGroup(@Param("bigGroupId") Integer bigGroupId);

        /**
         * 获取班级下的大组列表
         */
        @Select("SELECT bg.big_group_id as bigGroupId, bg.name as bigGroupName " +
                        "FROM t_big_group bg " +
                        "JOIN t_class c ON bg.class_id = c.class_id " +
                        "WHERE bg.class_id = #{classId} " +
                        "ORDER BY bg.name")
        List<Map<String, Object>> getBigGroupsByClass(@Param("classId") Integer classId);

        /**
         * 获取小组下的成员列表
         */
        @Select("SELECT u.user_id as userId, COALESCE(u.nickname, u.account) as name " +
                        "FROM t_camp_enrollment ce " +
                        "JOIN t_user u ON ce.user_id = u.user_id " +
                        "WHERE ce.small_group_id = #{smallGroupId} " +
                        "AND ce.is_completed = 0 " +
                        "ORDER BY COALESCE(u.nickname, u.account)")
        List<Map<String, Object>> getMembersBySmallGroup(@Param("smallGroupId") Integer smallGroupId);

        /**
         * 获取组内成员数量
         */
        @Select("<script>" +
                        "SELECT COUNT(DISTINCT ce.user_id) FROM t_camp_enrollment ce " +
                        "JOIN t_camp_plan cp ON ce.camp_id = cp.camp_id " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id AND bg.class_id = c.class_id " +
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id "
                        +
                        "WHERE ce.is_completed = 0 " +
                        "<choose>" +
                        "    <when test='groupType == \"class\"'>AND ce.class_id = #{groupId}</when>" +
                        "    <when test='groupType == \"bigGroup\"'>AND ce.big_group_id = #{groupId}</when>" +
                        "    <when test='groupType == \"smallGroup\"'>AND ce.small_group_id = #{groupId}</when>" +
                        "</choose>" +
                        "AND DATE(cp.plan_date) = #{date} " +
                        "</script>")
        Integer getMemberCountByGroup(@Param("groupType") String groupType, @Param("groupId") Integer groupId,
                        @Param("date") String date);

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
        Integer checkMemberExcellent(@Param("userId") Long userId, @Param("date") String date,
                        @Param("status") String status);

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

        List<MyHomeworkDTO> selectMyHomeworkList(@Param("userId") Long userId);

        List<ExcellentShowcaseDTO> selectExcellentShowcaseList();

        Integer selectHomeworkIdByUserAndPlan(@Param("userId") Long userId, @Param("planId") Integer planId);

        int updateHomeworkContent(@Param("homeworkId") Integer homeworkId, @Param("taskId") Integer taskId,
                        @Param("content") String content);

        int insertHomework(com.daily.dailychineseculture.entity.Homework homework);

        int countTotalPlansByCamp(@Param("campId") Integer campId);

        int countSubmittedHomeworkByUserAndCamp(@Param("userId") Long userId, @Param("campId") Integer campId);

        int countMissedConsecutiveDays(@Param("userId") Long userId, @Param("campId") Integer campId, @Param("threshold") int threshold);

        int countLateSubmissions(@Param("userId") Long userId, @Param("campId") Integer campId);

        int countMissedSubmissions(@Param("userId") Long userId, @Param("campId") Integer campId);
        /**
         * 根据营期ID获取营期信息
         */
        @Select("SELECT camp_id as campId, term, type_id as typeId FROM t_camp WHERE camp_id = #{campId}")
        Map<String, Object> getCampInfo(@Param("campId") Integer campId);

        /**
         * 根据计划ID获取排课计划信息
         */
        @Select("SELECT day_index as dayIndex FROM t_camp_plan WHERE plan_id = #{planId}")
        Map<String, Object> getCampPlanInfo(@Param("planId") Integer planId);

        /**
         * 根据作业ID获取班级ID
         */
        @Select("SELECT ce.class_id FROM t_homework h " +
                        "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
                        "INNER JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
                        "WHERE h.homework_id = #{homeworkId}")
        Integer getClassIdByHomeworkId(@Param("homeworkId") Integer homeworkId);

        /**
         * 根据小组ID获取小组群ID
         */
        @Select("SELECT chat_id FROM t_chat WHERE target_type = 'small_group' AND target_id = #{smallGroupId}")
        Integer getSmallGroupChatId(@Param("smallGroupId") Integer smallGroupId);

        /**
         * 根据大组ID获取大组群ID
         */
        @Select("SELECT chat_id FROM t_chat WHERE target_type = 'big_group' AND target_id = #{bigGroupId}")
        Integer getBigGroupChatId(@Param("bigGroupId") Integer bigGroupId);

        /**
         * 根据班级ID获取班级群ID
         */
        @Select("SELECT chat_id FROM t_chat WHERE target_type = 'class' AND target_id = #{classId}")
        Integer getClassGroupChatId(@Param("classId") Integer classId);

        /**
         * 获取小组的学组userId
         */
        @Select("SELECT da.user_id " +
                        "FROM t_duty_assignment da " +
                        "JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
                        "JOIN t_camp camp ON da.camp_id = camp.camp_id " +
                        "WHERE ds.target_type = 'small_group' " +
                        "AND ds.target_id = #{smallGroupId} " +
                        "AND da.duty_type = '学组' " +
                        "AND da.end_time IS NULL " +
                        "AND camp.end_time > NOW() " +
                        "LIMIT 1")
        Long getSmallGroupManagerUserId(@Param("smallGroupId") Integer smallGroupId);

        @Select("<script>" +
                        "SELECT COUNT(DISTINCT h.user_id) FROM t_homework h " +
                        "JOIN t_camp_enrollment ce ON h.user_id = ce.user_id " +
                        "JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id AND bg.class_id = c.class_id " +
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id "
                        +
                        "WHERE ce.is_completed = 0 " +
                        "<choose>" +
                        "    <when test='groupType == \"class\"'>AND ce.class_id = #{groupId}</when>" +
                        "    <when test='groupType == \"bigGroup\"'>AND ce.big_group_id = #{groupId}</when>" +
                        "    <when test='groupType == \"smallGroup\"'>AND ce.small_group_id = #{groupId}</when>" +
                        "</choose>" +
                        "AND DATE(cp.plan_date) = #{date} " +
                        "AND DATE(h.submit_time) = DATE(cp.plan_date) " +
                        "</script>")
        Integer getSubmittedCountByGroup(@Param("groupType") String groupType, @Param("groupId") Integer groupId,
                        @Param("date") String date);

        @Select("<script>" +
                        "SELECT COUNT(DISTINCT h.user_id) FROM t_homework h " +
                        "JOIN t_camp_enrollment ce ON h.user_id = ce.user_id " +
                        "JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id AND bg.class_id = c.class_id " +
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id "
                        +
                        "WHERE ce.is_completed = 0 " +
                        "<choose>" +
                        "    <when test='groupType == \"class\"'>AND ce.class_id = #{groupId}</when>" +
                        "    <when test='groupType == \"bigGroup\"'>AND ce.big_group_id = #{groupId}</when>" +
                        "    <when test='groupType == \"smallGroup\"'>AND ce.small_group_id = #{groupId}</when>" +
                        "</choose>" +
                        "AND DATE(cp.plan_date) = #{date} " +
                        "AND DATE(h.submit_time) > DATE(cp.plan_date) " +
                        "</script>")
        Integer getLateCountByGroup(@Param("groupType") String groupType, @Param("groupId") Integer groupId,
                        @Param("date") String date);

        @Select("<script>" +
                        "SELECT COUNT(DISTINCT h.user_id) FROM t_homework h " +
                        "JOIN t_camp_enrollment ce ON h.user_id = ce.user_id " +
                        "JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id AND bg.class_id = c.class_id " +
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id "
                        +
                        "WHERE ce.is_completed = 0 " +
                        "<choose>" +
                        "    <when test='groupType == \"class\"'>AND ce.class_id = #{groupId}</when>" +
                        "    <when test='groupType == \"bigGroup\"'>AND ce.big_group_id = #{groupId}</when>" +
                        "    <when test='groupType == \"smallGroup\"'>AND ce.small_group_id = #{groupId}</when>" +
                        "</choose>" +
                        "AND DATE(cp.plan_date) = #{date} " +
                        "</script>")
        Integer getCompletedCountByGroup(@Param("groupType") String groupType, @Param("groupId") Integer groupId,
                        @Param("date") String date);

        @Select("<script>" +
                        "SELECT COUNT(DISTINCT h.user_id) FROM t_homework h " +
                        "JOIN t_camp_enrollment ce ON h.user_id = ce.user_id " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id AND bg.class_id = c.class_id " +
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id "
                        +
                        "WHERE ce.is_completed = 0 " +
                        "<choose>" +
                        "    <when test='status == \"small_group_excellent\"'>AND h.is_small_group_excellent = 1</when>"
                        +
                        "    <when test='status == \"big_group_excellent\"'>AND h.is_big_group_excellent = 1</when>" +
                        "    <otherwise>AND (h.is_small_group_excellent = 1 OR h.is_big_group_excellent = 1)</otherwise>"
                        +
                        "</choose>" +
                        "<choose>" +
                        "    <when test='groupType == \"class\"'>AND ce.class_id = #{groupId}</when>" +
                        "    <when test='groupType == \"bigGroup\"'>AND ce.big_group_id = #{groupId}</when>" +
                        "    <when test='groupType == \"smallGroup\"'>AND ce.small_group_id = #{groupId}</when>" +
                        "</choose>" +
                        "<if test='date != null and date != \"\"'> AND DATE(h.submit_time) = #{date}</if>" +
                        "</script>")
        Integer getExcellentCountByGroup(@Param("groupType") String groupType, @Param("groupId") Integer groupId,
                        @Param("date") String date, @Param("status") String status);

        /**
         * 获取用户的小组信息
         */
        @Select("SELECT ce.camp_id as campId, ce.class_id as classId, ce.big_group_id as bigGroupId, ce.small_group_id as smallGroupId, COALESCE(u.nickname, u.account) as name "
                        +
                        "FROM t_camp_enrollment ce " +
                        "INNER JOIN t_user u ON ce.user_id = u.user_id " +
                        "INNER JOIN t_camp camp ON ce.camp_id = camp.camp_id " +
                        "WHERE ce.user_id = #{userId} " +
                        "AND ce.is_completed = 0 " +
                        "AND camp.end_time > NOW() " +
                        "LIMIT 1")
        Map<String, Object> getUserGroupInfo(@Param("userId") Long userId);

        /**
         * 根据用户ID和计划ID获取作业信息
         */
        @Select("SELECT h.content " +
                        "FROM t_homework h " +
                        "WHERE h.user_id = #{userId} " +
                        "AND h.plan_id = #{planId} " +
                        "LIMIT 1")
        Map<String, Object> getHomeworkInfoByUserAndPlan(@Param("userId") Long userId, @Param("planId") Integer planId);

        /**
         * 根据计划ID获取营期信息
         */
        @Select("SELECT c.camp_id as campId, c.term, c.name, ct.level_name as levelName FROM t_camp c JOIN t_camp_plan cp ON c.camp_id = cp.camp_id JOIN t_camp_type ct ON c.type_id = ct.type_id WHERE cp.plan_id = #{planId}")
        Map<String, Object> getCampInfoByPlanId(@Param("planId") Integer planId);

        /**
         * 根据作业ID获取计划ID
         */
        @Select("SELECT plan_id FROM t_homework WHERE homework_id = #{homeworkId}")
        Integer getPlanIdByHomeworkId(@Param("homeworkId") Integer homeworkId);

        /**
         * 获取用户信息
         */
        @Select("SELECT region FROM t_user WHERE user_id = #{userId}")
        Map<String, Object> getUserInfo(@Param("userId") Long userId);

        /**
         * 获取排课计划日期
         */
        @Select("SELECT plan_date as planDate FROM t_camp_plan WHERE plan_id = #{planId}")
        Map<String, Object> getCampPlanDate(@Param("planId") Integer planId);
}