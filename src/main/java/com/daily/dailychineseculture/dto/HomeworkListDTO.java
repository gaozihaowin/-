package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.List;

/**
 * 作业列表DTO
 */
@Data
public class HomeworkListDTO {
    /**
     * 作业列表
     */
    private List<HomeworkItem> list;

    /**
     * 总数
     */
    private Integer total;

    /**
     * 作业项
     */
    @Data
    public static class HomeworkItem {
        /**
         * 作业ID
         */
        private Integer homeworkId;

        /**
         * 学生姓名
         */
        private String name;

        /**
         * 是否优秀作业
         */
        private Boolean isExcellent;

        /**
         * 提交时间
         */
        private String submitTime;

        /**
         * 所属分组
         */
        private String organization;

        public HomeworkItem() {}

        public HomeworkItem(Integer homeworkId, String name, Boolean isExcellent, String submitTime, String organization) {
            this.homeworkId = homeworkId;
            this.name = name;
            this.isExcellent = isExcellent;
            this.submitTime = submitTime;
            this.organization = organization;
        }
    }
}