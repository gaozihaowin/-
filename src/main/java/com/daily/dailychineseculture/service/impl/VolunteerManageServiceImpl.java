package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.MemberManageDTO;
import com.daily.dailychineseculture.dto.DutyAssignmentDTO;
import com.daily.dailychineseculture.mapper.VolunteerManageMapper;
import com.daily.dailychineseculture.service.VolunteerManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 志愿者管理服务实现类
 */
@Service
public class VolunteerManageServiceImpl implements VolunteerManageService {

    @Autowired
    private VolunteerManageMapper volunteerManageMapper;

    @Override
    public List<Map<String, Object>> getManagementScopes(Long userId) {
        // 直接从数据库查询真实的管理范围数据
        return volunteerManageMapper.getManagementScope(userId);
    }

    @Override
    public MemberManageDTO getMemberManageInfo(Long userId, Integer assignmentId, Integer smallGroupId) {
        MemberManageDTO result = new MemberManageDTO();
        System.out.println("获取成员信息 - userId: " + userId + ", assignmentId: " + assignmentId + ", smallGroupId: " + smallGroupId);

        // 如果指定了smallGroupId，直接获取该小组的成员列表
        if (smallGroupId != null) {
            List<Map<String, Object>> smallGroupMembers = volunteerManageMapper.getSmallGroupMembers(smallGroupId);
            System.out.println("小组成员查询结果: " + smallGroupMembers);
            if (smallGroupMembers != null && !smallGroupMembers.isEmpty()) {
                MemberManageDTO.SmallGroupInfo smallGroupInfo = new MemberManageDTO.SmallGroupInfo();
                smallGroupInfo.setSmallGroupId(smallGroupId);
                smallGroupInfo.setSmallGroupName("小组" + smallGroupId);
                smallGroupInfo.setMembers(smallGroupMembers.stream()
                        .map(this::convertToMemberInfo)
                        .collect(Collectors.toList()));
                result.setSmallGroupList(List.of(smallGroupInfo));
            }
            return result;
        }

        // 获取当前用户的管理范围
        List<Map<String, Object>> scopeList = volunteerManageMapper.getManagementScope(userId);
        System.out.println("管理范围: " + scopeList);
        if (scopeList.isEmpty()) {
            return result;
        }

        // 如果指定了assignmentId，则只显示该管理范围
        Map<String, Object> targetScope;
        if (assignmentId != null) {
            targetScope = scopeList.stream()
                    .filter(scope -> assignmentId.equals(scope.get("assignmentId")))
                    .findFirst()
                    .orElse(scopeList.get(0));
        } else {
            targetScope = scopeList.get(0);
        }

        // 设置营期信息
        MemberManageDTO.CampInfo campInfo = new MemberManageDTO.CampInfo();
        campInfo.setCampId(getSafeInteger(targetScope, "campId"));
        campInfo.setCampName(getSafeString(targetScope, "campName"));
        campInfo.setStatus("进行中");
        result.setCampInfo(campInfo);

        // 根据用户职位类型获取不同的成员信息
        String dutyType = getSafeString(targetScope, "dutyType");

        if ("学班".equals(dutyType) || "检班".equals(dutyType)) {
            // 班级管理者：获取班级成员
            Integer classId = getSafeInteger(targetScope, "classId");
            System.out.println("查询班级成员 - classId: " + classId);
            if (classId != null) {
                List<Map<String, Object>> classMembers = volunteerManageMapper.getClassMembers(classId);
                System.out.println("班级成员查询结果: " + classMembers);
                if (classMembers != null && !classMembers.isEmpty()) {
                    MemberManageDTO.ClassInfo classInfo = new MemberManageDTO.ClassInfo();
                    classInfo.setClassId(classId);
                    classInfo.setClassName(getSafeString(targetScope, "className"));
                    classInfo.setMembers(classMembers.stream()
                            .map(this::convertToMemberInfo)
                            .collect(Collectors.toList()));

                    result.setClassList(List.of(classInfo));
                }
            }
            // 构建层级结构
            buildClassHierarchy(result, targetScope);
        } else if ("学委".equals(dutyType) || "检委".equals(dutyType)) {
            // 大组管理者：获取大组成员
            Integer bigGroupId = getSafeInteger(targetScope, "bigGroupId");
            if (bigGroupId != null) {
                List<Map<String, Object>> bigGroupMembers = volunteerManageMapper.getBigGroupMembers(bigGroupId);

                if (bigGroupMembers != null && !bigGroupMembers.isEmpty()) {
                    MemberManageDTO.BigGroupInfo bigGroupInfo = new MemberManageDTO.BigGroupInfo();
                    bigGroupInfo.setBigGroupId(bigGroupId);
                    bigGroupInfo.setBigGroupName(getSafeString(targetScope, "bigGroupName"));
                    bigGroupInfo.setClassId(getSafeInteger(targetScope, "classId"));
                    bigGroupInfo.setClassName(getSafeString(targetScope, "className"));
                    bigGroupInfo.setMembers(bigGroupMembers.stream()
                            .map(this::convertToMemberInfo)
                            .collect(Collectors.toList()));

                    result.setBigGroupList(List.of(bigGroupInfo));
                }
            }
            // 构建层级结构
            buildBigGroupHierarchy(result, targetScope);
        } else if ("学组".equals(dutyType) || "检组".equals(dutyType)) {
            // 小组管理者：获取小组成员
            Integer sgId = getSafeInteger(targetScope, "smallGroupId");
            if (sgId != null) {
                List<Map<String, Object>> smallGroupMembers = volunteerManageMapper.getSmallGroupMembers(sgId);

                if (smallGroupMembers != null && !smallGroupMembers.isEmpty()) {
                    MemberManageDTO.SmallGroupInfo smallGroupInfo = new MemberManageDTO.SmallGroupInfo();
                    smallGroupInfo.setSmallGroupId(sgId);
                    smallGroupInfo.setSmallGroupName(getSafeString(targetScope, "smallGroupName"));
                    smallGroupInfo.setBigGroupId(getSafeInteger(targetScope, "bigGroupId"));
                    smallGroupInfo.setBigGroupName(getSafeString(targetScope, "bigGroupName"));
                    smallGroupInfo.setClassId(getSafeInteger(targetScope, "classId"));
                    smallGroupInfo.setClassName(getSafeString(targetScope, "className"));
                    smallGroupInfo.setMembers(smallGroupMembers.stream()
                            .map(this::convertToMemberInfo)
                            .collect(Collectors.toList()));

                    result.setSmallGroupList(List.of(smallGroupInfo));
                }
            }
            // 构建层级结构
            buildSmallGroupHierarchy(result, targetScope);
        }

        return result;
    }

    @Override
    public DutyAssignmentDTO getDutyAssignmentInfo(Long userId, Integer assignmentId) {
        DutyAssignmentDTO result = new DutyAssignmentDTO();

        // 获取当前用户的管理范围
        List<Map<String, Object>> scopeList = volunteerManageMapper.getManagementScope(userId);

        if (scopeList.isEmpty()) {
            return result;
        }

        // 如果指定了assignmentId，则只显示该管理范围
        Map<String, Object> targetScope;
        if (assignmentId != null) {
            targetScope = scopeList.stream()
                    .filter(scope -> assignmentId.equals(scope.get("assignmentId")))
                    .findFirst()
                    .orElse(scopeList.get(0));
        } else {
            targetScope = scopeList.get(0);
        }

        String dutyType = getSafeString(targetScope, "dutyType");

        // 设置管理范围
        DutyAssignmentDTO.ManagementScope managementScope = new DutyAssignmentDTO.ManagementScope();
        managementScope.setCampId(getSafeInteger(targetScope, "campId"));
        managementScope.setCampName(getSafeString(targetScope, "campName"));
        managementScope.setClassId(getSafeInteger(targetScope, "classId"));
        managementScope.setClassName(getSafeString(targetScope, "className"));
        managementScope.setBigGroupId(getSafeInteger(targetScope, "bigGroupId"));
        managementScope.setBigGroupName(getSafeString(targetScope, "bigGroupName"));
        managementScope.setDutyType(dutyType);
        result.setManagementScope(managementScope);

        // 根据职位类型获取可分配的岗位
        if ("学班".equals(dutyType) || "检班".equals(dutyType)) {
            // 班级管理者：可以分配大组的学委/检委，以及大组下面小组的学组/检组
            Integer classId = getSafeInteger(targetScope, "classId");
            if (classId != null) {
                List<Map<String, Object>> bigGroups = volunteerManageMapper.getAssignableBigGroups(classId);

                if (bigGroups != null && !bigGroups.isEmpty()) {
                    List<DutyAssignmentDTO.AssignableDuty> assignableDuties = bigGroups.stream()
                            .flatMap(bg -> {
                                Integer bigGroupId = getSafeInteger(bg, "targetId");
                                String bigGroupName = getSafeString(bg, "targetName");

                                // 检查学委岗位
                                List<Map<String, Object>> xueweiVolunteers = volunteerManageMapper.getCurrentVolunteers("big_group", bigGroupId);
                                Map<String, Object> xuewei = xueweiVolunteers != null ? xueweiVolunteers.stream()
                                        .filter(v -> "学委".equals(v.get("dutyType")))
                                        .findFirst().orElse(null) : null;

                                // 检查检委岗位
                                List<Map<String, Object>> jianweiVolunteers = volunteerManageMapper.getCurrentVolunteers("big_group", bigGroupId);
                                Map<String, Object> jianwei = jianweiVolunteers != null ? jianweiVolunteers.stream()
                                        .filter(v -> "检委".equals(v.get("dutyType")))
                                        .findFirst().orElse(null) : null;

                                // 获取该大组下面的小组列表
                                List<Map<String, Object>> smallGroups = volunteerManageMapper.getAssignableSmallGroups(bigGroupId);
                                List<DutyAssignmentDTO.AssignableDuty> smallGroupDuties = smallGroups != null ? smallGroups.stream()
                                        .flatMap(sg -> {
                                            Integer smallGroupId = getSafeInteger(sg, "targetId");
                                            String smallGroupName = getSafeString(sg, "targetName");

                                            // 检查学组岗位
                                            List<Map<String, Object>> xuezuVolunteers = volunteerManageMapper.getCurrentVolunteers("small_group", smallGroupId);
                                            Map<String, Object> xuezu = xuezuVolunteers != null ? xuezuVolunteers.stream()
                                                    .filter(v -> "学组".equals(v.get("dutyType")))
                                                    .findFirst().orElse(null) : null;

                                            // 检查检组岗位
                                            List<Map<String, Object>> jianzuVolunteers = volunteerManageMapper.getCurrentVolunteers("small_group", smallGroupId);
                                            Map<String, Object> jianzu = jianzuVolunteers != null ? jianzuVolunteers.stream()
                                                    .filter(v -> "检组".equals(v.get("dutyType")))
                                                    .findFirst().orElse(null) : null;
                                            // 为小组名称添加大组前缀
                                            String fullSmallGroupName = bigGroupName + " - " + smallGroupName;
                                            return List.of(
                                                    createAssignableDuty("small_group", smallGroupId, fullSmallGroupName, "学组", xuezu),
                                                    createAssignableDuty("small_group", smallGroupId, fullSmallGroupName, "检组", jianzu)
                                            ).stream();
                                        })
                                        .collect(Collectors.toList()) : List.of();

                                // 合并大组和小组的可分配岗位
                                List<DutyAssignmentDTO.AssignableDuty> allDuties = new ArrayList<>();
                                allDuties.add(createAssignableDuty("big_group", bigGroupId, bigGroupName, "学委", xuewei));
                                allDuties.add(createAssignableDuty("big_group", bigGroupId, bigGroupName, "检委", jianwei));
                                allDuties.addAll(smallGroupDuties);

                                return allDuties.stream();
                            })
                            .collect(Collectors.toList());

                    result.setAssignableDuties(assignableDuties);
                }
            }
        }
        else if ("学委".equals(dutyType) || "检委".equals(dutyType)) {
            // 大组管理者：可以分配小组的学组/检组
            Integer bigGroupId = getSafeInteger(targetScope, "bigGroupId");
            if (bigGroupId != null) {
                List<Map<String, Object>> smallGroups = volunteerManageMapper.getAssignableSmallGroups(bigGroupId);
                if (smallGroups != null && !smallGroups.isEmpty()) {
                    String bigGroupName = getSafeString(targetScope, "bigGroupName");
                    List<DutyAssignmentDTO.AssignableDuty> assignableDuties = smallGroups.stream()
                            .flatMap(sg -> {
                                Integer smallGroupId = getSafeInteger(sg, "targetId");
                                String smallGroupName = getSafeString(sg, "targetName");
                                // 检查学组岗位
                                List<Map<String, Object>> xuezuVolunteers = volunteerManageMapper.getCurrentVolunteers("small_group", smallGroupId);
                                Map<String, Object> xuezu = xuezuVolunteers != null ? xuezuVolunteers.stream()
                                        .filter(v -> "学组".equals(v.get("dutyType")))
                                        .findFirst().orElse(null) : null;
                                // 检查检组岗位
                                List<Map<String, Object>> jianzuVolunteers = volunteerManageMapper.getCurrentVolunteers("small_group", smallGroupId);
                                Map<String, Object> jianzu = jianzuVolunteers != null ? jianzuVolunteers.stream()
                                        .filter(v -> "检组".equals(v.get("dutyType")))
                                        .findFirst().orElse(null) : null;
                                // 为小组名称添加大组前缀
                                String fullSmallGroupName = bigGroupName + " - " + smallGroupName;
                                return List.of(
                                        createAssignableDuty("small_group", smallGroupId, fullSmallGroupName, "学组", xuezu),
                                        createAssignableDuty("small_group", smallGroupId, fullSmallGroupName, "检组", jianzu)
                                ).stream();
                            })
                            .collect(Collectors.toList());
                    result.setAssignableDuties(assignableDuties);
                }
            }
        }


        return result;
    }

    @Override
    public boolean assignDuty(Long managerUserId, Long targetUserId, String targetType,
                              Integer targetId, String dutyType) {
        try {
            // 调试信息
            System.out.println("分配岗位参数:");
            System.out.println("managerUserId: " + managerUserId);
            System.out.println("targetUserId: " + targetUserId);
            System.out.println("targetType: " + targetType);
            System.out.println("targetId: " + targetId);
            System.out.println("dutyType: " + dutyType);

            // 检查目标用户是否已担任该职位
            List<Map<String, Object>> existingDuties = volunteerManageMapper.getCurrentVolunteers(targetType, targetId);
            boolean alreadyAssigned = existingDuties.stream()
                    .anyMatch(duty -> dutyType.equals(duty.get("dutyType")) && targetUserId.equals(duty.get("userId")));

            if (alreadyAssigned) {
                System.out.println("用户已担任该职位");
                return false;
            }

            // 获取当前营期ID（从管理者的管理范围中获取）
            List<Map<String, Object>> managerScopes = volunteerManageMapper.getManagementScope(managerUserId);
            if (managerScopes.isEmpty()) {
                System.out.println("管理者没有管理范围");
                return false;
            }

            System.out.println("管理范围: " + managerScopes);

            // 验证管理者是否有权限分配该岗位
            boolean hasPermission = managerScopes.stream().anyMatch(scope -> {
                String scopeTargetType = getSafeString(scope, "targetType");
                Integer scopeTargetId = getSafeInteger(scope, scopeTargetType + "Id");

                // 班级管理者可以分配大组和小组岗位
                if ("class".equals(scopeTargetType)) {
                    return true; // 班级管理者有所有权限
                }
                // 大组管理者只能分配小组岗位
                else if ("big_group".equals(scopeTargetType) && "small_group".equals(targetType)) {
                    return true;
                }
                // 小组管理者没有分配权限
                return false;
            });

            if (!hasPermission) {
                System.out.println("管理者没有权限分配该岗位");
                return false;
            }

            Integer campId = getSafeInteger(managerScopes.get(0), "campId");
            if (campId == null) {
                System.out.println("无法获取营期ID");
                return false;
            }

            System.out.println("营期ID: " + campId);

            // 分配新职位
            Integer result = volunteerManageMapper.assignDuty(targetUserId, campId, dutyType);
            System.out.println("分配结果: " + result);

            if (result > 0) {
                // 获取新分配的assignment_id
                Integer assignmentId = volunteerManageMapper.getLastInsertId();
                System.out.println("新分配的assignment_id: " + assignmentId);

                // 添加职责范围
                Integer scopeResult = volunteerManageMapper.addDutyScope(assignmentId, targetId, targetType);
                System.out.println("添加职责范围结果: " + scopeResult);

                return scopeResult > 0;
            }

            return false;
        } catch (Exception e) {
            System.out.println("分配岗位异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeDuty(Long managerUserId, Integer assignmentId) {
        try {
            // 移除职位
            return volunteerManageMapper.removeDuty(assignmentId) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将Map转换为MemberInfo对象
     */
    private MemberManageDTO.MemberInfo convertToMemberInfo(Map<String, Object> memberMap) {
        MemberManageDTO.MemberInfo memberInfo = new MemberManageDTO.MemberInfo();
        try {
            memberInfo.setAccount(getSafeString(memberMap, "account"));      // 账户名
            memberInfo.setNickname(getSafeString(memberMap, "nickname"));   // 昵称
            memberInfo.setUsername(getSafeString(memberMap, "account"));    // 用账户名作为显示名
            memberInfo.setAvatar(getSafeString(memberMap, "avatar"));
            memberInfo.setPhone(getSafeString(memberMap, "phone"));
            memberInfo.setGender(getSafeInteger(memberMap, "gender"));
            memberInfo.setBirthday(getSafeString(memberMap, "birthday"));
            memberInfo.setAge(getSafeInteger(memberMap, "age"));             // 年龄
            memberInfo.setRegion(getSafeString(memberMap, "region"));
            memberInfo.setOccupation(getSafeString(memberMap, "occupation"));
            memberInfo.setCampName(getSafeString(memberMap, "campName"));    // 营期名称
            memberInfo.setClassName(getSafeString(memberMap, "className"));  // 班级名称
            memberInfo.setBigGroupName(getSafeString(memberMap, "bigGroupName")); // 大组名称
            memberInfo.setSmallGroupName(getSafeString(memberMap, "smallGroupName")); // 小组名称
            memberInfo.setStatus(getSafeString(memberMap, "status"));

        } catch (Exception e) {
            // 转换失败，返回空对象
            e.printStackTrace();
        }

        return memberInfo;
    }

    /**
     * 创建可分配岗位对象
     */
    private DutyAssignmentDTO.AssignableDuty createAssignableDuty(String targetType, Integer targetId,
                                                                  String targetName, String dutyType,
                                                                  Map<String, Object> currentVolunteer) {
        DutyAssignmentDTO.AssignableDuty duty = new DutyAssignmentDTO.AssignableDuty();
        duty.setTargetType(targetType);
        duty.setTargetId(targetId);
        duty.setTargetName(targetName);
        duty.setDutyType(dutyType);
        duty.setDutyName(dutyType);
        duty.setIsVacant(currentVolunteer == null);

        if (currentVolunteer != null) {
            duty.setCurrentUserId(getSafeInteger(currentVolunteer, "userId") != null ? getSafeInteger(currentVolunteer, "userId").longValue() : null);
            duty.setCurrentUsername(getSafeString(currentVolunteer, "username"));
            String username = getSafeString(currentVolunteer, "username");
            String account = getSafeString(currentVolunteer, "account");
            duty.setCurrentUsername(username.isEmpty() ? account : username);
            duty.setAssignmentId(getSafeInteger(currentVolunteer, "assignmentId"));  // 添加这一行
        }

        return duty;
    }

    /**
     * 构建班级层级结构
     */
    private void buildClassHierarchy(MemberManageDTO result, Map<String, Object> targetScope) {
        Integer classId = getSafeInteger(targetScope, "classId");
        if (classId == null) return;

        List<MemberManageDTO.HierarchyItem> hierarchyList = new ArrayList<>();

        // 创建班级节点
        MemberManageDTO.HierarchyItem classNode = new MemberManageDTO.HierarchyItem();
        classNode.setId(classId.longValue());
        classNode.setName(getSafeString(targetScope, "className"));
        classNode.setType("class");
        classNode.setParentId(null);
        classNode.setExpandable(true);
        classNode.setChildren(new ArrayList<>());

        // 获取班级下的大组
        List<Map<String, Object>> bigGroups = volunteerManageMapper.getAssignableBigGroups(classId);
        if (bigGroups != null) {
            for (Map<String, Object> bigGroup : bigGroups) {
                Integer bigGroupId = getSafeInteger(bigGroup, "targetId");
                String bigGroupName = getSafeString(bigGroup, "targetName");

                // 创建大组节点
                MemberManageDTO.HierarchyItem bigGroupNode = new MemberManageDTO.HierarchyItem();
                bigGroupNode.setId(bigGroupId.longValue());
                bigGroupNode.setName(bigGroupName);
                bigGroupNode.setType("bigGroup");
                bigGroupNode.setParentId(classId.longValue());
                bigGroupNode.setExpandable(true);
                bigGroupNode.setChildren(new ArrayList<>());

                // 获取大组下的小组
                List<Map<String, Object>> smallGroups = volunteerManageMapper.getAssignableSmallGroups(bigGroupId);
                if (smallGroups != null) {
                    for (Map<String, Object> smallGroup : smallGroups) {
                        Integer smallGroupId = getSafeInteger(smallGroup, "targetId");
                        String smallGroupName = getSafeString(smallGroup, "targetName");

                        // 创建小组节点
                        MemberManageDTO.HierarchyItem smallGroupNode = new MemberManageDTO.HierarchyItem();
                        smallGroupNode.setId(smallGroupId.longValue());
                        smallGroupNode.setName(smallGroupName);
                        smallGroupNode.setType("smallGroup");
                        smallGroupNode.setParentId(bigGroupId.longValue());
                        smallGroupNode.setExpandable(true);
                        smallGroupNode.setChildren(new ArrayList<>());

                        // 获取小组成员
                        List<Map<String, Object>> members = volunteerManageMapper.getSmallGroupMembers(smallGroupId);
                        if (members != null) {
                            for (Map<String, Object> member : members) {
                                // 创建成员节点
                                MemberManageDTO.HierarchyItem memberNode = new MemberManageDTO.HierarchyItem();
                                memberNode.setId(getSafeString(member, "account").hashCode() * 1000L); // 使用account作为唯一标识
                                memberNode.setName(getSafeString(member, "nickname") != null && !getSafeString(member, "nickname").isEmpty() ? getSafeString(member, "nickname") : getSafeString(member, "account"));
                                memberNode.setType("member");
                                memberNode.setParentId(smallGroupId.longValue());
                                memberNode.setExpandable(false);
                                memberNode.setChildren(null);

                                smallGroupNode.getChildren().add(memberNode);
                            }
                        }

                        bigGroupNode.getChildren().add(smallGroupNode);
                    }
                }

                classNode.getChildren().add(bigGroupNode);
            }
        }

        hierarchyList.add(classNode);
        result.setHierarchyList(hierarchyList);
    }

    /**
     * 构建大组层级结构
     */
    private void buildBigGroupHierarchy(MemberManageDTO result, Map<String, Object> targetScope) {
        Integer bigGroupId = getSafeInteger(targetScope, "bigGroupId");
        if (bigGroupId == null) return;

        List<MemberManageDTO.HierarchyItem> hierarchyList = new ArrayList<>();

        // 创建大组节点
        MemberManageDTO.HierarchyItem bigGroupNode = new MemberManageDTO.HierarchyItem();
        bigGroupNode.setId(bigGroupId.longValue());
        bigGroupNode.setName(getSafeString(targetScope, "bigGroupName"));
        bigGroupNode.setType("bigGroup");
        bigGroupNode.setParentId(null);
        bigGroupNode.setExpandable(true);
        bigGroupNode.setChildren(new ArrayList<>());

        // 获取大组下的小组
        List<Map<String, Object>> smallGroups = volunteerManageMapper.getAssignableSmallGroups(bigGroupId);
        if (smallGroups != null) {
            for (Map<String, Object> smallGroup : smallGroups) {
                Integer smallGroupId = getSafeInteger(smallGroup, "targetId");
                String smallGroupName = getSafeString(smallGroup, "targetName");

                // 创建小组节点
                MemberManageDTO.HierarchyItem smallGroupNode = new MemberManageDTO.HierarchyItem();
                smallGroupNode.setId(smallGroupId.longValue());
                smallGroupNode.setName(smallGroupName);
                smallGroupNode.setType("smallGroup");
                smallGroupNode.setParentId(bigGroupId.longValue());
                smallGroupNode.setExpandable(true);
                smallGroupNode.setChildren(new ArrayList<>());

                // 获取小组成员
                List<Map<String, Object>> members = volunteerManageMapper.getSmallGroupMembers(smallGroupId);
                if (members != null) {
                    for (Map<String, Object> member : members) {
                        // 创建成员节点
                        MemberManageDTO.HierarchyItem memberNode = new MemberManageDTO.HierarchyItem();
                        memberNode.setId(getSafeString(member, "account").hashCode() * 1000L); // 使用account作为唯一标识
                        memberNode.setName(getSafeString(member, "nickname") != null && !getSafeString(member, "nickname").isEmpty() ? getSafeString(member, "nickname") : getSafeString(member, "account"));
                        memberNode.setType("member");
                        memberNode.setParentId(smallGroupId.longValue());
                        memberNode.setExpandable(false);
                        memberNode.setChildren(null);

                        smallGroupNode.getChildren().add(memberNode);
                    }
                }

                bigGroupNode.getChildren().add(smallGroupNode);
            }
        }

        hierarchyList.add(bigGroupNode);
        result.setHierarchyList(hierarchyList);
    }

    /**
     * 构建小组层级结构
     */
    private void buildSmallGroupHierarchy(MemberManageDTO result, Map<String, Object> targetScope) {
        Integer smallGroupId = getSafeInteger(targetScope, "smallGroupId");
        if (smallGroupId == null) return;

        List<MemberManageDTO.HierarchyItem> hierarchyList = new ArrayList<>();

        // 创建小组节点
        MemberManageDTO.HierarchyItem smallGroupNode = new MemberManageDTO.HierarchyItem();
        smallGroupNode.setId(smallGroupId.longValue());
        smallGroupNode.setName(getSafeString(targetScope, "smallGroupName"));
        smallGroupNode.setType("smallGroup");
        smallGroupNode.setParentId(null);
        smallGroupNode.setExpandable(true);
        smallGroupNode.setChildren(new ArrayList<>());

        // 获取小组成员
        List<Map<String, Object>> members = volunteerManageMapper.getSmallGroupMembers(smallGroupId);
        if (members != null) {
            for (Map<String, Object> member : members) {
                // 创建成员节点
                MemberManageDTO.HierarchyItem memberNode = new MemberManageDTO.HierarchyItem();
                memberNode.setId(getSafeString(member, "account").hashCode() * 1000L); // 使用account作为唯一标识
                memberNode.setName(getSafeString(member, "nickname") != null && !getSafeString(member, "nickname").isEmpty() ? getSafeString(member, "nickname") : getSafeString(member, "account"));
                memberNode.setType("member");
                memberNode.setParentId(smallGroupId.longValue());
                memberNode.setExpandable(false);
                memberNode.setChildren(null);

                smallGroupNode.getChildren().add(memberNode);
            }
        }

        hierarchyList.add(smallGroupNode);
        result.setHierarchyList(hierarchyList);
    }

    /**
     * 安全获取字符串值，处理null值
     */
    private String getSafeString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * 安全获取整数值，处理null值
     */
    private Integer getSafeInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Number ? ((Number) value).intValue() : null;
    }
}