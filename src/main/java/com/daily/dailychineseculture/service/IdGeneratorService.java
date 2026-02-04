package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.mapper.UserSeqMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * ID生成服务类
 * 根据年份生成唯一的用户ID
 */
@Service
public class IdGeneratorService {

    @Autowired
    private UserSeqMapper userSeqMapper;

    /**
     * 生成用户ID
     * 格式: YYYY + 6位序号 (如: 2026000001)
     */
    @Transactional
    public synchronized Long generateUserId() {
        int currentYear = LocalDate.now().getYear();
        
        // 查询当前年份的序列号记录
        Integer currentSeq = userSeqMapper.getCurrentSeq(currentYear);
        
        if (currentSeq == null) {
            // 如果该年份记录不存在，创建新记录
            userSeqMapper.insertYearSeq(currentYear, 0);
            currentSeq = 0;
        }
        
        // 序号加1
        int nextSeq = currentSeq + 1;
        
        // 更新序列号
        userSeqMapper.updateCurrentSeq(currentYear, nextSeq);
        
        // 生成用户ID: 年份 + 6位序号
        return Long.valueOf(currentYear + String.format("%06d", nextSeq));
    }
}