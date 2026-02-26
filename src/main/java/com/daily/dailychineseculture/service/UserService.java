package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.UserInfoDTO;
import com.daily.dailychineseculture.entity.User;
import com.daily.dailychineseculture.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 用户服务类
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IdGeneratorService idGeneratorService;

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 创建用户
     */
    public User createUser(User user) {
        // 生成用户ID
        Long userId = idGeneratorService.generateUserId();
        user.setUserId(userId);
        user.setCreateTime(new Date());
        user.setStatus(1); // 默认状态为正常
        user.setGender(0); // 默认性别为未知
        
        userMapper.insert(user);
        return user;
    }

    /**
     * 更新用户
     */
    public User updateUser(User user) {
        int result = userMapper.update(user);
        if (result > 0) {
            return user;
        }
        return null;
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long userId) {
        userMapper.deleteById(userId);
    }

    /**
     * 检查用户是否存在
     * @param username 用户名
     * @return 是否存在
     */
    public boolean checkUserExists(String username) {
        try {
            User user = userMapper.selectByAccount(username);
            return user != null;
        } catch (Exception e) {
            // 如果查询出错，默认返回false
            return false;
        }
    }

    /**
     * 验证用户密码
     * @param username 用户名
     * @param password 密码
     * @return 密码是否正确
     */
    public boolean verifyPassword(String username, String password) {
        try {
            User user = userMapper.selectByAccount(username);
            return user != null && password.equals(user.getPassword());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 创建新用户
     * @param username 用户名
     * @param password 密码
     * @return 是否创建成功
     */
    public boolean createUser(String username, String password) {
        try {
            User user = new User();
            user.setAccount(username);
            user.setPassword(password);
            user.setCreateTime(new Date());
            user.setStatus(1);
            user.setGender(0);
            user.setOpenid(""); // 微信openid设为空字符串
            
            // 生成用户ID
            Long userId = idGeneratorService.generateUserId();
            user.setUserId(userId);
            
            int result = userMapper.insert(user);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户对象
     */
    public User getUserByUsername(String username) {
        return userMapper.selectByAccount(username);
    }
    
    /**
     * 检查用户信息是否完整
     * @param user 用户对象
     * @return true表示信息完整，false表示信息不完整
     */
    public boolean isUserInfoComplete(User user) {
        if (user == null) {
            return false;
        }
        
        // 检查手机号是否为空或默认值
        boolean phoneValid = user.getPhone() != null && !user.getPhone().trim().isEmpty();
        
        // 检查头像是否为空或默认值
        boolean avatarValid = user.getAvatar() != null && !user.getAvatar().trim().isEmpty();
        
        // 检查性别是否为有效值（非0）
        boolean genderValid = user.getGender() != null && user.getGender() != 0;
        
        // 检查生日是否设置
        boolean birthdayValid = user.getBirthday() != null;
        
        // 所有字段都必须有效才算信息完整
        return phoneValid && avatarValid && genderValid && birthdayValid;
    }
    
    /**
     * 将User实体转换为UserInfoDTO
     * @param user 用户实体
     * @return UserInfoDTO对象
     */
    public UserInfoDTO convertToUserInfoDTO(User user) {
        if (user == null) {
            return null;
        }
        
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setUserid(String.valueOf(user.getUserId()));
        userInfoDTO.setUsername(user.getAccount());
        userInfoDTO.setAvatar(user.getAvatar() != null ? user.getAvatar() : "");
        userInfoDTO.setPhone(user.getPhone() != null ? user.getPhone() : "");
        userInfoDTO.setGender(user.getGender() != null ? user.getGender() : 0);
        
        // 生日格式化处理
        if (user.getBirthday() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            userInfoDTO.setBirthday(sdf.format(user.getBirthday()));
        } else {
            userInfoDTO.setBirthday("");
        }
        
        return userInfoDTO;
    }
    
    /**
     * 创建新用户并返回用户对象
     * @param username 用户名
     * @param password 密码
     * @return 用户对象
     */
    public User createUserWithReturn(String username, String password) {
        try {
            System.out.println("开始创建新用户: " + username);
            
            User user = new User();
            user.setAccount(username);
            user.setPassword(password);
            user.setCreateTime(new Date());
            user.setStatus(1);
            user.setGender(0);
            
            // 设置默认值避免数据库NOT NULL约束
            user.setAvatar(""); // 空字符串头像
            user.setPhone(""); // 空字符串手机号
            user.setRegion(""); // 空字符串地域
            user.setProfession(""); // 空字符串职业
            user.setBirthday(null); // null生日
            
            // 生成用户ID
            Long userId = idGeneratorService.generateUserId();
            user.setUserId(userId);
            
            System.out.println("准备插入用户数据: " + user);
            
            int result = userMapper.insert(user);
            System.out.println("数据库插入结果: " + result);
            
            if (result > 0) {
                System.out.println("用户创建成功: " + username + ", userId: " + userId);
                return user;
            } else {
                System.err.println("用户创建失败: 数据库返回影响行数为0");
                return null;
            }
        } catch (Exception e) {
            System.err.println("=== 用户注册异常详情 ===");
            System.err.println("用户名: " + username);
            System.err.println("异常类型: " + e.getClass().getSimpleName());
            System.err.println("异常信息: " + e.getMessage());
            System.err.println("异常堆栈:");
            e.printStackTrace();
            System.err.println("=====================");
            return null;
        }
    }
}