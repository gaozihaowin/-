package com.daily.dailychineseculture.service;

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
}