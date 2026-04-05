package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.AssetCheckDTO;

public interface DutyService {
    AssetCheckDTO checkAssets(Long userId);
    void executeHandover(Long oldUserId, Long newUserId);
}