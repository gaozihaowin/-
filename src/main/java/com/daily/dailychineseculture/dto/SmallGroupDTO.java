package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 小组数据传输对象
 */
@Data
public class SmallGroupDTO {
    /**
     * 小组ID
     */
    private Integer smallGroupId;

    /**
     * 小组名称
     */
    private String name;

    /**
     * 大组名称
     */
    private String bigGroupName;

    /**
     * 大组ID
     */
    private Integer bigGroupId;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 营期名称
     */
    private String campName;

    public SmallGroupDTO() {}

    public SmallGroupDTO(Integer smallGroupId, String name, String bigGroupName, Integer bigGroupId, String className, String campName) {
        this.smallGroupId = smallGroupId;
        this.name = name;
        this.bigGroupName = bigGroupName;
        this.bigGroupId = bigGroupId;
        this.className = className;
        this.campName = campName;
    }
}