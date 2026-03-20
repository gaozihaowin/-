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
     * 模块索引（第几周）
     */
    private Integer moduleIndex;
    
    /**
     * 模块名称
     */
    private String moduleName;
    
    /**
     * 讲师姓名
     */
    private String teacherName;
    
    /**
     * 是否完成：0-未完成，1-已完成
     */
    private Integer isFinished;
}
