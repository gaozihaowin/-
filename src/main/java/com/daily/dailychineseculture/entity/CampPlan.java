package com.daily.dailychineseculture.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;
import java.util.Date;

/**
 * 排课计划实体类
 * 对应数据库表：t_camp_plan
 */
@Data
@Alias("CampPlan")
public class CampPlan {
    /**
     * 计划 ID
     */
    private Integer planId;
    
    /**
     * 营期 ID
     */
    private Integer campId;
    
    /**
     * 第几天
     */
    private Integer dayIndex;
    
    /**
     * 具体日期
     */
    private Date planDate;
    
    /**
     * 导读标题
     */
    private String title;
    
    /**
     * 视频链接
     */
    private String videoUrl;
    
    /**
     * 图文链接
     */
    private String graphicUrl;
    
    /**
     * 模块索引（第几周）
     */
  private Integer moduleIndex;
    
    /**
     * 模块名称
     */
  private String moduleName;
    
    /**
     * 阅读篇目
     */
  private String readingTitle;
    
    /**
     * 讲师姓名
     */
  private String teacherName;
    
    /**
     * 视频时长（分钟）
     */
  private Integer videoDuration;
}
