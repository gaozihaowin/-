package com.daily.dailychineseculture.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;
import java.util.Map;

/**
 * 班级、大组、小组Mapper接口
 * 优化点：确保学员数据查询稳定，兼容空值，支持分班后的数据查询
 */
@Mapper
public interface ClassMapper {

    /**
     * 获取所有班级列表（包含营期信息）
     */
    @Select("SELECT c.class_id, c.name, camp.name as camp_name, camp.camp_id " +
            "FROM t_class c " +
            "JOIN t_camp camp ON c.camp_id = camp.camp_id " +
            "ORDER BY camp.camp_id DESC, c.class_id ASC")
    List<Map<String, Object>> getAllClasses();

    /**
     * 获取所有大组列表（包含班级和营期信息）
     */
    @Select("SELECT bg.big_group_id, bg.name, c.name as class_name, c.class_id, camp.name as camp_name " +
            "FROM t_big_group bg " +
            "JOIN t_class c ON bg.class_id = c.class_id " +
            "JOIN t_camp camp ON c.camp_id = camp.camp_id " +
            "ORDER BY camp.camp_id DESC, c.class_id ASC, bg.big_group_id ASC")
    List<Map<String, Object>> getAllBigGroups();

    /**
     * 获取所有小组列表（包含大组、班级和营期信息）
     */
    @Select("SELECT sg.small_group_id, sg.name, bg.name as big_group_name, bg.big_group_id, " +
            "c.name as class_name, camp.name as camp_name " +
            "FROM t_small_group sg " +
            "JOIN t_big_group bg ON sg.big_group_id = bg.big_group_id " +
            "JOIN t_class c ON bg.class_id = c.class_id " +
            "JOIN t_camp camp ON c.camp_id = camp.camp_id " +
            "ORDER BY camp.camp_id DESC, c.class_id ASC, bg.big_group_id ASC, sg.small_group_id ASC")
    List<Map<String, Object>> getAllSmallGroups();

    /**
     * 获取指定营期的所有学员（包含已分班/未分班）
     * 优化点：
     * 1. 去掉 class_id IS NULL 限制，确保能查询到所有学员
     * 2. 增加 COALESCE 处理空值，避免前端接收null字段报错
     * 3. 补充 class_id 字段，方便前端判断学员是否已分班
     */
    @Select("SELECT " +
            "u.user_id, " +
            "u.account, " +
            "COALESCE(u.nickname, u.account) as nickname, " + // 昵称空则用账号兜底
            "COALESCE(u.region, '未知地域') as region, " +    // 地域空则显示未知
            "u.birthday, " +
            "COALESCE(u.gender, '未知') as gender, " +        // 性别空则显示未知
            "COALESCE(u.grade, '未知年级') as grade, " +      // 年级空则显示未知
            "COALESCE(u.phone, '未填写') as phone, " +        // 电话空则显示未填写
            "ce.class_id " +                                  // 补充班级ID字段
            "FROM t_camp_enrollment ce " +
            "JOIN t_user u ON ce.user_id = u.user_id " +
            "WHERE ce.camp_id = #{campId} " +
            "ORDER BY u.region ASC, u.user_id ASC")
    List<Map<String, Object>> getAuditPassStudents(@Param("campId") Integer campId);

    /**
     * 根据营期和班级ID查询学员
     * 优化点：增加空值处理，确保返回字段统一
     */
    @Select("SELECT " +
            "u.user_id, " +
            "u.account, " +
            "COALESCE(u.nickname, u.account) as nickname, " +
            "COALESCE(u.region, '未知地域') as region, " +
            "u.birthday, " +
            "COALESCE(u.gender, '未知') as gender, " +
            "COALESCE(u.grade, '未知年级') as grade, " +
            "COALESCE(u.phone, '未填写') as phone, " +
            "ce.class_id " +
            "FROM t_camp_enrollment ce " +
            "JOIN t_user u ON ce.user_id = u.user_id " +
            "WHERE ce.camp_id = #{campId} AND ce.class_id = #{classId} " +
            "ORDER BY u.region ASC")
    List<Map<String, Object>> getStudentsByClassId(@Param("campId") Integer campId, @Param("classId") Integer classId);

    /**
     * 更新学员的班级ID（分班核心操作）
     * 优化点：增加 WHERE 条件，确保只更新指定营期的学员，避免误操作
     */
    @Update("UPDATE t_camp_enrollment " +
            "SET class_id = #{classId} " +
            "WHERE user_id = #{userId} AND camp_id = #{campId}")
    int updateStudentClassId(@Param("userId") Integer userId,
                             @Param("classId") Integer classId,
                             @Param("campId") Integer campId);

    /**
     * 新增：重置指定营期所有学员的班级ID（方便重新分班）
     * 用途：分班测试时，可快速重置所有学员为未分班状态
     */
    @Update("UPDATE t_camp_enrollment " +
            "SET class_id = NULL " +
            "WHERE camp_id = #{campId}")
    int resetStudentClassId(@Param("campId") Integer campId);

    /**
     * 根据班级ID获取班级名称
     */
    @Select("SELECT name FROM t_class WHERE class_id = #{classId}")
    String getClassNameById(@Param("classId") Integer classId);
}