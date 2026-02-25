package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 我的课程VO类
 * 用于"我的课程"页面数据展示
 * 
 * @author Java后端架构师
 * @since 2026-02-25
 */
@Data
public class MyCourseVO {
    
    /**
     * 课程ID
     */
    private Long id;
    
    /**
     * 状态编码
     * ing: 学习中
     * hist: 已结营
     * done: 已结业
     */
    private String status;
    
    /**
     * 状态文本描述
     */
    private String statusText;
    
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
     * 更新日期（格式化为yyyy-MM-dd）
     */
    private String updateDate;
    
    /**
     * 学习进度（0-100）
     */
    private Integer progress;
}