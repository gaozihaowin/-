package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.AdminLoginRequest;
import com.daily.dailychineseculture.dto.AdminLoginResult;
import com.daily.dailychineseculture.dto.CampListPageDTO;
import com.daily.dailychineseculture.dto.CampTypeOptionDTO;
import com.daily.dailychineseculture.dto.RecentCampDTO;
import com.daily.dailychineseculture.entity.User;
import com.daily.dailychineseculture.mapper.UserMapper;
import com.daily.dailychineseculture.service.AdminAuthService;
import com.daily.dailychineseculture.service.CampService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PC 端后台管理控制器
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private AdminAuthService adminAuthService;
    
    @Autowired
    private CampService campService;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 管理员登录接口
     * POST /api/admin/login
     * 
     * @param request 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<AdminLoginResult> adminLogin(@RequestBody AdminLoginRequest request) {
        try {
            AdminLoginResult loginResult = adminAuthService.adminLogin(request);
            return Result.success(loginResult);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (RuntimeException e) {
            // 处理特定的业务异常
            String errorMsg = e.getMessage();
            
            // 检查是否包含 403 标记（无权以该身份登录）
            if (errorMsg.contains(":403")) {
                return Result.build(403, errorMsg.replace(":403", ""), null);
            } else if ("账号或密码错误".equals(errorMsg)) {
                return Result.build(401, errorMsg, null);
            } else {
                return Result.build(400, errorMsg, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误，请稍后重试");
        }
    }
    
    /**
     * 获取最近活跃课程列表（仪表盘用）
     * GET /api/admin/dashboard/recent-camps
     * 
     * @return 最近活跃课程列表
     */
    @GetMapping("/dashboard/recent-camps")
    public Result<List<RecentCampDTO>> getRecentCamps() {
        try {
            List<RecentCampDTO> recentCamps = campService.getRecentCamps();
            return Result.success(recentCamps);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取最新课程失败：" + e.getMessage());
        }
    }
    
    /**
     * 分页查询营期列表（营期管理大盘）
     * GET /api/admin/camps
     * 
     * @param page 当前页码（默认 1）
     * @param size 每页大小（默认 10）
     * @param keyword 关键词（可选，模糊匹配营期名称）
     * @param status 状态（可选，精确匹配）
     * @param typeId 体系类型 ID（可选，精确匹配）
     * @return 分页结果
     */
    @GetMapping("/camps")
    public Result<CampListPageDTO> getCampList(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "size", defaultValue = "10") Integer size,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "status", required = false) Integer status,
        @RequestParam(value = "typeId", required = false) Integer typeId
    ) {
        try {
            CampListPageDTO campList = campService.getCampList(page, size, keyword, status, typeId);
            return Result.success(campList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取营期列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取所有营期类型（用于下拉选项）
     * GET /api/admin/camp-types/options
     * 
     * @return 营期类型列表
     */
    @GetMapping("/camp-types/options")
    public Result<List<CampTypeOptionDTO>> getCampTypeOptions() {
        try {
            List<CampTypeOptionDTO> types = campService.getAllCampTypes();
            return Result.success(types);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取营期类型失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/profile")
    public com.daily.dailychineseculture.common.ResponseResult<Map<String, Object>> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) {
            return com.daily.dailychineseculture.common.ResponseResult.error(404, "用户不存在");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", String.valueOf(user.getUserId()));
        data.put("account", user.getAccount());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("phone", user.getPhone());
        data.put("region", user.getRegion());
        data.put("birthday", user.getBirthday() != null
            ? new SimpleDateFormat("yyyy-MM-dd").format(user.getBirthday()) : null);
        data.put("profession", user.getProfession());
        data.put("gender", user.getGender());
        return com.daily.dailychineseculture.common.ResponseResult.success("查询成功", data);
    }
    
    @PutMapping("/profile")
    public com.daily.dailychineseculture.common.ResponseResult<String> updateProfile(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) {
            return com.daily.dailychineseculture.common.ResponseResult.error(404, "用户不存在");
        }
        if (body.get("nickname") != null) user.setNickname((String) body.get("nickname"));
        if (body.get("avatar") != null) user.setAvatar((String) body.get("avatar"));
        if (body.get("phone") != null) user.setPhone((String) body.get("phone"));
        if (body.get("region") != null) user.setRegion((String) body.get("region"));
        if (body.get("profession") != null) user.setProfession((String) body.get("profession"));
        if (body.get("gender") != null) user.setGender(((Number) body.get("gender")).intValue());
        if (body.get("birthday") != null && !((String) body.get("birthday")).isEmpty()) {
            try {
                user.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse((String) body.get("birthday")));
            } catch (Exception ignored) {}
        }
        userMapper.update(user);
        return com.daily.dailychineseculture.common.ResponseResult.success("保存成功");
    }
    
    @PutMapping("/profile/password")
    public com.daily.dailychineseculture.common.ResponseResult<String> updatePassword(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) {
            return com.daily.dailychineseculture.common.ResponseResult.error(404, "用户不存在");
        }
        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");
        String confirmPwd = body.get("confirmPassword");
        if (!user.getPassword().equals(oldPwd)) {
            return com.daily.dailychineseculture.common.ResponseResult.error(400, "当前密码错误");
        }
        if (newPwd == null || newPwd.length() < 6) {
            return com.daily.dailychineseculture.common.ResponseResult.error(400, "新密码不能少于6位");
        }
        if (!newPwd.equals(confirmPwd)) {
            return com.daily.dailychineseculture.common.ResponseResult.error(400, "两次密码不一致");
        }
        user.setPassword(newPwd);
        userMapper.update(user);
        return com.daily.dailychineseculture.common.ResponseResult.success("密码修改成功");
    }
}
