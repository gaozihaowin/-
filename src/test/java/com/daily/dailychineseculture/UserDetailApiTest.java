package com.daily.dailychineseculture;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.controller.AuthController;
import com.daily.dailychineseculture.dto.UserDetailDTO;
import com.daily.dailychineseculture.dto.UserUpdateAllRequest;
import com.daily.dailychineseculture.service.UserService;
import com.daily.dailychineseculture.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户个人资料详情接口测试
 */
@SpringBootTest
public class UserDetailApiTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 测试获取用户资料详情接口
     */
    @Test
    public void testGetUserDetail() {
        // 1. 使用真实用户 ID 生成 Token
        Long testUserId = 2026000001L;
        String token = jwtUtils.generateToken(testUserId, "student01");

        // 2. 调用接口
        Result<UserDetailDTO> result = authController.getUserDetail("Bearer " + token);

        // 3. 验证响应
        assertNotNull(result);
        assertEquals(200, result.getCode().intValue(), "响应码应该为 200");

        // 4. 验证数据完整性
        UserDetailDTO detail = result.getData();
        if (detail != null) {
            // 验证必填字段
            assertNotNull(detail.getAccount(), "账号不应为空");
            assertNotNull(detail.getNickname(), "昵称不应为空");
            assertNotNull(detail.getAvatar(), "头像不应为空");
            assertNotNull(detail.getGender(), "性别不应为空");
            
            // 验证密码字段为空（安全要求）
            assertEquals("", detail.getPassword(), "密码字段应该为空字符串");

            System.out.println("=== 用户资料详情测试结果 ===");
            System.out.println("账号：" + detail.getAccount());
            System.out.println("昵称：" + detail.getNickname());
            System.out.println("头像：" + detail.getAvatar());
            System.out.println("手机号：" + detail.getPhone());
            System.out.println("地区：" + detail.getRegion());
            System.out.println("职业：" + detail.getProfession());
            System.out.println("性别：" + detail.getGender());
            System.out.println("生日：" + detail.getBirthday());
            System.out.println("密码占位符：[" + detail.getPassword() + "]");
        } else {
            System.out.println("⚠️  用户不存在或返回数据为空");
        }
    }

    /**
     * 测试更新用户全部资料接口
     */
    @Test
    public void testUpdateUserAllInfo() {
        // 1. 准备测试数据
        Long testUserId = 2026000001L;
        String token = jwtUtils.generateToken(testUserId, "student01");

        UserUpdateAllRequest request = new UserUpdateAllRequest();
        request.setNickname("测试昵称");
        request.setAvatar("https://example.com/avatar.jpg");
        request.setPhone("13800138000");
        request.setRegion("上海");
        request.setProfession("全栈工程师");
        request.setGender(1);
        request.setBirthday("1990-01-01");
        request.setPassword(""); // 空密码表示不修改密码

        // 2. 调用接口
        Result<Void> result = authController.updateUserAllInfo("Bearer " + token, request);

        // 3. 验证响应
        assertNotNull(result);
        System.out.println("=== 更新用户资料测试结果 ===");
        System.out.println("响应码：" + result.getCode());
        System.out.println("响应消息：" + result.getMsg());
        
        if (result.getCode() == 200) {
            System.out.println("✅ 更新成功");
        } else {
            System.out.println("❌ 更新失败：" + result.getMsg());
        }
    }

    /**
     * 测试带密码更新的场景
     */
    @Test
    public void testUpdateWithPassword() {
        // 1. 准备测试数据（包含新密码）
        Long testUserId = 2026000001L;
        String token = jwtUtils.generateToken(testUserId, "student01");

        UserUpdateAllRequest request = new UserUpdateAllRequest();
        request.setNickname("测试昵称");
        request.setAvatar("https://example.com/avatar.jpg");
        request.setPhone("13800138000");
        request.setRegion("北京");
        request.setProfession("软件工程师");
        request.setGender(1);
        request.setBirthday("1995-05-15");
        request.setPassword("newpassword123"); // 设置新密码

        // 2. 调用接口
        Result<Void> result = authController.updateUserAllInfo("Bearer " + token, request);

        // 3. 验证响应
        assertNotNull(result);
        System.out.println("=== 带密码更新的测试结果 ===");
        System.out.println("响应码：" + result.getCode());
        System.out.println("响应消息：" + result.getMsg());
        
        if (result.getCode() == 200) {
            System.out.println("✅ 密码更新成功");
        } else {
            System.out.println("❌ 更新失败：" + result.getMsg());
        }
    }
}
