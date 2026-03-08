package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.CampTypeDTO;
import com.daily.dailychineseculture.mapper.CampTypeMapper;
import com.daily.dailychineseculture.service.CampTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 营期类型 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class CampTypeServiceImpl implements CampTypeService {
    
    private final CampTypeMapper campTypeMapper;
    
    @Override
    public List<CampTypeDTO> getAllCampTypes() {
        return campTypeMapper.selectAllCampTypes();
    }
    
    @Override
    public CampTypeDTO getCampTypeById(Integer typeId) {
        return campTypeMapper.selectCampTypeById(typeId);
    }
    
    @Override
    public void createCampType(CampTypeDTO campType) {
        campTypeMapper.insertCampType(campType);
    }
    
    @Override
    public void updateCampType(CampTypeDTO campType) {
        campTypeMapper.updateCampType(campType);
    }
    
    @Override
    public void deleteCampType(Integer typeId) {
        campTypeMapper.deleteCampType(typeId);
    }
}
