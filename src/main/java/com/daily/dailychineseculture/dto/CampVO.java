package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 热门课程VO类
 * 用于首页热门课程推荐展示
 */
@Data
public class CampVO {
    /**
     * 营期ID
     */
    private Integer id;
    
    /**
     * 营销角标
     */
    private String tag;
    
    /**
     * 班级类型名称
     */
    private String type;
    
    /**
     * 期数（格式化为"第X期"）
     */
    private String term;
    
    /**
     * 课程标题
     */
    private String title;
    
    /**
     * 报名人数（格式化为千分位）
     */
    private String count;
}