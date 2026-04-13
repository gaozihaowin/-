package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 课件分页结果 DTO
 */
@Data
public class CourseMaterialPageResultDTO {

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页数据列表
     */
    private Object list;
}
