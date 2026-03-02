package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.*;
import com.daily.dailychineseculture.mapper.HomeworkMapper;
import com.daily.dailychineseculture.mapper.VolunteerManageMapper;
import com.daily.dailychineseculture.service.HomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    private VolunteerManageMapper volunteerManageMapper;

    /**
     * 获取作业列表（支持搜索）
     */
    @Override
    public HomeworkListDTO getHomeworkList(Long userId, String type, Integer id, String status, String date, String searchKeyword) {
        // 检查权限
        checkVolunteerAuth(userId, type, id);

        // 获取学员ID列表
        List<Long> studentIds = homeworkMapper.getStudentIdsByScope(type, id);

        HomeworkListDTO result = new HomeworkListDTO();
        result.setList(new ArrayList<>());
        result.setTotal(0);

        if (studentIds.isEmpty()) {
            return result;
        }

        // 获取作业列表
        List<Map<String, Object>> homeworkList = homeworkMapper.getHomeworkList(studentIds, status, date,searchKeyword);

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

            // 处理 is_excellent 字段
            Boolean isExcellent = false;
            Object isExcellentObj = item.get("isExcellent");
            if (isExcellentObj != null) {
                isExcellent = (Boolean) isExcellentObj;
            }

            // 处理 submit_time 字段
            String submitTimeStr = "";
            Object submitTimeObj = item.get("submit_time");
            if (submitTimeObj != null) {
                if (submitTimeObj instanceof java.util.Date) {
                    submitTimeStr = sdf.format((java.util.Date) submitTimeObj);
                } else if (submitTimeObj instanceof java.time.LocalDateTime) {
                    java.time.LocalDateTime localDateTime = (java.time.LocalDateTime) submitTimeObj;
                    java.util.Date dateObj = java.util.Date.from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
                    submitTimeStr = sdf.format(dateObj);
                }
            }

            // 获取组织信息
            String campName = (String) item.get("camp_name");
            String className = (String) item.get("class_name");
            String bigGroupName = (String) item.get("big_group_name");
            String smallGroupName = (String) item.get("small_group_name");

            List<String> organizationParts = new ArrayList<>();
            if (campName != null && !campName.isEmpty()) organizationParts.add(campName);
            if (className != null && !className.isEmpty()) organizationParts.add(className);
            if (bigGroupName != null && !bigGroupName.isEmpty()) organizationParts.add(bigGroupName);
            if (smallGroupName != null && !smallGroupName.isEmpty()) organizationParts.add(smallGroupName);

            String organization = organizationParts.isEmpty() ? "未分组" : String.join("-", organizationParts);

            items.add(new HomeworkListDTO.HomeworkItem(homeworkId, name, isExcellent, submitTimeStr, organization));
        }

        result.setList(items);
        result.setTotal(items.size());

        return result;
    }

    /**
     * 标记优秀作业
     */
    @Override
    public boolean markExcellentHomework(Integer homeworkId, Boolean isExcellent) {
        // 检查作业是否存在
        int exists = homeworkMapper.checkHomeworkExists(homeworkId);
        if (exists == 0) {
            return false;
        }

        int result = homeworkMapper.markExcellentHomework(homeworkId, isExcellent);
        return result > 0;
    }

    /**
     * 获取作业详情
     */
    @Override
    public HomeworkDetailDTO getHomeworkDetail(Integer homeworkId) {
        Map<String, Object> homework = homeworkMapper.getHomeworkDetail(homeworkId);
        if (homework == null) {
            return null;
        }

        Integer homeworkIdResult = (Integer) homework.get("homework_id");
        Long userId = ((Number) homework.get("user_id")).longValue();
        String content = (String) homework.get("content");

        // 获取学生姓名
        String studentName = (String) homework.get("name");
        if (studentName == null) studentName = "学员" + userId;

        // 处理 is_excellent 字段
        Boolean isExcellent = false;
        Object isExcellentObj = homework.get("isExcellent");
        if (isExcellentObj != null) {
            isExcellent = (Boolean) isExcellentObj;
        }

        // 处理 submit_time 字段
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String submitTimeStr = "";
        Object submitTimeObj = homework.get("submit_time");
        if (submitTimeObj != null) {
            if (submitTimeObj instanceof java.util.Date) {
                submitTimeStr = sdf.format((java.util.Date) submitTimeObj);
            } else if (submitTimeObj instanceof java.time.LocalDateTime) {
                java.time.LocalDateTime localDateTime = (java.time.LocalDateTime) submitTimeObj;
                java.util.Date dateObj = java.util.Date.from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
                submitTimeStr = sdf.format(dateObj);
            }
        }

        // 获取组织信息
        String campName = (String) homework.get("camp_name");
        String className = (String) homework.get("class_name");
        String bigGroupName = (String) homework.get("big_group_name");
        String smallGroupName = (String) homework.get("small_group_name");

        List<String> organizationParts = new ArrayList<>();
        if (campName != null && !campName.isEmpty()) organizationParts.add(campName);
        if (className != null && !className.isEmpty()) organizationParts.add(className);
        if (bigGroupName != null && !bigGroupName.isEmpty()) organizationParts.add(bigGroupName);
        if (smallGroupName != null && !smallGroupName.isEmpty()) organizationParts.add(smallGroupName);

        String organization = organizationParts.isEmpty() ? "未分配" : String.join("-", organizationParts);

        if (content == null) {
            content = "无作业内容";
        }

        return new HomeworkDetailDTO(homeworkIdResult, studentName, userId, organization,
                submitTimeStr, isExcellent, content);
    }

    /**
     * 检查志愿者权限
     */
    private void checkVolunteerAuth(Long userId, String type, Integer id) {
        // 类型映射
        Map<String, String> targetTypeMap = new HashMap<>();
        targetTypeMap.put("class", "class");
        targetTypeMap.put("bigGroup", "big_group");
        targetTypeMap.put("big_group", "big_group");  // 添加下划线命名支持
        targetTypeMap.put("smallGroup", "small_group");
        targetTypeMap.put("small_group", "small_group");

        String targetType = targetTypeMap.get(type);
        if (targetType == null) {
            throw new RuntimeException("无效的筛选类型");
        }

        Integer authCount = 0;

        try {
            switch (type) {
                case "class":
                    authCount = homeworkMapper.checkVolunteerAuth(userId, targetType, id);
                    break;
                case "bigGroup":
                case "big_group":
                    authCount = homeworkMapper.checkBigGroupAuth(userId, id);
                    break;
                case "smallGroup":
                case "small_group":
                    authCount = homeworkMapper.checkSmallGroupAuth(userId, id);
                    break;
            }
        } catch (Exception e) {
            // 捕获类型转换异常
            throw new RuntimeException("权限检查失败：" + e.getMessage());
        }

        if (authCount == null || authCount == 0) {
            throw new RuntimeException("无权限访问该范围数据");
        }
    }

    /**
     * 获取作业统计数据
     */
    @Override
    public Map<String, Object> getHomeworkStatistics(Long userId, String type, Integer id, String date, String searchKeyword) {
        // 检查权限
        checkVolunteerAuth(userId, type, id);

        // 获取学员ID列表
        List<Long> studentIds = homeworkMapper.getStudentIdsByScope(type, id);

        // 确保包含当前用户（管理者）自己
        if (!studentIds.contains(userId)) {
            studentIds.add(userId);
        }
        // 根据搜索关键字过滤学员
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            studentIds = filterStudentsByKeyword(studentIds, searchKeyword);
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", studentIds.size());
        statistics.put("completedCount", 0);
        statistics.put("pendingCount", studentIds.size());
        statistics.put("lateCount", 0);
        statistics.put("completionRate", 0);
        statistics.put("onTimeRate", 0);
        statistics.put("hasHomework", false);

        Integer planId = homeworkMapper.getPlanIdByDate(date);
        if (planId != null) {
            statistics.put("hasHomework", true);

            if (!studentIds.isEmpty()) {
                // 获取已交作业的学生名单（当天提交）
                List<Map<String, Object>> submittedList = homeworkMapper.getSubmittedHomeworkList(studentIds, date);
                // 获取迟交作业的学生名单（超过当天提交）
                List<Map<String, Object>> lateList = homeworkMapper.getLateHomeworkList(studentIds, date);
                // 获取未交作业的学生名单
                List<Map<String, Object>> notSubmittedList = homeworkMapper.getNotSubmittedHomeworkList(studentIds, date);

                int onTimeCount = submittedList.size();
                int lateCount = lateList.size();
                int completedCount = onTimeCount + lateCount;
                int pendingCount = notSubmittedList.size();
                int totalCount = studentIds.size();

                int completionRate = totalCount > 0 ? (completedCount * 100) / totalCount : 0;
                int onTimeRate = completedCount > 0 ? (onTimeCount * 100) / completedCount : 0;
                statistics.put("completedCount", completedCount);
                statistics.put("pendingCount", pendingCount);
                statistics.put("lateCount", lateCount);
                statistics.put("onTimeCount", onTimeCount);
                statistics.put("completionRate", completionRate);
                statistics.put("onTimeRate", onTimeRate);
            }
        }

        return statistics;
    }

    /**
     * 获取作业状态名单（已交/未交/迟交）
     */
    @Override
    public Map<String, Object> getHomeworkStatusList(Long userId, String type, Integer id, String date,String searchKeyword) {
        // 检查权限
        checkVolunteerAuth(userId, type, id);

        // 获取学员ID列表
        List<Long> studentIds = homeworkMapper.getStudentIdsByScope(type, id);

        // 确保包含当前用户（管理者）自己
        if (!studentIds.contains(userId)) {
            studentIds.add(userId);
        }
        // 根据搜索关键字过滤学员
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            studentIds = filterStudentsByKeyword(studentIds, searchKeyword);
        }

        Map<String, Object> result = new HashMap<>();

        // 获取营期计划信息
        Integer planId = homeworkMapper.getPlanIdByDate(date);
        Map<String, Object> campPlan = new HashMap<>();
        campPlan.put("planId", planId);
        campPlan.put("deadline", date); // 使用plan_date作为截止时间
        result.put("campPlan", campPlan);

        // 获取已交作业的学生名单（当天提交）
        List<Map<String, Object>> submittedList = homeworkMapper.getSubmittedHomeworkList(studentIds, date);
        result.put("submittedList", submittedList);

        // 获取未交作业的学生名单
        List<Map<String, Object>> notSubmittedList = homeworkMapper.getNotSubmittedHomeworkList(studentIds, date);
        result.put("notSubmittedList", notSubmittedList);

        // 获取迟交作业的学生名单（超过当天提交）
        List<Map<String, Object>> lateList = homeworkMapper.getLateHomeworkList(studentIds, date);
        result.put("lateList", lateList);

        // 计算统计数据
        int totalCount = studentIds.size();
        int submittedCount = submittedList.size();
        int notSubmittedCount = notSubmittedList.size();
        int lateCount = lateList.size();
        int onTimeCount = submittedCount; // 当天提交的都算按时

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", totalCount);
        statistics.put("submittedCount", submittedCount);
        statistics.put("notSubmittedCount", notSubmittedCount);
        statistics.put("lateCount", lateCount);
        statistics.put("onTimeCount", onTimeCount);
        statistics.put("completionRate", totalCount > 0 ? (submittedCount * 100) / totalCount : 0);
        statistics.put("onTimeRate", submittedCount > 0 ? (onTimeCount * 100) / submittedCount : 0);

        result.put("statistics", statistics);

        return result;
    }

    /**
     * 根据搜索关键字过滤学员
     */
    private List<Long> filterStudentsByKeyword(List<Long> studentIds, String searchKeyword) {
        if (studentIds.isEmpty() || searchKeyword == null || searchKeyword.isEmpty()) {
            return studentIds;
        }

        // 使用HomeworkMapper的filterStudentsByKeyword方法进行过滤
        return homeworkMapper.filterStudentsByKeyword(studentIds, searchKeyword);
    }
}