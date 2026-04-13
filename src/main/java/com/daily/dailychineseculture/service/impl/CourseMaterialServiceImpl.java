package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.common.BusinessException;
import com.daily.dailychineseculture.dto.CourseMaterialPageDTO;
import com.daily.dailychineseculture.dto.CourseMaterialPageResultDTO;
import com.daily.dailychineseculture.dto.CourseMaterialRequestDTO;
import com.daily.dailychineseculture.entity.CourseMaterial;
import com.daily.dailychineseculture.entity.MaterialCategory;
import com.daily.dailychineseculture.mapper.CourseMaterialMapper;
import com.daily.dailychineseculture.mapper.MaterialCategoryMapper;
import com.daily.dailychineseculture.mapper.PlanTaskMapper;
import com.daily.dailychineseculture.service.CourseMaterialService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 课件资源服务实现类
 */
@Service
public class CourseMaterialServiceImpl implements CourseMaterialService {

    @Autowired
    private CourseMaterialMapper courseMaterialMapper;

    @Autowired
    private MaterialCategoryMapper materialCategoryMapper;

    @Autowired
    private PlanTaskMapper planTaskMapper;

    @Override
    public CourseMaterialPageResultDTO getMaterialPage(CourseMaterialPageDTO pageDTO) {
        if (pageDTO.getPageNum() == null || pageDTO.getPageNum() < 1) {
            pageDTO.setPageNum(1);
        }
        if (pageDTO.getPageSize() == null || pageDTO.getPageSize() < 1) {
            pageDTO.setPageSize(10);
        }
        List<Long> categoryIds = null;
        if (pageDTO.getCategoryId() != null) {
            List<MaterialCategory> all = materialCategoryMapper.selectAll();
            Map<Long, List<Long>> parentIdMap = all.stream()
                    .collect(Collectors.groupingBy(
                            MaterialCategory::getParentId,
                            Collectors.mapping(MaterialCategory::getCategoryId, Collectors.toList())
                    ));
            List<Long> resultIds = new ArrayList<>();
            resultIds.add(pageDTO.getCategoryId());
            Set<Long> visitedIds = new HashSet<>();
            visitedIds.add(pageDTO.getCategoryId());
            findChildrenIds(pageDTO.getCategoryId(), parentIdMap, visitedIds, resultIds);
            categoryIds = resultIds;
        }
        PageHelper.startPage(pageDTO.getPageNum(), pageDTO.getPageSize());
        List<CourseMaterial> list = courseMaterialMapper.selectPage(
                categoryIds,
                pageDTO.getType(),
                pageDTO.getKeyword()
        );
        PageInfo<CourseMaterial> pageInfo = new PageInfo<>(list);
        CourseMaterialPageResultDTO result = new CourseMaterialPageResultDTO();
        result.setTotal(pageInfo.getTotal());
        result.setList(pageInfo.getList());
        return result;
    }

    private void findChildrenIds(Long parentId, Map<Long, List<Long>> parentIdMap,
                                  Set<Long> visitedIds, List<Long> resultIds) {
        List<Long> children = parentIdMap.get(parentId);
        if (children == null) {
            return;
        }
        for (Long childId : children) {
            if (visitedIds.contains(childId)) {
                continue;
            }
            visitedIds.add(childId);
            resultIds.add(childId);
            findChildrenIds(childId, parentIdMap, visitedIds, resultIds);
        }
    }

    @Override
    @Transactional
    public void addMaterial(CourseMaterialRequestDTO requestDTO) {
        CourseMaterial material = new CourseMaterial();
        material.setCategoryId(requestDTO.getCategoryId());
        material.setName(requestDTO.getName());
        material.setType(requestDTO.getType());
        material.setUrl(requestDTO.getUrl());
        material.setSize(requestDTO.getSize() != null ? requestDTO.getSize() : 0L);
        material.setDuration(requestDTO.getDuration() != null ? requestDTO.getDuration() : 0);
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            Long userId = (Long) request.getAttribute("userId");
            material.setCreateBy(userId != null ? userId : 0L);
        } catch (Exception e) {
            material.setCreateBy(0L);
        }
        courseMaterialMapper.insert(material);
    }

    @Override
    @Transactional
    public void deleteMaterial(Long id) {
        if (id == null) {
            throw new BusinessException("课件ID不能为空");
        }
        Integer count = planTaskMapper.countByMaterialId(id);
        if (count != null && count > 0) {
            throw new BusinessException("该素材已被排课任务引用，禁止删除！");
        }
        courseMaterialMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateMaterial(CourseMaterialRequestDTO requestDTO) {
        if (requestDTO.getMaterialId() == null) {
            throw new BusinessException("课件ID不能为空");
        }
        CourseMaterial existingMaterial = courseMaterialMapper.selectById(requestDTO.getMaterialId());
        if (existingMaterial == null) {
            throw new BusinessException("课件不存在");
        }
        String oldUrl = existingMaterial.getUrl();
        CourseMaterial material = new CourseMaterial();
        material.setMaterialId(requestDTO.getMaterialId());
        if (requestDTO.getCategoryId() != null) {
            material.setCategoryId(requestDTO.getCategoryId());
        }
        if (requestDTO.getName() != null) {
            material.setName(requestDTO.getName());
        }
        if (requestDTO.getType() != null) {
            material.setType(requestDTO.getType());
        }
        if (requestDTO.getUrl() != null) {
            material.setUrl(requestDTO.getUrl());
        }
        if (requestDTO.getSize() != null) {
            material.setSize(requestDTO.getSize());
        }
        if (requestDTO.getDuration() != null) {
            material.setDuration(requestDTO.getDuration());
        }
        courseMaterialMapper.update(material);
        if (requestDTO.getUrl() != null && !requestDTO.getUrl().equals(oldUrl)) {
            planTaskMapper.updateTaskUrlByMaterialId(requestDTO.getMaterialId(), requestDTO.getUrl());
        }
    }
}
