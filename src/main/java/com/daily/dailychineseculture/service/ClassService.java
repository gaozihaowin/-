package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.common.Result; // 新增：导入Result类（autoAssign方法的返回类型）
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

    /**
     * 新增：自动分班接口（地域邻近优先）
     * @param campId 营期ID
     * @param perClassNum 每班人数上限
     * @return 分班结果（成功/失败+数据）
     */
    Result autoAssign(Integer campId, Integer perClassNum);
}