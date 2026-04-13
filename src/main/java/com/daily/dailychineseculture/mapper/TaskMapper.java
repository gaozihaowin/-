package com.daily.dailychineseculture.mapper;

import com.daily.dailychineseculture.dto.TaskContentDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TaskMapper {

    TaskContentDTO selectTaskContent(@Param("taskId") Integer taskId, @Param("userId") Long userId);
}
