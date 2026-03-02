package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.List;

/**
 * 管理成员DTO
 */
@Data
public class MemberManageDTO {

    /**
     * 营期信息
     */
    private CampInfo campInfo;

    /**
     * 班级列表
     */
    private List<ClassInfo> classList;

    /**
     * 大组列表
     */
    private List<BigGroupInfo> bigGroupList;

    /**
     * 小组列表
     */
    private List<SmallGroupInfo> smallGroupList;

    /**
     * 营期信息
     */
    @Data
    public static class CampInfo {
        private Integer campId;
        private String campName;
        private String status;
    }

    /**
     * 班级信息
     */
    @Data
    public static class ClassInfo {
        private Integer classId;
        private String className;
        private List<MemberInfo> members;
    }

    /**
     * 大组信息
     */
    @Data
    public static class BigGroupInfo {
        private Integer bigGroupId;
        private String bigGroupName;
        private Integer classId;
        private String className;
        private List<MemberInfo> members;
    }

    /**
     * 小组信息
     */
    @Data
    public static class SmallGroupInfo {
        private Integer smallGroupId;
        private String smallGroupName;
        private Integer bigGroupId;
        private String bigGroupName;
        private Integer classId;
        private String className;
        private List<MemberInfo> members;
    }

    /**
     * 成员信息
     */
    @Data
    public static class MemberInfo {
        private String account;      // 账户名
        private String nickname;     // 昵称
        private String username;     // 保持兼容性
        private String avatar;
        private String phone;
        private Integer gender;
        private String birthday;
        private Integer age;         // 年龄
        private String region;
        private String occupation;
        private String campName;     // 营期名称
        private String className;    // 班级名称
        private String bigGroupName; // 大组名称
        private String smallGroupName; // 小组名称
        private String status;
    }
}