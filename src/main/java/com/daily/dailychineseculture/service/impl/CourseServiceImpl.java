package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.CampPlanDTO;
import com.daily.dailychineseculture.dto.CampScheduleDTO;
import com.daily.dailychineseculture.dto.MyCourseVO;
import com.daily.dailychineseculture.dto.PlanItemDTO;
import com.daily.dailychineseculture.entity.CampPlan;
import com.daily.dailychineseculture.mapper.CampPlanMapper;
import com.daily.dailychineseculture.mapper.MyCourseMapper;
import com.daily.dailychineseculture.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程服务实现类
 * 实现课程相关业务逻辑
 * 
 * @author Java后端架构师
 * @since 2026-02-25
 */
@Service
public class CourseServiceImpl implements CourseService {
    
    @Autowired
   private MyCourseMapper myCourseMapper;
    
    @Autowired
   private CampPlanMapper campPlanMapper;
    
    @Override
   public List<MyCourseVO> getMyCourses(Long userId, Integer tabType) {
        // 参数校验
        if (userId == null || tabType == null) {
            throw new IllegalArgumentException("用户ID和标签类型不能为空");
        }
        
        if (tabType < 1 || tabType > 3) {
            throw new IllegalArgumentException("标签类型必须为1、2或3");
        }
        
        // 查询我的课程列表
        return myCourseMapper.selectMyCourses(userId, tabType);
    }
    
    @Override
   public List<CampScheduleDTO> getCourseSchedule(Integer campId) {
        // 1. 查询营期的所有课程计划（已按 day_index 升序）
        List<CampPlan> allPlans = campPlanMapper.selectCourseScheduleByCampId(campId);
        
        if (allPlans == null || allPlans.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 按 moduleIndex 分组
        Map<Integer, List<CampPlan>> groupedByModule = allPlans.stream()
                .collect(Collectors.groupingBy(CampPlan::getModuleIndex));
        
        // 3. 组装 DTO 列表
        List<CampScheduleDTO> scheduleList = new ArrayList<>();
        for (Map.Entry<Integer, List<CampPlan>> entry : groupedByModule.entrySet()) {
            Integer moduleIndex = entry.getKey();
            List<CampPlan> plans = entry.getValue();
            
            // 获取模块名称并拼接中文周次
            String moduleName = plans.get(0).getModuleName();
            String fullModuleName = buildWeekName(moduleIndex, moduleName);
            
            // 转换为 PlanItemDTO 列表
            List<PlanItemDTO> planItems = plans.stream()
                    .map(plan -> new PlanItemDTO(
                            plan.getPlanId(),
                            plan.getDayIndex(),
                            plan.getTitle(),
                            plan.getReadingTitle(),
                            plan.getTeacherName(),
                            plan.getVideoDuration()
                    ))
                    .collect(Collectors.toList());
            
            scheduleList.add(new CampScheduleDTO(moduleIndex, fullModuleName, planItems));
        }
        
        // 4. 按 moduleIndex 升序排序
        scheduleList.sort((a, b) -> a.getModuleIndex().compareTo(b.getModuleIndex()));
        
        return scheduleList;
    }
    
    /**
     * 将阿拉伯数字转为中文周次名称
     * 例如：1 -> "一", 2 -> "二", 3 -> "三"
     * 最终返回："第一周：基础认知"
     */
  private String buildWeekName(Integer moduleIndex, String moduleName) {
        String[] chineseNumbers = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        
        String chineseNumber;
        if (moduleIndex >= 1 && moduleIndex <= 10) {
            chineseNumber = chineseNumbers[moduleIndex];
        } else {
            // 如果超过 10，直接显示数字
            chineseNumber = moduleIndex.toString();
        }
        
        return "第" + chineseNumber + "周：" + moduleName;
    }
}