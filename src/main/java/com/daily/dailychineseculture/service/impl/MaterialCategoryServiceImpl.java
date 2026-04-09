package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.common.BusinessException;
import com.daily.dailychineseculture.dto.MaterialCategoryRequestDTO;
import com.daily.dailychineseculture.dto.MaterialCategoryTreeVO;
import com.daily.dailychineseculture.entity.MaterialCategory;
import com.daily.dailychineseculture.mapper.CourseMaterialMapper;
import com.daily.dailychineseculture.mapper.MaterialCategoryMapper;
import com.daily.dailychineseculture.service.MaterialCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课件分类服务实现类
 */
@Service
public class MaterialCategoryServiceImpl implements MaterialCategoryService {

    @Autowired
    private MaterialCategoryMapper materialCategoryMapper;

    @Autowired
    private CourseMaterialMapper courseMaterialMapper;

    @Override
    public List<MaterialCategoryTreeVO> getCategoryTree() {
        List<MaterialCategory> allCategories = materialCategoryMapper.selectAll();
        return buildTree(allCategories);
    }

    private List<MaterialCategoryTreeVO> buildTree(List<MaterialCategory> categories) {
        Map<Long, List<MaterialCategory>> parentIdMap = categories.stream()
                .collect(Collectors.groupingBy(MaterialCategory::getParentId));

        List<MaterialCategoryTreeVO> rootNodes = new ArrayList<>();
        for (MaterialCategory category : categories) {
            MaterialCategoryTreeVO vo = new MaterialCategoryTreeVO();
            vo.setCategoryId(category.getCategoryId());
            vo.setParentId(category.getParentId());
            vo.setName(category.getName());
            vo.setSort(category.getSort());
            vo.setChildren(new ArrayList<>());
            rootNodes.add(vo);
        }

        for (MaterialCategoryTreeVO node : rootNodes) {
            List<MaterialCategory> children = parentIdMap.get(node.getCategoryId());
            if (children != null && !children.isEmpty()) {
                List<MaterialCategoryTreeVO> childNodes = children.stream()
                        .map(category -> {
                            MaterialCategoryTreeVO childVO = new MaterialCategoryTreeVO();
                            childVO.setCategoryId(category.getCategoryId());
                            childVO.setParentId(category.getParentId());
                            childVO.setName(category.getName());
                            childVO.setSort(category.getSort());
                            childVO.setChildren(new ArrayList<>());
                            return childVO;
                        })
                        .collect(Collectors.toList());
                node.setChildren(childNodes);
            }
        }

        return rootNodes.stream()
                .filter(node -> node.getParentId() == 0)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addCategory(MaterialCategoryRequestDTO requestDTO) {
        MaterialCategory category = new MaterialCategory();
        category.setParentId(requestDTO.getParentId() != null ? requestDTO.getParentId() : 0L);
        category.setName(requestDTO.getName());
        category.setSort(requestDTO.getSort() != null ? requestDTO.getSort() : 0);
        materialCategoryMapper.insert(category);
    }

    @Override
    @Transactional
    public void updateCategory(MaterialCategoryRequestDTO requestDTO) {
        if (requestDTO.getCategoryId() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        MaterialCategory category = new MaterialCategory();
        category.setCategoryId(requestDTO.getCategoryId());
        if (requestDTO.getParentId() != null) {
            category.setParentId(requestDTO.getParentId());
        }
        if (requestDTO.getName() != null) {
            category.setName(requestDTO.getName());
        }
        if (requestDTO.getSort() != null) {
            category.setSort(requestDTO.getSort());
        }
        materialCategoryMapper.update(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (id == null) {
            throw new BusinessException("分类ID不能为空");
        }
        Integer childCount = materialCategoryMapper.countByParentId(id);
        if (childCount != null && childCount > 0) {
            throw new BusinessException("存在子分类，无法删除");
        }
        Integer materialCount = courseMaterialMapper.countByCategoryId(id);
        if (materialCount != null && materialCount > 0) {
            throw new BusinessException("分类下存在课件，无法删除");
        }
        materialCategoryMapper.deleteById(id);
    }
}
