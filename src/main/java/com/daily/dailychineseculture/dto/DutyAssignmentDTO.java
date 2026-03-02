package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.List;

/**
 * 分配岗位DTO
 */
@Data
public class DutyAssignmentDTO {

    /**
     * 当前用户的管理范围
     */
    private ManagementScope managementScope;

    /**
     * 可分配的岗位列表
     */
    private List<AssignableDuty> assignableDuties;

    /**
     * 空缺岗位列表
     */
    private List<VacantDuty> vacantDuties;

    /**
     * 管理范围
     */
    @Data
    public static class ManagementScope {
        private Integer campId;
        private String campName;
        private Integer classId;
        private String className;
        private Integer bigGroupId;
        private String bigGroupName;
        private String dutyType; // 当前用户的职位类型
    }

    /**
     * 可分配的岗位
     */
    // 在AssignableDuty类中添加assignmentId字段
    @Data
    public static class AssignableDuty {
        private String targetType;
        private Integer targetId;
        private String targetName;
        private String dutyType;
        private String dutyName;
        private Boolean isVacant;
        private Long currentUserId;
        private String currentUsername;
        private Integer assignmentId;
    }

    /**
     * 空缺岗位
     */
    @Data
    public static class VacantDuty {
        private String targetType; // class/big_group/small_group
        private Integer targetId;
        private String targetName;
        private String dutyType; // 空缺的职位类型
        private String dutyName; // 职位名称
        private String vacancyReason; // 空缺原因
    }


}