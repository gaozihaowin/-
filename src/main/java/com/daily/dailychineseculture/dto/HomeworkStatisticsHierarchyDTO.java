package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.List;

/**
 * 作业统计层级DTO
 */
@Data
public class HomeworkStatisticsHierarchyDTO {
    /**
     * 层级列表
     */
    private List<StatisticsItem> list;

    /**
     * 统计项
     */
    @Data
    public static class StatisticsItem {
        /**
         * ID
         */
        private Integer id;

        /**
         * 名称
         */
        private String name;

        /**
         * 类型：class/bigGroup/smallGroup
         */
        private String type;

        /**
         * 总人数
         */
        private Integer totalCount;

        /**
         * 已交人数
         */
        private Integer completedCount;

        /**
         * 未交人数
         */
        private Integer pendingCount;

        /**
         * 迟交人数
         */
        private Integer lateCount;

        /**
         * 完成率
         */
        private Double completionRate;

        /**
         * 按时提交率
         */
        private Double onTimeRate;

        /**
         * 是否有作业
         */
        private Boolean hasHomework;

        /**
         * 子级统计列表
         */
        private List<StatisticsItem> children;
    }
}