package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.mapper.VolunteerManageMapper;
import com.daily.dailychineseculture.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 用户搜索控制器
 */
@RestController
@RequestMapping("/user")
public class UserSearchController {

    @Autowired
    private VolunteerManageMapper volunteerManageMapper;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 搜索用户（用于分配岗位）
     */
    @GetMapping("/search")
    public ResponseResult<List<Map<String, Object>>> searchUsers(@RequestHeader("Authorization") String token,
                                                                 @RequestParam("keyword") String keyword) {
        try {
            Long currentUserId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));

            if (keyword == null || keyword.trim().length() < 2) {
                return ResponseResult.error("搜索关键词至少需要2个字符");
            }

            List<Map<String, Object>> users = volunteerManageMapper.searchUsers(keyword.trim(), currentUserId);
            return ResponseResult.success(users);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("搜索用户失败：" + e.getMessage());
        }
    }
}