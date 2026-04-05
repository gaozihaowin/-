package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.dto.AssetCheckDTO;
import com.daily.dailychineseculture.dto.HandoverReq;
import com.daily.dailychineseculture.service.DutyService;
import com.daily.dailychineseculture.common.ResponseResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/duty")
public class DutyController {

    @Resource
    private DutyService dutyService;

    @GetMapping("/assets/{userId}")
    public ResponseResult<AssetCheckDTO> checkAssets(@PathVariable Long userId) {
        if (userId == null) {
            return ResponseResult.error(400, "userId 不能为空");
        }
        AssetCheckDTO result = dutyService.checkAssets(userId);
        return ResponseResult.success("查询成功", result);
    }

    @PostMapping("/handover")
    public ResponseResult<String> handover(@RequestBody HandoverReq req) {
        if (req == null || req.getOldUserId() == null || req.getNewUserId() == null) {
            return ResponseResult.error(400, "oldUserId 和 newUserId 不能为空");
        }
        if (req.getOldUserId().equals(req.getNewUserId())) {
            return ResponseResult.error(400, "交接人和接收人不能是同一人");
        }
        dutyService.executeHandover(req.getOldUserId(), req.getNewUserId());
        return ResponseResult.success("交接成功");
    }
}