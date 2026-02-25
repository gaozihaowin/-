package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.dto.MyCourseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 我的课程Mapper接口
 * 处理学员课程相关数据查询
 * 
 * @author Java后端架构师
 * @since 2026-02-25
 */
@Mapper
public interface MyCourseMapper {
    
    /**
     * 根据用户ID和标签类型查询我的课程列表
     * 
     * @param userId 用户ID
     * @param tabType 标签类型：1-正在学习, 2-历史课程, 3-已结业
     * @return 我的课程列表
     */
    @Select({
        "<script>",
        "SELECT ",
        "    e.camp_id AS id,",
        "    CASE ",
        "        WHEN e.is_completed = 1 THEN 'done'",
        "        WHEN c.status = 2 AND e.is_completed = 0 THEN 'hist'",
        "        ELSE 'ing'",
        "    END AS status,",
        "    CASE ",
        "        WHEN e.is_completed = 1 THEN '已结业'",
        "        WHEN c.status = 2 AND e.is_completed = 0 THEN '已结营'",
        "        ELSE '学习中'",
        "    END AS statusText,",
        "    ct.level_name AS type,",
        "    CONCAT('第', c.term, '期') AS term,",
        "    c.name AS title,",
        "    DATE_FORMAT(e.create_time, '%Y-%m-%d') AS updateDate,",
        "    e.progress",
        "FROM t_camp_enrollment e",
        "JOIN t_camp c ON e.camp_id = c.camp_id",
        "JOIN t_camp_type ct ON c.type_id = ct.type_id",
        "WHERE e.user_id = #{userId}",
        "<choose>",
        "    <when test='tabType == 1'>",
        "        AND e.is_completed = 0 AND c.status = 1",
        "    </when>",
        "    <when test='tabType == 2'>",
        "        AND e.is_completed = 0 AND c.status = 2",
        "    </when>",
        "    <when test='tabType == 3'>",
        "        AND e.is_completed = 1",
        "    </when>",
        "</choose>",
        "ORDER BY e.create_time DESC",
        "</script>"
    })
    List<MyCourseVO> selectMyCourses(@Param("userId") Long userId, @Param("tabType") Integer tabType);
}