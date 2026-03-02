package com.daily.dailychineseculture.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

/**
 * 班级、大组、小组Mapper接口
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
}