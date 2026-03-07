package com.daily.dailychineseculture.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 营期类型实体类
 * 对应数据库表：t_camp_type
 */
@Data
@Alias("CampType")
public class CampType {
    /**
     * 类型 ID
     */
    private Integer typeId;
    
    /**
     * 等级名称（如：明理班、诚意班等）
     */
    private String levelName;
}
