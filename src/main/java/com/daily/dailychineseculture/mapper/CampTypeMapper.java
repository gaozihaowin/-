package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.dto.CampTypeDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 营期类型 Mapper 接口
 */
@Mapper
public interface CampTypeMapper {
    
    /**
     * 查询所有营期类型（全量列表）
     * @return 营期类型列表
     */
    List<CampTypeDTO> selectAllCampTypes();
    
    /**
     * 根据 ID 查询营期类型
     * @param typeId 类型 ID
     * @return 营期类型 DTO
     */
    CampTypeDTO selectCampTypeById(@Param("typeId") Integer typeId);
    
    /**
     * 新增营期类型
     * @param campType 营期类型 DTO
     * @return 影响行数
     */
    int insertCampType(CampTypeDTO campType);
    
    /**
     * 修改营期类型
     * @param campType 营期类型 DTO
     * @return 影响行数
     */
    int updateCampType(CampTypeDTO campType);
    
    /**
     * 删除营期类型
     * @param typeId 类型 ID
     * @return 影响行数
     */
    int deleteCampType(@Param("typeId") Integer typeId);
}
