package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.ShortcutDTO;

import java.util.List;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {
    
    /**
     * 获取仪表盘快捷入口列表
     * 根据当前登录用户角色动态返回
     * 
     * @return 快捷入口列表
     */
    List<ShortcutDTO> getShortcuts();
}
