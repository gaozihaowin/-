package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.*;
import com.daily.dailychineseculture.entity.User;
import com.daily.dailychineseculture.service.UserAuthService;
import com.daily.dailychineseculture.service.UserService;
import com.daily.dailychineseculture.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;

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

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserAuthService authService;

    @Value("${wx.appid:}")
    private String wxAppid;

    @Value("${wx.secret:}")
    private String wxSecret;

    /**
     * 用户登录接口
     * 支持账号密码登录，包含信息完整性检查
     *
     * @param loginRequest 登录请求参数 {"username": "student01", "password": "123456"}
     * @return 登录结果 {
     *         "code": 200,
     *         "msg": "登录成功",
     *         "data": {
     *         "token": "eyJhbGci...",
     *         "isComplete": false,
     *         "userInfo": {
     *         "userid": "2026000001",
     *         "username": "student01",
     *         "avatar": "",
     *         "phone": "13800138000",
     *         "gender": 0,
     *         "birthday": ""
     *         }
     *         }
     *         }
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
     *
     * @param user       用户对象
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

    /**
     * 微信一键登录接口
     */
    @PostMapping("/wxLogin")
    public Result<Map<String, Object>> wxLogin(@RequestBody WxLoginRequest wxLoginRequest) {
        try {
            String code = wxLoginRequest.getCode();
            String nickname = wxLoginRequest.getNickname();
            String avatar = wxLoginRequest.getAvatar();

            if (code == null || code.trim().isEmpty()) {
                return Result.error("缺少微信授权码");
            }
            if (nickname == null || nickname.trim().isEmpty() || avatar == null || avatar.trim().isEmpty()) {
                return Result.error("请完善微信头像和昵称");
            }

            // 调用微信API获取openid
            String openid = getWechatOpenid(code);
            if (openid == null) {
                return Result.error("微信授权失败");
            }

            // 查询或创建用户
            User user = userService.findOrCreateWxUser(openid, nickname, avatar);
            if (user == null) {
                return Result.error("用户创建失败");
            }

            if (user.getStatus() != 1) {
                return Result.error("账号已冻结");
            }

            // 生成token
            String token = jwtUtils.generateToken(user.getUserId(), user.getAccount());

            // 构造用户信息
            UserInfoDTO userInfo = new UserInfoDTO();
            userInfo.setUserid(user.getUserId().toString());
            userInfo.setUsername(user.getNickname() != null ? user.getNickname() : user.getAccount());
            userInfo.setAvatar(user.getAvatar() != null ? user.getAvatar() : "https://img.icons8.com/color/96/person-male.png");
            userInfo.setPhone(user.getPhone() != null ? user.getPhone() : "");

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userInfo", userInfo);

            return Result.build(200, "微信登录成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误，请稍后重试");
        }
    }

    /**
     * 获取用户信息接口
     * 返回用户基本信息及统计指标（地区、职业、年数、学时）
     * 
     * @param token JWT 令牌
     * @return 用户个人信息 {
     *         "code": 200,
     *         "msg": "success",
     *         "data": {
     *         "userId": "2026000001",
     *         "account": "student01",
     *         "nickname": "微信昵称或默认昵称",
     *         "avatar": "https://...",
     *         "currentIdentity": "学员端",
     *         "statsList": [
     *         { "label": "地区", "value": "北京" },
     *         { "label": "职业", "value": "IT 工程师" },
     *         { "label": "年数", "value": "0" },
     *         { "label": "学时", "value": "12h" }
     *         ]
     *         }
     *         }
     */
    @GetMapping("/user/info")
    public Result<com.daily.dailychineseculture.dto.UserProfileDTO> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            // 1. 解析 Token 获取用户 ID
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error("无效的 Token");
            }
    
            // 2. 调用 Service 获取用户个人信息
            com.daily.dailychineseculture.dto.UserProfileDTO userProfile = userService.getUserProfile(userId);
            if (userProfile == null) {
                return Result.error("用户不存在");
            }
    
            // 3. 返回成功响应
            return Result.success(userProfile);
        } catch (Exception e) {
            System.err.println("=== 获取用户信息异常详情 ===");
            System.err.println("异常类型：" + e.getClass().getSimpleName());
            System.err.println("异常信息：" + e.getMessage());
            e.printStackTrace();
            return Result.error("服务器内部错误，请稍后重试");
        }
    }

    /**
     * 获取用户个人资料详情接口（包含所有字段）
     * 用于个人资料编辑页面展示完整信息
     * 
     * @param token JWT 令牌
     * @return 用户资料详情 {
     *         "code": 200,
     *         "msg": "success",
     *         "data": {
     *         "account": "student01",
     *         "nickname": "Mystery",
     *         "avatar": "https://...",
     *         "phone": "13800000000",
     *         "region": "北京",
     *         "profession": "IT 工程师",
     *         "gender": 1,
     *         "birthday": "1990-01-01",
     *         "password": ""
     *         }
     *         }
     */
    @GetMapping("/user/detail")
    public Result<com.daily.dailychineseculture.dto.UserDetailDTO> getUserDetail(@RequestHeader("Authorization") String token) {
        try {
            // 1. 解析 Token 获取用户 ID
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error("无效的 Token");
            }

            // 2. 调用 Service 获取用户资料详情
            com.daily.dailychineseculture.dto.UserDetailDTO userDetail = userService.getUserDetail(userId);
            if (userDetail == null) {
                return Result.error("用户不存在");
            }

            // 3. 返回成功响应
            return Result.success(userDetail);
        } catch (Exception e) {
            System.err.println("=== 获取用户资料详情异常 ===");
            System.err.println("异常类型：" + e.getClass().getSimpleName());
            System.err.println("异常信息：" + e.getMessage());
            e.printStackTrace();
            return Result.error("服务器内部错误，请稍后重试");
        }
    }

    /**
     * 更新用户全部资料接口
     * 接收前端全量字段并更新到数据库
     * 
     * @param token JWT 令牌
     * @param request 更新请求 {
     *         "avatar": "https://...",
     *         "nickname": "Mystery",
     *         "password": "newpassword123",
     *         "phone": "13800000000",
     *         "region": "上海",
     *         "profession": "全栈开发",
     *         "gender": 1,
     *         "birthday": "1990-01-01"
     *         }
     * @return 保存结果
     */
    @PostMapping("/user/updateAll")
    public Result<Void> updateUserAllInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody com.daily.dailychineseculture.dto.UserUpdateAllRequest request) {
        try {
            // 1. 解析 Token 获取用户 ID
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error("无效的 Token");
            }

            // 2. 参数校验
            if (request == null) {
                return Result.error("请求参数不能为空");
            }

            // 3. 调用 Service 更新用户资料
            boolean success = userService.updateUserAllInfo(userId, request);
            
            if (success) {
                return Result.successMsg("保存成功");
            } else {
                return Result.error("保存失败");
            }
        } catch (IllegalArgumentException e) {
            // 参数校验失败
            return Result.error(e.getMessage());
        } catch (Exception e) {
            System.err.println("=== 更新用户全部资料异常 ===");
            System.err.println("异常类型：" + e.getClass().getSimpleName());
            System.err.println("异常信息：" + e.getMessage());
            e.printStackTrace();
            return Result.error("服务器内部错误，请稍后重试");
        }
    }

    /**
     * 退出登录接口（通用）
     * 注意：此接口适用于所有客户端，如需区分客户端类型，请使用专用的退出接口
     */
    @PostMapping("/user/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            System.out.println("用户 " + userId + " 退出登录");
            return Result.successMsg("退出登录成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误，请稍后重试");
        }
    }

    /**
     * 小程序端 - 退出登录接口（C 端专属）
     * POST /api/app/user/logout
     * 
     * 业务逻辑：
     * 1. 销毁 Token（JWT 无需服务端操作，前端删除本地存储即可）
     * 2. 容错处理：即使 Token 已过期、不存在或解析失败，也返回成功
     * 3. 确保前端能顺利执行本地清理逻辑
     *
     * @param token JWT 令牌（可选，允许为空或已过期）
     * @return 统一响应结果 {
     *         "code": 200,
     *         "msg": "退出登录成功",
     *         "data": null
     *         }
     */
    @PostMapping("/app/user/logout")
    public Result<Void> appLogout(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 尝试解析 Token（仅用于日志记录，不做强校验）
            if (token != null && !token.trim().isEmpty()) {
                String cleanToken = token.replace("Bearer ", "");
                try {
                    Long userId = jwtUtils.getUserIdFromToken(cleanToken);
                    System.out.println("小程序端用户 " + userId + " 退出登录");
                } catch (Exception e) {
                    // Token 已过期或解析失败，不做处理，继续返回成功
                    System.out.println("小程序端用户退出登录（Token 已失效）");
                }
            } else {
                System.out.println("小程序端用户退出登录（未携带 Token）");
            }
            
            // 无论 Token 状态如何，都返回成功
            // JWT 机制下，服务端无需执行额外操作，前端删除本地存储的 Token 即可
            return Result.successMsg("退出登录成功");
            
        } catch (Exception e) {
            // 捕获所有异常，确保不抛出 500 错误
            System.err.println("=== 小程序端退出登录异常 ===");
            System.err.println("异常类型：" + e.getClass().getSimpleName());
            System.err.println("异常信息：" + e.getMessage());
            e.printStackTrace();
            
            // 即使发生异常，也返回成功，确保前端能执行本地清理
            return Result.successMsg("退出登录成功");
        }
    }

    /**
     * 小程序端 - 用户切换身份接口
     * 支持学员、志愿者等身份切换（基于 appointmentId）
     *
     * @param token JWT 令牌
     * @param request 切换请求 {"assignmentId": 1}
     * @return 新的 Token {"token": "eyJhbGci..."}
     */
    @PostMapping("/app/user/switch-identity")
    public Result<Map<String, Object>> appSwitchIdentity(
            @RequestHeader("Authorization") String token,
            @RequestBody SwitchIdentityRequest request) {
        try {
            // 1. 解析 Token 获取用户 ID
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error("无效的 Token");
            }

            System.out.println("小程序端收到身份切换请求，userId: " + userId + ", request: " + request);

            // 2. 调用 Service 执行身份切换（使用 APP 类型）
            String newToken = authService.executeIdentitySwitch(userId, request, "APP");

            // 3. 构造返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("token", newToken);

            return Result.success(result);
        } catch (IllegalArgumentException e) {
            System.err.println("身份切换参数校验失败：" + e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            System.err.println("身份切换异常：" + e.getMessage());
            e.printStackTrace();
            return Result.error("服务器内部错误，请稍后重试");
        }
    }

    /**
     * 更新用户信息接口
     */
    @PostMapping("/updateUserInfo")
    public Result<Void> updateUserInfo(@RequestHeader("Authorization") String token,
                                       @RequestBody Map<String, String> request) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            String nickname = request.get("nickname");
            String avatar = request.get("avatar");

            if ((nickname == null || nickname.trim().isEmpty()) &&
                    (avatar == null || avatar.trim().isEmpty())) {
                return Result.error("请传入要修改的信息");
            }

            boolean success = userService.updateUserInfo(userId, nickname, avatar);
            if (success) {
                return Result.successMsg("信息修改成功");
            } else {
                return Result.error("信息修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误，请稍后重试");
        }
    }

    /**
     * 获取微信openid
     */
    private String getWechatOpenid(String code) {
        try {
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    wxAppid, wxSecret, code);

            System.out.println("🔗 调用微信API: " + url);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();

            System.out.println("📨 微信API响应: " + responseBody);

            if (responseBody == null) {
                System.err.println("❌ 微信API返回空响应");
                return null;
            }

            String openid = extractOpenidFromResponse(responseBody);
            if (openid == null) {
                System.err.println("❌ 无法从响应中提取openid");
                return null;
            }

            System.out.println("✅ 获取到openid: " + openid);
            return openid;

        } catch (Exception e) {
            System.err.println("❌ 获取微信openid异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从微信响应中提取openid
     */
    private String extractOpenidFromResponse(String responseBody) {
        try {
            if (responseBody.contains("\"openid\"")) {
                int startIndex = responseBody.indexOf("\"openid\":\"") + 10;
                int endIndex = responseBody.indexOf("\"", startIndex);
                if (startIndex > 9 && endIndex > startIndex) {
                    return responseBody.substring(startIndex, endIndex);
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("❌ 解析openid失败: " + e.getMessage());
            return null;
        }
    }
}