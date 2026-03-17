package com.daily.dailychineseculture.service;

/**
 * 学员报名服务接口
 */
public interface EnrollmentService {
    
    /**
     * 检查用户是否已报名指定营期
     * 
     * @param userId 用户 ID
     * @param campId 营期 ID
     * @return true-已报名，false-未报名
     */
    Boolean checkEnrollment(Long userId, Integer campId);
}
