package com.daily.dailychineseculture.mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface VolunteerManageMapper {

    @Select("<script>" +
            "SELECT DISTINCT " +
            "   da.assignment_id AS assignmentId, " +
            "   c.camp_id AS campId, " +
            "   c.name AS campName, " +
            "   COALESCE(cl.class_id, bg_class.class_id, sg_big_class.class_id) AS classId, " +
            "   COALESCE(cl.name, bg_class.name, sg_big_class.name) AS className, " +
            "   COALESCE(bg.big_group_id, sg_big.big_group_id) AS bigGroupId, " +
            "   COALESCE(bg.name, sg_big.name) AS bigGroupName, " +
            "   sg.small_group_id AS smallGroupId, " +
            "   sg.name AS smallGroupName, " +
            "   da.duty_type AS dutyType, " +
            "   ds.target_type AS targetType " +
            "FROM t_duty_assignment da " +
            "LEFT JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
            "LEFT JOIN t_camp c ON da.camp_id = c.camp_id " +
            "LEFT JOIN t_class cl ON ds.target_id = cl.class_id AND ds.target_type = 'class' " +
            "LEFT JOIN t_big_group bg ON ds.target_id = bg.big_group_id AND ds.target_type = 'big_group' " +
            "LEFT JOIN t_class bg_class ON bg.class_id = bg_class.class_id " +
            "LEFT JOIN t_small_group sg ON ds.target_id = sg.small_group_id AND ds.target_type = 'small_group' " +
            "LEFT JOIN t_big_group sg_big ON sg.big_group_id = sg_big.big_group_id " +
            "LEFT JOIN t_class sg_big_class ON sg_big.class_id = sg_big_class.class_id " +
            "WHERE da.user_id = #{userId} " +
            "AND da.duty_type IN ('检组', '学组', '检委', '学委', '学班', '检班') " +
            "AND ds.assignment_id IS NOT NULL " +
            "AND da.end_time IS NULL " +
            "AND c.end_time > NOW() " +
            "ORDER BY da.duty_type, ds.target_type" +
            "</script>")
    List<Map<String, Object>> getManagementScope(@Param("userId") Long userId);

    // ==============================
    // 班级成员（已修复，带 userId）
    // ==============================
    @Select("SELECT " +
            "   u.user_id AS userId, " +
            "   u.account, u.nickname, u.phone, u.gender, u.birthday, " +
            "   TIMESTAMPDIFF(YEAR, u.birthday, CURDATE()) AS age, " +
            "   u.region, u.profession AS occupation, " +
            "   c.name AS campName, cl.name AS className, " +
            "   bg.name AS bigGroupName, sg.name AS smallGroupName " +
            "FROM t_camp_enrollment ce " +
            "LEFT JOIN t_user u ON ce.user_id = u.user_id " +
            "LEFT JOIN t_camp c ON ce.camp_id = c.camp_id " +
            "LEFT JOIN t_class cl ON ce.class_id = cl.class_id " +
            "LEFT JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id " +
            "LEFT JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id " +
            "WHERE ce.class_id = #{classId} AND ce.is_completed = 0")
    List<Map<String, Object>> getClassMembers(@Param("classId") Integer classId);

    // ==============================
    // 大组成员（已修复，带 userId）
    // ==============================
    @Select("SELECT " +
            "   u.user_id AS userId, " +
            "   u.account, u.nickname, u.phone, u.gender, u.birthday, " +
            "   TIMESTAMPDIFF(YEAR, u.birthday, CURDATE()) AS age, " +
            "   u.region, u.profession AS occupation, " +
            "   c.name AS campName, cl.name AS className, " +
            "   bg.name AS bigGroupName, sg.name AS smallGroupName " +
            "FROM t_camp_enrollment ce " +
            "LEFT JOIN t_user u ON ce.user_id = u.user_id " +
            "LEFT JOIN t_camp c ON ce.camp_id = c.camp_id " +
            "LEFT JOIN t_class cl ON ce.class_id = cl.class_id " +
            "LEFT JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id " +
            "LEFT JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id " +
            "WHERE ce.big_group_id = #{bigGroupId} AND ce.is_completed = 0")
    List<Map<String, Object>> getBigGroupMembers(@Param("bigGroupId") Integer bigGroupId);

    // ==============================
    // 🔥 小组成员（彻底修复！！！）
    // ==============================
    @Select("SELECT " +
            "   u.user_id AS userId, " +  // 👈 👈 👈 这里加回来了！
            "   u.account, u.nickname, u.phone, u.gender, u.birthday, " +
            "   TIMESTAMPDIFF(YEAR, u.birthday, CURDATE()) AS age, " +
            "   u.region, u.profession AS occupation, " +
            "   c.name AS campName, cl.name AS className, " +
            "   bg.name AS bigGroupName, sg.name AS smallGroupName " +
            "FROM t_camp_enrollment ce " +
            "LEFT JOIN t_user u ON ce.user_id = u.user_id " +
            "LEFT JOIN t_camp c ON ce.camp_id = c.camp_id " +
            "LEFT JOIN t_class cl ON ce.class_id = cl.class_id " +
            "LEFT JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id " +
            "LEFT JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id " +
            "WHERE ce.small_group_id = #{smallGroupId} AND ce.is_completed = 0")
    List<Map<String, Object>> getSmallGroupMembers(@Param("smallGroupId") Integer smallGroupId);

    @Select("SELECT " +
            "   da.assignment_id AS assignmentId, " +
            "   da.user_id AS userId, " +
            "   u.nickname AS username, u.account, " +
            "   da.duty_type AS dutyType, " +
            "   ds.target_type AS targetType, " +
            "   ds.target_id AS targetId " +
            "FROM t_duty_assignment da " +
            "LEFT JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
            "LEFT JOIN t_user u ON da.user_id = u.user_id " +
            "WHERE ds.target_type = #{targetType} " +
            "AND ds.target_id = #{targetId} " +
            "AND da.end_time IS NULL")
    List<Map<String, Object>> getCurrentVolunteers(@Param("targetType") String targetType,
                                                   @Param("targetId") Integer targetId);

    @Select("SELECT bg.big_group_id AS targetId, bg.name AS targetName, 'big_group' AS targetType " +
            "FROM t_big_group bg WHERE bg.class_id = #{classId}")
    List<Map<String, Object>> getAssignableBigGroups(@Param("classId") Integer classId);

    @Select("SELECT sg.small_group_id AS targetId, sg.name AS targetName, 'small_group' AS targetType " +
            "FROM t_small_group sg WHERE sg.big_group_id = #{bigGroupId}")
    List<Map<String, Object>> getAssignableSmallGroups(@Param("bigGroupId") Integer bigGroupId);

    @Select("SELECT COUNT(*) " +
            "FROM t_duty_assignment da " +
            "LEFT JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
            "WHERE da.user_id = #{userId} " +
            "AND ds.target_type = #{targetType} " +
            "AND ds.target_id = #{targetId} " +
            "AND da.duty_type = #{dutyType} " +
            "AND da.end_time IS NULL")
    Integer checkUserDutyExists(@Param("userId") Long userId,
                                @Param("targetType") String targetType,
                                @Param("targetId") Integer targetId,
                                @Param("dutyType") String dutyType);

    @Insert("INSERT INTO t_duty_assignment (user_id, camp_id, duty_type, start_time) " +
            "VALUES (#{userId}, #{campId}, #{dutyType}, NOW())")
    Integer assignDuty(@Param("userId") Long userId,
                       @Param("campId") Integer campId,
                       @Param("dutyType") String dutyType);

    @Insert("UPDATE t_duty_assignment SET end_time = NOW() WHERE assignment_id = #{assignmentId}")
    Integer removeDuty(@Param("assignmentId") Integer assignmentId);

    @Insert("INSERT INTO t_duty_scope (assignment_id, target_id, target_type) " +
            "VALUES (#{assignmentId}, #{targetId}, #{targetType})")
    Integer addDutyScope(@Param("assignmentId") Integer assignmentId,
                         @Param("targetId") Integer targetId,
                         @Param("targetType") String targetType);

    @Select("SELECT LAST_INSERT_ID()")
    Integer getLastInsertId();

    @Select("<script>" +
            "SELECT user_id AS userId, nickname AS username, avatar, phone FROM t_user " +
            "WHERE (account LIKE CONCAT('%', #{keyword}, '%') OR nickname LIKE CONCAT('%', #{keyword}, '%') OR phone LIKE CONCAT('%', #{keyword}, '%')) " +
            "<if test='excludeUserId != null'> AND user_id != #{excludeUserId} </if>" +
            "LIMIT 10" +
            "</script>")
    List<Map<String, Object>> searchUsers(@Param("keyword") String keyword, @Param("excludeUserId") Long excludeUserId);

    @Select("SELECT " +
            "   da.assignment_id AS assignmentId, " +
            "   da.user_id AS userId, " +
            "   u.nickname AS username, u.account, " +
            "   da.duty_type AS dutyType, " +
            "   ds.target_type AS targetType, " +
            "   ds.target_id AS targetId " +
            "FROM t_duty_assignment da " +
            "LEFT JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
            "LEFT JOIN t_user u ON da.user_id = u.user_id " +
            "WHERE ds.target_type = #{scopeType} " +
            "AND da.user_id = #{userId} " +
            "AND da.end_time IS NULL")
    List<Map<String, Object>> getCurrentVolunteersByUserId(@Param("scopeType") String scopeType, @Param("userId") Long userId);

    @Select("SELECT bg.big_group_id, bg.name as bigGroupName, bg.class_id " +
            "FROM t_big_group bg WHERE bg.big_group_id = #{bigGroupId}")
    Map<String, Object> getBigGroupInfo(@Param("bigGroupId") Integer bigGroupId);

    @Select("SELECT sg.small_group_id, sg.name as smallGroupName, " +
            "sg.big_group_id, bg.class_id " +
            "FROM t_small_group sg " +
            "LEFT JOIN t_big_group bg ON sg.big_group_id = bg.big_group_id " +
            "WHERE sg.small_group_id = #{smallGroupId}")
    Map<String, Object> getSmallGroupInfo(@Param("smallGroupId") Integer smallGroupId);

}