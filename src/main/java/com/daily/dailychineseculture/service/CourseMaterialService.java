package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.CourseMaterialPageDTO;
import com.daily.dailychineseculture.dto.CourseMaterialPageResultDTO;
import com.daily.dailychineseculture.dto.CourseMaterialRequestDTO;

/**
 * 课件资源服务接口
 */
public interface CourseMaterialService {

    /**
     * 分页查询课件
     * @param pageDTO 分页参数
     * @return 分页结果
     */
    CourseMaterialPageResultDTO getMaterialPage(CourseMaterialPageDTO pageDTO);

    /**
     * 新增课件
     * @param requestDTO 请求参数
     */
    void addMaterial(CourseMaterialRequestDTO requestDTO);

    /**
     * 删除课件
     * @param id 课件ID
     */
    void deleteMaterial(Long id);

    /**
     * 修改课件（包含URL同步逻辑）
     * @param requestDTO 请求参数
     */
    void updateMaterial(CourseMaterialRequestDTO requestDTO);
}
