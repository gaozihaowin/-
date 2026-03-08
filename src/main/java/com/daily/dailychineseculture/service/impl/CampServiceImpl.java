package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.CampDTO;
import com.daily.dailychineseculture.dto.CampListPageDTO;
import com.daily.dailychineseculture.dto.CampListItemDTO;
import com.daily.dailychineseculture.dto.CampTypeOptionDTO;
import com.daily.dailychineseculture.dto.CampVO;
import com.daily.dailychineseculture.dto.RecentCampDTO;
import com.daily.dailychineseculture.entity.Camp;
import com.daily.dailychineseculture.mapper.CampMapper;
import com.daily.dailychineseculture.service.CampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    
    @Override
    public List<RecentCampDTO> getRecentCamps() {
        // 查询最近活跃的 5 个营期
        List<Camp> camps = campMapper.selectRecentCamps();
        
        // 转换为 DTO 并处理状态文本
        List<RecentCampDTO> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (Camp camp : camps) {
            RecentCampDTO dto = new RecentCampDTO();
            dto.setCampId(camp.getCampId());
            dto.setCampName(camp.getName());
            dto.setVisitCount(camp.getEnrollCount() != null ? camp.getEnrollCount() : 0);
            dto.setStatusCode(camp.getStatus() != null ? camp.getStatus() : 0);
            dto.setStatusText(getStatusText(camp.getStatus()));
            dto.setStartTime(camp.getStartTime() != null ? sdf.format(camp.getStartTime()) : "");
            // instructor 使用默认值 "致良知教研团队"
            
            result.add(dto);
        }
        
        return result;
    }
    
    @Override
    public CampListPageDTO getCampList(Integer page, Integer size, String keyword, Integer status, Integer typeId) {
        // 设置默认值
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 查询总数
        long total = campMapper.countCampList(keyword, status, typeId);
        
        // 分页查询数据
        List<CampListItemDTO> list = new ArrayList<>();
        if (total > 0) {
            list = campMapper.selectCampList(keyword, status, typeId, offset, size);
        }
        
        // 组装分页结果
        CampListPageDTO pageDTO = new CampListPageDTO();
        pageDTO.setTotal(total);
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setList(list);
        
        return pageDTO;
    }
    
    @Override
    public List<CampTypeOptionDTO> getAllCampTypes() {
        return campMapper.selectAllCampTypes();
    }
    
    @Override
    public void addCamp(CampDTO campDTO) {
        // 创建营期实体
        Camp camp = new Camp();
        camp.setTypeId(campDTO.getTypeId());
        camp.setTerm(campDTO.getTerm());
        camp.setName(campDTO.getName());
        camp.setIntro(campDTO.getIntro());
        camp.setStartTime(campDTO.getStartTime());
        camp.setEndTime(campDTO.getEndTime());
        camp.setStatus(campDTO.getStatus() != null ? campDTO.getStatus() : 0);
        camp.setTag(campDTO.getTag());
        // 强制设置 enroll_count 为 0，不接受前端传值
        camp.setEnrollCount(0);
        
        // 执行插入操作
        campMapper.insertCamp(camp);
    }
    
    @Override
    public void updateCamp(CampDTO campDTO) {
        // 验证 campId 是否存在
        if (campDTO.getCampId() == null) {
            throw new IllegalArgumentException("编辑营期时，campId 不能为空");
        }
        
        // 创建营期实体
        Camp camp = new Camp();
        camp.setCampId(campDTO.getCampId());
        camp.setTypeId(campDTO.getTypeId());
        camp.setTerm(campDTO.getTerm());
        camp.setName(campDTO.getName());
        camp.setIntro(campDTO.getIntro());
        camp.setStartTime(campDTO.getStartTime());
        camp.setEndTime(campDTO.getEndTime());
        camp.setStatus(campDTO.getStatus() != null ? campDTO.getStatus() : 0);
        camp.setTag(campDTO.getTag());
        // 注意：不要更新 enroll_count，保留真实的报名人数
        
        // 执行更新操作
        campMapper.updateCamp(camp);
    }
    
    /**
     * 根据状态码获取状态文本
     * @param status 状态码：0-待开课，1-进行中，2-已结束
     * @return 状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "待开课";
        }
        switch (status) {
            case 0:
                return "待开课";
            case 1:
                return "进行中";
            case 2:
                return "已结束";
            default:
                return "未知状态";
        }
    }
}