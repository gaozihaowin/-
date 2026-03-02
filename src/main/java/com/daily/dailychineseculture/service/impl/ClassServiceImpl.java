package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.*;
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
}