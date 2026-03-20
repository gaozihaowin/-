package com.daily.dailychineseculture.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserTaskRecordMapper {
    int upsertDoneRecord(@Param("userId") Long userId, @Param("planId") Integer planId, @Param("taskId") Integer taskId);
}
