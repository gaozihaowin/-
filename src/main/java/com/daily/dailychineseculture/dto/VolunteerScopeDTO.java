package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.List;

/**
 * 志愿者管理范围DTO
 */
@Data
public class VolunteerScopeDTO {
    /**
     * 班级列表
     */
    private List<ScopeItem> classList;

    /**
     * 大组列表
     */
    private List<ScopeItem> bigGroupList;

    /**
     * 小组列表
     */
    private List<ScopeItem> smallGroupList;

    /**
     * 范围项
     */
    @Data
    public static class ScopeItem {
        /**
         * 目标ID
         */
        private Integer id;

        /**
         * 名称
         */
        private String name;

        public ScopeItem() {}

        public ScopeItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}