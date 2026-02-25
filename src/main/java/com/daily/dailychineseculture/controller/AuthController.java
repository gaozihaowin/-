package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.LoginRequest;
import com.daily.dailychineseculture.dto.LoginResult;
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
        
        // 检查数据库中是否存在该用户名
        boolean userExists = userService.checkUserExists(username);
        
        if (userExists) {
            // 用户存在，验证密码
            boolean passwordCorrect = userService.verifyPassword(username, password);
            if (passwordCorrect) {
                User user = userService.getUserByUsername(username);
                return Result.success(buildSuccessLoginResult(user));
            } else {
                return Result.error("用户名或密码错误");
            }
        } else {
            // 用户不存在，自动注册
            User newUser = userService.createUserWithReturn(username, password);
            if (newUser != null) {
                return Result.build(201, "用户注册成功", buildSuccessLoginResult(newUser));
            } else {
                return Result.error("用户注册失败");
            }
        }
    }

    /**
     * 构建成功的登录结果
     * @param user 用户对象
     * @return LoginResult对象
     */
    private LoginResult buildSuccessLoginResult(User user) {
        LoginResult result = new LoginResult();
        
        // 生成真实的JWT token
        String token = jwtUtils.generateToken(user.getUserId(), user.getAccount());
        result.setToken(token);
        
        // 设置用户信息
        LoginResult.UserInfo userInfo = new LoginResult.UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setName(user.getAccount());
        userInfo.setAvatar(user.getAvatar() != null ? user.getAvatar() : "http://example.com/avatar.jpg");
        result.setUserInfo(userInfo);
        
        return result;
    }
}