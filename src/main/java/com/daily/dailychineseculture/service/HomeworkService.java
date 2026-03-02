package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.*;
import java.util.Map;


/**
 * 作业服务接口
 */
public interface HomeworkService {

    /**
     * 获取作业列表（支持搜索）
     */
    HomeworkListDTO getHomeworkList(Long userId, String type, Integer id, String status, String date, String searchKeyword);

    /**
     * 标记优秀作业
     */
    boolean markExcellentHomework(Integer homeworkId, Boolean isExcellent);

    /**
     * 获取作业详情
     */
    HomeworkDetailDTO getHomeworkDetail(Integer homeworkId);

    /**
     * 获取作业统计数据
     */
    Map<String, Object> getHomeworkStatistics(Long userId, String type, Integer id, String date, String searchKeyword);

    /**
     * 获取作业状态名单（已交/未交/迟交）
     */
    Map<String, Object> getHomeworkStatusList(Long userId, String type, Integer id, String date, String searchKeyword);
}

