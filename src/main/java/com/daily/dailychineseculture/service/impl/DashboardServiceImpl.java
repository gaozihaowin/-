package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.ShortcutDTO;
import com.daily.dailychineseculture.service.DashboardService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 仪表盘服务实现类
 * 当前阶段使用硬编码方式返回固定列表
 */
@Service
public class DashboardServiceImpl implements DashboardService {
    
    @Override
    public List<ShortcutDTO> getShortcuts() {
        // 硬编码返回 COURSE_ADMIN 角色的快捷入口列表
        // TODO: 未来可从数据库或配置中动态加载
        return List.of(
            ShortcutDTO.builder()
                .id("shortcut_plan")
                .title("教务排课工作台")
                .icon("icon-calendar")
                .route("/camp-plan")
                .bgColor("#fdfbf7")
                .sortOrder(1)
                .build(),
            
            ShortcutDTO.builder()
                .id("shortcut_type")
                .title("课程体系大纲")
                .icon("icon-books")
                .route("/camp-type")
                .bgColor("#fdfbf7")
                .sortOrder(2)
                .build(),
            
            ShortcutDTO.builder()
                .id("shortcut_material")
                .title("课件资源中台")
                .icon("icon-video-library")
                .route("/materials")
                .bgColor("#fdfbf7")
                .sortOrder(3)
                .build(),
            
            ShortcutDTO.builder()
                .id("shortcut_analytics")
                .title("营期学情雷达")
                .icon("icon-radar-chart")
                .route("/analytics")
                .bgColor("#fdfbf7")
                .sortOrder(4)
                .build()
        );
    }
}
