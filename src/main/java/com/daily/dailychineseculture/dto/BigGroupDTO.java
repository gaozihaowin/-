package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 大组数据传输对象
 */
@Data
public class BigGroupDTO {
    /**
     * 大组ID
     */
    private Integer bigGroupId;

    /**
     * 大组名称
     */
    private String name;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 班级ID
     */
    private Integer classId;

    /**
     * 营期名称
     */
    private String campName;

    public BigGroupDTO() {}

    public BigGroupDTO(Integer bigGroupId, String name, String className, Integer classId, String campName) {
        this.bigGroupId = bigGroupId;
        this.name = name;
        this.className = className;
        this.classId = classId;
        this.campName = campName;
    }
}