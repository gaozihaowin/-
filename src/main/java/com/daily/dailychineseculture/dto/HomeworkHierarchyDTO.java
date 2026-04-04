package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.List;

/**
 * 作业层级列表DTO
 * 支持大组-小组-成员的层级展示
 */
@Data
public class HomeworkHierarchyDTO {

    /**
     * 层级列表
     */
    private List<HierarchyItem> list;

    /**
     * 层级项
     */
    @Data
    public static class HierarchyItem {
        /**
         * 层级ID
         */
        private Long id;

        /**
         * 层级名称
         */
        private String name;

        /**
         * 层级类型（class/bigGroup/smallGroup/member）
         */
        private String type;

        /**
         * 父级ID
         */
        private Long parentId;

        /**
         * 子层级列表
         */
        private List<HierarchyItem> children;

        /**
         * 是否可展开（有子层级）
         */
        private Boolean expandable;
    }
}