package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.CampVO;
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
}