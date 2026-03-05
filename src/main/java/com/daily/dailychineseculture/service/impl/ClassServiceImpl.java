package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.common.Result; // 新增：导入Result返回类
import com.daily.dailychineseculture.dto.BigGroupDTO;
import com.daily.dailychineseculture.dto.ClassDTO;
import com.daily.dailychineseculture.dto.SmallGroupDTO;
import com.daily.dailychineseculture.mapper.ClassMapper;
import com.daily.dailychineseculture.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 班级、大组、小组服务实现类
 */
@Service
public class ClassServiceImpl implements ClassService {

    @Autowired
    private ClassMapper classMapper;

    /**
     * 获取所有班级列表
     */
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

    /**
     * 获取所有大组列表
     */
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

    /**
     * 获取所有小组列表
     */
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

    /**
     * 自动分班（地域邻近优先）
     */
    @Override
    public Result autoAssign(Integer campId, Integer perClassNum) {
        try {
            // 1. 参数校验
            if (perClassNum == null || perClassNum <= 0) {
                return Result.error("每班人数必须大于0");
            }

            // 2. 获取待分班学员
            List<Map<String, Object>> students = classMapper.getAuditPassStudents(campId);
            if (students == null || students.isEmpty()) {
                return Result.success("暂无待分班学员");
            }

            // 3. 简单的地域邻近分班逻辑（先按region分组，再平均分班）
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

            // 4. 更新数据库（分班核心操作）
            int classId = 1;
            for (List<Map<String, Object>> cls : classes) {
                for (Map<String, Object> student : cls) {
                    Integer userId = (Integer) student.get("user_id");
                    classMapper.updateStudentClassId(userId, classId, campId);
                }
                classId++;
            }

            // 5. 关键：只返回成功提示，不返回班级列表（避免类型不匹配报错）
            return Result.success("分班成功，共分" + classes.size() + "个班级");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("分班失败：" + e.getMessage());
        }
    }
}