package com.daily.dailychineseculture.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface VolunteerManageMapper {

    @Select("<script>" +
            "SELECT DISTINCT " +
            "   da.assignment_id AS assignmentId, " +
            "   c.camp_id AS campId, " +
            "   c.type_id AS typeId, " +
            "   CONCAT('第', c.term, '期', c.name) AS campName, " +
            "   c.term, " +
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
            "LEFT JOIN t_small_group sg ON ds.target_id = sg.small_group_id AND ds.target_type = 'small_group' "
            +
            "LEFT JOIN t_big_group sg_big ON sg.big_group_id = sg_big.big_group_id " +
            "LEFT JOIN t_class sg_big_class ON sg_big.class_id = sg_big_class.class_id " +
            "WHERE da.user_id = #{userId} " +
            "AND (da.duty_type IN ('检组', '学组', '检委', '学委', '学班', '检班') OR da.duty_type = 'volunteer_admin') "
            +
            "AND da.end_time IS NULL " +
            "AND (c.end_time > NOW() OR da.duty_type = 'volunteer_admin') " +
            "ORDER BY da.duty_type, ds.target_type" +
            "</script>")
    List<Map<String, Object>> getManagementScope(@Param("userId") Long userId);

    // ==============================
    // 班级成员
    // ==============================
    @Select("SELECT " +
            "   u.user_id AS userId, " +
            "   u.account, u.nickname, u.phone, u.gender, u.birthday, " +
            "   TIMESTAMPDIFF(YEAR, u.birthday, CURDATE()) AS age, " +
            "   u.region, u.profession AS occupation, " +
            "   CONCAT('第', c.term, '期', c.name) AS campName, cl.name AS className, " +
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
    // 大组成员
    // ==============================
    @Select("SELECT " +
            "   u.user_id AS userId, " +
            "   u.account, u.nickname, u.phone, u.gender, u.birthday, " +
            "   TIMESTAMPDIFF(YEAR, u.birthday, CURDATE()) AS age, " +
            "   u.region, u.profession AS occupation, " +
            "   CONCAT('第', c.term, '期', c.name) AS campName, cl.name AS className, " +
            "   bg.name AS bigGroupName, sg.name AS smallGroupName " +
            "FROM t_camp_enrollment ce " +
            "LEFT JOIN t_user u ON ce.user_id = u.user_id " +
            "LEFT JOIN t_camp c ON ce.camp_id = c.camp_id " +
            "LEFT JOIN t_class cl ON ce.class_id = cl.class_id " +
            "LEFT JOIN t_big_group bg ON ce.big_group_id = bg.big_group_id " +
            "LEFT JOIN t_small_group sg ON ce.small_group_id = sg.small_group_id " +
            "WHERE ce.big_group_id = #{bigGroupId} AND ce.is_completed = 0")
    List<Map<String, Object>> getBigGroupMembers(@Param("bigGroupId") Integer bigGroupId);

    @Select("SELECT " +
            "   u.user_id AS userId, " +
            "   u.account, u.nickname, u.phone, u.gender, u.birthday, " +
            "   TIMESTAMPDIFF(YEAR, u.birthday, CURDATE()) AS age, " +
            "   u.region, u.profession AS occupation, " +
            "   CONCAT('第', c.term, '期', c.name) AS campName, cl.name AS className, " +
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
            "SELECT user_id AS userId, nickname, account, avatar, phone FROM t_user " +
            "WHERE (account LIKE CONCAT('%', #{keyword}, '%') OR nickname LIKE CONCAT('%', #{keyword}, '%') OR phone LIKE CONCAT('%', #{keyword}, '%')) "
            +
            "<if test='excludeUserId != null'> AND user_id != #{excludeUserId} </if>" +
            "LIMIT 10" +
            "</script>")
    List<Map<String, Object>> searchUsers(@Param("keyword") String keyword,
                                          @Param("excludeUserId") Long excludeUserId);

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
    List<Map<String, Object>> getCurrentVolunteersByUserId(@Param("scopeType") String scopeType,
                                                           @Param("userId") Long userId);

    @Select("SELECT bg.big_group_id, bg.name as bigGroupName, bg.class_id " +
            "FROM t_big_group bg WHERE bg.big_group_id = #{bigGroupId}")
    Map<String, Object> getBigGroupInfo(@Param("bigGroupId") Integer bigGroupId);

    @Select("SELECT sg.small_group_id, sg.name as smallGroupName, " +
            "sg.big_group_id, bg.class_id " +
            "FROM t_small_group sg " +
            "LEFT JOIN t_big_group bg ON sg.big_group_id = bg.big_group_id " +
            "WHERE sg.small_group_id = #{smallGroupId}")
    Map<String, Object> getSmallGroupInfo(@Param("smallGroupId") Integer smallGroupId);

    // 获取用户所有岗位记录（档案用）
    @Select("SELECT " +
            "   da.assignment_id, " +
            "   da.duty_type, " +
            "   da.start_time, " +
            "   da.end_time, " +
            "   c.camp_id, " +
            "   c.term, " +
            "   CONCAT('第', c.term, '期', c.name) AS camp_name, " +
            "   COALESCE(cl.name, bg_class.name, sg_big_class.name) AS class_name, " +
            "   COALESCE(bg.name, sg_big.name) AS big_group_name, " +
            "   sg.name AS small_group_name " +
            "FROM t_duty_assignment da " +
            "LEFT JOIN t_camp c ON da.camp_id = c.camp_id " +
            "LEFT JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
            "LEFT JOIN t_class cl ON ds.target_type='class' AND ds.target_id=cl.class_id " +
            "LEFT JOIN t_big_group bg ON ds.target_type='big_group' AND ds.target_id=bg.big_group_id " +
            "LEFT JOIN t_class bg_class ON bg.class_id = bg_class.class_id " +
            "LEFT JOIN t_small_group sg ON ds.target_type='small_group' AND ds.target_id=sg.small_group_id "
            +
            "LEFT JOIN t_big_group sg_big ON sg.big_group_id = sg_big.big_group_id " +
            "LEFT JOIN t_class sg_big_class ON sg_big.class_id = sg_big_class.class_id " +
            "WHERE da.user_id = #{userId} " +
            "ORDER BY da.start_time DESC")
    List<Map<String, Object>> getUserAllAssignments(@Param("userId") Long userId);

    @Select("SELECT c.*, IFNULL(u.nickname, u.account) AS receiver_name FROM t_certificate c LEFT JOIN t_user u ON c.user_id = u.user_id WHERE c.user_id = #{userId} AND c.assignment_id IS NOT NULL ORDER BY c.issue_time DESC")
    List<Map<String, Object>> getCertificatesByUser(@Param("userId") Long userId);

    @Select("SELECT c.*, IFNULL(u.nickname, u.account) AS receiver_name FROM t_certificate c LEFT JOIN t_user u ON c.user_id = u.user_id WHERE c.user_id = #{userId} ORDER BY c.issue_time DESC")
    List<Map<String, Object>> getAllCertificatesByUser(@Param("userId") Long userId);

    @Select("SELECT * FROM t_certificate WHERE cert_id = #{certificateId}")
    Map<String, Object> getCertificateById(@Param("certificateId") Integer certificateId);

    // 颁发证书
    @Insert("INSERT INTO t_certificate (user_id, type, number, assignment_id, homework_id, issue_time) " +
            "VALUES (#{userId}, #{certificateType}, #{certNumber}, #{assignmentId}, #{homeworkId}, NOW())")
    int issueCertificate(
            @Param("userId") Long userId,
            @Param("certificateType") String certificateType,
            @Param("certNumber") String certNumber,
            @Param("assignmentId") Integer assignmentId,
            @Param("homeworkId") Long homeworkId);

    // 取消颁发证书
    @Delete("DELETE FROM t_certificate WHERE user_id=#{userId} AND type=#{certificateType} AND ((assignment_id=#{assignmentId} AND assignment_id IS NOT NULL) OR (homework_id=#{homeworkId} AND homework_id IS NOT NULL) OR (assignment_id IS NULL AND homework_id IS NULL))")
    int cancelCertificate(
            @Param("userId") Long userId,
            @Param("certificateType") String certificateType,
            @Param("assignmentId") Integer assignmentId,
            @Param("homeworkId") Long homeworkId);

    // 检查志愿者在特定职位是否已颁发证书
    @Select("SELECT COUNT(*) FROM t_certificate WHERE user_id=#{userId} AND type=#{certificateType} AND ((assignment_id=#{assignmentId} AND assignment_id IS NOT NULL) OR (homework_id=#{homeworkId} AND homework_id IS NOT NULL) OR (assignment_id IS NULL AND homework_id IS NULL))")
    int checkCertificateIssued(
            @Param("userId") Long userId,
            @Param("certificateType") String certificateType,
            @Param("assignmentId") Integer assignmentId,
            @Param("homeworkId") Long homeworkId);

    @Select("SELECT MAX(cert_id) FROM t_certificate")
    Integer getMaxCertificateId();

    @Select("SELECT * FROM t_user WHERE user_id = #{userId}")
    Map<String, Object> getUserById(@Param("userId") Long userId);

    @Select("SELECT u.user_id, u.account, u.nickname, u.phone, u.avatar, da.duty_type, da.assignment_id, da.start_time, da.end_time, "
            +
            "CONCAT('第', c.term, '期', c.name) AS camp_name,cl.name AS class_name, bg.name AS big_group_name, sg.name AS small_group_name "
            +
            "FROM t_user u " +
            "JOIN t_duty_assignment da ON u.user_id = da.user_id " +
            "JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
            "JOIN t_class cl ON ds.target_id = cl.class_id AND ds.target_type = 'class' " +
            "JOIN t_camp_type ct ON cl.type_id = ct.type_id " +
            "JOIN t_camp c ON da.camp_id = c.camp_id " +
            "LEFT JOIN t_big_group bg ON ds.target_type='big_group' AND ds.target_id=bg.big_group_id " +
            "LEFT JOIN t_small_group sg ON ds.target_type='small_group' AND ds.target_id=sg.small_group_id "
            +
            "WHERE ct.type_id = #{typeId} " +
            "AND da.duty_type IN ('学班', '检班') " +
            "AND da.end_time IS NULL")
    List<Map<String, Object>> getVolunteersByTypeId(@Param("typeId") Integer typeId);

    @Select("SELECT u.user_id, u.account, u.nickname, u.phone, u.avatar, da.duty_type, da.assignment_id " +
            "FROM t_user u " +
            "JOIN t_duty_assignment da ON u.user_id = da.user_id " +
            "JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
            "WHERE ds.target_type = #{targetType} AND ds.target_id = #{targetId} " +
            "AND da.end_time IS NULL")
    List<Map<String, Object>> getManagedVolunteers(@Param("targetType") String targetType,
                                                   @Param("targetId") Integer targetId);

    // 获取班级内的学委/检委
    @Select("SELECT u.user_id, u.account, u.nickname, u.phone, u.avatar, da.duty_type, da.assignment_id, da.start_time, da.end_time, "
            +
            "CONCAT('第', c.term, '期', c.name) AS camp_name, cl.name AS class_name, bg.name AS big_group_name, sg.name AS small_group_name "
            +
            "FROM t_user u " +
            "JOIN t_duty_assignment da ON u.user_id = da.user_id " +
            "JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
            "JOIN t_big_group bg ON ds.target_id = bg.big_group_id AND ds.target_type = 'big_group' " +
            "JOIN t_class cl ON bg.class_id = cl.class_id " +
            "JOIN t_camp c ON da.camp_id = c.camp_id " +
            "LEFT JOIN t_small_group sg ON ds.target_type='small_group' AND ds.target_id=sg.small_group_id "
            +
            "WHERE bg.class_id = #{classId} " +
            "AND da.duty_type IN ('学委', '检委') " +
            "AND da.end_time IS NULL")
    List<Map<String, Object>> getCommitteeMembersByClassId(@Param("classId") Integer classId);

    // 获取大组内的学组/检组
    @Select("SELECT u.user_id, u.account, u.nickname, u.phone, u.avatar, da.duty_type, da.assignment_id, da.start_time, da.end_time, "
            +
            "CONCAT('第', c.term, '期', c.name) AS camp_name, cl.name AS class_name, bg.name AS big_group_name, sg.name AS small_group_name "
            +
            "FROM t_user u " +
            "JOIN t_duty_assignment da ON u.user_id = da.user_id " +
            "JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
            "JOIN t_small_group sg ON ds.target_id = sg.small_group_id AND ds.target_type = 'small_group' "
            +
            "JOIN t_big_group bg ON sg.big_group_id = bg.big_group_id " +
            "JOIN t_class cl ON bg.class_id = cl.class_id " +
            "JOIN t_camp c ON da.camp_id = c.camp_id " +
            "WHERE bg.big_group_id = #{bigGroupId} " +
            "AND da.duty_type IN ('学组', '检组') " +
            "AND da.end_time IS NULL")
    List<Map<String, Object>> getGroupLeadersByBigGroupId(@Param("bigGroupId") Integer bigGroupId);

    // 根据管理范围获取志愿者列表
    @Select("SELECT u.user_id, u.account, u.nickname, u.phone, u.avatar, da.duty_type, da.assignment_id " +
            "FROM t_user u " +
            "JOIN t_duty_assignment da ON u.user_id = da.user_id " +
            "JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
            "WHERE da.assignment_id = #{assignmentId} " +
            "AND da.end_time IS NULL")
    List<Map<String, Object>> getVolunteersByScope(@Param("assignmentId") String assignmentId);

    // 根据typeId获取营期类型名称
    @Select("SELECT level_name FROM t_camp_type WHERE type_id = #{typeId}")
    String getCampTypeName(@Param("typeId") Integer typeId);

    // 获取正在进行的营期列表
    @Select("SELECT camp_id as campId, CONCAT('第', term, '期', name) as campName, '进行中' as status FROM t_camp WHERE end_time IS NULL OR end_time > CURRENT_TIMESTAMP")
    List<Map<String, Object>> getActiveCamps();

    // 获取营期下的班级列表
    @Select("SELECT c.class_id as classId, c.name as className, (SELECT COUNT(*) FROM t_camp_enrollment WHERE class_id = c.class_id AND is_completed = 0) as memberCount, (SELECT COUNT(*) FROM t_duty_assignment da JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id WHERE ds.target_type = 'class' AND ds.target_id = c.class_id AND da.duty_type IN ('学班', '检班') AND da.end_time IS NULL) > 0 as hasMonitor FROM t_class c WHERE c.camp_id = #{campId}")
    List<Map<String, Object>> getClassesByCampId(@Param("campId") Integer campId);

    // 获取营期下的班长列表
    @Select("SELECT u.user_id as userId, u.nickname, u.phone, u.account, da.duty_type as dutyType, da.assignment_id as assignmentId, CONCAT('第', c.term, '期', c.name) as campName, cl.name as className, da.start_time as startTime, da.end_time as endTime FROM t_duty_assignment da JOIN t_user u ON da.user_id = u.user_id JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id JOIN t_class cl ON ds.target_id = cl.class_id JOIN t_camp c ON cl.camp_id = c.camp_id WHERE c.camp_id = #{campId} AND da.duty_type IN ('学班', '检班') AND da.end_time IS NULL")
    List<Map<String, Object>> getMonitorsByCampId(@Param("campId") Integer campId);

    @Select("SELECT COUNT(*) FROM t_duty_assignment WHERE user_id = #{userId} AND duty_type = 'volunteer_admin' AND end_time IS NULL")
    Integer checkAdminRole(@Param("userId") Long userId);

    /**
     * 根据班级ID获取营期ID
     */
    @Select("SELECT camp_id FROM t_class WHERE class_id = #{classId}")
    Integer getCampIdByClassId(@Param("classId") Integer classId);

    /**
     * 根据大组ID获取营期ID
     */
    @Select("SELECT c.camp_id FROM t_big_group bg JOIN t_class c ON bg.class_id = c.class_id WHERE bg.big_group_id = #{bigGroupId}")
    Integer getCampIdByBigGroupId(@Param("bigGroupId") Integer bigGroupId);

    /**
     * 根据小组ID获取营期ID
     */
    @Select("SELECT c.camp_id FROM t_small_group sg JOIN t_big_group bg ON sg.big_group_id = bg.big_group_id JOIN t_class c ON bg.class_id = c.class_id WHERE sg.small_group_id = #{smallGroupId}")
    Integer getCampIdBySmallGroupId(@Param("smallGroupId") Integer smallGroupId);

    // 根据作业ID获取证书列表
    @Select("SELECT * FROM t_certificate WHERE homework_id=#{homeworkId}")
    List<Map<String, Object>> getCertificatesByHomeworkId(@Param("homeworkId") Long homeworkId);
}