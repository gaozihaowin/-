package com.daily.dailychineseculture.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface ClassMapper {

    @Select("SELECT c.class_id, c.name, camp.name as camp_name, camp.camp_id " +
            "FROM t_class c " +
            "JOIN t_camp camp ON c.camp_id = camp.camp_id " +
            "ORDER BY camp.camp_id DESC, c.class_id ASC")
    List<Map<String, Object>> getAllClasses();

    @Select("SELECT bg.big_group_id, bg.name, c.name as class_name, c.class_id, camp.name as camp_name " +
            "FROM t_big_group bg " +
            "JOIN t_class c ON bg.class_id = c.class_id " +
            "JOIN t_camp camp ON c.camp_id = camp.camp_id " +
            "ORDER BY camp.camp_id DESC, c.class_id ASC, bg.big_group_id ASC")
    List<Map<String, Object>> getAllBigGroups();

    @Select("SELECT sg.small_group_id, sg.name, bg.name as big_group_name, bg.big_group_id, " +
            "c.name as class_name, camp.name as camp_name " +
            "FROM t_small_group sg " +
            "JOIN t_big_group bg ON sg.big_group_id = bg.big_group_id " +
            "JOIN t_class c ON bg.class_id = c.class_id " +
            "JOIN t_camp camp ON c.camp_id = camp.camp_id " +
            "ORDER BY camp.camp_id DESC, c.class_id ASC, bg.big_group_id ASC, sg.small_group_id ASC")
    List<Map<String, Object>> getAllSmallGroups();

    @Select("SELECT " +
            "u.user_id, " +
            "u.account, " +
            "COALESCE(u.nickname, u.account) as nickname, " +
            "COALESCE(u.region, '未知地域') as region, " +
            "u.birthday, " +
            "COALESCE(u.gender, '未知') as gender, " +
            "COALESCE(u.profession, '未知职业') as profession, " +
            "COALESCE(u.phone, '未填写') as phone, " +
            "ce.class_id, " +
            "c.name as class_name " +
            "FROM t_camp_enrollment ce " +
            "JOIN t_user u ON ce.user_id = u.user_id " +
            "LEFT JOIN t_class c ON ce.class_id = c.class_id " +
            "WHERE ce.camp_id = #{campId} " +
            "ORDER BY u.region ASC, u.user_id ASC")
    List<Map<String, Object>> getAuditPassStudents(@Param("campId") Integer campId);

    @Select("SELECT " +
            "u.user_id, " +
            "u.account, " +
            "COALESCE(u.nickname, u.account) as nickname, " +
            "COALESCE(u.region, '未知地域') as region, " +
            "u.birthday, " +
            "COALESCE(u.gender, '未知') as gender, " +
            "COALESCE(u.profession, '未知职业') as profession, " +
            "COALESCE(u.phone, '未填写') as phone, " +
            "ce.class_id, " +
            "c.name as class_name " +
            "FROM t_camp_enrollment ce " +
            "JOIN t_user u ON ce.user_id = u.user_id " +
            "LEFT JOIN t_class c ON ce.class_id = c.class_id " +
            "WHERE ce.camp_id = #{campId} AND ce.class_id = #{classId} " +
            "ORDER BY u.region ASC")
    List<Map<String, Object>> getStudentsByClassId(@Param("campId") Integer campId, @Param("classId") Integer classId);

    @Update("UPDATE t_camp_enrollment " +
            "SET class_id = #{classId} " +
            "WHERE user_id = #{userId} AND camp_id = #{campId}")
    int updateStudentClassId(@Param("userId") Integer userId,
                             @Param("classId") Integer classId,
                             @Param("campId") Integer campId);

    @Update("UPDATE t_camp_enrollment " +
            "SET class_id = NULL " +
            "WHERE camp_id = #{campId}")
    int resetStudentClassId(@Param("campId") Integer campId);

    @Select("SELECT name FROM t_class WHERE class_id = #{classId}")
    String getClassNameById(@Param("classId") Integer classId);

    @Select("SELECT c.class_id, c.name, camp.name as camp_name, camp.camp_id " +
            "FROM t_class c " +
            "JOIN t_camp camp ON c.camp_id = camp.camp_id " +
            "WHERE c.camp_id = #{campId} " +
            "ORDER BY c.class_id ASC")
    List<Map<String, Object>> getClassesByCampId(@Param("campId") Integer campId);

    @Insert("INSERT INTO t_class (camp_id, name) VALUES (#{campId}, #{className})")
    int insertClass(@Param("campId") Integer campId, @Param("className") String className);

    @Select("SELECT LAST_INSERT_ID()")
    Integer getLastInsertId();

    @Delete("DELETE FROM t_class WHERE camp_id = #{campId}")
    int deleteClassesByCampId(@Param("campId") Integer campId);
}
