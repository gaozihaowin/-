package com.daily.dailychineseculture.entity;

import lombok.Data;
import java.util.Date;

/**
 * 职位分配实体类
 * 对应数据库表：t_duty_assignment
 */
@Data
public class DutyAssignment {
    
    /**
     * 职位分配 ID（主键）
     */
    private Integer assignmentId;
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 营期 ID（全局管理员为 null）
     */
    private Integer campId;
    
    /**
     * 职位类型代码
     * COURSE_ADMIN(课程管理), ARCHIVE_ADMIN(档案管理), VOLUNTEER(志愿者) 等
     */
    private String dutyType;
    
    /**
     * 职位名称
     */
    private String dutyName;
    
    /**
     * 任职开始时间
     */
    private Date startTime;
    
    /**
     * 任职结束时间（null 表示永久）
     */
    private Date endTime;
    
    /**
     * 志愿者服务开始时间
     */
    private Date volunteerStartTime;
    
    /**
     * 志愿者服务结束时间
     */
    private Date volunteerEndTime;
    
    /**
     * 创建时间
     */
    private Date createTime;
}
