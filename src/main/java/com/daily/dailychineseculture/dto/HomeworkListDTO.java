package com.daily.dailychineseculture.dto;

import lombok.Data;

import java.util.Date;
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
         * 是否小组优秀作业
         */
        private Integer isSmallGroupExcellent; // 0否，1是


        /**
         * 是否大组优秀作业
         */
        private Integer isBigGroupExcellent;   // 0否，1是

        /**
         * 提交时间
         */
        private Date submitTime;

        /**
         * 所属分组
         */
        private String organization;

        public HomeworkItem() {}

        public HomeworkItem(Integer homeworkId, String name, Integer isSmallGroupExcellent, Integer isBigGroupExcellent, Date submitTime, String organization) {
            this.homeworkId = homeworkId;
            this.name = name;
            this.isSmallGroupExcellent = isSmallGroupExcellent;
            this.isBigGroupExcellent = isBigGroupExcellent;
            this.submitTime = submitTime;
            this.organization = organization;
        }
    }
}