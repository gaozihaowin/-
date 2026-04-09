package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.MaterialCategoryRequestDTO;
import com.daily.dailychineseculture.dto.MaterialCategoryTreeVO;

import java.util.List;

/**
 * 课件分类服务接口
 */
public interface MaterialCategoryService {

    /**
     * 获取分类树
     * @return 分类树列表
     */
    List<MaterialCategoryTreeVO> getCategoryTree();

    /**
     * 新增分类
     * @param requestDTO 请求参数
     */
    void addCategory(MaterialCategoryRequestDTO requestDTO);

    /**
     * 修改分类
     * @param requestDTO 请求参数
     */
    void updateCategory(MaterialCategoryRequestDTO requestDTO);

    /**
     * 删除分类
     * @param id 分类ID
     */
    void deleteCategory(Long id);
}
