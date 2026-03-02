package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.ClassDTO;
import com.daily.dailychineseculture.dto.BigGroupDTO;
import com.daily.dailychineseculture.dto.SmallGroupDTO;
import java.util.List;

/**
 * 班级、大组、小组服务接口
 */
public interface ClassService {

    /**
     * 获取所有班级列表
     */
    List<ClassDTO> getAllClasses();

    /**
     * 获取所有大组列表
     */
    List<BigGroupDTO> getAllBigGroups();

    /**
     * 获取所有小组列表
     */
    List<SmallGroupDTO> getAllSmallGroups();
}