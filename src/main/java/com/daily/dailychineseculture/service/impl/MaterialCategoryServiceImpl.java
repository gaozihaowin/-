package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.common.BusinessException;
import com.daily.dailychineseculture.dto.MaterialCategoryRequestDTO;
import com.daily.dailychineseculture.dto.MaterialCategorySortDTO;
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

@Service
public class MaterialCategoryServiceImpl implements MaterialCategoryService {

    @Autowired
    private MaterialCategoryMapper materialCategoryMapper;

    @Autowired
    private CourseMaterialMapper courseMaterialMapper;

    @Override
    public List<MaterialCategoryTreeVO> getCategoryTree() {
        List<MaterialCategory> allCategories = materialCategoryMapper.selectAll();
        Map<Long, List<MaterialCategory>> parentIdMap = allCategories.stream()
                .collect(Collectors.groupingBy(MaterialCategory::getParentId));
        List<MaterialCategoryTreeVO> rootNodes = allCategories.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .map(this::convertToTreeVO)
                .collect(Collectors.toList());
        for (MaterialCategoryTreeVO node : rootNodes) {
            buildChildren(node, parentIdMap);
        }
        return rootNodes;
    }

    private MaterialCategoryTreeVO convertToTreeVO(MaterialCategory category) {
        MaterialCategoryTreeVO vo = new MaterialCategoryTreeVO();
        vo.setCategoryId(category.getCategoryId());
        vo.setParentId(category.getParentId());
        vo.setName(category.getName());
        vo.setSort(category.getSort());
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    private void buildChildren(MaterialCategoryTreeVO node, Map<Long, List<MaterialCategory>> parentIdMap) {
        List<MaterialCategory> children = parentIdMap.get(node.getCategoryId());
        if (children == null || children.isEmpty()) {
            return;
        }
        List<MaterialCategoryTreeVO> childNodes = children.stream()
                .map(this::convertToTreeVO)
                .collect(Collectors.toList());
        for (MaterialCategoryTreeVO childNode : childNodes) {
            buildChildren(childNode, parentIdMap);
        }
        node.setChildren(childNodes);
    }

    @Override
    @Transactional
    public void addCategory(MaterialCategoryRequestDTO requestDTO) {
        MaterialCategory category = new MaterialCategory();
        Long parentId = requestDTO.getParentId() != null ? requestDTO.getParentId() : 0L;
        category.setParentId(parentId);
        category.setName(requestDTO.getName());
        Integer maxSort = materialCategoryMapper.selectMaxSortByParentId(parentId);
        Integer sort = (maxSort == null) ? 10 : maxSort + 10;
        category.setSort(sort);
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
        List<MaterialCategory> all = materialCategoryMapper.selectAll();
        Map<Long, List<MaterialCategory>> parentIdMap = all.stream()
                .collect(Collectors.groupingBy(MaterialCategory::getParentId));
        List<Long> idsToDelete = new ArrayList<>();
        idsToDelete.add(id);
        collectDescendantIds(id, parentIdMap, idsToDelete);
        Integer materialCount = courseMaterialMapper.countByCategoryIds(idsToDelete);
        if (materialCount != null && materialCount > 0) {
            throw new BusinessException("删除失败：该分类（或其子分类）中仍存放有课件资源。请先将课件删除或转移至其他分类！");
        }
        materialCategoryMapper.deleteByIds(idsToDelete);
    }

    private void collectDescendantIds(Long parentId, Map<Long, List<MaterialCategory>> parentIdMap, List<Long> resultIds) {
        List<MaterialCategory> children = parentIdMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return;
        }
        for (MaterialCategory child : children) {
            resultIds.add(child.getCategoryId());
            collectDescendantIds(child.getCategoryId(), parentIdMap, resultIds);
        }
    }

    @Override
    @Transactional
    public void batchUpdateSort(List<MaterialCategorySortDTO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        materialCategoryMapper.batchUpdateSort(list);
    }
}
