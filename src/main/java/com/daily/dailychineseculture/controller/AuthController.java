package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.LoginRequest;
import com.daily.dailychineseculture.dto.LoginResult;
import com.daily.dailychineseculture.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录接口
     * @param loginRequest 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody LoginRequest loginRequest) {
        // 参数校验
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            return Result.error("请输入账号");
        }
        
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            return Result.error("请输入密码");
        }

        String username = loginRequest.getUsername().trim();
        String password = loginRequest.getPassword().trim();
        
        // 手机号格式验证
        if (username.matches("^1[3-9]\\d{9}$")) {
            if (password.length() < 6) {
                return Result.error("密码长度不能少于6位");
            }
        }
        
        // 模拟数据库查询逻辑
        // 如果是admin/123则返回成功，否则检查数据库中是否存在该用户
        if ("admin".equals(username) && "123".equals(password)) {
            return Result.success(buildSuccessLoginResult());
        }

        // 检查数据库中是否存在该用户名
        boolean userExists = userService.checkUserExists(username);
        
        if (userExists) {
            // 用户存在，验证密码
            boolean passwordCorrect = userService.verifyPassword(username, password);
            if (passwordCorrect) {
                return Result.success(buildSuccessLoginResult());
            } else {
                return Result.error("用户名或密码错误");
            }
        } else {
            // 用户不存在，创建新用户
            boolean userCreated = userService.createUser(username, password);
            if (userCreated) {
                return Result.build(200, "用户注册成功", buildSuccessLoginResult());
            } else {
                return Result.error("用户创建失败");
            }
        }
    }

    /**
     * 构建成功的登录结果
     * @return LoginResult对象
     */
    private LoginResult buildSuccessLoginResult() {
        LoginResult result = new LoginResult();
        
        // 生成假的JWT token
        result.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." + 
                       UUID.randomUUID().toString().replace("-", "") + 
                       ".fake_signature_for_demo");
        
        // 设置用户信息
        LoginResult.UserInfo userInfo = new LoginResult.UserInfo();
        userInfo.setName("致良知学员");
        userInfo.setAvatar("http://example.com/avatar.jpg");
        result.setUserInfo(userInfo);
        
        return result;
    }
}