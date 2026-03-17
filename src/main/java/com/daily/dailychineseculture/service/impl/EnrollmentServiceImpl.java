package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.mapper.EnrollmentMapper;
import com.daily.dailychineseculture.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 学员报名服务实现类
 */
@Service
public class EnrollmentServiceImpl implements EnrollmentService {
    
    @Autowired
    private EnrollmentMapper enrollmentMapper;
    
    @Override
    public Boolean checkEnrollment(Long userId, Integer campId) {
        // 参数校验
        if (userId == null || campId == null) {
            throw new IllegalArgumentException("用户 ID 和营期 ID 不能为空");
        }
        
        // 查询报名记录数
        Integer count = enrollmentMapper.checkEnrollment(userId, campId);
        
        // 返回检查结果
        return count != null && count > 0;
    }
}
