package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.entity.User;
import com.daily.dailychineseculture.service.IdGeneratorService;
import com.daily.dailychineseculture.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private IdGeneratorService idGeneratorService;

    /**
     * 获取所有用户
     */
    @GetMapping
    public ResponseResult<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseResult.success("获取用户列表成功", users);
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{userId}")
    public ResponseResult<User> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            return ResponseResult.success("获取用户成功", user);
        } else {
            return ResponseResult.error(404, "用户不存在");
        }
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ResponseResult<User> createUser(@RequestBody User user) {
        // 生成用户ID
        Long userId = idGeneratorService.generateUserId();
        user.setUserId(userId);
        
        User createdUser = userService.createUser(user);
        return ResponseResult.success("用户创建成功", createdUser);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{userId}")
    public ResponseResult<User> updateUser(@PathVariable Long userId, @RequestBody User user) {
        user.setUserId(userId);
        User updatedUser = userService.updateUser(user);
        if (updatedUser != null) {
            return ResponseResult.success("用户更新成功", updatedUser);
        } else {
            return ResponseResult.error(404, "用户不存在");
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public ResponseResult<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseResult.success("用户删除成功", null);
    }
}