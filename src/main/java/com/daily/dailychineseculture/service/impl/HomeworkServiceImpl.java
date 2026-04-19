package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.*;
import com.daily.dailychineseculture.entity.Homework;
import com.daily.dailychineseculture.mapper.GroupChatMapper;
import com.daily.dailychineseculture.mapper.HomeworkMapper;
import com.daily.dailychineseculture.mapper.UserTaskRecordMapper;
import com.daily.dailychineseculture.mapper.VolunteerManageMapper;
import com.daily.dailychineseculture.service.GroupChatService;
import com.daily.dailychineseculture.service.HomeworkService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.daily.dailychineseculture.service.VolunteerManageService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.daily.dailychineseculture.common.BusinessException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 作业服务实现类
 */
@Service
public class HomeworkServiceImpl implements HomeworkService {

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Autowired
    private UserTaskRecordMapper userTaskRecordMapper;

    @Autowired
    private VolunteerManageMapper volunteerManageMapper;

    @Autowired
    private VolunteerManageService volunteerManageService;

    @Autowired
    private GroupChatService groupChatService;

    @Autowired
    private GroupChatMapper groupChatMapper;

    /**
     * 获取作业列表
     */
    @Override
    public HomeworkListDTO getHomeworkList(Long userId, String type, Integer id, String status, String date) {
        try {
            // 检查权限
            checkVolunteerAuth(userId, type, id);

            HomeworkListDTO result = new HomeworkListDTO();
            result.setList(new ArrayList<>());
            result.setTotal(0);

            // 获取作业列表
            List<Map<String, Object>> homeworkList = homeworkMapper.getHomeworkList(status, date, type, id);

            List<HomeworkListDTO.HomeworkItem> items = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 精确到秒

            for (Map<String, Object> item : homeworkList) {
                Integer homeworkId = (Integer) item.get("homeworkId");
                Long userIdFromDb = item.get("userId") != null ? ((Number) item.get("userId")).longValue() : null;
                String name = (String) item.get("name");
                // 确保名称不为空，处理null和空字符串的情况
                if (name == null || name.isEmpty()) {
                    name = userIdFromDb != null ? "学员" + userIdFromDb : "学员" + homeworkId;
                }

                // 处理优秀状态字段
                Integer isSmallGroupExcellent = 0;
                Object isSmallGroupExcellentObj = item.get("isSmallGroupExcellent");
                if (isSmallGroupExcellentObj != null) {
                    if (isSmallGroupExcellentObj instanceof Boolean) {
                        isSmallGroupExcellent = ((Boolean) isSmallGroupExcellentObj) ? 1 : 0;
                    } else if (isSmallGroupExcellentObj instanceof Number) {
                        isSmallGroupExcellent = ((Number) isSmallGroupExcellentObj).intValue();
                    }
                }

                Integer isBigGroupExcellent = 0;
                Object isBigGroupExcellentObj = item.get("isBigGroupExcellent");
                if (isBigGroupExcellentObj != null) {
                    if (isBigGroupExcellentObj instanceof Boolean) {
                        isBigGroupExcellent = ((Boolean) isBigGroupExcellentObj) ? 1 : 0;
                    } else if (isBigGroupExcellentObj instanceof Number) {
                        isBigGroupExcellent = ((Number) isBigGroupExcellentObj).intValue();
                    }
                }

                // 处理 submit_time 字段
                Date submitTime = null;
                Object submitTimeObj = item.get("submitTime");
                if (submitTimeObj != null) {
                    if (submitTimeObj instanceof java.util.Date) {
                        submitTime = (java.util.Date) submitTimeObj;
                    } else if (submitTimeObj instanceof java.time.LocalDateTime) {
                        java.time.LocalDateTime localDateTime = (java.time.LocalDateTime) submitTimeObj;
                        submitTime = java.util.Date
                                .from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
                    }
                }

                // 获取组织信息
                String campName = (String) item.get("campName"); // 正确：使用驼峰形式
                String className = (String) item.get("className");
                String bigGroupName = (String) item.get("bigGroupName");
                String smallGroupName = (String) item.get("smallGroupName");

                List<String> organizationParts = new ArrayList<>();
                if (campName != null && !campName.isEmpty())
                    organizationParts.add(campName);
                if (className != null && !className.isEmpty())
                    organizationParts.add(className);
                if (bigGroupName != null && !bigGroupName.isEmpty())
                    organizationParts.add(bigGroupName);
                if (smallGroupName != null && !smallGroupName.isEmpty())
                    organizationParts.add(smallGroupName);

                String organization = organizationParts.isEmpty() ? "未分组" : String.join("-", organizationParts);

                // 获取作业的证书列表
                List<HomeworkListDTO.HomeworkItem.CertificateInfo> certificates = new ArrayList<>();
                List<Map<String, Object>> certList = volunteerManageMapper
                        .getCertificatesByHomeworkId(homeworkId.longValue());
                for (Map<String, Object> certInfo : certList) {
                    String certType = (String) certInfo.get("type");
                    Date issueTime = null;
                    Object issueTimeObj = certInfo.get("issue_time");
                    if (issueTimeObj != null) {
                        if (issueTimeObj instanceof java.util.Date) {
                            issueTime = (java.util.Date) issueTimeObj;
                        } else if (issueTimeObj instanceof java.time.LocalDateTime) {
                            java.time.LocalDateTime localDateTime = (java.time.LocalDateTime) issueTimeObj;
                            issueTime = java.util.Date
                                    .from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
                        }
                    }
                    certificates.add(new HomeworkListDTO.HomeworkItem.CertificateInfo(certType, issueTime));
                }

                items.add(new HomeworkListDTO.HomeworkItem(homeworkId, name, isSmallGroupExcellent, isBigGroupExcellent,
                        submitTime, organization, certificates));
            }

            result.setList(items);
            result.setTotal(items.size());

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 标记小组优秀作业
     */
    @Override
    public boolean markSmallGroupExcellent(Integer homeworkId, Integer isSmallGroupExcellent) {
        try {
            // 检查作业是否存在
            int exists = homeworkMapper.checkHomeworkExists(homeworkId);
            if (exists == 0) {
                return false;
            }

            // 当取消小组优秀时，自动取消大组优秀
            if (isSmallGroupExcellent != null && isSmallGroupExcellent == 0) {
                // 检查是否为大组优秀
                Integer isBigGroupExcellent = homeworkMapper.checkBigGroupExcellent(homeworkId);
                if (isBigGroupExcellent != null && isBigGroupExcellent == 1) {
                    // 自动取消大组优秀
                    homeworkMapper.markBigGroupExcellent(homeworkId, 0);
                }
            }

            int result = homeworkMapper.markSmallGroupExcellent(homeworkId, isSmallGroupExcellent);
            if (isSmallGroupExcellent != null && isSmallGroupExcellent == 1) {
                forwardHomeworkToBigGroup(homeworkId);
            }
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 标记大组优秀作业
     */
    @Override
    public boolean markBigGroupExcellent(Integer homeworkId, Integer isBigGroupExcellent) {
        try {
            // 检查作业是否存在
            int exists = homeworkMapper.checkHomeworkExists(homeworkId);
            if (exists == 0) {
                return false;
            }

            // 检查是否为小组优秀
            Integer isSmallGroupExcellent = homeworkMapper.checkSmallGroupExcellent(homeworkId);
            if (isBigGroupExcellent != null && isBigGroupExcellent == 1
                    && (isSmallGroupExcellent == null || isSmallGroupExcellent != 1)) {
                throw new RuntimeException("只有小组优秀作业才能被评定为大组优秀");
            }

            int result = homeworkMapper.markBigGroupExcellent(homeworkId, isBigGroupExcellent);
            if (isBigGroupExcellent != null && isBigGroupExcellent == 1) {
                forwardHomeworkToClassGroup(homeworkId);
            }
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 检查作业是否为小组优秀
     */
    @Override
    public boolean checkSmallGroupExcellent(Integer homeworkId) {
        try {
            Integer result = homeworkMapper.checkSmallGroupExcellent(homeworkId);
            return result != null && result == 1;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取作业详情
     */
    @Override
    public HomeworkDetailDTO getHomeworkDetail(Integer homeworkId) {
        try {
            Map<String, Object> detailMap = homeworkMapper.getHomeworkDetail(homeworkId);
            if (detailMap == null) {
                return null;
            }

            Integer homeworkIdFromDb = (Integer) detailMap.get("homeworkId");
            Long userId = detailMap.get("userId") != null ? ((Number) detailMap.get("userId")).longValue() : null;
            String studentName = (String) detailMap.get("name");
            String content = (String) detailMap.get("content");

            // 处理优秀状态字段
            Integer isSmallGroupExcellent = 0;
            Object isSmallGroupExcellentObj = detailMap.get("isSmallGroupExcellent");
            if (isSmallGroupExcellentObj != null) {
                if (isSmallGroupExcellentObj instanceof Boolean) {
                    isSmallGroupExcellent = ((Boolean) isSmallGroupExcellentObj) ? 1 : 0;
                } else if (isSmallGroupExcellentObj instanceof Number) {
                    isSmallGroupExcellent = ((Number) isSmallGroupExcellentObj).intValue();
                }
            }

            Integer isBigGroupExcellent = 0;
            Object isBigGroupExcellentObj = detailMap.get("isBigGroupExcellent");
            if (isBigGroupExcellentObj != null) {
                if (isBigGroupExcellentObj instanceof Boolean) {
                    isBigGroupExcellent = ((Boolean) isBigGroupExcellentObj) ? 1 : 0;
                } else if (isBigGroupExcellentObj instanceof Number) {
                    isBigGroupExcellent = ((Number) isBigGroupExcellentObj).intValue();
                }
            }

            // 处理 submit_time 字段
            Date submitTime = null;
            Object submitTimeObj = detailMap.get("submitTime");
            if (submitTimeObj != null) {
                if (submitTimeObj instanceof java.util.Date) {
                    submitTime = (java.util.Date) submitTimeObj;
                } else if (submitTimeObj instanceof java.time.LocalDateTime) {
                    java.time.LocalDateTime localDateTime = (java.time.LocalDateTime) submitTimeObj;
                    submitTime = java.util.Date
                            .from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
                }
            }

            // 获取组织信息
            String campName = (String) detailMap.get("campName"); // 正确：使用驼峰形式
            String className = (String) detailMap.get("className");
            String bigGroupName = (String) detailMap.get("bigGroupName");
            String smallGroupName = (String) detailMap.get("smallGroupName");

            List<String> organizationParts = new ArrayList<>();
            if (campName != null && !campName.isEmpty())
                organizationParts.add(campName);
            if (className != null && !className.isEmpty())
                organizationParts.add(className);
            if (bigGroupName != null && !bigGroupName.isEmpty())
                organizationParts.add(bigGroupName);
            if (smallGroupName != null && !smallGroupName.isEmpty())
                organizationParts.add(smallGroupName);

            String organization = organizationParts.isEmpty() ? "未分组" : String.join("-", organizationParts);

            return new HomeworkDetailDTO(homeworkIdFromDb, studentName, userId, organization, submitTime,
                    isSmallGroupExcellent, isBigGroupExcellent, content);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取作业统计数据
     */
    @Override
    public Map<String, Object> getHomeworkStatistics(Long userId, String type, Integer id, String date) {
        try {
            // 检查权限
            checkVolunteerAuth(userId, type, id);

            // 获取学员ID列表
            List<Long> studentIds = homeworkMapper.getStudentIdsByScope(type, id);

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", studentIds.size());
            statistics.put("completedCount", 0);
            statistics.put("pendingCount", studentIds.size());
            statistics.put("lateCount", 0);
            statistics.put("completionRate", 0);
            statistics.put("onTimeRate", 0);
            statistics.put("hasHomework", false);

            // 获取管理范围所属的营期ID
            Integer campId = null;
            if ("class".equals(type)) {
                campId = volunteerManageMapper.getCampIdByClassId(id);
            } else if ("big_group".equals(type) || "bigGroup".equals(type)) {
                campId = volunteerManageMapper.getCampIdByBigGroupId(id);
            } else if ("small_group".equals(type) || "smallGroup".equals(type)) {
                campId = volunteerManageMapper.getCampIdBySmallGroupId(id);
            }

            // 检查当天是否有作业（基于管理范围所属营期的计划）
            Integer planId = null;
            if (campId != null) {
                planId = homeworkMapper.getPlanIdByDateAndCamp(date, campId);
            }

            boolean hasHomework = planId != null;
            if (hasHomework && !studentIds.isEmpty()) {
                // 获取已完成作业的人数
                int completedCount = homeworkMapper.getCompletedHomeworkCount(studentIds, date);
                statistics.put("completedCount", completedCount);
                statistics.put("pendingCount", studentIds.size() - completedCount);

                // 计算完成率
                double completionRate = studentIds.size() > 0 ? (double) completedCount / studentIds.size() * 100 : 0;
                statistics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);

                // 获取已交作业的学生名单（当天提交）
                List<Map<String, Object>> submittedList = homeworkMapper.getSubmittedHomeworkList(studentIds, date);
                // 获取迟交作业的学生名单（超过当天提交）
                List<Map<String, Object>> lateList = homeworkMapper.getLateHomeworkList(studentIds, date);
                statistics.put("lateCount", lateList.size());
                // 计算按时提交率
                double onTimeRate = completedCount > 0
                        ? (double) (completedCount - lateList.size()) / completedCount * 100
                        : 0;
                statistics.put("onTimeRate", Math.round(onTimeRate * 100.0) / 100.0);
            }
            statistics.put("hasHomework", hasHomework);
            return statistics;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取作业状态名单（已交/未交/迟交）
     */
    @Override
    public Map<String, Object> getHomeworkStatusList(Long userId, String type, Integer id, String date) {
        try {
            checkVolunteerAuth(userId, type, id);
            List<Long> studentIds = homeworkMapper.getStudentIdsByScope(type, id);
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", studentIds.size());
            result.put("submittedList", new ArrayList<>());
            result.put("pendingList", new ArrayList<>());
            result.put("lateList", new ArrayList<>());
            result.put("hasHomework", false);

            // 获取管理范围所属的营期ID
            Integer campId = null;
            if ("class".equals(type)) {
                campId = volunteerManageMapper.getCampIdByClassId(id);
            } else if ("big_group".equals(type) || "bigGroup".equals(type)) {
                campId = volunteerManageMapper.getCampIdByBigGroupId(id);
            } else if ("small_group".equals(type) || "smallGroup".equals(type)) {
                campId = volunteerManageMapper.getCampIdBySmallGroupId(id);
            }

            // 检查当天是否有作业（基于管理范围所属营期的计划）
            Integer planId = null;
            if (campId != null) {
                planId = homeworkMapper.getPlanIdByDateAndCamp(date, campId);
            }

            boolean hasHomework = planId != null;
            if (hasHomework && !studentIds.isEmpty()) {
                List<Map<String, Object>> submittedList = homeworkMapper.getSubmittedHomeworkList(studentIds, date);
                List<Map<String, Object>> lateList = homeworkMapper.getLateHomeworkList(studentIds, date);
                List<Map<String, Object>> pendingList = homeworkMapper.getPendingHomeworkList(studentIds, date);
                result.put("submittedList", submittedList);
                result.put("lateList", lateList);
                result.put("pendingList", pendingList);
            }
            result.put("hasHomework", hasHomework);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取作业层级列表（大组-小组-成员）
     */
    @Override
    public HomeworkHierarchyDTO getHomeworkHierarchyList(Long userId, String date, String status, String dutyType,
            Integer targetId) {
        try {
            // 打印日志，查看传入的参数
            System.out.println("获取作业层级列表 - 用户ID: " + userId);
            System.out.println("获取作业层级列表 - 日期: " + date);
            System.out.println("获取作业层级列表 - 状态: " + status);
            System.out.println("获取作业层级列表 - 职位类型: " + dutyType);
            System.out.println("获取作业层级列表 - 目标ID: " + targetId);

            HomeworkHierarchyDTO result = new HomeworkHierarchyDTO();
            result.setList(new ArrayList<>());

            // 检查 targetId 是否为 null
            if (targetId == null) {
                System.out.println("获取作业层级列表 - 错误: targetId 为 null");
                return result;
            }

            // 检查权限
            if ("学委".equals(dutyType) || "检委".equals(dutyType)) {
                checkVolunteerAuth(userId, "big_group", targetId);
            } else if ("学班".equals(dutyType) || "检班".equals(dutyType)) {
                checkVolunteerAuth(userId, "class", targetId);
            } else if ("学组".equals(dutyType) || "检组".equals(dutyType)) {
                checkVolunteerAuth(userId, "small_group", targetId);
            }

            // 根据职位类型和目标ID构建层级结构
            Map<String, Object> scope = new HashMap<>();
            if ("学委".equals(dutyType) || "检委".equals(dutyType)) {
                // 学委/检委：显示大组下的所有小组列表
                scope.put("bigGroupId", targetId);
                // 获取大组名称
                Map<String, Object> bigGroupInfo = homeworkMapper.getBigGroupInfo(targetId);
                String bigGroupName = bigGroupInfo != null ? (String) bigGroupInfo.get("name") : "大组" + targetId;
                scope.put("bigGroupName", bigGroupName);
                buildBigGroupHierarchy(result, scope);
            } else if ("学班".equals(dutyType) || "检班".equals(dutyType)) {
                // 学班/检班：显示班级下的大组-小组层级
                scope.put("classId", targetId);
                // 获取班级名称
                Map<String, Object> classInfo = homeworkMapper.getClassInfo(targetId);
                String className = classInfo != null ? (String) classInfo.get("name") : "班级" + targetId;
                scope.put("className", className);
                buildClassHierarchy(result, scope);
            } else if ("学组".equals(dutyType) || "检组".equals(dutyType)) {
                // 学组/检组：显示小组下的成员列表
                scope.put("smallGroupId", targetId);
                // 获取小组名称
                Map<String, Object> smallGroupInfo = homeworkMapper.getSmallGroupInfo(targetId);
                String smallGroupName = smallGroupInfo != null ? (String) smallGroupInfo.get("name") : "小组" + targetId;
                scope.put("smallGroupName", smallGroupName);
                buildSmallGroupHierarchy(result, scope);
            }

            System.out.println("获取作业层级列表 - 结果列表大小: " + result.getList().size());
            return result;
        } catch (Exception e) {
            System.out.println("获取作业层级列表 - 错误: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 构建班级层级（学班/检班视角）
     */
    private void buildClassHierarchy(HomeworkHierarchyDTO result, Map<String, Object> scope) {
        try {
            // 安全获取classId，处理Long和Integer类型
            Object classIdObj = scope.get("classId");
            Long classId = null;
            if (classIdObj instanceof Integer) {
                classId = ((Integer) classIdObj).longValue();
            } else if (classIdObj instanceof Long) {
                classId = (Long) classIdObj;
            }
            if (classId != null) {
                // 直接获取班级下的大组列表，不显示班级层级
                List<Map<String, Object>> bigGroups = homeworkMapper.getBigGroupsByClass(classId.intValue());
                for (Map<String, Object> bigGroup : bigGroups) {
                    // 安全获取bigGroupId，处理Long和Integer类型
                    Object bigGroupIdObj = bigGroup.get("bigGroupId");
                    Long bigGroupId = null;
                    if (bigGroupIdObj instanceof Integer) {
                        bigGroupId = ((Integer) bigGroupIdObj).longValue();
                    } else if (bigGroupIdObj instanceof Long) {
                        bigGroupId = (Long) bigGroupIdObj;
                    }
                    String bigGroupName = (String) bigGroup.get("bigGroupName");
                    HomeworkHierarchyDTO.HierarchyItem bigGroupItem = new HomeworkHierarchyDTO.HierarchyItem();
                    bigGroupItem.setId(bigGroupId);
                    bigGroupItem.setName(bigGroupName);
                    bigGroupItem.setType("bigGroup");
                    bigGroupItem.setParentId(null); // 直接作为根节点
                    bigGroupItem.setChildren(new ArrayList<>());
                    bigGroupItem.setExpandable(true);
                    // 获取大组下的小组列表
                    List<Map<String, Object>> smallGroups = homeworkMapper
                            .getSmallGroupsByBigGroup(bigGroupId.intValue());
                    for (Map<String, Object> smallGroup : smallGroups) {
                        // 安全获取smallGroupId，处理Long和Integer类型
                        Object smallGroupIdObj = smallGroup.get("smallGroupId");
                        Long smallGroupId = null;
                        if (smallGroupIdObj instanceof Integer) {
                            smallGroupId = ((Integer) smallGroupIdObj).longValue();
                        } else if (smallGroupIdObj instanceof Long) {
                            smallGroupId = (Long) smallGroupIdObj;
                        }
                        String smallGroupName = (String) smallGroup.get("smallGroupName");
                        HomeworkHierarchyDTO.HierarchyItem smallGroupItem = new HomeworkHierarchyDTO.HierarchyItem();
                        smallGroupItem.setId(smallGroupId);
                        smallGroupItem.setName(smallGroupName);
                        smallGroupItem.setType("smallGroup");
                        smallGroupItem.setParentId(bigGroupId);
                        smallGroupItem.setChildren(new ArrayList<>());
                        smallGroupItem.setExpandable(true);
                        // 获取小组成员列表
                        List<Map<String, Object>> members = homeworkMapper
                                .getMembersBySmallGroup(smallGroupId.intValue());
                        for (Map<String, Object> member : members) {
                            Long memberUserId = ((Number) member.get("userId")).longValue();
                            String memberName = (String) member.get("name");
                            HomeworkHierarchyDTO.HierarchyItem memberItem = new HomeworkHierarchyDTO.HierarchyItem();
                            memberItem.setId(memberUserId);
                            memberItem.setName(memberName);
                            memberItem.setType("member");
                            memberItem.setParentId(smallGroupId);
                            memberItem.setExpandable(false);
                            smallGroupItem.getChildren().add(memberItem);
                        }

                        bigGroupItem.getChildren().add(smallGroupItem);
                    }

                    bigGroupItem.setExpandable(!bigGroupItem.getChildren().isEmpty());
                    result.getList().add(bigGroupItem);
                }
            }
        } catch (Exception e) {
            System.out.println("构建班级层级 - 错误: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 构建大组层级（学委/检委视角）
     */
    private void buildBigGroupHierarchy(HomeworkHierarchyDTO result, Map<String, Object> scope) {
        try {
            // 安全获取bigGroupId，处理Long和Integer类型
            Object bigGroupIdObj = scope.get("bigGroupId");
            Long bigGroupId = null;
            if (bigGroupIdObj instanceof Integer) {
                bigGroupId = ((Integer) bigGroupIdObj).longValue();
            } else if (bigGroupIdObj instanceof Long) {
                bigGroupId = (Long) bigGroupIdObj;
            }
            if (bigGroupId != null) {
                // 直接获取大组下的小组列表，不显示大组层级
                System.out.println("构建大组层级 - 大组ID: " + bigGroupId);
                List<Map<String, Object>> smallGroups = homeworkMapper.getSmallGroupsByBigGroup(bigGroupId.intValue());
                System.out.println("构建大组层级 - 小组列表大小: " + smallGroups.size());
                for (Map<String, Object> smallGroup : smallGroups) {
                    System.out.println("构建大组层级 - 小组ID: " + smallGroup.get("smallGroupId") + ", 小组名称: "
                            + smallGroup.get("smallGroupName"));
                    // 安全获取smallGroupId，处理Long和Integer类型
                    Object smallGroupIdObj = smallGroup.get("smallGroupId");
                    Long smallGroupId = null;
                    if (smallGroupIdObj instanceof Integer) {
                        smallGroupId = ((Integer) smallGroupIdObj).longValue();
                    } else if (smallGroupIdObj instanceof Long) {
                        smallGroupId = (Long) smallGroupIdObj;
                    }
                    String smallGroupName = (String) smallGroup.get("smallGroupName");
                    HomeworkHierarchyDTO.HierarchyItem smallGroupItem = new HomeworkHierarchyDTO.HierarchyItem();
                    smallGroupItem.setId(smallGroupId);
                    smallGroupItem.setName(smallGroupName);
                    smallGroupItem.setType("smallGroup");
                    smallGroupItem.setParentId(null); // 直接作为根节点
                    smallGroupItem.setChildren(new ArrayList<>());
                    smallGroupItem.setExpandable(true);
                    // 获取小组成员列表
                    List<Map<String, Object>> members = homeworkMapper.getMembersBySmallGroup(smallGroupId.intValue());
                    for (Map<String, Object> member : members) {
                        Long memberUserId = ((Number) member.get("userId")).longValue();
                        String memberName = (String) member.get("name");
                        HomeworkHierarchyDTO.HierarchyItem memberItem = new HomeworkHierarchyDTO.HierarchyItem();
                        memberItem.setId(memberUserId);
                        memberItem.setName(memberName);
                        memberItem.setType("member");
                        memberItem.setParentId(smallGroupId);
                        memberItem.setExpandable(false);
                        smallGroupItem.getChildren().add(memberItem);
                    }

                    smallGroupItem.setExpandable(!smallGroupItem.getChildren().isEmpty());
                    result.getList().add(smallGroupItem);
                }
            }
        } catch (Exception e) {
            System.out.println("构建大组层级 - 错误: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 构建小组层级（学组/检组视角）
     */
    private void buildSmallGroupHierarchy(HomeworkHierarchyDTO result, Map<String, Object> scope) {
        try {
            // 安全获取smallGroupId，处理Long和Integer类型
            Object smallGroupIdObj = scope.get("smallGroupId");
            Long smallGroupId = null;
            if (smallGroupIdObj instanceof Integer) {
                smallGroupId = ((Integer) smallGroupIdObj).longValue();
            } else if (smallGroupIdObj instanceof Long) {
                smallGroupId = (Long) smallGroupIdObj;
            }
            if (smallGroupId != null) {
                // 直接获取小组成员列表，不显示小组层级
                List<Map<String, Object>> members = homeworkMapper.getMembersBySmallGroup(smallGroupId.intValue());
                for (Map<String, Object> member : members) {
                    Long memberUserId = ((Number) member.get("userId")).longValue();
                    String memberName = (String) member.get("name");
                    HomeworkHierarchyDTO.HierarchyItem memberItem = new HomeworkHierarchyDTO.HierarchyItem();
                    memberItem.setId(memberUserId);
                    memberItem.setName(memberName);
                    memberItem.setType("member");
                    memberItem.setParentId(null); // 直接作为根节点
                    memberItem.setExpandable(false);
                    result.getList().add(memberItem);
                }
            }
        } catch (Exception e) {
            System.out.println("构建小组层级 - 错误: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 检查志愿者权限
     */
    private void checkVolunteerAuth(Long userId, String type, Integer id) {
        try {
            System.out.println("检查权限 - 用户ID: " + userId + ", 类型: " + type + ", ID: " + id);
            // 先检查直接权限
            Integer authCount = homeworkMapper.checkVolunteerAuth(userId, type, id);
            System.out.println("直接权限检查结果: " + authCount);
            // 如果直接权限不足，检查层级权限
            if (authCount == null || authCount == 0) {
                if ("small_group".equals(type)) {
                    // 检查用户是否有大组权限
                    Integer bigGroupId = getBigGroupIdBySmallGroupId(id);
                    if (bigGroupId != null) {
                        Integer bigGroupAuth = homeworkMapper.checkBigGroupAuth(userId, bigGroupId);
                        System.out.println("大组权限检查结果: " + bigGroupAuth);
                        if (bigGroupAuth != null && bigGroupAuth > 0) {
                            return; // 有大组权限，允许访问
                        }
                        // 检查用户是否有班级权限
                        Integer classId = getClassIdByBigGroupId(bigGroupId);
                        if (classId != null) {
                            Integer classAuth = homeworkMapper.checkVolunteerAuth(userId, "class", classId);
                            System.out.println("班级权限检查结果: " + classAuth);
                            if (classAuth != null && classAuth > 0) {
                                return; // 有班级权限，允许访问
                            }
                        }
                    }
                } else if ("big_group".equals(type)) {
                    // 检查用户是否有班级权限
                    Integer classId = getClassIdByBigGroupId(id);
                    if (classId != null) {
                        Integer classAuth = homeworkMapper.checkVolunteerAuth(userId, "class", classId);
                        System.out.println("班级权限检查结果: " + classAuth);
                        if (classAuth != null && classAuth > 0) {
                            return; // 有班级权限，允许访问
                        }
                    }
                }
                throw new RuntimeException("无权限访问该范围数据");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 根据小组ID获取大组ID
     */
    private Integer getBigGroupIdBySmallGroupId(Integer smallGroupId) {
        try {
            Map<String, Object> result = homeworkMapper.getBigGroupIdBySmallGroupId(smallGroupId);
            if (result != null && result.containsKey("bigGroupId")) {
                return (Integer) result.get("bigGroupId");
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据大组ID获取班级ID
     */
    private Integer getClassIdByBigGroupId(Integer bigGroupId) {
        try {
            Map<String, Object> result = homeworkMapper.getClassIdByBigGroupId(bigGroupId);
            if (result != null && result.containsKey("classId")) {
                return (Integer) result.get("classId");
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取作业统计层级数据
     */
    @Override
    public HomeworkStatisticsHierarchyDTO getHomeworkStatisticsHierarchy(Long userId, String type, Integer id,
            String date) {
        try {
            // 检查权限
            checkVolunteerAuth(userId, type, id);

            HomeworkStatisticsHierarchyDTO result = new HomeworkStatisticsHierarchyDTO();
            result.setList(new ArrayList<>());

            if ("class".equals(type)) {
                // 班级层级：班级 -> 大组 -> 小组
                buildClassStatisticsHierarchy(result, id, date);
            } else if ("big_group".equals(type) || "bigGroup".equals(type)) {
                // 大组层级：大组 -> 小组
                buildBigGroupStatisticsHierarchy(result, id, date);
            } else if ("small_group".equals(type) || "smallGroup".equals(type)) {
                // 小组层级：只有小组
                buildSmallGroupStatisticsHierarchy(result, id, date);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 构建班级统计层级
     */
    private void buildClassStatisticsHierarchy(HomeworkStatisticsHierarchyDTO result, Integer classId, String date) {
        // 获取班级信息
        Map<String, Object> classInfo = homeworkMapper.getClassInfo(classId);
        if (classInfo == null)
            return;

        String className = (String) classInfo.get("name");

        // 构建班级统计项
        HomeworkStatisticsHierarchyDTO.StatisticsItem classItem = buildStatisticsItem("class", classId, className,
                date);

        // 获取班级下的大组列表
        List<Map<String, Object>> bigGroups = homeworkMapper.getBigGroupsByClass(classId);
        for (Map<String, Object> bigGroup : bigGroups) {
            Integer bigGroupId = (Integer) bigGroup.get("bigGroupId");
            String bigGroupName = (String) bigGroup.get("bigGroupName");

            // 构建大组统计项
            HomeworkStatisticsHierarchyDTO.StatisticsItem bigGroupItem = buildStatisticsItem("bigGroup", bigGroupId,
                    bigGroupName, date);

            // 获取大组下的小组列表
            List<Map<String, Object>> smallGroups = homeworkMapper.getSmallGroupsByBigGroup(bigGroupId);
            for (Map<String, Object> smallGroup : smallGroups) {
                Integer smallGroupId = (Integer) smallGroup.get("smallGroupId");
                String smallGroupName = (String) smallGroup.get("smallGroupName");

                // 构建小组统计项
                HomeworkStatisticsHierarchyDTO.StatisticsItem smallGroupItem = buildStatisticsItem("smallGroup",
                        smallGroupId, smallGroupName, date);
                bigGroupItem.getChildren().add(smallGroupItem);
            }

            classItem.getChildren().add(bigGroupItem);
        }

        result.getList().add(classItem);
    }

    /**
     * 构建大组统计层级
     */
    private void buildBigGroupStatisticsHierarchy(HomeworkStatisticsHierarchyDTO result, Integer bigGroupId,
            String date) {
        // 获取大组信息
        Map<String, Object> bigGroupInfo = homeworkMapper.getBigGroupInfo(bigGroupId);
        if (bigGroupInfo == null)
            return;

        String bigGroupName = (String) bigGroupInfo.get("name");

        // 构建大组统计项
        HomeworkStatisticsHierarchyDTO.StatisticsItem bigGroupItem = buildStatisticsItem("bigGroup", bigGroupId,
                bigGroupName, date);

        // 获取大组下的小组列表
        List<Map<String, Object>> smallGroups = homeworkMapper.getSmallGroupsByBigGroup(bigGroupId);
        for (Map<String, Object> smallGroup : smallGroups) {
            Integer smallGroupId = (Integer) smallGroup.get("smallGroupId");
            String smallGroupName = (String) smallGroup.get("smallGroupName");

            // 构建小组统计项
            HomeworkStatisticsHierarchyDTO.StatisticsItem smallGroupItem = buildStatisticsItem("smallGroup",
                    smallGroupId, smallGroupName, date);
            bigGroupItem.getChildren().add(smallGroupItem);
        }

        result.getList().add(bigGroupItem);
    }

    /**
     * 构建小组统计层级
     */
    private void buildSmallGroupStatisticsHierarchy(HomeworkStatisticsHierarchyDTO result, Integer smallGroupId,
            String date) {
        // 获取小组信息
        Map<String, Object> smallGroupInfo = homeworkMapper.getSmallGroupInfo(smallGroupId);
        if (smallGroupInfo == null)
            return;

        String smallGroupName = (String) smallGroupInfo.get("name");

        // 构建小组统计项
        HomeworkStatisticsHierarchyDTO.StatisticsItem smallGroupItem = buildStatisticsItem("smallGroup", smallGroupId,
                smallGroupName, date);
        result.getList().add(smallGroupItem);
    }

    /**
     * 构建统计项
     */
    private HomeworkStatisticsHierarchyDTO.StatisticsItem buildStatisticsItem(String type, Integer id, String name,
            String date) {
        HomeworkStatisticsHierarchyDTO.StatisticsItem item = new HomeworkStatisticsHierarchyDTO.StatisticsItem();
        item.setId(id);
        item.setName(name);
        item.setType(type);
        item.setChildren(new ArrayList<>());

        // 获取统计数据
        Integer totalCount = homeworkMapper.getMemberCountByGroup(type, id, date); // 传入日期参数
        Integer onTimeCount = homeworkMapper.getSubmittedCountByGroup(type, id, date); // 准时提交人数
        Integer lateCount = homeworkMapper.getLateCountByGroup(type, id, date); // 迟交人数

        // 计算总提交人数
        Integer totalSubmittedCount = (onTimeCount != null ? onTimeCount : 0) + (lateCount != null ? lateCount : 0);

        item.setTotalCount(totalCount != null ? totalCount : 0);
        item.setCompletedCount(onTimeCount != null ? onTimeCount : 0); // 准时提交人数
        item.setLateCount(lateCount != null ? lateCount : 0);

        // 未交人数 = 总人数 - 总提交人数
        item.setPendingCount(item.getTotalCount() - totalSubmittedCount);

        // 计算完成率（总提交人数/总人数）
        double completionRate = item.getTotalCount() > 0 ? (double) totalSubmittedCount / item.getTotalCount() * 100
                : 0;
        item.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);

        // 计算按时完成率（按时提交人数/总人数）
        double onTimeRate = item.getTotalCount() > 0
                ? (double) (onTimeCount != null ? onTimeCount : 0) / item.getTotalCount() * 100
                : 0;
        item.setOnTimeRate(Math.round(onTimeRate * 100.0) / 100.0);

        // 检查是否有作业（基于管理范围所属营期的计划）
        Integer campId = null;
        if ("class".equals(type)) {
            campId = volunteerManageMapper.getCampIdByClassId(id);
        } else if ("bigGroup".equals(type)) {
            campId = volunteerManageMapper.getCampIdByBigGroupId(id);
        } else if ("smallGroup".equals(type)) {
            campId = volunteerManageMapper.getCampIdBySmallGroupId(id);
        }

        Integer planId = null;
        if (campId != null) {
            planId = homeworkMapper.getPlanIdByDateAndCamp(date, campId);
        }
        item.setHasHomework(planId != null);

        return item;
    }

    @Override
    public MyHomeworkPageDTO getMyHomeworkPage(Long userId, Integer page, Integer size) {
        PageHelper.startPage(page, size);
        List<MyHomeworkDTO> list = homeworkMapper.selectMyHomeworkList(userId);
        PageInfo<MyHomeworkDTO> pageInfo = new PageInfo<>(list);
        MyHomeworkPageDTO result = new MyHomeworkPageDTO();
        result.setTotal(pageInfo.getTotal());
        result.setList(pageInfo.getList());
        return result;
    }

    @Override
    public ExcellentShowcasePageDTO getExcellentShowcasePage(Integer page, Integer size) {
        PageHelper.startPage(page, size);
        List<ExcellentShowcaseDTO> list = homeworkMapper.selectExcellentShowcaseList();
        PageInfo<ExcellentShowcaseDTO> pageInfo = new PageInfo<>(list);
        ExcellentShowcasePageDTO result = new ExcellentShowcasePageDTO();
        result.setTotal(pageInfo.getTotal());
        result.setList(pageInfo.getList());
        return result;
    }

    @Override
    @Transactional
    public void submitHomework(Long userId, HomeworkSubmitDTO dto) {
        if (dto.getPlanId() == null) {
            throw new BusinessException("排课计划ID不能为空");
        }
        if (dto.getTaskId() == null) {
            throw new BusinessException("任务ID不能为空");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BusinessException("作业内容不能为空");
        }
        Integer existingId = homeworkMapper.selectHomeworkIdByUserAndPlan(userId, dto.getPlanId());
        if (existingId != null) {
            homeworkMapper.updateHomeworkContent(existingId, dto.getTaskId(), dto.getContent());
        } else {
            Homework homework = new Homework();
            homework.setUserId(userId);
            homework.setPlanId(dto.getPlanId());
            homework.setTaskId(dto.getTaskId());
            homework.setContent(dto.getContent());
            homeworkMapper.insertHomework(homework);
        }
        userTaskRecordMapper.upsertDoneRecord(userId, dto.getPlanId(), dto.getTaskId());
        forwardHomeworkToGroup(userId, dto.getPlanId());
    }

    /**
     * 转发作业到小组群
     */
    private void forwardHomeworkToGroup(Long userId, Integer planId) {
        try {
            System.out.println("开始转发作业到小组群 - 用户ID: " + userId + ", 计划ID: " + planId);

            // 获取用户的小组信息
            Map<String, Object> userGroupInfo = homeworkMapper.getUserGroupInfo(userId);
            System.out.println("获取用户小组信息结果: " + (userGroupInfo != null ? "成功" : "失败"));
            if (userGroupInfo == null) {
                System.out.println("用户没有有效的小组信息，转发失败");
                return;
            }

            Integer classId = userGroupInfo.get("classId") != null ? ((Number) userGroupInfo.get("classId")).intValue()
                    : null;
            Integer bigGroupId = userGroupInfo.get("bigGroupId") != null
                    ? ((Number) userGroupInfo.get("bigGroupId")).intValue()
                    : null;
            Integer smallGroupId = userGroupInfo.get("smallGroupId") != null
                    ? ((Number) userGroupInfo.get("smallGroupId")).intValue()
                    : null;
            String userName = (String) userGroupInfo.get("name");
            System.out.println("用户小组信息 - 班级ID: " + classId + ", 大组ID: " + bigGroupId + ", 小组ID: " + smallGroupId
                    + ", 用户名: " + userName);

            // 获取小组群聊ID
            com.daily.dailychineseculture.entity.GroupChat smallGroupChat = groupChatMapper
                    .getGroupChatByTypeAndIds("smallGroup", classId, bigGroupId, smallGroupId);
            System.out.println("获取小组群聊结果: " + (smallGroupChat != null ? "成功" : "失败"));
            if (smallGroupChat == null) {
                System.out.println("小组群聊不存在，转发失败");
                return;
            }
            System.out.println("小组群聊ID: " + smallGroupChat.getChatId() + ", 群聊名称: " + smallGroupChat.getName());

            // 获取作业内容
            Map<String, Object> homeworkInfo = homeworkMapper.getHomeworkInfoByUserAndPlan(userId, planId);
            System.out.println("获取作业信息结果: " + (homeworkInfo != null ? "成功" : "失败"));
            if (homeworkInfo == null) {
                System.out.println("作业信息不存在，转发失败");
                return;
            }

            String content = (String) homeworkInfo.get("content");
            System.out.println("作业内容: " + content);

            // 获取营期和计划信息
            Map<String, Object> campInfo = homeworkMapper.getCampInfoByPlanId(planId);
            Map<String, Object> campPlanInfo = homeworkMapper.getCampPlanInfo(planId);
            System.out.println("获取营期信息结果: " + (campInfo != null ? "成功" : "失败"));
            System.out.println("获取计划信息结果: " + (campPlanInfo != null ? "成功" : "失败"));

            // 构建作业模板内容
            String templateContent = buildHomeworkTemplate(userId, planId, content, campInfo, campPlanInfo, userName);
            System.out.println("构建的模板内容: " + templateContent);

            // 构建消息内容
            String messageContent = templateContent;
            System.out.println("构建的消息内容: " + messageContent);

            // 发送消息到小组群
            // 获取小组的学组（小组管理员）ID
            Long smallGroupManagerId = homeworkMapper.getSmallGroupManagerUserId(smallGroupId);
            // 如果获取不到小组管理员ID，使用作业提交者作为发送者
            Long senderId = smallGroupManagerId != null ? smallGroupManagerId : userId;
            System.out.println("消息发送者ID: " + senderId);

            // 发送消息到小组群
            int result = groupChatMapper.sendMessage(smallGroupChat.getChatId(), senderId, null, messageContent, "text",
                    "", 0);
            System.out.println("发送消息结果: " + (result > 0 ? "成功" : "失败"));
            if (result > 0) {
                System.out.println("作业转发到小组群成功");
            } else {
                System.out.println("作业转发到小组群失败");
            }
        } catch (Exception e) {
            System.out.println("转发作业到小组群异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 转发作业到大组群
     */
    private void forwardHomeworkToBigGroup(Integer homeworkId) {
        try {
            System.out.println("开始转发作业到大组群 - 作业ID: " + homeworkId);

            // 获取作业信息
            Map<String, Object> homeworkInfo = homeworkMapper.getHomeworkDetail(homeworkId);
            System.out.println("获取作业信息结果: " + (homeworkInfo != null ? "成功" : "失败"));
            if (homeworkInfo == null) {
                System.out.println("作业信息不存在，转发失败");
                return;
            }

            Long userId = homeworkInfo.get("userId") != null ? ((Number) homeworkInfo.get("userId")).longValue() : null;
            String userName = (String) homeworkInfo.get("name");
            String content = (String) homeworkInfo.get("content");

            Map<String, Object> userGroupInfo = homeworkMapper.getUserGroupInfo(userId);
            Integer classId = userGroupInfo.get("classId") != null ? ((Number) userGroupInfo.get("classId")).intValue()
                    : null;
            Integer bigGroupId = userGroupInfo.get("bigGroupId") != null
                    ? ((Number) userGroupInfo.get("bigGroupId")).intValue()
                    : null;

            System.out.println(
                    "作业信息 - 用户ID: " + userId + ", 用户名: " + userName + ", 班级ID: " + classId + ", 大组ID: " + bigGroupId);
            System.out.println("作业内容: " + content);

            // 获取大组群聊ID
            com.daily.dailychineseculture.entity.GroupChat bigGroupChat = groupChatMapper
                    .getGroupChatByTypeAndIds("bigGroup", classId, bigGroupId, null);
            System.out.println("获取大组群聊结果: " + (bigGroupChat != null ? "成功" : "失败"));
            if (bigGroupChat == null) {
                System.out.println("大组群聊不存在，转发失败");
                return;
            }
            System.out.println("大组群聊ID: " + bigGroupChat.getChatId() + ", 群聊名称: " + bigGroupChat.getName());

            // 获取作业的计划ID
            Integer planId = homeworkMapper.getPlanIdByHomeworkId(homeworkId);
            System.out.println("获取计划ID结果: " + (planId != null ? planId : "失败"));

            // 获取营期和计划信息
            Map<String, Object> campInfo = null;
            Map<String, Object> campPlanInfo = null;
            if (planId != null) {
                campInfo = homeworkMapper.getCampInfoByPlanId(planId);
                campPlanInfo = homeworkMapper.getCampPlanInfo(planId);
                System.out.println("获取营期信息结果: " + (campInfo != null ? "成功" : "失败"));
                System.out.println("获取计划信息结果: " + (campPlanInfo != null ? "成功" : "失败"));
            }

            // 构建作业模板内容
            String templateContent = buildHomeworkTemplate(userId, planId, content, campInfo, campPlanInfo, userName);
            System.out.println("构建的模板内容: " + templateContent);

            // 构建消息内容
            String messageContent = userName + " 的作业被评为小组优秀作业：\n" + templateContent;
            System.out.println("构建的消息内容: " + messageContent);

            // 获取小组的学组（小组管理员）ID
            Integer smallGroupId = homeworkMapper.getSmallGroupIdByHomeworkId(homeworkId);
            // 获取小组的学组（小组管理员）ID
            Long smallGroupManagerId = homeworkMapper.getSmallGroupManagerUserId(smallGroupId);
            // 如果获取不到小组管理员ID，使用作业提交者作为发送者
            Long senderId = smallGroupManagerId != null ? smallGroupManagerId : userId;
            System.out.println("消息发送者ID: " + senderId);

            // 发送消息到大组群
            int result = groupChatMapper.sendMessage(bigGroupChat.getChatId(), senderId, null, messageContent, "text",
                    "", 0);
            System.out.println("发送消息结果: " + (result > 0 ? "成功" : "失败"));
            if (result > 0) {
                System.out.println("作业转发到大组群成功");
            } else {
                System.out.println("作业转发到大组群失败");
            }
        } catch (Exception e) {
            System.out.println("转发作业到大组群异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 转发作业到班级群
     */
    private void forwardHomeworkToClassGroup(Integer homeworkId) {
        try {
            System.out.println("开始转发作业到班级群 - 作业ID: " + homeworkId);

            // 获取作业信息
            Map<String, Object> homeworkInfo = homeworkMapper.getHomeworkDetail(homeworkId);
            System.out.println("获取作业信息结果: " + (homeworkInfo != null ? "成功" : "失败"));
            if (homeworkInfo == null) {
                System.out.println("作业信息不存在，转发失败");
                return;
            }

            Long userId = homeworkInfo.get("userId") != null ? ((Number) homeworkInfo.get("userId")).longValue() : null;
            String userName = (String) homeworkInfo.get("name");
            String content = (String) homeworkInfo.get("content");

            Map<String, Object> userGroupInfo = homeworkMapper.getUserGroupInfo(userId);
            Integer classId = userGroupInfo.get("classId") != null ? ((Number) userGroupInfo.get("classId")).intValue()
                    : null;

            System.out.println("作业信息 - 用户ID: " + userId + ", 用户名: " + userName + ", 班级ID: " + classId);
            System.out.println("作业内容: " + content);

            // 获取班级群聊ID
            com.daily.dailychineseculture.entity.GroupChat classGroupChat = groupChatMapper
                    .getGroupChatByTypeAndIds("class", classId, null, null);
            System.out.println("获取班级群聊结果: " + (classGroupChat != null ? "成功" : "失败"));
            if (classGroupChat == null) {
                System.out.println("班级群聊不存在，转发失败");
                return;
            }
            System.out.println("班级群聊ID: " + classGroupChat.getChatId() + ", 群聊名称: " + classGroupChat.getName());

            // 获取作业的计划ID
            Integer planId = homeworkMapper.getPlanIdByHomeworkId(homeworkId);
            System.out.println("获取计划ID结果: " + (planId != null ? planId : "失败"));

            // 获取营期和计划信息
            Map<String, Object> campInfo = null;
            Map<String, Object> campPlanInfo = null;
            if (planId != null) {
                campInfo = homeworkMapper.getCampInfoByPlanId(planId);
                campPlanInfo = homeworkMapper.getCampPlanInfo(planId);
                System.out.println("获取营期信息结果: " + (campInfo != null ? "成功" : "失败"));
                System.out.println("获取计划信息结果: " + (campPlanInfo != null ? "成功" : "失败"));
            }

            // 构建作业模板内容
            String templateContent = buildHomeworkTemplate(userId, planId, content, campInfo, campPlanInfo, userName);
            System.out.println("构建的模板内容: " + templateContent);

            // 构建消息内容
            String messageContent = userName + " 的作业被评为大组优秀作业：\n" + templateContent;
            System.out.println("构建的消息内容: " + messageContent);

            // 获取小组的学组（小组管理员）ID
            Integer smallGroupId = homeworkMapper.getSmallGroupIdByHomeworkId(homeworkId);
            Long smallGroupManagerId = smallGroupId != null ? homeworkMapper.getSmallGroupManagerUserId(smallGroupId)
                    : null;
            // 如果获取不到小组管理员ID，使用默认值 1L
            // 如果获取不到小组管理员ID，使用作业提交者作为发送者
            Long senderId = smallGroupManagerId != null ? smallGroupManagerId : userId;
            System.out.println("消息发送者ID: " + senderId);

            // 发送消息到班级群
            int result = groupChatMapper.sendMessage(classGroupChat.getChatId(), senderId, null, messageContent, "text",
                    "", 0);
            System.out.println("发送消息结果: " + (result > 0 ? "成功" : "失败"));
            if (result > 0) {
                System.out.println("作业转发到班级群成功");
            } else {
                System.out.println("作业转发到班级群失败");
            }
        } catch (Exception e) {
            System.out.println("转发作业到班级群异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 构建作业模板内容
     */
    private String buildHomeworkTemplate(Long userId, Integer planId, String content, Map<String, Object> campInfo,
            Map<String, Object> campPlanInfo, String userName) {
        try {
            // 获取营期信息
            Integer campId = null;
            String campName = null;
            String levelName = null;
            if (campInfo != null) {
                campId = campInfo.get("campId") != null ? ((Number) campInfo.get("campId")).intValue() : null;
                campName = (String) campInfo.get("name");
                levelName = (String) campInfo.get("levelName");
            }

            // 获取计划信息
            Integer dayIndex = null;
            if (campPlanInfo != null) {
                dayIndex = campPlanInfo.get("dayIndex") != null ? ((Number) campPlanInfo.get("dayIndex")).intValue()
                        : null;
            }

            // 获取用户信息
            Map<String, Object> userInfo = homeworkMapper.getUserInfo(userId);
            String region = userInfo != null ? (String) userInfo.get("region") : "";

            // 构建模板内容
            StringBuilder template = new StringBuilder();
            template.append("致良知线上").append(levelName != null ? levelName : "X");
            template.append(campName != null ? campName : "XX").append("功课学习第")
                    .append(dayIndex != null ? dayIndex : "X").append("天\n\n");
            template.append("时间：").append(getPlanDate(planId)).append("\n\n");
            template.append("姓名：").append(userName != null ? userName : "").append("\n");
            template.append("地区：").append(region != null ? region : "").append("\n");

            template.append("｜当｜下｜即｜未｜来｜\n");
            template.append("【自省 利他 致良知】\n");
            template.append(content != null ? content : "");
            template.append("\n\n今日总得分10分✔\n");
            template.append("理上明、事上磨、境上炼\n");
            template.append("去私欲、存天理、致良知\n");
            template.append("明理、立志、践行、印证\n");
            template.append("做圣贤文化实践的榜样，\n");
            template.append("为社会做出实质性贡献。");

            return template.toString();
        } catch (Exception e) {
            System.out.println("构建作业模板异常: " + e.getMessage());
            e.printStackTrace();
            return content != null ? content : "";
        }
    }

    /**
     * 获取排课计划的日期
     */
    private String getPlanDate(Integer planId) {
        try {
            if (planId == null)
                return "X年X月X日";
            Map<String, Object> planInfo = homeworkMapper.getCampPlanDate(planId);
            if (planInfo != null && planInfo.containsKey("planDate")) {
                Object planDateObj = planInfo.get("planDate");
                if (planDateObj instanceof java.util.Date) {
                    java.util.Date planDate = (java.util.Date) planDateObj;
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年MM月dd日");
                    return sdf.format(planDate);
                } else if (planDateObj instanceof java.time.LocalDate) {
                    java.time.LocalDate planDate = (java.time.LocalDate) planDateObj;
                    return planDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
                } else if (planDateObj instanceof String) {
                    return (String) planDateObj;
                }
            }
            return "X年X月X日";
        } catch (Exception e) {
            e.printStackTrace();
            return "X年X月X日";
        }
    }

}