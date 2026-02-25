package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.CampVO;
import com.daily.dailychineseculture.entity.Camp;
import com.daily.dailychineseculture.mapper.CampMapper;
import com.daily.dailychineseculture.service.CampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 营期服务实现类
 */
@Service
public class CampServiceImpl implements CampService {
    
    @Autowired
    private CampMapper campMapper;
    
    @Override
    public List<Camp> getHotCamps() {
        // 查询最新的5个营期，按开营时间倒序排列
        return campMapper.selectHotCamps();
    }
    
    @Override
    public List<CampVO> getHotCourses() {
        // 查询热门课程推荐，联表查询并格式化数据
        return campMapper.selectHotCourses();
    }
    
    @Override
    public Camp getCampById(Integer campId) {
        return campMapper.selectById(campId);
    }
    
    @Override
    public List<Camp> getAllCamps() {
        return campMapper.selectAll();
    }
}