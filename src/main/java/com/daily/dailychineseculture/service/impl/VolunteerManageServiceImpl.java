package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.HomeworkDetailDTO;
import com.daily.dailychineseculture.dto.MemberManageDTO;
import com.daily.dailychineseculture.dto.DutyAssignmentDTO;
import com.daily.dailychineseculture.mapper.HomeworkMapper;
import com.daily.dailychineseculture.mapper.VolunteerManageMapper;
import com.daily.dailychineseculture.service.VolunteerManageService;
import com.daily.dailychineseculture.mapper.UserMapper;
import com.daily.dailychineseculture.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 志愿者管理服务实现类
 */
@Service
public class VolunteerManageServiceImpl implements VolunteerManageService {

    @Autowired
    private VolunteerManageMapper volunteerManageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Override
    public List<Map<String, Object>> getManagementScopes(Long userId) {
        return volunteerManageMapper.getManagementScope(userId);
    }

    @Override
    public MemberManageDTO getMemberManageInfo(Long userId, Integer assignmentId, Integer smallGroupId) {
        MemberManageDTO result = new MemberManageDTO();
        System.out.println(
                "获取成员信息 - userId: " + userId + ", assignmentId: " + assignmentId + ", smallGroupId: " + smallGroupId);

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

        List<Map<String, Object>> scopeList = volunteerManageMapper.getManagementScope(userId);
        System.out.println("管理范围: " + scopeList);
        if (scopeList.isEmpty()) {
            return result;
        }

        Map<String, Object> targetScope;
        if (assignmentId != null) {
            targetScope = scopeList.stream()
                    .filter(scope -> assignmentId.equals(scope.get("assignmentId")))
                    .findFirst()
                    .orElse(scopeList.get(0));
        } else {
            targetScope = scopeList.get(0);
        }

        MemberManageDTO.CampInfo campInfo = new MemberManageDTO.CampInfo();
        campInfo.setCampId(getSafeInteger(targetScope, "campId"));
        campInfo.setCampName(getSafeString(targetScope, "campName"));
        campInfo.setStatus("进行中");
        result.setCampInfo(campInfo);

        String dutyType = getSafeString(targetScope, "dutyType");

        if ("学班".equals(dutyType) || "检班".equals(dutyType)) {
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
            buildClassHierarchy(result, targetScope);
        } else if ("学委".equals(dutyType) || "检委".equals(dutyType)) {
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
            buildBigGroupHierarchy(result, targetScope);
        } else if ("学组".equals(dutyType) || "检组".equals(dutyType)) {
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
            buildSmallGroupHierarchy(result, targetScope);
        }

        return result;
    }

    @Override
    public DutyAssignmentDTO getDutyAssignmentInfo(Long userId, Integer assignmentId) {
        DutyAssignmentDTO result = new DutyAssignmentDTO();

        List<Map<String, Object>> scopeList = volunteerManageMapper.getManagementScope(userId);

        if (scopeList.isEmpty()) {
            return result;
        }

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

        DutyAssignmentDTO.ManagementScope managementScope = new DutyAssignmentDTO.ManagementScope();
        managementScope.setCampId(getSafeInteger(targetScope, "campId"));
        managementScope.setCampName(getSafeString(targetScope, "campName"));
        managementScope.setClassId(getSafeInteger(targetScope, "classId"));
        managementScope.setClassName(getSafeString(targetScope, "className"));
        managementScope.setBigGroupId(getSafeInteger(targetScope, "bigGroupId"));
        managementScope.setBigGroupName(getSafeString(targetScope, "bigGroupName"));
        managementScope.setDutyType(dutyType);
        result.setManagementScope(managementScope);

        if ("学班".equals(dutyType) || "检班".equals(dutyType)) {
            Integer classId = getSafeInteger(targetScope, "classId");
            if (classId != null) {
                List<Map<String, Object>> bigGroups = volunteerManageMapper.getAssignableBigGroups(classId);

                if (bigGroups != null && !bigGroups.isEmpty()) {
                    List<DutyAssignmentDTO.AssignableDuty> assignableDuties = bigGroups.stream()
                            .flatMap(bg -> {
                                Integer bigGroupId = getSafeInteger(bg, "targetId");
                                String bigGroupName = getSafeString(bg, "targetName");

                                List<Map<String, Object>> xueweiVolunteers = volunteerManageMapper
                                        .getCurrentVolunteers("big_group", bigGroupId);
                                Map<String, Object> xuewei = xueweiVolunteers != null ? xueweiVolunteers.stream()
                                        .filter(v -> "学委".equals(v.get("dutyType")))
                                        .findFirst().orElse(null) : null;

                                List<Map<String, Object>> jianweiVolunteers = volunteerManageMapper
                                        .getCurrentVolunteers("big_group", bigGroupId);
                                Map<String, Object> jianwei = jianweiVolunteers != null ? jianweiVolunteers.stream()
                                        .filter(v -> "检委".equals(v.get("dutyType")))
                                        .findFirst().orElse(null) : null;

                                List<Map<String, Object>> smallGroups = volunteerManageMapper
                                        .getAssignableSmallGroups(bigGroupId);
                                List<DutyAssignmentDTO.AssignableDuty> smallGroupDuties = smallGroups != null
                                        ? smallGroups.stream()
                                                .flatMap(sg -> {
                                                    Integer smallGroupId = getSafeInteger(sg, "targetId");
                                                    String smallGroupName = getSafeString(sg, "targetName");

                                                    List<Map<String, Object>> xuezuVolunteers = volunteerManageMapper
                                                            .getCurrentVolunteers("small_group", smallGroupId);
                                                    Map<String, Object> xuezu = xuezuVolunteers != null
                                                            ? xuezuVolunteers.stream()
                                                                    .filter(v -> "学组".equals(v.get("dutyType")))
                                                                    .findFirst().orElse(null)
                                                            : null;

                                                    List<Map<String, Object>> jianzuVolunteers = volunteerManageMapper
                                                            .getCurrentVolunteers("small_group", smallGroupId);
                                                    Map<String, Object> jianzu = jianzuVolunteers != null
                                                            ? jianzuVolunteers.stream()
                                                                    .filter(v -> "检组".equals(v.get("dutyType")))
                                                                    .findFirst().orElse(null)
                                                            : null;
                                                    String fullSmallGroupName = bigGroupName + " - " + smallGroupName;
                                                    return List.of(
                                                            createAssignableDuty("small_group", smallGroupId,
                                                                    fullSmallGroupName, "学组", xuezu),
                                                            createAssignableDuty("small_group", smallGroupId,
                                                                    fullSmallGroupName, "检组", jianzu))
                                                            .stream();
                                                })
                                                .collect(Collectors.toList())
                                        : List.of();

                                List<DutyAssignmentDTO.AssignableDuty> allDuties = new ArrayList<>();
                                allDuties
                                        .add(createAssignableDuty("big_group", bigGroupId, bigGroupName, "学委", xuewei));
                                allDuties.add(
                                        createAssignableDuty("big_group", bigGroupId, bigGroupName, "检委", jianwei));
                                allDuties.addAll(smallGroupDuties);

                                return allDuties.stream();
                            })
                            .collect(Collectors.toList());

                    result.setAssignableDuties(assignableDuties);
                }
            }
        } else if ("学委".equals(dutyType) || "检委".equals(dutyType)) {
            Integer bigGroupId = getSafeInteger(targetScope, "bigGroupId");
            if (bigGroupId != null) {
                List<Map<String, Object>> smallGroups = volunteerManageMapper.getAssignableSmallGroups(bigGroupId);
                if (smallGroups != null && !smallGroups.isEmpty()) {
                    String bigGroupName = getSafeString(targetScope, "bigGroupName");
                    List<DutyAssignmentDTO.AssignableDuty> assignableDuties = smallGroups.stream()
                            .flatMap(sg -> {
                                Integer smallGroupId = getSafeInteger(sg, "targetId");
                                String smallGroupName = getSafeString(sg, "targetName");
                                List<Map<String, Object>> xuezuVolunteers = volunteerManageMapper
                                        .getCurrentVolunteers("small_group", smallGroupId);
                                Map<String, Object> xuezu = xuezuVolunteers != null ? xuezuVolunteers.stream()
                                        .filter(v -> "学组".equals(v.get("dutyType")))
                                        .findFirst().orElse(null) : null;
                                List<Map<String, Object>> jianzuVolunteers = volunteerManageMapper
                                        .getCurrentVolunteers("small_group", smallGroupId);
                                Map<String, Object> jianzu = jianzuVolunteers != null ? jianzuVolunteers.stream()
                                        .filter(v -> "检组".equals(v.get("dutyType")))
                                        .findFirst().orElse(null) : null;
                                String fullSmallGroupName = bigGroupName + " - " + smallGroupName;
                                return List.of(
                                        createAssignableDuty("small_group", smallGroupId, fullSmallGroupName, "学组",
                                                xuezu),
                                        createAssignableDuty("small_group", smallGroupId, fullSmallGroupName, "检组",
                                                jianzu))
                                        .stream();
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
            System.out.println("分配岗位参数:");
            System.out.println("managerUserId: " + managerUserId);
            System.out.println("targetUserId: " + targetUserId);
            System.out.println("targetType: " + targetType);
            System.out.println("targetId: " + targetId);
            System.out.println("dutyType: " + dutyType);

            List<Map<String, Object>> existingDuties = volunteerManageMapper.getCurrentVolunteers(targetType, targetId);
            boolean alreadyAssigned = existingDuties.stream()
                    .anyMatch(duty -> dutyType.equals(duty.get("dutyType")) && targetUserId.equals(duty.get("userId")));

            if (alreadyAssigned) {
                System.out.println("用户已担任该职位");
                return false;
            }

            List<Map<String, Object>> managerScopes = volunteerManageMapper.getManagementScope(managerUserId);
            if (managerScopes.isEmpty()) {
                System.out.println("管理者没有管理范围");
                return false;
            }

            System.out.println("管理范围: " + managerScopes);

            boolean hasPermission = managerScopes.stream().anyMatch(scope -> {
                String scopeDutyType = getSafeString(scope, "dutyType");
                String scopeTargetType = getSafeString(scope, "targetType");
                Integer scopeTargetId = getSafeInteger(scope, scopeTargetType + "Id");
                // 超级管理员可以分配任何岗位
                if ("SUPER_ADMIN".equals(scopeDutyType)) {
                    return true;
                } else if ("class".equals(scopeTargetType)) {
                    return true;
                } else if ("big_group".equals(scopeTargetType) && "small_group".equals(targetType)) {
                    return true;
                }
                return false;
            });

            if (!hasPermission) {
                System.out.println("管理者没有权限分配该岗位");
                return false;
            }

            Integer campId = getSafeInteger(managerScopes.get(0), "campId");

            // 如果是超级管理员，根据targetId查询营期ID
            if (campId == null) {
                String scopeDutyType = getSafeString(managerScopes.get(0), "dutyType");
                if ("SUPER_ADMIN".equals(scopeDutyType)) {
                    if ("class".equals(targetType)) {
                        campId = volunteerManageMapper.getCampIdByClassId(targetId);
                        System.out.println("通过班级ID查询营期ID: " + campId);
                    }
                }

                if (campId == null) {
                    System.out.println("无法获取营期ID");
                    return false;
                }
            }

            System.out.println("营期ID: " + campId);

            Integer result = volunteerManageMapper.assignDuty(targetUserId, campId, dutyType);
            System.out.println("分配结果: " + result);

            if (result > 0) {
                Integer assignmentId = volunteerManageMapper.getLastInsertId();
                System.out.println("新分配的assignment_id: " + assignmentId);

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
            return volunteerManageMapper.removeDuty(assignmentId) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private MemberManageDTO.MemberInfo convertToMemberInfo(Map<String, Object> memberMap) {
        MemberManageDTO.MemberInfo memberInfo = new MemberManageDTO.MemberInfo();
        try {
            memberInfo.setAccount(getSafeString(memberMap, "account"));
            memberInfo.setNickname(getSafeString(memberMap, "nickname"));
            memberInfo.setUsername(getSafeString(memberMap, "account"));
            memberInfo.setAvatar(getSafeString(memberMap, "avatar"));
            memberInfo.setPhone(getSafeString(memberMap, "phone"));
            memberInfo.setGender(getSafeInteger(memberMap, "gender"));
            memberInfo.setBirthday(getSafeString(memberMap, "birthday"));
            memberInfo.setAge(getSafeInteger(memberMap, "age"));
            memberInfo.setRegion(getSafeString(memberMap, "region"));
            memberInfo.setOccupation(getSafeString(memberMap, "occupation"));
            memberInfo.setCampName(getSafeString(memberMap, "campName"));
            memberInfo.setClassName(getSafeString(memberMap, "className"));
            memberInfo.setBigGroupName(getSafeString(memberMap, "bigGroupName"));
            memberInfo.setSmallGroupName(getSafeString(memberMap, "smallGroupName"));
            memberInfo.setStatus(getSafeString(memberMap, "status"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return memberInfo;
    }

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
            duty.setCurrentUserId(getSafeInteger(currentVolunteer, "userId") != null
                    ? getSafeInteger(currentVolunteer, "userId").longValue()
                    : null);
            String username = getSafeString(currentVolunteer, "username");
            String account = getSafeString(currentVolunteer, "account");
            duty.setCurrentUsername(username.isEmpty() ? account : username);
            duty.setAssignmentId(getSafeInteger(currentVolunteer, "assignmentId"));
        }

        return duty;
    }

    private void buildClassHierarchy(MemberManageDTO result, Map<String, Object> targetScope) {
        Integer classId = getSafeInteger(targetScope, "classId");
        if (classId == null)
            return;

        List<MemberManageDTO.HierarchyItem> hierarchyList = new ArrayList<>();

        MemberManageDTO.HierarchyItem classNode = new MemberManageDTO.HierarchyItem();
        classNode.setId(classId.longValue());
        classNode.setName(getSafeString(targetScope, "className"));
        classNode.setType("class");
        classNode.setParentId(null);
        classNode.setExpandable(true);
        classNode.setChildren(new ArrayList<>());

        List<Map<String, Object>> bigGroups = volunteerManageMapper.getAssignableBigGroups(classId);
        if (bigGroups != null) {
            for (Map<String, Object> bigGroup : bigGroups) {
                Integer bigGroupId = getSafeInteger(bigGroup, "targetId");
                String bigGroupName = getSafeString(bigGroup, "targetName");

                MemberManageDTO.HierarchyItem bigGroupNode = new MemberManageDTO.HierarchyItem();
                bigGroupNode.setId(bigGroupId.longValue());
                bigGroupNode.setName(bigGroupName);
                bigGroupNode.setType("bigGroup");
                bigGroupNode.setParentId(classId.longValue());
                bigGroupNode.setExpandable(true);
                bigGroupNode.setChildren(new ArrayList<>());

                List<Map<String, Object>> smallGroups = volunteerManageMapper.getAssignableSmallGroups(bigGroupId);
                if (smallGroups != null) {
                    for (Map<String, Object> smallGroup : smallGroups) {
                        Integer smallGroupId = getSafeInteger(smallGroup, "targetId");
                        String smallGroupName = getSafeString(smallGroup, "targetName");

                        MemberManageDTO.HierarchyItem smallGroupNode = new MemberManageDTO.HierarchyItem();
                        smallGroupNode.setId(smallGroupId.longValue());
                        smallGroupNode.setName(smallGroupName);
                        smallGroupNode.setType("smallGroup");
                        smallGroupNode.setParentId(bigGroupId.longValue());
                        smallGroupNode.setExpandable(true);
                        smallGroupNode.setChildren(new ArrayList<>());

                        List<Map<String, Object>> members = volunteerManageMapper.getSmallGroupMembers(smallGroupId);
                        if (members != null) {
                            for (Map<String, Object> member : members) {
                                MemberManageDTO.HierarchyItem memberNode = new MemberManageDTO.HierarchyItem();
                                memberNode.setId(getSafeString(member, "account").hashCode() * 1000L);
                                memberNode.setName(getSafeString(member, "nickname") != null
                                        && !getSafeString(member, "nickname").isEmpty()
                                                ? getSafeString(member, "nickname")
                                                : getSafeString(member, "account"));
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

    private void buildBigGroupHierarchy(MemberManageDTO result, Map<String, Object> targetScope) {
        Integer bigGroupId = getSafeInteger(targetScope, "bigGroupId");
        if (bigGroupId == null)
            return;

        List<MemberManageDTO.HierarchyItem> hierarchyList = new ArrayList<>();

        MemberManageDTO.HierarchyItem bigGroupNode = new MemberManageDTO.HierarchyItem();
        bigGroupNode.setId(bigGroupId.longValue());
        bigGroupNode.setName(getSafeString(targetScope, "bigGroupName"));
        bigGroupNode.setType("bigGroup");
        bigGroupNode.setParentId(null);
        bigGroupNode.setExpandable(true);
        bigGroupNode.setChildren(new ArrayList<>());

        List<Map<String, Object>> smallGroups = volunteerManageMapper.getAssignableSmallGroups(bigGroupId);
        if (smallGroups != null) {
            for (Map<String, Object> smallGroup : smallGroups) {
                Integer smallGroupId = getSafeInteger(smallGroup, "targetId");
                String smallGroupName = getSafeString(smallGroup, "targetName");

                MemberManageDTO.HierarchyItem smallGroupNode = new MemberManageDTO.HierarchyItem();
                smallGroupNode.setId(smallGroupId.longValue());
                smallGroupNode.setName(smallGroupName);
                smallGroupNode.setType("smallGroup");
                smallGroupNode.setParentId(bigGroupId.longValue());
                smallGroupNode.setExpandable(true);
                smallGroupNode.setChildren(new ArrayList<>());

                List<Map<String, Object>> members = volunteerManageMapper.getSmallGroupMembers(smallGroupId);
                if (members != null) {
                    for (Map<String, Object> member : members) {
                        MemberManageDTO.HierarchyItem memberNode = new MemberManageDTO.HierarchyItem();
                        memberNode.setId(getSafeString(member, "account").hashCode() * 1000L);
                        memberNode.setName(getSafeString(member, "nickname") != null
                                && !getSafeString(member, "nickname").isEmpty() ? getSafeString(member, "nickname")
                                        : getSafeString(member, "account"));
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

    private void buildSmallGroupHierarchy(MemberManageDTO result, Map<String, Object> targetScope) {
        Integer smallGroupId = getSafeInteger(targetScope, "smallGroupId");
        if (smallGroupId == null)
            return;

        List<MemberManageDTO.HierarchyItem> hierarchyList = new ArrayList<>();

        MemberManageDTO.HierarchyItem smallGroupNode = new MemberManageDTO.HierarchyItem();
        smallGroupNode.setId(smallGroupId.longValue());
        smallGroupNode.setName(getSafeString(targetScope, "smallGroupName"));
        smallGroupNode.setType("smallGroup");
        smallGroupNode.setParentId(null);
        smallGroupNode.setExpandable(true);
        smallGroupNode.setChildren(new ArrayList<>());

        List<Map<String, Object>> members = volunteerManageMapper.getSmallGroupMembers(smallGroupId);
        if (members != null) {
            for (Map<String, Object> member : members) {
                MemberManageDTO.HierarchyItem memberNode = new MemberManageDTO.HierarchyItem();
                memberNode.setId(getSafeString(member, "account").hashCode() * 1000L);
                memberNode.setName(
                        getSafeString(member, "nickname") != null && !getSafeString(member, "nickname").isEmpty()
                                ? getSafeString(member, "nickname")
                                : getSafeString(member, "account"));
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

    private String getSafeString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private Integer getSafeInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    @Override
    public List<Map<String, Object>> getCertificatesByUser(Long userId) {
        try {
            List<Map<String, Object>> certificates = volunteerManageMapper.getCertificatesByUser(userId);
            List<Map<String, Object>> duties = getUserAllAssignments(userId);

            for (Map<String, Object> cert : certificates) {
                String certType = getSafeString(cert, "type");
                String certNumber = getSafeString(cert, "number");
                String position = getMatchingPositionByCertType(cert, certType, certNumber, duties);
                cert.put("camp_class_info", position);
            }
            return certificates;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String getMatchingPositionByCertType(Map<String, Object> cert, String certType, String certNumber,
            List<Map<String, Object>> duties) {
        // 学员证书识别：homeworkId 不为空
        Object homeworkId = cert.get("homework_id");
        if (homeworkId != null) {
            return getStudentCertPosition(certNumber);
        }
        if (duties == null || duties.isEmpty())
            return "服务期间";

        Integer targetAssignmentId = null;
        Integer targetCampId = null;
        if (certNumber != null && certNumber.contains("-")) {
            String[] parts = certNumber.split("-");
            // 类型-营期ID-职位ID-序号
            if (parts.length >= 4) {
                try {
                    targetCampId = Integer.parseInt(parts[1]);
                    targetAssignmentId = Integer.parseInt(parts[2]);
                } catch (Exception ignored) {
                }
            }

            else if (parts.length == 3) {
                try {
                    targetCampId = Integer.parseInt(parts[1]);
                } catch (Exception ignored) {
                }
            }
        }

        List<String> targetDuties;
        boolean needFullPath = false;

        switch (certType) {
            case "优秀班长":
                targetDuties = List.of("学班", "检班");
                break;
            case "优秀大组组长":
                targetDuties = List.of("学委", "检委");
                break;
            case "优秀小组组长":
            case "优秀志愿者":
                targetDuties = List.of("学组", "检组");
                needFullPath = true;
                break;
            default:
                targetDuties = List.of("学班", "检班", "学委", "检委", "学组", "检组");
        }

        if (targetAssignmentId != null) {
            for (Map<String, Object> d : duties) {
                Integer assignId = getSafeInteger(d, "assignment_id");
                if (targetAssignmentId.equals(assignId)) {
                    // 直接用这个职位，完全精准
                    String camp = getSafeString(d, "camp_name");
                    String cls = getSafeString(d, "class_name");
                    String big = getSafeString(d, "big_group_name");
                    String small = getSafeString(d, "small_group_name");

                    StringBuilder sb = new StringBuilder();
                    if (!camp.isEmpty())
                        sb.append(camp);
                    if (!cls.isEmpty())
                        sb.append("-").append(cls);

                    if (!"优秀班长".equals(certType)) {
                        if (!big.isEmpty())
                            sb.append("-").append(big);
                        if (needFullPath && !small.isEmpty())
                            sb.append("-").append(small);
                    }

                    return sb.length() > 0 ? sb.toString() : "服务期间";
                }
            }
        }

        List<Map<String, Object>> candidates = new ArrayList<>();
        for (Map<String, Object> d : duties) {
            Integer campId = getSafeInteger(d, "camp_id");
            // 只匹配当前证书的营期
            if (targetCampId != null && !targetCampId.equals(campId)) {
                continue;
            }

            String dt = getSafeString(d, "duty_type");
            if (dt == null)
                dt = getSafeString(d, "dutyType");
            if (targetDuties.contains(dt)) {
                candidates.add(d);
            }
        }

        if (candidates.isEmpty())
            candidates = duties;
        Map<String, Object> best = candidates.get(0);

        if ("优秀班长".equals(certType)) {
            for (Map<String, Object> c : candidates) {
                String small = getSafeString(c, "small_group_name");
                if (small == null || small.isEmpty()) {
                    best = c;
                    break;
                }
            }
        } else if ("优秀大组组长".equals(certType)) {
            for (Map<String, Object> c : candidates) {
                String big = getSafeString(c, "big_group_name");
                String small = getSafeString(c, "small_group_name");
                if (big != null && !big.isEmpty() && (small == null || small.isEmpty())) {
                    best = c;
                    break;
                }
            }
        } else if ("优秀小组组长".equals(certType) || "优秀志愿者".equals(certType)) {
            for (Map<String, Object> c : candidates) {
                String small = getSafeString(c, "small_group_name");
                if (small != null && !small.isEmpty()) {
                    best = c;
                    break;
                }
            }
        }

        String camp = getSafeString(best, "camp_name");
        String cls = getSafeString(best, "class_name");
        String big = getSafeString(best, "big_group_name");
        String small = getSafeString(best, "small_group_name");

        StringBuilder sb = new StringBuilder();
        if (!camp.isEmpty())
            sb.append(camp);
        if (!cls.isEmpty())
            sb.append("-").append(cls);

        if (!"优秀班长".equals(certType)) {
            if (!big.isEmpty())
                sb.append("-").append(big);
            if (needFullPath && !small.isEmpty())
                sb.append("-").append(small);
        }

        return sb.length() > 0 ? sb.toString() : "服务期间";
    }

    private String getStudentCertPosition(String certNumber) {
        // 从证书编号中提取 homeworkId（OTHER-0-0-作业ID-序号 格式）
        Integer homeworkId = null;
        if (certNumber != null && certNumber.contains("-")) {
            String[] parts = certNumber.split("-");
            if (parts.length >= 4) {
                try {
                    homeworkId = Integer.parseInt(parts[3]);
                    System.out.println("=== 学员证书 === 从编号中解析出 homeworkId: " + homeworkId);
                } catch (Exception e) {
                    System.out.println("解析 homeworkId 失败: " + e.getMessage());
                }
            }
        }

        if (homeworkId == null) {
            System.out.println("homeworkId 为空，返回 '学习期间'");
            return "学习期间";
        }

        try {
            System.out.println("尝试获取作业详情，homeworkId: " + homeworkId);
            Map<String, Object> homeworkDetail = homeworkMapper.getHomeworkDetail(homeworkId);
            if (homeworkDetail != null) {
                System.out.println("获取作业详情成功，keys: " + homeworkDetail.keySet());

                // 构建学习位置信息
                String campName = (String) homeworkDetail.get("campName");
                String className = (String) homeworkDetail.get("className");
                String bigGroupName = (String) homeworkDetail.get("bigGroupName");
                String smallGroupName = (String) homeworkDetail.get("smallGroupName");

                StringBuilder sb = new StringBuilder();
                if (campName != null && !campName.isEmpty()) sb.append(campName);
                if (className != null && !className.isEmpty()) sb.append("-").append(className);
                if (bigGroupName != null && !bigGroupName.isEmpty()) sb.append("-").append(bigGroupName);
                if (smallGroupName != null && !smallGroupName.isEmpty()) sb.append("-").append(smallGroupName);

                String position = sb.toString();
                if (!position.isEmpty()) {
                    System.out.println("构建的学习位置: " + position);
                    return position;
                } else {
                    System.out.println("构建的学习位置为空");
                }
            } else {
                System.out.println("作业详情为空");
            }
        } catch (Exception e) {
            System.out.println("获取作业详情失败: " + e.getMessage());
        }

        System.out.println("返回 '学习期间'");
        return "学习期间";
    }

    @Override
    public boolean issueCertificate(Long volunteerId, String certificateType, Integer assignmentId, Long homeworkId) {
        try {
            int count = volunteerManageMapper.checkCertificateIssued(volunteerId, certificateType, assignmentId,
                    homeworkId);
            if (count > 0) {
                return false;
            }
            // 颁发证书时，获取职位对应的营期ID
            List<Map<String, Object>> duties = getUserAllAssignments(volunteerId);
            Integer campId = null;
            if (assignmentId != null) {
                // 按assignment_id精准获取营期
                for (Map<String, Object> d : duties) {
                    if (assignmentId.equals(getSafeInteger(d, "assignment_id"))) {
                        campId = getSafeInteger(d, "camp_id");
                        break;
                    }
                }
            } else if (!duties.isEmpty()) {
                campId = getSafeInteger(duties.get(0), "camp_id");
            }
            String certNumber = generateCertificateNumber(certificateType, campId, assignmentId, homeworkId);
            int result = volunteerManageMapper.issueCertificate(volunteerId, certificateType, certNumber, assignmentId,
                    homeworkId);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean cancelCertificate(Long volunteerId, String certificateType, Integer assignmentId, Long homeworkId) {
        try {
            int result = volunteerManageMapper.cancelCertificate(volunteerId, certificateType, assignmentId,
                    homeworkId);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean checkCertificateIssued(Long volunteerId, String certificateType, Integer assignmentId,
            Long homeworkId) {
        try {
            int count = volunteerManageMapper.checkCertificateIssued(volunteerId, certificateType, assignmentId,
                    homeworkId);
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getCertificatesByHomeworkId(Long homeworkId) {
        try {
            return volunteerManageMapper.getCertificatesByHomeworkId(homeworkId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getVolunteerDetail(Long volunteerId) {
        try {
            return volunteerManageMapper.getUserById(volunteerId);
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.HashMap<>();
        }
    }

    @Override
    public List<Map<String, Object>> getManagedVolunteers(Long userId, Integer assignmentId) {
        try {
            List<Map<String, Object>> scopes = volunteerManageMapper.getManagementScope(userId);
            List<Map<String, Object>> volunteers = new ArrayList<>();

            if (assignmentId != null) {
                Map<String, Object> targetScope = scopes.stream()
                        .filter(scope -> assignmentId.equals(scope.get("assignmentId")))
                        .findFirst()
                        .orElse(null);

                if (targetScope != null) {
                    processScope(targetScope, volunteers);
                }
            } else {
                for (Map<String, Object> scope : scopes) {
                    processScope(scope, volunteers);
                }
            }

            return volunteers;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void processScope(Map<String, Object> scope, List<Map<String, Object>> volunteers) {
        String dutyType = getSafeString(scope, "dutyType");

        if (dutyType.startsWith("admin")) {
            Integer typeId = getTypeIdFromDutyType(dutyType);
            if (typeId != null) {
                List<Map<String, Object>> classMonitors = volunteerManageMapper.getVolunteersByTypeId(typeId);
                for (Map<String, Object> volunteer : classMonitors) {
                    processVolunteerInfo(volunteer);
                }
                volunteers.addAll(classMonitors);
            }
        } else if ("学班".equals(dutyType) || "检班".equals(dutyType)) {
            Integer classId = getSafeInteger(scope, "classId");
            if (classId != null) {
                List<Map<String, Object>> committeeMembers = volunteerManageMapper
                        .getCommitteeMembersByClassId(classId);
                for (Map<String, Object> volunteer : committeeMembers) {
                    processVolunteerInfo(volunteer);
                }
                volunteers.addAll(committeeMembers);
            }
        } else if ("学委".equals(dutyType) || "检委".equals(dutyType)) {
            Integer bigGroupId = getSafeInteger(scope, "bigGroupId");
            if (bigGroupId != null) {
                List<Map<String, Object>> groupLeaders = volunteerManageMapper.getGroupLeadersByBigGroupId(bigGroupId);
                for (Map<String, Object> volunteer : groupLeaders) {
                    processVolunteerInfo(volunteer);
                }
                volunteers.addAll(groupLeaders);
            }
        }
    }

    private void processVolunteerInfo(Map<String, Object> volunteer) {
        try {
            Object startTimeObj = volunteer.get("start_time");
            Object endTimeObj = volunteer.get("end_time");
            Date startTime = null;
            Date endTime = null;
            if (startTimeObj != null) {
                if (startTimeObj instanceof java.time.LocalDateTime) {
                    startTime = Date.from(((java.time.LocalDateTime) startTimeObj)
                            .atZone(java.time.ZoneId.systemDefault()).toInstant());
                } else if (startTimeObj instanceof Date) {
                    startTime = (Date) startTimeObj;
                }
            }
            if (endTimeObj != null) {
                if (endTimeObj instanceof java.time.LocalDateTime) {
                    endTime = Date.from(((java.time.LocalDateTime) endTimeObj).atZone(java.time.ZoneId.systemDefault())
                            .toInstant());
                } else if (endTimeObj instanceof Date) {
                    endTime = (Date) endTimeObj;
                }
            }
            String campName = getSafeString(volunteer, "camp_name");
            String className = getSafeString(volunteer, "class_name");
            String bigGroupName = getSafeString(volunteer, "big_group_name");
            String smallGroupName = getSafeString(volunteer, "small_group_name");
            StringBuilder serviceLocation = new StringBuilder();
            if (campName != null && !campName.isEmpty()) {
                serviceLocation.append(campName);
                if (className != null && !className.isEmpty()) {
                    serviceLocation.append("-").append(className);
                    if (bigGroupName != null && !bigGroupName.isEmpty()) {
                        serviceLocation.append("-").append(bigGroupName);
                        if (smallGroupName != null && !smallGroupName.isEmpty()) {
                            serviceLocation.append("-").append(smallGroupName);
                        }
                    }
                }
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
            String displayStartTime = startTime != null ? sdf.format(startTime) : "";
            String displayEndTime = endTime != null ? sdf.format(endTime) : "至今";
            String serviceTime = displayStartTime + " ~ " + displayEndTime;
            volunteer.put("service_location", serviceLocation.toString());
            volunteer.put("service_time", serviceTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Map<String, Object>> getUserAllAssignments(Long userId) {
        try {
            List<Map<String, Object>> assignments = volunteerManageMapper.getUserAllAssignments(userId);
            List<Map<String, Object>> filteredAssignments = new ArrayList<>();

            List<String> validDutyTypes = List.of("学委", "检委", "学班", "检班", "学组", "检组");

            for (Map<String, Object> assignment : assignments) {
                Integer assignmentId = assignment.get("assignment_id") != null
                        ? ((Number) assignment.get("assignment_id")).intValue()
                        : null;
                String dutyType = (String) assignment.get("duty_type");

                if (dutyType == null || !validDutyTypes.contains(dutyType)) {
                    continue;
                }

                Object startTimeObj = assignment.get("start_time");
                Object endTimeObj = assignment.get("end_time");
                Date startTime = null;
                Date endTime = null;

                if (startTimeObj != null) {
                    if (startTimeObj instanceof java.time.LocalDateTime) {
                        startTime = Date.from(((java.time.LocalDateTime) startTimeObj)
                                .atZone(java.time.ZoneId.systemDefault()).toInstant());
                    } else if (startTimeObj instanceof Date) {
                        startTime = (Date) startTimeObj;
                    }
                }

                if (endTimeObj != null) {
                    if (endTimeObj instanceof java.time.LocalDateTime) {
                        endTime = Date.from(((java.time.LocalDateTime) endTimeObj)
                                .atZone(java.time.ZoneId.systemDefault()).toInstant());
                    } else if (endTimeObj instanceof Date) {
                        endTime = (Date) endTimeObj;
                    }
                }
                String campName = (String) assignment.get("camp_name");
                String className = (String) assignment.get("class_name");
                String bigGroupName = (String) assignment.get("big_group_name");
                String smallGroupName = (String) assignment.get("small_group_name");

                StringBuilder serviceLocation = new StringBuilder();

                if (campName != null && !campName.isEmpty()) {
                    serviceLocation.append(campName);

                    if (className != null && !className.isEmpty()) {
                        serviceLocation.append("-").append(className);

                        if (bigGroupName != null && !bigGroupName.isEmpty()) {
                            serviceLocation.append("-").append(bigGroupName);

                            if (smallGroupName != null && !smallGroupName.isEmpty()) {
                                serviceLocation.append("-").append(smallGroupName);
                            }
                        }
                    }
                }

                boolean isActive = endTime == null;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
                String displayStartTime = startTime != null ? sdf.format(startTime) : "未设置";
                String displayEndTime = endTime != null ? sdf.format(endTime) : (isActive ? "至今" : "未知");
                String serviceTime = displayStartTime + "-" + displayEndTime;

                assignment.put("service_location", serviceLocation.toString());
                assignment.put("service_status", isActive ? "正参与" : "已结束");
                assignment.put("service_time", serviceTime);
                assignment.put("duty_name", dutyType);
                assignment.put("assignment_id", assignmentId);

                filteredAssignments.add(assignment);
            }

            return filteredAssignments;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String generateCertificateNumber(String certificateType, Integer campId, Integer assignmentId,
            Long homeworkId) {
        try {
            Integer maxId = volunteerManageMapper.getMaxCertificateId();
            maxId = maxId == null ? 0 : maxId;
            String typeCode = "";

            switch (certificateType) {
                case "优秀班长":
                    typeCode = "YXBZ";
                    break;
                case "优秀大组组长":
                    typeCode = "YXDZZ";
                    break;
                case "优秀小组组长":
                    typeCode = "YXXZZ";
                    break;
                case "优秀小组功课":
                    typeCode = "YXXZGK";
                    break;
                case "优秀班级功课":
                    typeCode = "YXBJGK";
                    break;
                default:
                    typeCode = "OTHER";
            }

            if (campId == null)
                campId = 0;
            if (assignmentId == null)
                assignmentId = 0;
            if (homeworkId == null)
                homeworkId = 0L;

            return typeCode + "-" + campId + "-" + assignmentId + "-" + homeworkId + "-"
                    + String.format("%06d", maxId + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return "OTHER-0-0-0-000001";
        }
    }

    private Integer getTypeIdFromDutyType(String dutyType) {
        if (dutyType.startsWith("admin")) {
            try {
                int adminLevel = Integer.parseInt(dutyType.replace("admin", ""));
                return adminLevel;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean checkAdminPermission(Long userId) {
        try {
            // 直接查询用户是否有 volunteer_admin 类型的职责
            Integer count = volunteerManageMapper.checkAdminRole(userId);
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getActiveCamps() {
        try {
            return volunteerManageMapper.getActiveCamps();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getClassesByCampId(Integer campId) {
        try {
            return volunteerManageMapper.getClassesByCampId(campId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getMonitorsByCampId(Integer campId) {
        try {
            List<Map<String, Object>> monitors = volunteerManageMapper.getMonitorsByCampId(campId);
            for (Map<String, Object> monitor : monitors) {
                String campName = getSafeString(monitor, "campName");
                String className = getSafeString(monitor, "className");
                StringBuilder position = new StringBuilder();
                if (!campName.isEmpty()) {
                    position.append(campName);
                    if (!className.isEmpty()) {
                        position.append("-").append(className);
                    }
                }
                monitor.put("position", position.toString());

                String startTime = getSafeString(monitor, "startTime");
                String endTime = getSafeString(monitor, "endTime");
                String timeRange = startTime != null && !startTime.isEmpty()
                        ? (endTime != null && !endTime.isEmpty()
                                ? startTime.substring(0, 10) + " ~ " + endTime.substring(0, 10)
                                : startTime.substring(0, 10) + " ~ 至今")
                        : "未知";
                monitor.put("timeRange", timeRange);

                monitor.put("userId", monitor.get("userId"));
                monitor.put("assignmentId", monitor.get("assignmentId"));
                monitor.put("dutyType", monitor.get("dutyType"));
                monitor.put("certIssued", false);
            }
            return monitors;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getAllCertificatesByUser(Long userId) {
        try {
            List<Map<String, Object>> certificates = volunteerManageMapper.getAllCertificatesByUser(userId);
            List<Map<String, Object>> duties = getUserAllAssignments(userId);

            for (Map<String, Object> cert : certificates) {
                String certType = getSafeString(cert, "type");
                String certNumber = getSafeString(cert, "number");
                String position = getMatchingPositionByCertType(cert, certType, certNumber, duties);
                cert.put("camp_class_info", position);
            }
            return certificates;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}