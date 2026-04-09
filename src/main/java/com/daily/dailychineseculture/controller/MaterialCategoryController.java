package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.MaterialCategoryRequestDTO;
import com.daily.dailychineseculture.dto.MaterialCategoryTreeVO;
import com.daily.dailychineseculture.service.MaterialCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课件分类管理 Controller
 */
@RestController
@RequestMapping("/api/admin/material-categories")
public class MaterialCategoryController {

    @Autowired
    private MaterialCategoryService materialCategoryService;

    /**
     * 获取分类树
     * GET /api/admin/material-categories/tree
     */
    @GetMapping("/tree")
    public ResponseResult<List<MaterialCategoryTreeVO>> getCategoryTree() {
        List<MaterialCategoryTreeVO> tree = materialCategoryService.getCategoryTree();
        return ResponseResult.success(tree);
    }

    /**
     * 新增分类
     * POST /api/admin/material-categories
     */
    @PostMapping
    public ResponseResult<String> addCategory(@RequestBody MaterialCategoryRequestDTO requestDTO) {
        materialCategoryService.addCategory(requestDTO);
        return ResponseResult.success("新增分类成功");
    }

    /**
     * 修改分类
     * PUT /api/admin/material-categories
     */
    @PutMapping
    public ResponseResult<String> updateCategory(@RequestBody MaterialCategoryRequestDTO requestDTO) {
        materialCategoryService.updateCategory(requestDTO);
        return ResponseResult.success("修改分类成功");
    }

    /**
     * 删除分类
     * DELETE /api/admin/material-categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseResult<String> deleteCategory(@PathVariable("id") Long id) {
        materialCategoryService.deleteCategory(id);
        return ResponseResult.success("删除分类成功");
    }
}
