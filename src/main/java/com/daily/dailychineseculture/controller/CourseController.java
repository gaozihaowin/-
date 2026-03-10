package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.CampVO;
import com.daily.dailychineseculture.dto.MyCourseVO;
import com.daily.dailychineseculture.service.CampService;
import com.daily.dailychineseculture.service.CourseService;
import com.daily.dailychineseculture.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程控制器
 * 提供课程相关API接口
 * 
 * @author Java后端架构师
 * @since 2026-02-25
 */
@RestController
@RequestMapping("/courses")
public class CourseController {
    
    @Autowired
    private CampService campService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private JwtUtils jwtUtils;
        
    /**
     * 获取热门课程推荐列表（小程序端公开接口）
     * 接口路径：GET /courses/hot
     * 功能描述：联表查询t_camp和 t_camp_type，优先展示 tag='热招' 的课程，
     *          按 enroll_count 降序排列，取最新的 5 条
     * 
     * @return 统一响应结果，包含热门课程推荐列表
     */
    @GetMapping("/hot")
    public ResponseResult<List<CampVO>> getHotCourses() {
        List<CampVO> hotCourses = campService.getHotCourses();
        return ResponseResult.success("查询成功", hotCourses);
    }
        
    /**
     * 获取我的课程列表
     * 
     * @param token JWT 令牌
     * @param tabType 标签类型：1-正在学习，2-历史课程，3-已结业
     * @return 我的课程列表
     */
    @GetMapping
    public Result<List<MyCourseVO>> getMyCourses(
            @RequestHeader("Authorization") String token,
            @RequestParam Integer tabType) {
        
        try {
            // 从token中解析用户ID
            Long userId = jwtUtils.getUserIdFromToken(token);
            
            // 参数校验
            if (tabType == null) {
                return Result.error("标签类型不能为空");
            }
            if (tabType < 1 || tabType > 3) {
                return Result.error("标签类型必须为1、2或3");
            }
            
            List<MyCourseVO> myCourses = courseService.getMyCourses(userId, tabType);
            return Result.success(myCourses);
        } catch (RuntimeException e) {
            return Result.build(401, "未授权：" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error("获取我的课程失败：" + e.getMessage());
        }
    }
}