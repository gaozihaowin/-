package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.CampDTO;
import com.daily.dailychineseculture.dto.CampListPageDTO;
import com.daily.dailychineseculture.dto.CampTypeOptionDTO;
import com.daily.dailychineseculture.dto.CampVO;
import com.daily.dailychineseculture.dto.RecentCampDTO;
import com.daily.dailychineseculture.entity.Camp;

import java.util.List;

/**
 * 营期服务接口
 */
public interface CampService {
    
    /**
     * 获取热门营期列表（最新的5个）
     * @return 热门营期列表
     */
    List<Camp> getHotCamps();
    
    /**
     * 获取热门课程推荐列表
     * 联表查询t_camp和t_camp_type，按开营时间倒序取最新的5条
     * @return 热门课程VO列表
     */
    List<CampVO> getHotCourses();
    
    /**
     * 根据ID获取营期详情
     * @param campId 营期ID
     * @return 营期详情
     */
    Camp getCampById(Integer campId);
    
    /**
     * 获取所有营期
     * @return 所有营期列表
     */
    List<Camp> getAllCamps();
    
    /**
     * 获取最近活跃的课程列表（用于仪表盘）
     * 按开营时间倒序取最新 5 条记录，并进行状态字典转换
     * @return 最近活跃课程 DTO 列表
     */
    List<RecentCampDTO> getRecentCamps();
    
    /**
     * 分页查询营期列表（支持条件过滤）
     * @param page 当前页码（从 1 开始）
     * @param size 每页大小
     * @param keyword 关键词（可选，模糊匹配营期名称）
     * @param status 状态（可选，精确匹配）
     * @param typeId 体系类型 ID（可选，精确匹配）
     * @return 分页结果
     */
    CampListPageDTO getCampList(Integer page, Integer size, String keyword, Integer status, Integer typeId);
    
    /**
     * 获取所有营期类型（用于下拉选项）
     * @return 营期类型列表
     */
    List<CampTypeOptionDTO> getAllCampTypes();
    
    /**
     * 新增营期
     * @param campDTO 营期 DTO
     */
    void addCamp(CampDTO campDTO);
    
    /**
     * 编辑营期
     * @param campDTO 营期 DTO（必须包含 campId）
     */
    void updateCamp(CampDTO campDTO);
}