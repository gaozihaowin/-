package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.TaskContentDTO;
import com.daily.dailychineseculture.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/content/{taskId}")
    public Result<TaskContentDTO> getTaskContent(
            @PathVariable Integer taskId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        TaskContentDTO content = taskService.getTaskContent(taskId, userId);
        return Result.success(content);
    }
}
