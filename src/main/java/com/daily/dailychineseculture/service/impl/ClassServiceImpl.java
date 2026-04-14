package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.BigGroupDTO;
import com.daily.dailychineseculture.dto.ClassAssignResultDTO;
import com.daily.dailychineseculture.dto.ClassDTO;
import com.daily.dailychineseculture.dto.SmallGroupDTO;
import com.daily.dailychineseculture.mapper.ClassMapper;
import com.daily.dailychineseculture.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClassServiceImpl implements ClassService {

    @Autowired
    private ClassMapper classMapper;

    @Override
    public List<ClassDTO> getAllClasses() {
        List<Map<String, Object>> classList = classMapper.getAllClasses();
        List<ClassDTO> result = new ArrayList<>();
        for (Map<String, Object> item : classList) {
            Integer classId = (Integer) item.get("class_id");
            String name = (String) item.get("name");
            String campName = (String) item.get("camp_name");
            Integer campId = (Integer) item.get("camp_id");
            result.add(new ClassDTO(classId, name, campName, campId));
        }
        return result;
    }

    @Override
    public List<BigGroupDTO> getAllBigGroups() {
        List<Map<String, Object>> bigGroupList = classMapper.getAllBigGroups();
        List<BigGroupDTO> result = new ArrayList<>();
        for (Map<String, Object> item : bigGroupList) {
            Integer bigGroupId = (Integer) item.get("big_group_id");
            String name = (String) item.get("name");
            String className = (String) item.get("class_name");
            Integer classId = (Integer) item.get("class_id");
            String campName = (String) item.get("camp_name");
            result.add(new BigGroupDTO(bigGroupId, name, className, classId, campName));
        }
        return result;
    }

    @Override
    public List<SmallGroupDTO> getAllSmallGroups() {
        List<Map<String, Object>> smallGroupList = classMapper.getAllSmallGroups();
        List<SmallGroupDTO> result = new ArrayList<>();
        for (Map<String, Object> item : smallGroupList) {
            Integer smallGroupId = (Integer) item.get("small_group_id");
            String name = (String) item.get("name");
            String bigGroupName = (String) item.get("big_group_name");
            Integer bigGroupId = (Integer) item.get("big_group_id");
            String className = (String) item.get("class_name");
            String campName = (String) item.get("camp_name");
            result.add(new SmallGroupDTO(smallGroupId, name, bigGroupName, bigGroupId, className, campName));
        }
        return result;
    }

    @Override
    public Result autoAssign(Integer campId, Integer perClassNum) {
        try {
            if (perClassNum == null || perClassNum <= 0) {
                return Result.error("每班人数必须大于0");
            }
            List<Map<String, Object>> students = classMapper.getAuditPassStudents(campId);
            if (students == null || students.isEmpty()) {
                return Result.success("暂无待分班学员");
            }
            List<List<Map<String, Object>>> classes = new ArrayList<>();
            List<Map<String, Object>> currentClass = new ArrayList<>();
            for (Map<String, Object> student : students) {
                if (currentClass.size() >= perClassNum) {
                    classes.add(currentClass);
                    currentClass = new ArrayList<>();
                }
                currentClass.add(student);
            }
            if (!currentClass.isEmpty()) {
                classes.add(currentClass);
            }
            int classId = 1;
            for (List<Map<String, Object>> cls : classes) {
                for (Map<String, Object> student : cls) {
                    Integer userId = (Integer) student.get("user_id");
                    classMapper.updateStudentClassId(userId, classId, campId);
                }
                classId++;
            }
            return Result.success("分班成功，共分" + classes.size() + "个班级");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("分班失败：" + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getUnassignedStudents(Integer campId) {
        List<Map<String, Object>> allStudents = classMapper.getAuditPassStudents(campId);
        if (allStudents == null) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> unassigned = new ArrayList<>();
        for (Map<String, Object> student : allStudents) {
            Object classIdObj = student.get("class_id");
            if (classIdObj == null) {
                unassigned.add(student);
            }
        }
        return unassigned;
    }

    @Override
    public List<ClassDTO> getClassesByCampId(Integer campId) {
        List<Map<String, Object>> classList = classMapper.getClassesByCampId(campId);
        List<ClassDTO> result = new ArrayList<>();
        if (classList == null) {
            return result;
        }
        for (Map<String, Object> item : classList) {
            Integer classId = (Integer) item.get("class_id");
            String name = (String) item.get("name");
            String campName = item.get("camp_name") != null ? (String) item.get("camp_name") : "";
            Integer cid = (Integer) item.get("camp_id");
            result.add(new ClassDTO(classId, name, campName, cid));
        }
        return result;
    }

    @Override
    @Transactional
    public Result<ClassAssignResultDTO> assignStudentsByClassCount(Integer campId, Integer classCount) {
        try {
            if (campId == null || campId <= 0) {
                return Result.error("营期ID无效");
            }
            if (classCount == null || classCount <= 0) {
                return Result.error("班级数量必须大于0");
            }
            List<Map<String, Object>> unassignedStudents = getUnassignedStudents(campId);
            if (unassignedStudents.isEmpty()) {
                return Result.error("暂无待分班学员");
            }
            int totalStudents = unassignedStudents.size();
            int avgPerClass = (totalStudents + classCount - 1) / classCount;
            List<ClassAssignResultDTO.ClassDetail> classDetails = new ArrayList<>();
            List<List<Map<String, Object>>> classBuckets = new ArrayList<>();
            for (int i = 0; i < classCount; i++) {
                classBuckets.add(new ArrayList<>());
            }
            for (int i = 0; i < unassignedStudents.size(); i++) {
                int bucketIndex = i / avgPerClass;
                if (bucketIndex >= classCount) {
                    bucketIndex = classCount - 1;
                }
                classBuckets.get(bucketIndex).add(unassignedStudents.get(i));
            }
            for (int i = 0; i < classBuckets.size(); i++) {
                List<Map<String, Object>> bucket = classBuckets.get(i);
                if (bucket.isEmpty()) {
                    continue;
                }
                String className = "第" + (i + 1) + "班";
                Integer generatedClassId = classMapper.insertClassAndReturnId(campId, className);
                if (generatedClassId == null) {
                    continue;
                }
                List<ClassAssignResultDTO.StudentInfo> studentInfos = new ArrayList<>();
                for (Map<String, Object> s : bucket) {
                    Integer userId = (Integer) s.get("user_id");
                    classMapper.updateStudentClassId(userId, generatedClassId, campId);
                    ClassAssignResultDTO.StudentInfo info = new ClassAssignResultDTO.StudentInfo();
                    info.setUserId(((Number) s.get("user_id")).longValue());
                    info.setNickname(s.get("nickname") != null ? (String) s.get("nickname") : "");
                    info.setRegion(s.get("region") != null ? (String) s.get("region") : "");
                    info.setGender(s.get("gender") != null ? parseGender(s.get("gender")) : 0);
                    studentInfos.add(info);
                }
                ClassAssignResultDTO.ClassDetail classDetail = new ClassAssignResultDTO.ClassDetail();
                classDetail.setClassId(generatedClassId);
                classDetail.setClassName(className);
                classDetail.setStudentCount(bucket.size());
                classDetail.setStudents(studentInfos);
                classDetails.add(classDetail);
            }
            ClassAssignResultDTO dto = new ClassAssignResultDTO();
            dto.setCampId(campId);
            dto.setTotalStudents(totalStudents);
            dto.setClassCount(classDetails.size());
            dto.setAvgPerClass(avgPerClass);
            dto.setClasses(classDetails);
            return Result.success(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("分班失败：" + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getAllStudentsByCampId(Integer campId) {
        return classMapper.getAuditPassStudents(campId);
    }

    @Override
    @Transactional
    public Result<Void> moveStudentToClass(Long userId, Integer campId, Integer newClassId) {
        try {
            if (userId == null) {
                return Result.error("学员ID不能为空");
            }
            if (campId == null) {
                return Result.error("营期ID不能为空");
            }
            int rows = classMapper.updateStudentClassId(userId.intValue(), newClassId, campId);
            if (rows > 0) {
                return Result.success((Void) null);
            } else {
                return Result.error("移动学员失败：记录不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("移动学员失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getStudentDetail(Long userId, Integer campId) {
        if (userId == null || campId == null) {
            return null;
        }
        List<Map<String, Object>> students = classMapper.getAuditPassStudents(campId);
        if (students == null) {
            return null;
        }
        for (Map<String, Object> student : students) {
            Object uid = student.get("user_id");
            if (uid != null && ((Number) uid).longValue() == userId) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("userId", ((Number) student.get("user_id")).longValue());
                detail.put("account", student.get("account") != null ? student.get("account") : "");
                detail.put("nickname", student.get("nickname") != null ? student.get("nickname") : "");
                detail.put("region", student.get("region") != null ? student.get("region") : "");
                detail.put("birthday", student.get("birthday") != null ? student.get("birthday") : "");
                detail.put("gender", parseGender(student.get("gender")));
                detail.put("profession", student.get("profession") != null ? student.get("profession") : "");
                detail.put("phone", student.get("phone") != null ? student.get("phone") : "");
                detail.put("classId", student.get("class_id"));
                if (student.get("class_id") != null) {
                    String className = classMapper.getClassNameById((Integer) student.get("class_id"));
                    detail.put("className", className != null ? className : "");
                } else {
                    detail.put("className", "未分班");
                }
                return detail;
            }
        }
        return null;
    }

    @Override
    @Transactional
    public Result<Void> resetAssignment(Integer campId) {
        try {
            if (campId == null || campId <= 0) {
                return Result.error("营期ID无效");
            }
            int rows = classMapper.resetStudentClassId(campId);
            return Result.success((Void) null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("重置分班失败：" + e.getMessage());
        }
    }

    private int parseGender(Object gender) {
        if (gender == null) {
            return 0;
        }
        if (gender instanceof Integer) {
            return (Integer) gender;
        }
        String str = gender.toString();
        if ("男".equals(str) || "M".equalsIgnoreCase(str)) {
            return 1;
        } else if ("女".equals(str) || "F".equalsIgnoreCase(str)) {
            return 2;
        }
        return 0;
    }
}
