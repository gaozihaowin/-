package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.CampTypeDTO;
import com.daily.dailychineseculture.service.CampTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程体系分类管理 Controller
 * 提供 RESTful CRUD 接口
 */
@RestController
@RequestMapping("/api/admin/camp-types")
@RequiredArgsConstructor
public class CampTypeController {
    
    private final CampTypeService campTypeService;
    
    /**
     * 查询所有营期类型（全量列表）
     * GET /api/admin/camp-types
     * 
     * @return 统一响应结果，包含营期类型列表
     */
    @GetMapping
    public ResponseResult<List<CampTypeDTO>> getAllCampTypes() {
        List<CampTypeDTO> campTypes = campTypeService.getAllCampTypes();
        return ResponseResult.success("查询成功", campTypes);
    }
    
    /**
     * 新增营期类型
     * POST /api/admin/camp-types
     * 
     * @param campType 营期类型 DTO（包含 level 和 levelName）
     * @return 统一响应结果
     */
    @PostMapping
    public ResponseResult<String> createCampType(@RequestBody CampTypeDTO campType) {
        campTypeService.createCampType(campType);
        return ResponseResult.success("新增成功");
    }
    
    /**
     * 修改营期类型
     * PUT /api/admin/camp-types
     * 
     * @param campType 营期类型 DTO（包含 typeId、level 和 levelName）
     * @return 统一响应结果
     */
    @PutMapping
    public ResponseResult<String> updateCampType(@RequestBody CampTypeDTO campType) {
        campTypeService.updateCampType(campType);
        return ResponseResult.success("修改成功");
    }
    
    /**
     * 删除营期类型
     * DELETE /api/admin/camp-types/{typeId}
     * 
     * @param typeId 类型 ID
     * @return 统一响应结果
     */
    @DeleteMapping("/{typeId}")
    public ResponseResult<String> deleteCampType(@PathVariable Integer typeId) {
        campTypeService.deleteCampType(typeId);
        return ResponseResult.success("删除成功");
    }
}
