package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.PromotionCheckResultDTO;

public interface PromotionService {

    PromotionCheckResultDTO checkPromotionEligibility(Long userId, Integer campId);

    boolean promoteStudent(Long userId, Integer currentCampId, Integer targetCampId);

    int batchCheckAndMarkCompletion(Integer campId);

    int batchPromoteWithClassPreservation(Integer currentCampId, Integer targetCampId);
}
