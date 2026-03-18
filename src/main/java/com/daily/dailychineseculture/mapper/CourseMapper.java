package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程Mapper接口
 * 使用原生MyBatis注解方式实现
 */
@Mapper
public interface CourseMapper {
    
    /**
     * 查询正在招生/开课中的课程列表
     * 按开营时间升序排列（越近的越靠前）
     * 
     * @return 课程列表
     */
    @Select("SELECT " +
            "camp_id AS id, " +
            "name AS title, " +
            "name AS campName, " +
            "CONCAT('第', term, '期') AS batch, " +
            "intro AS description, " +
            "enroll_count AS participantCount, " +
            "status, " +
            "start_time AS startTime, " +
            "end_time AS endTime " +
            "FROM t_camp " +
            "WHERE status = 1 " +               // 拦截 1：必须是上架状态
            "AND end_time >= NOW() " +          // 拦截 2：只查还没结束的课程
            "ORDER BY start_time ASC")
    List<Course> selectActiveCourses();

    @Select("SELECT " +
            "camp_id AS id, " +
            "name AS title, " +
            "name AS campName, " +
            "CONCAT('第', term, '期') AS batch, " +
            "intro AS description, " +
            "enroll_count AS participantCount, " +
            "status, " +
            "start_time AS startTime, " +
            "end_time AS endTime " +
            "FROM t_camp " +
            "WHERE camp_id = #{id}")
    Course selectCourseById(@Param("id") Integer id);
}
