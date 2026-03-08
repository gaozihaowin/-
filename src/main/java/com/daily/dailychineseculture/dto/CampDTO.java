package com.daily.dailychineseculture.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 营期管理 DTO
 * 用于新增和编辑营期信息
 */
@Data
public class CampDTO {
    /**
     * 营期 ID（编辑时必填，新增时不填）
     */
    private Integer campId;
    
    /**
     * 营期类型 ID
     */
    private Integer typeId;
    
    /**
     * 期数
     */
    private Integer term;
    
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
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    
    /**
     * 结营时间
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    
    /**
     * 状态：0 未开始，1 进行中，2 已结束
     */
    private Integer status;
    
    /**
     * 标签
     */
    private String tag;
}
