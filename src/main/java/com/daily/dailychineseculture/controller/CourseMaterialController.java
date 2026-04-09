package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.CourseMaterialPageDTO;
import com.daily.dailychineseculture.dto.CourseMaterialPageResultDTO;
import com.daily.dailychineseculture.dto.CourseMaterialRequestDTO;
import com.daily.dailychineseculture.service.CourseMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课件资源管理 Controller
 */
@RestController
@RequestMapping("/api/admin/materials")
public class CourseMaterialController {

    @Autowired
    private CourseMaterialService courseMaterialService;

    /**
     * 分页查询课件
     * GET /api/admin/materials/page
     */
    @GetMapping("/page")
    public ResponseResult<CourseMaterialPageResultDTO> getMaterialPage(CourseMaterialPageDTO pageDTO) {
        CourseMaterialPageResultDTO result = courseMaterialService.getMaterialPage(pageDTO);
        return ResponseResult.success(result);
    }

    /**
     * 新增课件
     * POST /api/admin/materials
     */
    @PostMapping
    public ResponseResult<String> addMaterial(@RequestBody CourseMaterialRequestDTO requestDTO) {
        courseMaterialService.addMaterial(requestDTO);
        return ResponseResult.success("新增课件成功");
    }

    /**
     * 删除课件
     * DELETE /api/admin/materials/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseResult<String> deleteMaterial(@PathVariable("id") Long id) {
        courseMaterialService.deleteMaterial(id);
        return ResponseResult.success("删除课件成功");
    }

    /**
     * 修改课件
     * PUT /api/admin/materials
     */
    @PutMapping
    public ResponseResult<String> updateMaterial(@RequestBody CourseMaterialRequestDTO requestDTO) {
        courseMaterialService.updateMaterial(requestDTO);
        return ResponseResult.success("修改课件成功");
    }
}
