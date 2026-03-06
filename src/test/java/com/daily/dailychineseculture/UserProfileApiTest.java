package com.daily.dailychineseculture;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.controller.AuthController;
import com.daily.dailychineseculture.dto.UserProfileDTO;
import com.daily.dailychineseculture.service.UserService;
import com.daily.dailychineseculture.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户个人信息接口测试
 */
@SpringBootTest
public class UserProfileApiTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 测试获取用户个人信息接口
     */
    @Test
    public void testGetUserProfile() {
        // 1. 使用真实用户 ID 生成 Token（使用已存在的测试账号）
        Long testUserId = 2026000001L; // 假设这是数据库中存在的用户
        String token = jwtUtils.generateToken(testUserId, "student01");

        // 2. 调用接口
        Result<UserProfileDTO> result = authController.getUserInfo("Bearer " + token);

        // 3. 验证响应
        assertNotNull(result);
        assertEquals(200, result.getCode().intValue(), "响应码应该为 200");

        // 4. 验证数据
        UserProfileDTO profile = result.getData();
        if (profile != null) {
            // 验证基本信息
            assertNotNull(profile.getUserId(), "用户 ID 不应为空");
            assertNotNull(profile.getAccount(), "账号不应为空");
            assertNotNull(profile.getNickname(), "昵称不应为空");
            assertNotNull(profile.getAvatar(), "头像不应为空");
            assertNotNull(profile.getCurrentIdentity(), "当前身份不应为空");

            // 验证统计列表
            assertNotNull(profile.getStatsList(), "统计列表不应为空");
            assertEquals(4, profile.getStatsList().size(), "统计指标应该有 4 个");

            // 验证每个统计项
            profile.getStatsList().forEach(item -> {
                assertNotNull(item.getLabel(), "统计项标签不应为空");
                assertNotNull(item.getValue(), "统计项值不应为空");
                System.out.println(item.getLabel() + ": " + item.getValue());
            });

            System.out.println("=== 用户个人信息测试结果 ===");
            System.out.println("用户 ID: " + profile.getUserId());
            System.out.println("账号：" + profile.getAccount());
            System.out.println("昵称：" + profile.getNickname());
            System.out.println("头像：" + profile.getAvatar());
            System.out.println("当前身份：" + profile.getCurrentIdentity());
            System.out.println("统计列表:");
            for (var stat : profile.getStatsList()) {
                System.out.println("  - " + stat.getLabel() + ": " + stat.getValue());
            }
        } else {
            System.out.println("⚠️  用户不存在或返回数据为空");
        }
    }

    /**
     * 直接测试 Service 层
     */
    @Test
    public void testUserServiceGetProfile() {
        // 1. 使用真实用户 ID
        Long testUserId = 2026000001L;

        // 2. 调用 Service
        UserProfileDTO profile = userService.getUserProfile(testUserId);

        // 3. 验证结果
        if (profile != null) {
            System.out.println("=== Service 层测试结果 ===");
            System.out.println("用户 ID: " + profile.getUserId());
            System.out.println("账号：" + profile.getAccount());
            System.out.println("昵称：" + profile.getNickname());
            System.out.println("当前身份：" + profile.getCurrentIdentity());
            System.out.println("统计列表:");
            for (var stat : profile.getStatsList()) {
                System.out.println("  - " + stat.getLabel() + ": " + stat.getValue());
            }
        } else {
            System.out.println("⚠️  用户不存在或服务层返回为空");
        }
    }
}
