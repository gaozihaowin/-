package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.CampOptionDTO;
import com.daily.dailychineseculture.dto.CampPlanDTO;
import com.daily.dailychineseculture.dto.GenerateCalendarRequest;
import com.daily.dailychineseculture.entity.Camp;
import com.daily.dailychineseculture.entity.CampPlan;
import com.daily.dailychineseculture.mapper.CampMapper;
import com.daily.dailychineseculture.mapper.CampPlanMapper;
import com.daily.dailychineseculture.service.CampPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 排课计划 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class CampPlanServiceImpl implements CampPlanService {
    
    private final CampPlanMapper campPlanMapper;
    private final CampMapper campMapper;
    
    @Override
    public List<CampOptionDTO> getCampOptions() {
        return campMapper.selectCampOptions();
    }
    
    @Override
    public List<CampPlanDTO> getCampPlansByCampId(Integer campId) {
        return campPlanMapper.selectCampPlansByCampId(campId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateCalendar(GenerateCalendarRequest request) {
        Integer campId = request.getCampId();
        
        // 1. 校验：查询是否已存在排课计划
        int count = campPlanMapper.countCampPlansByCampId(campId);
        if (count > 0) {
            throw new RuntimeException("该营期已存在课表，请勿重复生成");
        }
        
        // 2. 查询营期信息
        Camp camp = campMapper.selectById(campId);
        if (camp == null) {
            throw new RuntimeException("未找到指定的营期");
        }
        
        // 3. 计算日期范围
        LocalDate startDate = convertToLocalDate(camp.getStartTime());
        LocalDate endDate = convertToLocalDate(camp.getEndTime());
        
        // 计算总天数（含起止日）
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (totalDays <= 0) {
            throw new RuntimeException("开营时间必须早于或等于结营时间");
        }
        
        // 4. 循环生成排课计划
        List<CampPlan> campPlans = new ArrayList<>();
        for (int i = 0; i < totalDays; i++) {
            CampPlan plan = new CampPlan();
            plan.setCampId(campId);
            plan.setDayIndex(i + 1); // day_index 从 1 开始递增
            plan.setPlanDate(Date.from(startDate.plusDays(i).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            plan.setTitle("");
            
            campPlans.add(plan);
        }
        
        // 5. 批量插入
        campPlanMapper.batchInsertCampPlans(campPlans);
    }
    
    @Override
    public void saveOrUpdateCampPlan(CampPlanDTO campPlan) {
        campPlanMapper.updateCampPlan(campPlan);
    }
    
    /**
     * 将 Date 转换为 LocalDate
     */
    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
