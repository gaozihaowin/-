package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.dto.CampTypeDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CampTypeMapper {

    List<CampTypeDTO> selectAllCampTypes();

    CampTypeDTO selectCampTypeById(@Param("typeId") Integer typeId);

    int insertCampType(CampTypeDTO campType);

    int updateCampType(CampTypeDTO campType);

    int deleteCampType(@Param("typeId") Integer typeId);

    CampTypeDTO selectNextPromotionType(@Param("level") String level);
}
