package com.daily.dailychineseculture;

import com.daily.dailychineseculture.dto.LoginRequest;
import com.daily.dailychineseculture.dto.LoginResult;
import com.daily.dailychineseculture.dto.UserInfoDTO;
import com.daily.dailychineseculture.entity.User;
import com.daily.dailychineseculture.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 登录接口信息完整性检查测试
 */
@SpringBootTest
public class LoginWithCompleteCheckTest {

    @Autowired
    private UserService userService;

    @Test
    public void testUserInfoCompleteCheck() {
        // 测试信息完整的用户
        User completeUser = new User();
        completeUser.setUserId(2026000001L);
        completeUser.setAccount("completeuser");
        completeUser.setPassword("123456");
        completeUser.setPhone("13800138000");
        completeUser.setAvatar("http://example.com/avatar.jpg");
        completeUser.setGender(1); // 男性
        completeUser.setBirthday(new Date());
        
        assertTrue(userService.isUserInfoComplete(completeUser), "信息完整的用户应该返回true");
        
        // 测试信息不完整的用户 - 缺少手机号
        User incompleteUser1 = new User();
        incompleteUser1.setUserId(2026000002L);
        incompleteUser1.setAccount("incompleteuser1");
        incompleteUser1.setPassword("123456");
        incompleteUser1.setPhone(""); // 空手机号
        incompleteUser1.setAvatar("http://example.com/avatar.jpg");
        incompleteUser1.setGender(1);
        incompleteUser1.setBirthday(new Date());
        
        assertFalse(userService.isUserInfoComplete(incompleteUser1), "缺少手机号的用户应该返回false");
        
        // 测试信息不完整的用户 - 缺少头像
        User incompleteUser2 = new User();
        incompleteUser2.setUserId(2026000003L);
        incompleteUser2.setAccount("incompleteuser2");
        incompleteUser2.setPassword("123456");
        incompleteUser2.setPhone("13800138000");
        incompleteUser2.setAvatar(""); // 空头像
        incompleteUser2.setGender(1);
        incompleteUser2.setBirthday(new Date());
        
        assertFalse(userService.isUserInfoComplete(incompleteUser2), "缺少头像的用户应该返回false");
        
        // 测试信息不完整的用户 - 性别为0
        User incompleteUser3 = new User();
        incompleteUser3.setUserId(2026000004L);
        incompleteUser3.setAccount("incompleteuser3");
        incompleteUser3.setPassword("123456");
        incompleteUser3.setPhone("13800138000");
        incompleteUser3.setAvatar("http://example.com/avatar.jpg");
        incompleteUser3.setGender(0); // 性别未知
        incompleteUser3.setBirthday(new Date());
        
        assertFalse(userService.isUserInfoComplete(incompleteUser3), "性别为0的用户应该返回false");
        
        // 测试信息不完整的用户 - 缺少生日
        User incompleteUser4 = new User();
        incompleteUser4.setUserId(2026000005L);
        incompleteUser4.setAccount("incompleteuser4");
        incompleteUser4.setPassword("123456");
        incompleteUser4.setPhone("13800138000");
        incompleteUser4.setAvatar("http://example.com/avatar.jpg");
        incompleteUser4.setGender(1);
        incompleteUser4.setBirthday(null); // 空生日
        
        assertFalse(userService.isUserInfoComplete(incompleteUser4), "缺少生日的用户应该返回false");
    }

    @Test
    public void testConvertToUserInfoDTO() {
        User user = new User();
        user.setUserId(2026000001L);
        user.setAccount("testuser");
        user.setPassword("123456");
        user.setPhone("13800138000");
        user.setAvatar("http://example.com/avatar.jpg");
        user.setGender(1);
        user.setBirthday(new Date());
        
        UserInfoDTO userInfoDTO = userService.convertToUserInfoDTO(user);
        
        assertNotNull(userInfoDTO);
        assertEquals("2026000001", userInfoDTO.getUserid());
        assertEquals("testuser", userInfoDTO.getUsername());
        assertEquals("http://example.com/avatar.jpg", userInfoDTO.getAvatar());
        assertEquals("13800138000", userInfoDTO.getPhone());
        assertEquals(Integer.valueOf(1), userInfoDTO.getGender());
        assertNotNull(userInfoDTO.getBirthday());
        assertFalse(userInfoDTO.getBirthday().isEmpty());
    }

    @Test
    public void testNullUserConversion() {
        UserInfoDTO userInfoDTO = userService.convertToUserInfoDTO(null);
        assertNull(userInfoDTO);
    }
}