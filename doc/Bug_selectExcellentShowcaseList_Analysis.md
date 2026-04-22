# Bug 分析报告：selectExcellentShowcaseList 报错

## 错误信息

```
Invalid bound statement (not found): com.daily.dailychineseculture.mapper.HomeworkMapper.selectExcellentShowcaseList
```

## 错误位置

- **报错接口**：`com.daily.dailychineseculture.mapper.HomeworkMapper.selectExcellentShowcaseList`
- **触发位置**：`HomeworkServiceImpl.getExcellentShowcasePage()` 第 949 行

---

## 一、Mapper 接口 (Java)

**完整路径**：`src/main/java/com/daily/dailychineseculture/mapper/HomeworkMapper.java`

```java
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
                        "LEFT JOIN t_small_group sg ON ds.target_id = sg.small_group_id AND ds.target_type = 'small_group' " +
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
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id " +
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
                "       CONCAT('第', camp.term, '期', ct.level_name) as campName " +
                "FROM t_homework h " +
                "INNER JOIN t_user u ON h.user_id = u.user_id " +
                "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                "INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
                "INNER JOIN t_camp_type ct ON camp.type_id = ct.type_id " +
                "INNER JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
                "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id AND bg.class_id = c.class_id " +
                "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id " +
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
                "<if test='status == \"excellent\"'> AND (h.is_small_group_excellent = 1 OR h.is_big_group_excellent = 1)</if>" +
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
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id " +
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
                "       CONCAT('第', camp.term, '期', ct.level_name) as campName, " +
                "       camp.camp_id as campId, cp.plan_id as planId " +
                "FROM t_homework h " +
                "INNER JOIN t_user u ON h.user_id = u.user_id " +
                "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                "INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
                "INNER JOIN t_camp_type ct ON camp.type_id = ct.type_id " +
                "INNER JOIN t_camp_enrollment ce ON h.user_id = ce.user_id AND ce.camp_id = camp.camp_id " +
                "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id AND bg.class_id = c.class_id " +
                "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id " +
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
                        "SELECT COALESCE(u.nickname, u.account) as name, u.phone " +
                        "FROM t_camp_enrollment ce " +
                        "INNER JOIN t_user u ON ce.user_id = u.user_id " +
                        "WHERE ce.user_id IN " +
                        "<foreach item='userId' collection='studentIds' open='(' separator=',' close=')'>" +
                        "    #{userId}" +
                        "</foreach>" +
                        "AND ce.user_id NOT IN (SELECT DISTINCT h.user_id FROM t_homework h " +
                        "    JOIN t_camp_plan cp ON h.plan_id = cp.plan_id WHERE DATE(cp.plan_date) = #{date}) " +
                        "ORDER BY COALESCE(u.nickname, u.account)" +
                        "</script>")
        List<Map<String, Object>> getNotSubmittedHomeworkList(@Param("studentIds") List<Long> studentIds,
                        @Param("date") String date);

        /**
         * 获取小组优秀作业列表
         */
        @Select("<script>" +
                        "SELECT h.homework_id as homeworkId, h.user_id as userId, h.content, h.submit_time as submitTime, " +
                        "       COALESCE(u.nickname, u.account) as name, " +
                        "       sg.name as smallGroupName, bg.name as bigGroupName, c.name as className " +
                        "FROM t_homework h " +
                        "INNER JOIN t_camp_enrollment ce ON h.user_id = ce.user_id " +
                        "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_user u ON h.user_id = u.user_id " +
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id " +
                        "INNER JOIN t_big_group bg ON sg.big_group_id = bg.big_group_id " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "WHERE ce.small_group_id = #{smallGroupId} " +
                        "AND h.is_small_group_excellent = 1 " +
                        "<if test='date != null and date != \"\"'> AND DATE(cp.plan_date) = #{date}</if>" +
                        "ORDER BY h.submit_time DESC" +
                        "</script>")
        List<Map<String, Object>> getSmallGroupExcellentHomeworkList(@Param("smallGroupId") Integer smallGroupId,
                        @Param("date") String date);

        /**
         * 获取大组优秀作业列表
         */
        @Select("<script>" +
                        "SELECT h.homework_id as homeworkId, h.user_id as userId, h.content, h.submit_time as submitTime, " +
                        "       COALESCE(u.nickname, u.account) as name, " +
                        "       bg.name as bigGroupName, c.name as className " +
                        "FROM t_homework h " +
                        "INNER JOIN t_camp_enrollment ce ON h.user_id = ce.user_id " +
                        "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_user u ON h.user_id = u.user_id " +
                        "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "WHERE ce.big_group_id = #{bigGroupId} " +
                        "AND h.is_big_group_excellent = 1 " +
                        "<if test='date != null and date != \"\"'> AND DATE(cp.plan_date) = #{date}</if>" +
                        "ORDER BY h.submit_time DESC" +
                        "</script>")
        List<Map<String, Object>> getBigGroupExcellentHomeworkList(@Param("bigGroupId") Integer bigGroupId,
                        @Param("date") String date);

        /**
         * 获取班级优秀作业列表
         */
        @Select("<script>" +
                        "SELECT h.homework_id as homeworkId, h.user_id as userId, h.content, h.submit_time as submitTime, " +
                        "       COALESCE(u.nickname, u.account) as name, " +
                        "       c.name as className, " +
                        "       CONCAT('第', camp.term, '期', ct.level_name) as campName " +
                        "FROM t_homework h " +
                        "INNER JOIN t_camp_enrollment ce ON h.user_id = ce.user_id " +
                        "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
                        "INNER JOIN t_camp_type ct ON camp.type_id = ct.type_id " +
                        "INNER JOIN t_user u ON h.user_id = u.user_id " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "WHERE ce.class_id = #{classId} " +
                        "AND (h.is_small_group_excellent = 1 OR h.is_big_group_excellent = 1) " +
                        "<if test='date != null and date != \"\"'> AND DATE(cp.plan_date) = #{date}</if>" +
                        "ORDER BY h.submit_time DESC" +
                        "</script>")
        List<Map<String, Object>> getClassExcellentHomeworkList(@Param("classId") Integer classId,
                        @Param("date") String date);

        /**
         * 获取班级大组优秀作业列表
         */
        @Select("<script>" +
                        "SELECT h.homework_id as homeworkId, h.user_id as userId, h.content, h.submit_time as submitTime, " +
                        "       COALESCE(u.nickname, u.account) as name, " +
                        "       c.name as className, " +
                        "       CONCAT('第', camp.term, '期', ct.level_name) as campName " +
                        "FROM t_homework h " +
                        "INNER JOIN t_camp_enrollment ce ON h.user_id = ce.user_id " +
                        "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
                        "INNER JOIN t_camp_type ct ON camp.type_id = ct.type_id " +
                        "INNER JOIN t_user u ON h.user_id = u.user_id " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "WHERE ce.class_id = #{classId} " +
                        "AND h.is_big_group_excellent = 1 " +
                        "<if test='date != null and date != \"\"'> AND DATE(cp.plan_date) = #{date}</if>" +
                        "ORDER BY h.submit_time DESC" +
                        "</script>")
        List<Map<String, Object>> getClassBigGroupExcellentHomeworkList(@Param("classId") Integer classId,
                        @Param("date") String date);

        /**
         * 获取班级小组优秀作业列表
         */
        @Select("<script>" +
                        "SELECT h.homework_id as homeworkId, h.user_id as userId, h.content, h.submit_time as submitTime, " +
                        "       COALESCE(u.nickname, u.account) as name, " +
                        "       c.name as className, " +
                        "       CONCAT('第', camp.term, '期', ct.level_name) as campName " +
                        "FROM t_homework h " +
                        "INNER JOIN t_camp_enrollment ce ON h.user_id = ce.user_id " +
                        "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
                        "INNER JOIN t_camp_type ct ON camp.type_id = ct.type_id " +
                        "INNER JOIN t_user u ON h.user_id = u.user_id " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "WHERE ce.class_id = #{classId} " +
                        "AND h.is_small_group_excellent = 1 " +
                        "<if test='date != null and date != \"\"'> AND DATE(cp.plan_date) = #{date}</if>" +
                        "ORDER BY h.submit_time DESC" +
                        "</script>")
        List<Map<String, Object>> getClassSmallGroupExcellentHomeworkList(@Param("classId") Integer classId,
                        @Param("date") String date);

        /**
         * 获取成员的作业列表
         */
        @Select("<script>" +
                        "SELECT h.homework_id as homeworkId, h.user_id as userId, h.content, h.submit_time as submitTime, " +
                        "       h.is_small_group_excellent as isSmallGroupExcellent, " +
                        "       h.is_big_group_excellent as isBigGroupExcellent, " +
                        "       cp.plan_date as planDate, " +
                        "       CONCAT('第', camp.term, '期', ct.level_name) as campName " +
                        "FROM t_homework h " +
                        "INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id " +
                        "INNER JOIN t_camp_type ct ON camp.type_id = ct.type_id " +
                        "WHERE h.user_id = #{userId} " +
                        "<if test='date != null and date != \"\"'> AND DATE(cp.plan_date) = #{date}</if>" +
                        "ORDER BY cp.plan_date DESC" +
                        "</script>")
        List<Map<String, Object>> getMemberHomeworkList(@Param("userId") Long userId, @Param("date") String date);

        /**
         * 获取班级已交作业学生数
         */
        @Select("<script>" +
                        "SELECT COUNT(DISTINCT h.user_id) FROM t_homework h " +
                        "JOIN t_camp_enrollment ce ON h.user_id = ce.user_id " +
                        "JOIN t_camp_plan cp ON h.plan_id = cp.plan_id " +
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id AND bg.class_id = c.class_id " +
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id " +
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
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id " +
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
                        "INNER JOIN t_class c ON ce.class_id = c.class_id " +
                        "INNER JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id AND bg.class_id = c.class_id " +
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id " +
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
                        "INNER JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id AND sg.big_group_id = bg.big_group_id " +
                        "WHERE ce.is_completed = 0 " +
                        "<choose>" +
                        "    <when test='status == \"small_group_excellent\"'>AND h.is_small_group_excellent = 1</when>" +
                        "    <when test='status == \"big_group_excellent\"'>AND h.is_big_group_excellent = 1</when>" +
                        "    <otherwise>AND (h.is_small_group_excellent = 1 OR h.is_big_group_excellent = 1)</otherwise>" +
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
        @Select("SELECT ce.camp_id as campId, ce.class_id as classId, ce.big_group_id as bigGroupId, ce.small_group_id as smallGroupId, COALESCE(u.nickname, u.account) as name " +
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

        List<MyHomeworkDTO> selectMyHomeworkList(@Param("userId") Long userId);

        // ========== 问题方法 ==========
        // 【第451行】声明了 selectExcellentShowcaseList() 方法，但没有任何 @Select 注解或 XML SQL 映射
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

        /**
         * 获取成员是否已提交作业
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
                        "    <otherwise>(is_small_group_excellent = 1 OR h.is_big_group_excellent = 1)</otherwise>" +
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
}
```

---

## 二、Mapper XML 映射文件

**完整路径**：`src/main/resources/mapper/HomeworkMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.daily.dailychineseculture.mapper.HomeworkMapper">

    <select id="countTotalPlansByCamp" resultType="int">
        SELECT COUNT(*)
        FROM t_camp_plan
        WHERE camp_id = #{campId}
    </select>

    <select id="countSubmittedHomeworkByUserAndCamp" resultType="int">
        SELECT COUNT(DISTINCT h.plan_id)
        FROM t_homework h
        JOIN t_camp_plan cp ON h.plan_id = cp.plan_id
        WHERE h.user_id = #{userId}
          AND cp.camp_id = #{campId}
    </select>

    <select id="countMissedConsecutiveDays" resultType="int">
        SELECT COUNT(DISTINCT cp.plan_date)
        FROM t_camp_plan cp
        LEFT JOIN t_homework h
            ON h.plan_id = cp.plan_id
            AND h.user_id = #{userId}
        WHERE cp.camp_id = #{campId}
          AND cp.plan_date &lt; CURDATE()
          AND h.homework_id IS NULL
        ORDER BY cp.plan_date DESC
        LIMIT #{threshold}
    </select>

    <select id="countLateSubmissions" resultType="int">
        SELECT COUNT(DISTINCT cp.plan_id)
        FROM t_camp_plan cp
        JOIN t_homework h ON h.plan_id = cp.plan_id AND h.user_id = #{userId}
        WHERE cp.camp_id = #{campId}
          AND cp.plan_date &lt; CURDATE()
          AND TIME(h.submit_time) > '18:30:00'
    </select>

    <select id="countMissedSubmissions" resultType="int">
        SELECT COUNT(DISTINCT cp.plan_id)
        FROM t_camp_plan cp
        LEFT JOIN t_homework h
            ON h.plan_id = cp.plan_id
            AND h.user_id = #{userId}
        WHERE cp.camp_id = #{campId}
          AND cp.plan_date &lt; CURDATE()
          AND h.homework_id IS NULL
    </select>

    <select id="selectHomeworkIdByUserAndPlan" resultType="int">
        SELECT homework_id
        FROM t_homework
        WHERE user_id = #{userId}
        AND plan_id = #{planId}
        LIMIT 1
    </select>

    <insert id="insertHomework" parameterType="com.daily.dailychineseculture.entity.Homework">
        INSERT INTO t_homework (user_id, plan_id, content, submit_time, is_small_group_excellent, is_big_group_excellent)
        VALUES (#{userId}, #{planId}, #{content}, #{submitTime}, #{isSmallGroupExcellent}, #{isBigGroupExcellent})
    </insert>

</mapper>
```

---

## 三、application.yml 配置文件

**完整路径**：`src/main/resources/application.yml`

```yaml
# src/main/resources/application.yml

server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://47.120.31.133:3306/camp_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false&allowMultiQueries=true
    username: root
    password: DailyChineseCultureCODE123.
    driver-class-name: com.mysql.cj.jdbc.Driver
  servlet:
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB

mybatis:
  # 开启驼峰命名转换 (数据库下划线字段自动映射到 Java 驼峰属性)
  configuration:
    map-underscore-to-camel-case: true
  # Mapper XML 文件位置
  mapper-locations: classpath:mapper/*.xml

# 微信小程序配置
wx:
  appid: wx58b4d74c673f584c
  secret: 1ffaef2d4f60b39e18dea3e19f0c924b

# 文件上传配置
file:
  upload-dir: ./uploads/
  max-size: 524288000
```

---

## 四、业务逻辑层 (Service)

**完整路径**：`src/main/java/com/daily/dailychineseculture/service/impl/HomeworkServiceImpl.java`

**调用关键片段**（第 946-955 行）：

```java
@Override
public ExcellentShowcasePageDTO getExcellentShowcasePage(Integer page, Integer size) {
    PageHelper.startPage(page, size);
    List<ExcellentShowcaseDTO> list = homeworkMapper.selectExcellentShowcaseList();  // 【第949行 - 报错位置】
    PageInfo<ExcellentShowcaseDTO> pageInfo = new PageInfo<>(list);
    ExcellentShowcasePageDTO result = new ExcellentShowcasePageDTO();
    result.setTotal(pageInfo.getTotal());
    result.setList(pageInfo.getList());
    return result;
}
```

---

## 五、DTO 实体类

**完整路径**：`src/main/java/com/daily/dailychineseculture/dto/ExcellentShowcaseDTO.java`

```java
package com.daily.dailychineseculture.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExcellentShowcaseDTO {
    private Integer homeworkId;
    private String authorName;
    private String avatar;
    private String campName;
    private String planTitle;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime submitTime;
}
```

---

## 六、架构诊断

### 根本原因分析

**"Invalid bound statement (not found)"** 错误的产生必须满足以下 **四个条件**：

| 条件 | 检查项 | Java 接口 | XML 映射 | 状态 |
|------|--------|-----------|----------|------|
| 1 | namespace 匹配 | `com.daily.dailychineseculture.mapper.HomeworkMapper` | `<mapper namespace="...">` | ✅ 匹配 |
| 2 | 方法名匹配 | `selectExcellentShowcaseList()` | `<select id="...">` | ❌ **XML 中不存在** |
| 3 | mapper-locations 扫描正确 | - | `classpath:mapper/*.xml` | ✅ 正确 |
| 4 | SQL 定义完整 | - | 需包含完整 SELECT 语句 | ❌ **缺失** |

### 核心问题

**`selectExcellentShowcaseList()` 方法只在 Java 接口中声明（第 451 行），但：**

1. **没有 @Select 注解**
2. **没有 @Insert/@Update/@Delete 注解**
3. **XML 文件中也没有对应 id 的 `<select>` 定义**

### XML 中存在的 id 列表

```xml
<!-- HomeworkMapper.xml 中实际存在的 SQL 映射 -->
countTotalPlansByCamp               ✅ 存在
countSubmittedHomeworkByUserAndCamp  ✅ 存在
countMissedConsecutiveDays           ✅ 存在
countLateSubmissions                 ✅ 存在
countMissedSubmissions               ✅ 存在
selectHomeworkIdByUserAndPlan        ✅ 存在
insertHomework                       ✅ 存在
selectExcellentShowcaseList          ❌ 【不存在】
```

---

## 七、修复方案

### 方案：在 XML 中添加 selectExcellentShowcaseList 的 SQL 映射

**修复文件**：`src/main/resources/mapper/HomeworkMapper.xml`

**在 `</mapper>` 标签之前添加以下 SQL**：

```xml
<select id="selectExcellentShowcaseList" resultType="com.daily.dailychineseculture.dto.ExcellentShowcaseDTO">
    SELECT
        h.homework_id as homeworkId,
        COALESCE(u.nickname, u.account, CONCAT('学员', h.user_id)) as authorName,
        u.avatar as avatar,
        CONCAT('第', camp.term, '期', ct.level_name) as campName,
        cp.title as planTitle,
        h.content as content,
        h.submit_time as submitTime
    FROM t_homework h
    INNER JOIN t_user u ON h.user_id = u.user_id
    INNER JOIN t_camp_plan cp ON h.plan_id = cp.plan_id
    INNER JOIN t_camp camp ON cp.camp_id = camp.camp_id
    INNER JOIN t_camp_type ct ON camp.type_id = ct.type_id
    WHERE (h.is_small_group_excellent = 1 OR h.is_big_group_excellent = 1)
    ORDER BY h.submit_time DESC
</select>
```

### 修复步骤

1. 打开 `src/main/resources/mapper/HomeworkMapper.xml`
2. 在 `</mapper>` 之前插入上述 `<select id="selectExcellentShowcaseList">` 片段
3. 保存文件
4. 重新编译部署

---

## 八、修复后验证

修复后，MyBatis 将能够正确找到并执行该 SQL 语句：

- **namespace**: `com.daily.dailychineseculture.mapper.HomeworkMapper` ✅
- **SQL id**: `selectExcellentShowcaseList` ✅
- **返回类型**: `ExcellentShowcaseDTO` ✅
