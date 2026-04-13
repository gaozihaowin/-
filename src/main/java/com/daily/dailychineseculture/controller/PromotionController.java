package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.PromotionCheckResultDTO;
import com.daily.dailychineseculture.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/promotion")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping("/check")
    public ResponseResult<PromotionCheckResultDTO> checkPromotionEligibility(
            @RequestParam Long userId,
            @RequestParam Integer campId) {
        try {
            PromotionCheckResultDTO result = promotionService.checkPromotionEligibility(userId, campId);
            return ResponseResult.success("查询成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("检查升班资格失败: " + e.getMessage());
        }
    }

    @PostMapping("/promote")
    public ResponseResult<Void> promoteStudent(@RequestBody Map<String, Object> request) {
        try {
            Long userId = ((Number) request.get("userId")).longValue();
            Integer currentCampId = ((Number) request.get("currentCampId")).intValue();
            Integer targetCampId = ((Number) request.get("targetCampId")).intValue();

            boolean success = promotionService.promoteStudent(userId, currentCampId, targetCampId);
            if (success) {
                return ResponseResult.success("升班成功", null);
            } else {
                return ResponseResult.error("升班失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("升班失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch-check")
    public ResponseResult<Integer> batchCheckAndMarkCompletion(@RequestParam Integer campId) {
        try {
            int count = promotionService.batchCheckAndMarkCompletion(campId);
            return ResponseResult.success("批量检查完成，共标记" + count + "名学员完成营期", count);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("批量检查失败: " + e.getMessage());
        }
    }
}
