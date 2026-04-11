package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.HomeworkDetailDTO;
import com.daily.dailychineseculture.dto.HomeworkListDTO;
import com.daily.dailychineseculture.dto.HomeworkHierarchyDTO;
import com.daily.dailychineseculture.dto.HomeworkStatisticsHierarchyDTO;
import com.daily.dailychineseculture.dto.HomeworkSubmitDTO;
import com.daily.dailychineseculture.dto.MyHomeworkPageDTO;
import com.daily.dailychineseculture.dto.ExcellentShowcasePageDTO;
import java.util.Map;

/**
 * 作业服务接口
 */
public interface HomeworkService {

    /**
     * 获取作业列表
     */
    HomeworkListDTO getHomeworkList(Long userId, String type, Integer id, String status, String date);

    /**
     * 标记小组优秀作业
     */
    boolean markSmallGroupExcellent(Integer homeworkId, Integer isSmallGroupExcellent);

    /**
     * 标记大组优秀作业
     */
    boolean markBigGroupExcellent(Integer homeworkId, Integer isBigGroupExcellent);

    /**
     * 检查作业是否为小组优秀
     */
    boolean checkSmallGroupExcellent(Integer homeworkId);

    /**
     * 获取作业详情
     */
    HomeworkDetailDTO getHomeworkDetail(Integer homeworkId);

    /**
     * 获取作业统计数据
     */
    Map<String, Object> getHomeworkStatistics(Long userId, String type, Integer id, String date);

    /**
     * 获取作业状态名单（已交/未交/迟交）
     */
    Map<String, Object> getHomeworkStatusList(Long userId, String type, Integer id, String date);

    /**
     * 获取作业层级列表（大组-小组-成员）
     */
    HomeworkHierarchyDTO getHomeworkHierarchyList(Long userId, String date, String status,String dutyType,Integer targetId);

    /**
     * 获取作业统计层级数据
     */
    HomeworkStatisticsHierarchyDTO getHomeworkStatisticsHierarchy(Long userId, String type, Integer id, String date);

    MyHomeworkPageDTO getMyHomeworkPage(Long userId, Integer page, Integer size);

    ExcellentShowcasePageDTO getExcellentShowcasePage(Integer page, Integer size);

    void submitHomework(Long userId, HomeworkSubmitDTO dto);
}