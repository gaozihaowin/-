package com.daily.dailychineseculture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表盘快捷入口 DTO
 * 用于返回根据角色动态生成的快捷操作网格
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortcutDTO {
    
    /**
     * 快捷入口 ID（唯一标识）
     */
    private String id;
    
    /**
     * 显示标题
     */
    private String title;
    
    /**
     * 图标类名
     */
    private String icon;
    
    /**
     * 前端路由路径
     */
    private String route;
    
    /**
     * 背景颜色
     */
    private String bgColor;
    
    /**
     * 排序序号（数字越小越靠前）
     */
    private Integer sortOrder;
}
