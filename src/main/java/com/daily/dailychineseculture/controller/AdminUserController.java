package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.mapper.UserMapper;
import com.daily.dailychineseculture.vo.UserSimpleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;

    @GetMapping("/search")
    public ResponseResult<List<UserSimpleVO>> searchUsers(
            @RequestParam(value = "keyword", required = false) String keyword) {
        List<UserSimpleVO> result = userMapper.searchUsersForAdmin(keyword);
        return ResponseResult.success(result);
    }
}
