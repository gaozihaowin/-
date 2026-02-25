package com.daily.dailychineseculture.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;
import java.util.Date;

/**
 * 营期实体类
 * 对应数据库表: t_camp
 */
@Data
@Alias("Camp")
public class Camp {
    /**
     * 营期ID
     */
    private Integer campId;
    
    /**
     * 营期类型ID
     */
    private Integer typeId;
    
    /**
     * 营期名称
     */
    private String name;
    
    /**
     * 营期介绍
     */
    private String intro;
    
    /**
     * 开营时间
     */
    private Date startTime;
    
    /**
     * 结营时间
     */
    private Date endTime;
    
    /**
     * 状态: 0未开始, 1进行中, 2已结束
     */
    private Integer status;
}