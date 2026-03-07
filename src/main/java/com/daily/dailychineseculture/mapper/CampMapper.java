package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.dto.CampListItemDTO;
import com.daily.dailychineseculture.dto.CampTypeOptionDTO;
import com.daily.dailychineseculture.dto.CampVO;
import com.daily.dailychineseculture.entity.Camp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 营期Mapper接口
 */
@Mapper
public interface CampMapper {
    
    /**
     * 查询最新的5个营期（按开营时间倒序）
     * 用于首页热门课程展示
     */
    @Select("SELECT camp_id, type_id, name, intro, start_time, end_time, status " +
            "FROM t_camp " +
            "ORDER BY start_time DESC " +
            "LIMIT 5")
    List<Camp> selectHotCamps();
    
    /**
     * 查询热门课程推荐（联表查询）
     * 按开营时间倒序，取最新的5条数据
     * @return 热门课程VO列表
     */
    @Select("SELECT c.camp_id AS id, " +
            "       c.tag, " +
            "       ct.level_name AS type, " +
            "       CONCAT('第', c.term, '期') AS term, " +
            "       c.name AS title, " +
            "       FORMAT(c.enroll_count, 0) AS count " +
            "FROM t_camp c " +
            "JOIN t_camp_type ct ON c.type_id = ct.type_id " +
            "ORDER BY c.start_time DESC " +
            "LIMIT 5")
    List<CampVO> selectHotCourses();
    
    /**
     * 根据ID查询营期详情
     */
    @Select("SELECT * FROM t_camp WHERE camp_id = #{campId}")
    Camp selectById(Integer campId);
    
    /**
     * 查询所有营期
     */
    @Select("SELECT * FROM t_camp")
    List<Camp> selectAll();
    
    /**
     * 查询最近活跃的 5 个营期（用于仪表盘展示）
     * 按开营时间倒序排列，返回最新 5 条记录
     * @return 最近活跃营期列表
     */
    @Select("SELECT camp_id, name, status, start_time, enroll_count " +
            "FROM t_camp " +
            "ORDER BY start_time DESC " +
            "LIMIT 5")
    List<Camp> selectRecentCamps();
    
    /**
     * 查询营期列表总数（支持条件过滤）
     * @param keyword 关键词（营期名称模糊匹配）
     * @param status 状态精确匹配
     * @param typeId 体系类型 ID 精确匹配
     * @return 总记录数
     */
    int countCampList(@Param("keyword") String keyword, @Param("status") Integer status, @Param("typeId") Integer typeId);
    
    /**
     * 分页查询营期列表（联表查询类型名称）
     * 按开营时间倒序排列
     * @param keyword 关键词（营期名称模糊匹配）
     * @param status 状态精确匹配
     * @param typeId 体系类型 ID 精确匹配
     * @param offset 偏移量
     * @param limit 每页大小
     * @return 营期列表项 DTO 列表
     */
    List<CampListItemDTO> selectCampList(
        @Param("keyword") String keyword,
        @Param("status") Integer status,
        @Param("typeId") Integer typeId,
        @Param("offset") int offset,
        @Param("limit") int limit
    );
    
    /**
     * 查询所有营期类型（用于下拉选项）
     * @return 营期类型列表
     */
    List<CampTypeOptionDTO> selectAllCampTypes();
}