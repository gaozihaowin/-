package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {

        /**
         * 查询所有用户
         */
        @Select("SELECT * FROM t_user")
        List<User> selectAll();

        /**
         * 根据ID查询用户
         */
        @Select("SELECT * FROM t_user WHERE user_id = #{userId}")
        User selectById(Long userId);

        /**
         * 根据账号查询用户
         */
        @Select("SELECT * FROM t_user WHERE account = #{account}")
        User selectByAccount(String account);

        /**
         * 插入用户
         */
        @Insert("INSERT INTO t_user(user_id, account, password, gender, create_time, status, openid, nickname, avatar) "
                +
                "VALUES(#{userId}, #{account}, #{password}, #{gender}, #{createTime}, #{status}, #{openid}, #{nickname}, #{avatar})")
        int insert(User user);

        /**
         * 更新用户
         */
        @Update("UPDATE t_user SET account=#{account}, password=#{password}, avatar=#{avatar}, " +
                "phone=#{phone}, region=#{region}, birthday=#{birthday}, profession=#{profession}, gender=#{gender} "
                +
                "WHERE user_id=#{userId}")
        int update(User user);

        /**
         * 删除用户
         */
        @Delete("DELETE FROM t_user WHERE user_id = #{userId}")
        int deleteById(Long userId);

        /**
         * 根据openid查询用户
         */
        @Select("SELECT * FROM t_user WHERE openid = #{openid}")
        User selectByOpenid(String openid);

        /**
         * 统计用户的志愿者分配数量
         * 检查6种志愿者职位：检组/学组/检委/学委/学班/检班
         */
        @Select("SELECT COUNT(*) FROM t_duty_assignment WHERE user_id = #{userId} AND duty_type IN ('检组', '学组', '检委', '学委', '学班', '检班')")
        Integer countVolunteerAssignments(Long userId);

        /**
         * 获取用户志愿者历史记录
         */
        @Select("SELECT DISTINCT " +
                "da.assignment_id AS assignmentId, " +
                "c.camp_id AS campId, " +
                "IFNULL(c.name, '未知营期') AS campName, " +
                "da.volunteer_start_time AS rawStartTime, " +
                "IFNULL(DATE_FORMAT(da.volunteer_start_time, '%Y.%m.%d %H:%i:%s'), '未设置') AS actualStartTime, "
                +
                "IFNULL(DATE_FORMAT(c.end_time, '%Y.%m.%d %H:%i:%s'), '未设置') AS campEndTime, " +
                "UNIX_TIMESTAMP(c.end_time) AS rawCampEndTime, " +
                "IFNULL(DATE_FORMAT(da.volunteer_end_time, '%Y.%m.%d %H:%i:%s'), '未设置') AS quitTime, " +
                "ds.target_type AS targetType, " +
                "IFNULL(da.duty_type, '志愿者') AS dutyName, " +
                "CASE " +
                "    WHEN ds.target_type = 'class' THEN CONCAT( " +
                "        IFNULL(c.name, '未知营期'), " +
                "        ' - ', " +
                "        IFNULL(cl.name, '未知班级') " +
                "    ) " +
                "    WHEN ds.target_type = 'big_group' THEN CONCAT( " +
                "        IFNULL(c.name, '未知营期'), " +
                "        ' - ', " +
                "        IFNULL(cl_bg.name, '未知班级'), " +
                "        ' - ', " +
                "        IFNULL(bg.name, '未知大组') " +
                "    ) " +
                "    WHEN ds.target_type = 'small_group' THEN CONCAT( " +
                "        IFNULL(c.name, '未知营期'), " +
                "        ' - ', " +
                "        IFNULL(cl_sg.name, '未知班级'), " +
                "        ' - ', " +
                "        IFNULL(bg_sg.name, '未知大组'), " +
                "        ' - ', " +
                "        IFNULL(sg.name, '未知小组') " +
                "    ) " +
                "    WHEN ds.target_type = 'camp' THEN IFNULL(c.name, '未知营期') " +
                "    ELSE '未知职责范围' " +
                "END AS fullTargetName " +
                "FROM t_duty_assignment da " +
                "LEFT JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
                "LEFT JOIN t_camp c ON da.camp_id = c.camp_id " +
                "LEFT JOIN t_class cl ON ds.target_type = 'class' AND ds.target_id = cl.class_id " +
                "LEFT JOIN t_big_group bg ON ds.target_type = 'big_group' AND ds.target_id = bg.big_group_id " +
                "LEFT JOIN t_class cl_bg ON bg.class_id = cl_bg.class_id " +
                "LEFT JOIN t_small_group sg ON ds.target_type = 'small_group' AND ds.target_id = sg.small_group_id "
                +
                "LEFT JOIN t_big_group bg_sg ON sg.big_group_id = bg_sg.big_group_id " +
                "LEFT JOIN t_class cl_sg ON bg_sg.class_id = cl_sg.class_id " +
                "WHERE da.user_id = #{userId} " +
                "AND da.duty_type IN ('检组', '学组', '检委', '学委', '学班', '检班') " +
                "AND ds.target_type IN ('class', 'big_group', 'small_group', 'camp') " +
                "ORDER BY da.volunteer_start_time DESC")
        List<Map<String, Object>> getVolunteerHistory(Long userId);

        /**
         * 更新志愿者服务结束时间（退出担当）
         */
        @Update("UPDATE t_duty_assignment " +
                "SET volunteer_end_time = NOW() " +
                "WHERE assignment_id = #{assignmentId} AND user_id = #{userId}")
        int updateVolunteerEndTime(@Param("assignmentId") Integer assignmentId, @Param("userId") Long userId);

        /**
         * 将志愿者服务结束时间更新为营期结束时间
         */
        @Update("UPDATE t_duty_assignment " +
                "SET volunteer_end_time = STR_TO_DATE(#{campEndTime}, '%Y.%m.%d %H:%i:%s') " +
                "WHERE assignment_id = #{assignmentId} AND user_id = #{userId} " +
                "AND volunteer_end_time IS NULL")
        int updateVolunteerEndTimeToCampEnd(@Param("assignmentId") Integer assignmentId,
                                            @Param("userId") Long userId,
                                            @Param("campEndTime") String campEndTime);

        /**
         * 检查用户是否有该职责任命
         */
        @Select("SELECT COUNT(*) FROM t_duty_assignment " +
                "WHERE assignment_id = #{assignmentId} AND user_id = #{userId}")
        int checkAssignmentExists(@Param("assignmentId") Integer assignmentId, @Param("userId") Long userId);

        /**
         * 获取志愿者统计信息
         */
        @Select("<script>" +
                "SELECT DISTINCT c.camp_id AS campId, c.name AS campName " +
                "FROM t_duty_assignment da " +
                "LEFT JOIN t_camp c ON da.camp_id = c.camp_id " +
                "WHERE da.user_id = #{userId} " +
                "AND da.duty_type IN ('检组', '学组', '检委', '学委', '学班', '检班') " +
                "</script>")
        List<Map<String, Object>> getVolunteerCamps(Long userId);

        @Select("<script>" +
                "SELECT DISTINCT " +
                "   c.camp_id AS campId, " +
                "   c.name AS campName, " +
                "   cl.class_id AS classId, " +
                "   cl.name AS className " +
                "FROM t_duty_assignment da " +
                "LEFT JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
                "LEFT JOIN t_class cl ON ds.target_id = cl.class_id " +
                "LEFT JOIN t_camp c ON da.camp_id = c.camp_id " +
                "WHERE da.user_id = #{userId} " +
                "AND da.duty_type IN ('检组', '学组', '检委', '学委', '学班', '检班') " +
                "AND ds.target_type = 'class' " +
                "</script>")
        List<Map<String, Object>> getVolunteerClasses(Long userId);

        @Select("<script>" +
                "SELECT DISTINCT " +
                "   c.camp_id AS campId, " +
                "   c.name AS campName, " +
                "   bg.big_group_id AS bigGroupId, " +
                "   bg.name AS bigGroupName, " +
                "   cl.class_id AS classId, " +
                "   cl.name AS className " +
                "FROM t_duty_assignment da " +
                "LEFT JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
                "LEFT JOIN t_big_group bg ON ds.target_id = bg.big_group_id " +
                "LEFT JOIN t_class cl ON bg.class_id = cl.class_id " +
                "LEFT JOIN t_camp c ON da.camp_id = c.camp_id " +
                "WHERE da.user_id = #{userId} " +
                "AND da.duty_type IN ('检组', '学组', '检委', '学委', '学班', '检班') " +
                "AND ds.target_type = 'big_group' " +
                "</script>")
        List<Map<String, Object>> getVolunteerBigGroups(Long userId);

        /**
         * 获取志愿者统计信息 - 负责的小组
         */
        @Select("<script>" +
                "SELECT DISTINCT " +
                "   c.camp_id AS campId, " +
                "   c.name AS campName, " +
                "   bg.big_group_id AS bigGroupId, " +
                "   bg.name AS bigGroupName, " +
                "   sg.small_group_id AS smallGroupId, " +
                "   sg.name AS smallGroupName, " +
                "   cl.class_id AS classId, " +
                "   cl.name AS className " +
                "FROM t_duty_assignment da " +
                "LEFT JOIN t_duty_scope ds ON da.assignment_id = ds.assignment_id " +
                "LEFT JOIN t_small_group sg ON ds.target_id = sg.small_group_id " +
                "LEFT JOIN t_big_group bg ON sg.big_group_id = bg.big_group_id " +
                "LEFT JOIN t_class cl ON bg.class_id = cl.class_id " +
                "LEFT JOIN t_camp c ON da.camp_id = c.camp_id " +
                "WHERE da.user_id = #{userId} " +
                "AND da.duty_type IN ('检组', '学组', '检委', '学委', '学班', '检班') " +
                "AND ds.target_type = 'small_group' " +
                "AND ds.assignment_id IS NOT NULL " +
                "</script>")
        List<Map<String, Object>> getVolunteerSmallGroups(Long userId);

        // ========== 完全修正后的查询待分班学员 SQL ==========
        @Select("SELECT u.* " +
                "FROM t_user u " +
                "JOIN t_camp_enrollment e ON u.user_id = e.user_id " +
                "WHERE e.camp_id = #{campId} AND e.class_id IS NULL")
        List<User> selectAuditPassStudents(@Param("campId") Long campId);

        // ========== 完全修正后的更新班级ID SQL（操作报名表） ==========
        @Update("UPDATE t_camp_enrollment " +
                "SET class_id = #{classId} " +
                "WHERE user_id = #{userId} AND camp_id = #{campId}")
        int updateEnrollmentClassId(@Param("userId") Long userId,
                                    @Param("campId") Long campId,
                                    @Param("classId") Long classId);
}