package com.daily.dailychineseculture.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;
import java.util.Date;

/**
 * 课程实体类
 * 对应数据库表: t_camp
 */
@Data
@Alias("Course")
public class Course {
    
    /**
     * 课程ID (对应 camp_id)
     */
    private Integer id;
    
    /**
     * 课程标题 (对应 name)
     */
    private String title;
    
    /**
     * 营期名称 (对应 name)
     */
    private String campName;
    
    /**
     * 批次/期数 (CONCAT('第', term, '期'))
     */
    private String batch;
    
    /**
     * 课程描述 (对应 intro)
     */
    private String description;
    
    /**
     * 参与人数 (对应 enroll_count)
     */
    private Integer participantCount;
    
    /**
     * 状态：1=招生中/开课中，0=已结束，-1=下架
     */
    private Integer status;
    
    /**
     * 开始时间 (对应 start_time)
     */
    private Date startTime;
    
    /**
     * 结束时间 (对应 end_time)
     */
    private Date endTime;
}
