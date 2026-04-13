package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.TaskContentDTO;

public interface TaskService {

    TaskContentDTO getTaskContent(Integer taskId, Long userId);
}
