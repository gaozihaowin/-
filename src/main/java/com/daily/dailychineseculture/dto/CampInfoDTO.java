package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 营期详情信息 DTO
 * 用于课程详情页顶部信息栏展示
 */
@Data
public class CampInfoDTO {
    /**
     * 营期 ID
     */
    private Integer campId;
    
    /**
     * 期数（原始值，来自数据库 t_camp.term）
     */
    private Integer term;
    
    /**
     * 营期名称（来自类型表）如：诚意班、明伦班等
     */
    private String campName;
    
    /**
     * 期数，格式："第 69 期"
     */
    private String batch;
    
    /**
     * 课程标题（来自营期表 name）
     */
    private String title;
    
    /**
     * 课程介绍（原始值，来自数据库 t_camp.intro）
     */
    private String intro;
    
    /**
     * 课程描述（来自营期表 intro）
     */
    private String description;
    
    /**
     * 参与人数（来自营期表 enroll_count）
     */
    private Integer participantCount;
    
    /**
     * 营期类型级别（来自类型表 level）如：CY, ML, DX, YZ, LZ
     */
    private String campType;
    
    /**
     * 标签（来自营期表 tag）如：热招、已结营等
     */
    private String tag;
}
