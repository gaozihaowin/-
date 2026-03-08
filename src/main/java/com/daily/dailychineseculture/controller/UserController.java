package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.SwitchIdentityRequest;
import com.daily.dailychineseculture.dto.UserCurrentInfoDTO;
import com.daily.dailychineseculture.dto.UserIdentityDTO;
import com.daily.dailychineseculture.dto.UserUpdateRequest;
import com.daily.dailychineseculture.entity.User;
import com.daily.dailychineseculture.service.IdGeneratorService;
import com.daily.dailychineseculture.service.UserAuthService;
import com.daily.dailychineseculture.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private IdGeneratorService idGeneratorService;
    
    @Autowired
    private UserAuthService userAuthService;

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

    /**
     * 更新用户个人信息（完善信息）
     * POST /user/update
     * 
     * @param request 用户信息更新请求
     * @param httpRequest HTTP 请求对象（用于获取登录用户 ID）
     * @return 统一响应结果
     */
    @PostMapping("/update")
    public ResponseResult<Void> updateUserInfo(@RequestBody UserUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            // 从请求属性中获取用户 ID（由认证拦截器设置）
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                System.err.println("未找到用户 ID，请确保已登录");
                return ResponseResult.error(401, "未登录或登录已过期");
            }

            System.out.println("收到用户信息更新请求，userId: " + userId);
            System.out.println("更新内容：" + request);

            // 调用 Service 层更新用户信息
            boolean success = userService.updateUserInfo(userId, request);

            if (success) {
                return ResponseResult.success("信息保存成功", null);
            } else {
                return ResponseResult.error(500, "信息更新失败");
            }
        } catch (RuntimeException e) {
            // 处理手机号重复等已知异常
            String errorMsg = e.getMessage();
            System.err.println("更新用户信息异常：" + errorMsg);

            // 判断是否是手机号重复错误
            if (errorMsg.contains("手机号已被其他账号绑定")) {
                return ResponseResult.error(400, errorMsg);
            }

            // 其他运行时异常
            return ResponseResult.error(400, errorMsg);
        } catch (Exception e) {
            // 处理未知异常
            System.err.println("=== 更新用户信息未知异常 ===");
            e.printStackTrace();
            System.err.println("===========================");
            return ResponseResult.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户的状态信息
     * GET /api/admin/user/me
     * 
     * @param httpRequest HTTP 请求对象（用于获取登录用户 ID）
     * @return 统一响应结果，包含用户当前信息
     */
    @GetMapping("/me")
    public ResponseResult<UserCurrentInfoDTO> getCurrentUserInfo(HttpServletRequest httpRequest) {
        try {
            // 从请求属性中获取用户 ID（由认证拦截器设置）
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                System.err.println("未找到用户 ID，请确保已登录");
                return ResponseResult.error(401, "未登录或登录已过期");
            }
            
            UserCurrentInfoDTO userInfo = userAuthService.getCurrentUserInfo(userId);
            return ResponseResult.success("操作成功", userInfo);
        } catch (Exception e) {
            System.err.println("获取当前用户信息异常：" + e.getMessage());
            e.printStackTrace();
            return ResponseResult.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户可切换的身份列表
     * GET /api/admin/user/identities
     * 
     * @param httpRequest HTTP 请求对象（用于获取登录用户 ID）
     * @return 统一响应结果，包含身份信息列表
     */
    @GetMapping("/identities")
    public ResponseResult<List<UserIdentityDTO>> getUserIdentities(HttpServletRequest httpRequest) {
        try {
            // 从请求属性中获取用户 ID（由认证拦截器设置）
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                System.err.println("未找到用户 ID，请确保已登录");
                return ResponseResult.error(401, "未登录或登录已过期");
            }
            
            List<UserIdentityDTO> identities = userAuthService.getUserIdentities(userId);
            return ResponseResult.success("操作成功", identities);
        } catch (Exception e) {
            System.err.println("获取用户身份列表异常：" + e.getMessage());
            e.printStackTrace();
            return ResponseResult.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
    
    /**
     * PC 管理端 - 执行身份切换
     * POST /api/admin/user/switch-identity
     * 
     * @param request 切换身份请求
     * @param httpRequest HTTP 请求对象（用于获取登录用户 ID）
     * @return 统一响应结果，包含新的 JWT Token
     */
    @PostMapping("/switch-identity")
    public ResponseResult<Map<String, Object>> switchIdentity(
            @RequestBody SwitchIdentityRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 从请求属性中获取用户 ID（由认证拦截器设置）
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                System.err.println("未找到用户 ID，请确保已登录");
                return ResponseResult.error(401, "未登录或登录已过期");
            }
            
            System.out.println("PC 管理端收到身份切换请求，userId: " + userId + ", request: " + request);
            
            // 执行身份切换，生成新的 JWT Token（使用 ADMIN 类型）
            String newToken = userAuthService.executeIdentitySwitch(userId, request, "ADMIN");
            
            // 构造返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("token", newToken);
            
            // TODO: 可以在这里补充完整的 userInfo
            // result.put("userInfo", userAuthService.getCurrentUserInfo(userId));
            
            return ResponseResult.success("切换成功", result);
        } catch (IllegalArgumentException e) {
            System.err.println("身份切换参数校验失败：" + e.getMessage());
            return ResponseResult.error(400, e.getMessage());
        } catch (Exception e) {
            System.err.println("身份切换异常：" + e.getMessage());
            e.printStackTrace();
            return ResponseResult.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
}