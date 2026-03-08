package com.daily.dailychineseculture.dto;

/**
 * 用户当前信息 DTO（使用 Java 21 Record）
 * 用于返回当前登录用户的状态信息
 */
public record UserCurrentInfoDTO(
    /**
     * 用户 ID
     */
    Long userId,
    
    /**
     * 昵称（若为空则取 account）
     */
    String nickname,
    
    /**
     * 头像 URL
     */
    String avatar,
    
    /**
     * 当前职责类型
     */
    String currentDuty,
    
    /**
     * 当前职责名称（根据字典转换）
     */
    String currentDutyName,
    
    /**
     * 未读通知数量（暂 mock 为 3）
     */
    Integer unreadNoticeCount
) {
    // Java 21 Record 紧凑构造函数，可用于参数校验或转换
    public UserCurrentInfoDTO {
        // 确保 nickname 不为空
        if (nickname == null || nickname.isBlank()) {
            nickname = "未命名用户";
        }
        
        // 确保 avatar 不为空
        if (avatar == null || avatar.isBlank()) {
            avatar = "";
        }
        
        // 确保 currentDutyName 不为空
        if (currentDutyName == null || currentDutyName.isBlank()) {
            currentDutyName = "课程管理员";
        }
        
        // 确保 unreadNoticeCount 不为负
        if (unreadNoticeCount == null) {
            unreadNoticeCount = 0;
        }
    }
}
