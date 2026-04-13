package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.MaterialCategoryRequestDTO;
import com.daily.dailychineseculture.dto.MaterialCategorySortDTO;
import com.daily.dailychineseculture.dto.MaterialCategoryTreeVO;

import java.util.List;

/**
 * 课件分类服务接口
 */
public interface MaterialCategoryService {

    List<MaterialCategoryTreeVO> getCategoryTree();

    void addCategory(MaterialCategoryRequestDTO requestDTO);

    void updateCategory(MaterialCategoryRequestDTO requestDTO);

    void deleteCategory(Long id);

    void batchUpdateSort(List<MaterialCategorySortDTO> list);
}
