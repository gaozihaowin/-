package com.daily.dailychineseculture.listener;

import com.daily.dailychineseculture.event.CampProgressUpdateEvent;
import com.daily.dailychineseculture.mapper.CampEnrollmentMapper;
import com.daily.dailychineseculture.mapper.CampPlanMapper;
import com.daily.dailychineseculture.mapper.UserDailyRecordMapper;
import com.daily.dailychineseculture.entity.UserDailyRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CampProgressUpdateListener {
    @Autowired
    private CampEnrollmentMapper campEnrollmentMapper;
    @Autowired
    private CampPlanMapper campPlanMapper;
    @Autowired
    private UserDailyRecordMapper userDailyRecordMapper;

    @Async
    @EventListener
    public void handleProgressUpdate(CampProgressUpdateEvent event) {
        Long userId = event.getUserId();
        Integer campId = event.getCampId();

        Integer totalDays = campPlanMapper.countTotalDaysByCampId(campId);
        if (totalDays == null || totalDays == 0) return;

        List<UserDailyRecord> allRecords = userDailyRecordMapper.selectByUserIdAndCampId(userId, campId);

        int completedDays = 0;
        if (allRecords != null && !allRecords.isEmpty()) {
            for (UserDailyRecord record : allRecords) {
                if (record.getIsAllCompleted() != null && record.getIsAllCompleted() == 1) {
                    completedDays++;
                }
            }
        }

        int overallProgress = (int) Math.floor(((double) completedDays / totalDays) * 100);
        overallProgress = Math.min(overallProgress, 100);

        campEnrollmentMapper.updateProgress(userId, campId, overallProgress);
    }
}