package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.common.BusinessException;
import com.daily.dailychineseculture.dto.TaskContentDTO;
import com.daily.dailychineseculture.mapper.TaskMapper;
import com.daily.dailychineseculture.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public TaskContentDTO getTaskContent(Integer taskId, Long userId) {
        if (taskId == null) {
            throw new BusinessException("任务ID不能为空");
        }
        TaskContentDTO content = taskMapper.selectTaskContent(taskId, userId);
        if (content == null) {
            throw new BusinessException("任务不存在或已下架");
        }
        return content;
    }
}
