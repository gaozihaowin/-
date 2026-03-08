package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.CampTypeDTO;

import java.util.List;

/**
 * 营期类型 Service 接口
 */
public interface CampTypeService {
    
    /**
     * 查询所有营期类型（全量列表）
     * @return 营期类型列表
     */
    List<CampTypeDTO> getAllCampTypes();
    
    /**
     * 根据 ID 查询营期类型
     * @param typeId 类型 ID
     * @return 营期类型 DTO
     */
    CampTypeDTO getCampTypeById(Integer typeId);
    
    /**
     * 新增营期类型
     * @param campType 营期类型 DTO
     */
    void createCampType(CampTypeDTO campType);
    
    /**
     * 修改营期类型
     * @param campType 营期类型 DTO
     */
    void updateCampType(CampTypeDTO campType);
    
    /**
     * 删除营期类型
     * @param typeId 类型 ID
     */
    void deleteCampType(Integer typeId);
}
