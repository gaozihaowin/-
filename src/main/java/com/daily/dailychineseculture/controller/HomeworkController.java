package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.Result;
import com.daily.dailychineseculture.dto.*;
import com.daily.dailychineseculture.service.HomeworkService;
import com.daily.dailychineseculture.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.daily.dailychineseculture.service.VolunteerManageService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
/**
 * 作业控制器
 * 提供优秀作业评选相关API接口
 */
@RestController
@RequestMapping("")
public class HomeworkController {

    @Autowired
    private HomeworkService homeworkService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private VolunteerManageService volunteerManageService;

    /**
     * 获取志愿者管理范围
     * 接口路径: GET /volunteer/scope
     * 功能描述: 获取当前志愿者可以管理的班级、大组、小组范围
     *
     * @param token JWT令牌
     * @return 管理范围信息
     */
    @GetMapping("/volunteer/scope")
    public Result<List<Map<String, Object>>> getVolunteerScope(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            List<Map<String, Object>> scope = volunteerManageService.getManagementScopes(userId);  // 调用VolunteerManageService的方法
            return Result.success(scope);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询志愿者权限范围异常：" + e.getMessage());
        }
    }



    /**
     * 获取作业名单
     * 接口路径: GET /homework/list/{type}/{id}/{status}
     * 功能描述: 获取指定范围内的作业列表，支持筛选全部作业或仅优秀作业
     *
     * @param token JWT令牌
     * @param type 范围类型（class/bigGroup/smallGroup）
     * @param id 范围ID
     * @param status 状态筛选（all/excellent）
     * @param date 日期筛选（可选）
     * @param keyword 搜索关键字（可选）
     * @return 作业列表
     */
    @GetMapping("/homework/list/{type}/{id}/{status}")
    public Result<HomeworkListDTO> getHomeworkList(@RequestHeader("Authorization") String token,
                                                   @PathVariable String type,
                                                   @PathVariable Integer id,
                                                   @PathVariable String status,
                                                   @RequestParam(value = "date", required = false) String date,
                                                   @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            HomeworkListDTO homeworkList = homeworkService.getHomeworkList(userId, type, id, status,date,keyword);
            return Result.success(homeworkList);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains("无效的筛选类型")) {
                return Result.error("无效的筛选类型");
            } else if (e.getMessage().contains("无权限")) {
                return Result.build(403, "无权限访问该范围数据", null);
            }
            return Result.error("获取作业名单异常：" + e.getMessage());
        }
    }

    /**
     * 标记优秀作业
     * 接口路径: POST /camp/homework/mark/{homeworkId}/{isExcellent}
     * 功能描述: 标记或取消标记优秀作业
     *
     * @param token JWT令牌
     * @param homeworkId 作业ID
     * @param isExcellent 是否优秀（true-是，false-否）
     * @return 操作结果
     */
    @PostMapping("/camp/homework/mark/{homeworkId}/{isExcellent}")
    public Result<Void> markExcellentHomework(@RequestHeader("Authorization") String token,
                                              @PathVariable Integer homeworkId,
                                              @PathVariable Boolean isExcellent) {
        try {
            // 验证参数
            boolean success = homeworkService.markExcellentHomework(homeworkId, isExcellent);
            if (success) {
                String message = isExcellent ? "标记优秀成功" : "取消优秀成功";
                return Result.successMsg(message);
            } else {
                return Result.error("作业不存在或标记失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("标记优秀作业异常：" + e.getMessage());
        }
    }

    /**
     * 获取优秀作业列表
     * 接口路径: GET /homework/excellent/list/{type}/{id}/{status}
     * 功能描述: 获取指定范围内的优秀作业列表（兼容原有接口）
     *
     * @param token JWT令牌
     * @param type 范围类型（class/bigGroup/smallGroup）
     * @param id 范围ID
     * @param status 状态筛选（all/excellent）
     * @param date 日期筛选（可选）
     * @param keyword 搜索关键字（可选）
     * @return 优秀作业列表
     */
    @GetMapping("/homework/excellent/list/{type}/{id}/{status}")
    public Result<HomeworkListDTO> getExcellentHomeworkList(@RequestHeader("Authorization") String token,
                                                            @PathVariable String type,
                                                            @PathVariable Integer id,
                                                            @PathVariable String status,
                                                            @RequestParam(value = "date", required = false) String date,
                                                            @RequestParam(value = "keyword", required = false) String keyword) {
        return getHomeworkList(token, type, id, status, date, keyword);
    }

    /**
     * 获取作业详情
     * 接口路径: GET /homework/detail/{homeworkId}
     * 功能描述: 获取作业详细信息
     *
     * @param token JWT令牌
     * @param homeworkId 作业ID
     * @return 作业详情
     */
    @GetMapping("/homework/detail/{homeworkId}")
    public Result<HomeworkDetailDTO> getHomeworkDetail(@RequestHeader("Authorization") String token,
                                                       @PathVariable Integer homeworkId) {
        try {
            HomeworkDetailDTO detail = homeworkService.getHomeworkDetail(homeworkId);
            if (detail == null) {
                return Result.error("作业不存在");
            }
            return Result.success(detail);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询作业详情异常：" + e.getMessage());
        }
    }
    /**
     * 获取作业统计数据
     * 接口路径: GET /homework/stats
     * 功能描述: 获取指定范围内的作业完成率统计
     *
     * @param token JWT令牌
     * @param type 范围类型（class/bigGroup/smallGroup）
     * @param id 范围ID
     * @param date 日期（可选）
     * @param keyword 搜索关键字（可选）
     * @return 作业统计数据
     */
    @GetMapping("/homework/stats")
    public Result<Map<String, Object>> getHomeworkStatistics(@RequestHeader("Authorization") String token,
                                                             @RequestParam String type,
                                                             @RequestParam Integer id,
                                                             @RequestParam(value = "date", required = false) String date,
                                                             @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            Map<String, Object> statistics = homeworkService.getHomeworkStatistics(userId, type, id, date,keyword);
            return Result.success(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains("无权限")) {
                return Result.build(403, "无权限访问该范围数据", null);
            }
            return Result.error("获取作业统计数据异常：" + e.getMessage());
        }
    }

    /**
     * 获取作业状态名单
     * 接口路径: GET /homework/status/list/{type}/{id}
     * 功能描述: 获取指定范围内的作业状态名单，包括已交、未交、迟交的学生名单
     *
     * @param token JWT令牌
     * @param type 范围类型（class/bigGroup/smallGroup）
     * @param id 范围ID
     * @param date 日期筛选
     * @param keyword 搜索关键字（可选）
     * @return 作业状态名单
     */
    @GetMapping("/homework/status/list/{type}/{id}")
    public Result<Map<String, Object>> getHomeworkStatusList(@RequestHeader("Authorization") String token,
                                                             @PathVariable String type,
                                                             @PathVariable Integer id,
                                                             @RequestParam(value = "date", required = true) String date,
                                                             @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
            Map<String, Object> statusList = homeworkService.getHomeworkStatusList(userId, type, id, date,keyword);
            return Result.success(statusList);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains("无效的筛选类型")) {
                return Result.error("无效的筛选类型");
            } else if (e.getMessage().contains("无权限")) {
                return Result.build(403, "无权限访问该范围数据", null);
            }
            return Result.error("获取作业状态名单异常：" + e.getMessage());
        }
    }
}
