package com.daily.dailychineseculture.dto;

import lombok.Data;

/**
 * 班级数据传输对象
 */
@Data
public class ClassDTO {
    /**
     * 班级ID
     */
    private Integer classId;

    /**
     * 班级名称
     */
    private String name;

    /**
     * 营期名称
     */
    private String campName;

    /**
     * 营期ID
     */
    private Integer campId;

    public ClassDTO() {}

    public ClassDTO(Integer classId, String name, String campName, Integer campId) {
        this.classId = classId;
        this.name = name;
        this.campName = campName;
        this.campId = campId;
    }
}