package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.LoginRequest;
import com.daily.dailychineseculture.dto.LoginResult;
import com.daily.dailychineseculture.dto.UserInfoDTO;
import com.daily.dailychineseculture.entity.User;
import com.daily.dailychineseculture.service.UserService;
import com.daily.dailychineseculture.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户登录接口
     * 支持账号密码登录，包含信息完整性检查
     * @param loginRequest 登录请求参数 {"username": "student01", "password": "123456"}
     * @return 登录结果 {
     *   "code": 200,
     *   "msg": "登录成功",
     *   "data": {
     *     "token": "eyJhbGci...",
     *     "isComplete": false,
     *     "userInfo": {
     *       "userid": "2026000001",
     *       "username": "student01",
     *       "avatar": "",
     *       "phone": "13800138000",
     *       "gender": 0,
     *       "birthday": ""
     *     }
     *   }
     * }
     */
    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 步骤 1：参数校验
            if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                return Result.error("请输入账号");
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return Result.error("请输入密码");
            }

            String username = loginRequest.getUsername().trim();
            String password = loginRequest.getPassword().trim();
            
            // 步骤 2：用户信息查询与比对
            boolean userExists = userService.checkUserExists(username);
            
            User user;
            if (userExists) {
                // 用户存在，验证密码
                boolean passwordCorrect = userService.verifyPassword(username, password);
                if (!passwordCorrect) {
                    return Result.build(401, "账号或密码错误", null);
                }
                user = userService.getUserByUsername(username);
            } else {
                // 用户不存在，自动注册
                System.out.println("检测到新用户，开始自动注册流程: " + username);
                user = userService.createUserWithReturn(username, password);
                if (user == null) {
                    System.err.println("自动注册失败，返回注册失败响应");
                    return Result.build(500, "用户注册失败，请查看服务器日志获取详细错误信息", null);
                }
                System.out.println("自动注册成功，生成登录结果");
                // 新注册用户信息必然不完整
                return Result.build(201, "注册并登录成功", buildLoginResult(user, false));
            }
            
            // 步骤 3：判断信息完整度（核心关键）
            boolean isComplete = userService.isUserInfoComplete(user);
            
            // 步骤 4：生成 Token 并返回
            return Result.success(buildLoginResult(user, isComplete));
            
        } catch (Exception e) {
            // 异常捕获和处理
            return Result.error("登录过程中发生错误: " + e.getMessage());
        }
    }

    /**
     * 构建登录结果
     * @param user 用户对象
     * @param isComplete 信息是否完整
     * @return LoginResult对象
     */
    private LoginResult buildLoginResult(User user, boolean isComplete) {
        LoginResult result = new LoginResult();
        
        // 生成真实的JWT token
        String token = jwtUtils.generateToken(user.getUserId(), user.getAccount());
        result.setToken(token);
        
        // 设置信息完整状态
        result.setIsComplete(isComplete);
        
        // 转换并设置用户信息
        UserInfoDTO userInfoDTO = userService.convertToUserInfoDTO(user);
        result.setUserInfo(userInfoDTO);
        
        return result;
    }
}