# Bug 排查相关代码收集

## Bug 描述
访问 `/api/admin/camp-plan/save-day` 报错：
```
No static resource api/admin/camp-plan/save-day
```

---

## 1. CampPlanController.java

```java
package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.CampOptionDTO;
import com.daily.dailychineseculture.dto.CampPlanDTO;
import com.daily.dailychineseculture.dto.CampPlanSaveDayDTO;
import com.daily.dailychineseculture.dto.GenerateCalendarRequest;
import com.daily.dailychineseculture.service.CampPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教务排课工作台 Controller
 * 支持排课计划的 CRUD 操作，以及任务的一对多管理
 */
@RestController
@RequestMapping("/api/admin/camp-plans")
@RequiredArgsConstructor
public class CampPlanController {

    private final CampPlanService campPlanService;

    /**
     * 获取某营期的排课时间轴
     * GET /api/admin/camp-plans?campId={campId}
     *
     * 每个排课计划会包含其下的所有任务列表
     *
     * @param campId 营期 ID
     * @return 统一响应结果，包含排课计划列表（含任务）
     */
    @GetMapping
    public ResponseResult<List<CampPlanDTO>> getCampPlans(@RequestParam Integer campId) {
        List<CampPlanDTO> plans = campPlanService.getCampPlansByCampId(campId);
        return ResponseResult.success("查询成功", plans);
    }

    /**
     * 一键生成空日历
     * POST /api/admin/camp-plans/generate
     *
     * @param request 生成日历请求
     * @return 统一响应结果
     */
    @PostMapping("/generate")
    public ResponseResult<String> generateCalendar(@RequestBody GenerateCalendarRequest request) {
        campPlanService.generateCalendar(request);
        return ResponseResult.success("日历框架生成成功");
    }

    /**
     * 新增一天的排课
     * POST /api/admin/camp-plans
     *
     * @param campPlan 排课计划 DTO（包含 campId, dayIndex, planDate 等基本信息）
     * @return 统一响应结果，包含新增后的排课计划（含 planId）
     */
    @PostMapping
    public ResponseResult<CampPlanDTO> addCampPlan(@RequestBody CampPlanDTO campPlan) {
        CampPlanDTO result = campPlanService.addCampPlan(campPlan);
        return ResponseResult.success("新增成功", result);
    }

    /**
     * 保存/更新单日课表
     * PUT /api/admin/camp-plans
     *
     * 包括更新排课基本信息和全量同步任务列表
     *
     * @param campPlan 排课计划 DTO（包含 planId, title, tasks 等）
     * @return 统一响应结果
     */
    @PutMapping
    public ResponseResult<String> saveOrUpdateCampPlan(@RequestBody CampPlanDTO campPlan) {
        campPlanService.saveOrUpdateCampPlan(campPlan);
        return ResponseResult.success("保存成功");
    }

    /**
     * 删除整天排课及挂载的所有任务
     * DELETE /api/admin/camp-plans/{planId}
     *
     * @param planId 排课 ID
     * @return 统一响应结果
     */
    @DeleteMapping("/{planId}")
    public ResponseResult<String> deleteCampPlan(@PathVariable Integer planId) {
        campPlanService.deleteCampPlan(planId);
        return ResponseResult.success("删除成功");
    }

    @PutMapping("/save-day")
    public ResponseResult<String> saveDay(@Valid @RequestBody CampPlanSaveDayDTO request) {
        campPlanService.saveDayPlan(request);
        return ResponseResult.success("保存成功");
    }
}
```

---

## 2. CampPlanService.java

```java
package com.daily.dailychineseculture.service;

import com.daily.dailychineseculture.dto.CampOptionDTO;
import com.daily.dailychineseculture.dto.CampPlanDTO;
import com.daily.dailychineseculture.dto.CampPlanSaveDayDTO;
import com.daily.dailychineseculture.dto.GenerateCalendarRequest;

import java.util.List;

/**
 * 排课计划 Service 接口
 */
public interface CampPlanService {

    /**
     * 获取营期下拉选项
     * @return 营期下拉选项列表
     */
    List<CampOptionDTO> getCampOptions();

    /**
     * 根据营期 ID 查询排课计划列表
     * 每个排课计划会包含其下的所有任务
     * @param campId 营期 ID
     * @return 排课计划列表
     */
    List<CampPlanDTO> getCampPlansByCampId(Integer campId);

    /**
     * 一键生成空日历
     * @param request 生成日历请求
     */
    void generateCalendar(GenerateCalendarRequest request);

    /**
     * 保存/更新单日课表
     * 包括更新排课基本信息和全量同步任务列表
     * @param campPlan 排课计划 DTO
     */
    void saveOrUpdateCampPlan(CampPlanDTO campPlan);

    /**
     * 新增一天的排课
     * @param campPlan 排课计划 DTO（包含 campId, dayIndex, planDate 等基本信息）
     * @return 新增后的排课计划（包含 planId）
     */
    CampPlanDTO addCampPlan(CampPlanDTO campPlan);

    /**
     * 删除整天排课及挂载的所有任务
     * @param planId 排课 ID
     */
    void deleteCampPlan(Integer planId);

    /**
     * 聚合保存单日排课（主表+任务列表全量刷新）
     * 采用全删全插策略：先删该日所有旧任务，再批量插入新任务
     * @param request 单日排课聚合保存请求
     */
    void saveDayPlan(CampPlanSaveDayDTO request);
}
```

---

## 3. CampPlanServiceImpl.java

```java
package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.CampOptionDTO;
import com.daily.dailychineseculture.dto.CampPlanDTO;
import com.daily.dailychineseculture.dto.CampPlanSaveDayDTO;
import com.daily.dailychineseculture.dto.GenerateCalendarRequest;
import com.daily.dailychineseculture.dto.PlanTaskDTO;
import com.daily.dailychineseculture.entity.Camp;
import com.daily.dailychineseculture.entity.CampPlan;
import com.daily.dailychineseculture.entity.PlanTask;
import com.daily.dailychineseculture.mapper.CampMapper;
import com.daily.dailychineseculture.mapper.CampPlanMapper;
import com.daily.dailychineseculture.mapper.PlanTaskMapper;
import com.daily.dailychineseculture.service.CampPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 排课计划 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class CampPlanServiceImpl implements CampPlanService {

    private final CampPlanMapper campPlanMapper;
    private final CampMapper campMapper;
    private final PlanTaskMapper planTaskMapper;

    @Override
    public List<CampOptionDTO> getCampOptions() {
        return campMapper.selectCampOptions();
    }

    /**
     * 根据营期 ID 查询排课计划列表
     * 每个排课计划会包含其下的所有任务
     */
    @Override
    public List<CampPlanDTO> getCampPlansByCampId(Integer campId) {
        // 1. 查询排课计划列表
        List<CampPlanDTO> plans = campPlanMapper.selectCampPlansByCampId(campId);

        // 2. 遍历每个排课计划，查询其下的所有任务
        for (CampPlanDTO plan : plans) {
            List<PlanTaskDTO> tasks = planTaskMapper.selectTasksByPlanId(plan.getPlanId());
            plan.setTasks(tasks);
        }

        return plans;
    }

    /**
     * 一键生成空日历
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateCalendar(GenerateCalendarRequest request) {
        Integer campId = request.getCampId();

        // 1. 校验：查询是否已存在排课计划
        int count = campPlanMapper.countCampPlansByCampId(campId);
        if (count > 0) {
            throw new RuntimeException("该营期已存在课表，请勿重复生成");
        }

        // 2. 查询营期信息
        Camp camp = campMapper.selectById(campId);
        if (camp == null) {
            throw new RuntimeException("未找到指定的营期");
        }

        // 3. 计算日期范围
        LocalDate startDate = convertToLocalDate(camp.getStartTime());
        LocalDate endDate = convertToLocalDate(camp.getEndTime());

        // 计算总天数（含起止日）
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (totalDays <= 0) {
            throw new RuntimeException("开营时间必须早于或等于结营时间");
        }

        // 4. 循环生成排课计划
        List<CampPlan> campPlans = new ArrayList<>();
        for (int i = 0; i < totalDays; i++) {
            CampPlan plan = new CampPlan();
            plan.setCampId(campId);
            plan.setDayIndex(i + 1);
            plan.setPlanDate(Date.from(startDate.plusDays(i).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            plan.setTitle("");

            campPlans.add(plan);
        }

        // 5. 批量插入
        campPlanMapper.batchInsertCampPlans(campPlans);
    }

    /**
     * 保存/更新单日课表
     * 包括更新排课基本信息和全量同步任务列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateCampPlan(CampPlanDTO campPlan) {
        Integer planId = campPlan.getPlanId();

        // 1. 更新排课基本信息
        campPlanMapper.updateCampPlan(campPlan);

        // 2. 全量同步任务列表
        if (campPlan.getTasks() != null) {
            syncTasks(planId, campPlan.getTasks());
        }
    }

    /**
     * 全量同步任务列表
     * 核心逻辑：
     * - 前端传了 taskId 且数据库有的，执行 update
     * - 前端传的 taskId 为 null 的，执行 insert
     * - 数据库里原有，但前端没传的 taskId，执行逻辑删除
     *
     * @param planId 排课 ID
     * @param newTasks 前端传来的任务列表
     */
    private void syncTasks(Integer planId, List<PlanTaskDTO> newTasks) {
        // 1. 查询数据库中该 planId 原有的所有任务 ID
        List<Integer> existingTaskIds = planTaskMapper.selectTaskIdsByPlanId(planId);
        Set<Integer> existingTaskIdSet = existingTaskIds.stream().collect(Collectors.toSet());

        // 2. 收集前端传来的有效 taskId（前端使用 Long，这里转 Integer）
        Set<Integer> newTaskIdSet = newTasks.stream()
                .map(dto -> dto.getTaskId() != null ? dto.getTaskId().intValue() : null)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 3. 找出需要删除的任务 ID（数据库有，前端没有）
        List<Integer> toDeleteTaskIds = existingTaskIds.stream()
                .filter(id -> !newTaskIdSet.contains(id))
                .collect(Collectors.toList());

        // 4. 批量逻辑删除不再需要的任务（is_deleted = 1）
        if (!toDeleteTaskIds.isEmpty()) {
            planTaskMapper.logicDeleteBatch(toDeleteTaskIds);
        }

        // 5. 遍历前端传来的任务，执行 insert 或 update
        for (PlanTaskDTO taskDTO : newTasks) {
            if (taskDTO.getTaskId() == null) {
                // 新增任务
                PlanTask newTask = convertToEntity(planId, taskDTO);
                planTaskMapper.insertTask(newTask);
                // 回填 taskId（如果前端需要）
                taskDTO.setTaskId(newTask.getTaskId());
            } else {
                // 更新任务
                PlanTask updateTask = convertToEntity(planId, taskDTO);
                planTaskMapper.updateTask(updateTask);
            }
        }
    }

    /**
     * 将 DTO 转换为实体
     */
    private PlanTask convertToEntity(Integer planId, PlanTaskDTO dto) {
        PlanTask task = new PlanTask();
        task.setTaskId(dto.getTaskId());
        task.setPlanId(planId);
        task.setTaskType(dto.getTaskType());
        task.setTaskName(dto.getTaskName());
        task.setTaskDesc(dto.getTaskDesc());
        task.setTaskUrl(dto.getTaskUrl());
        task.setDuration(dto.getDuration());
        task.setIsRequired(dto.getIsRequired());
        task.setSortOrder(dto.getSortOrder());
        return task;
    }

    /**
     * 新增一天的排课
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CampPlanDTO addCampPlan(CampPlanDTO campPlan) {
        // 1. 校验营期是否存在
        Camp camp = campMapper.selectById(campPlan.getCampId());
        if (camp == null) {
            throw new RuntimeException("未找到指定的营期");
        }

        // 2. 创建排课计划实体
        CampPlan plan = new CampPlan();
        plan.setCampId(campPlan.getCampId());
        plan.setDayIndex(campPlan.getDayIndex());
        plan.setPlanDate(campPlan.getPlanDate());
        plan.setTitle(campPlan.getTitle() != null ? campPlan.getTitle() : "");

        // 3. 插入排课计划
        campPlanMapper.insertCampPlan(plan);

        // 4. 如果有任务列表，同步插入任务
        if (campPlan.getTasks() != null && !campPlan.getTasks().isEmpty()) {
            for (PlanTaskDTO taskDTO : campPlan.getTasks()) {
                PlanTask newTask = convertToEntity(plan.getPlanId(), taskDTO);
                planTaskMapper.insertTask(newTask);
            }
            // 重新查询任务列表
            List<PlanTaskDTO> tasks = planTaskMapper.selectTasksByPlanId(plan.getPlanId());
            campPlan.setTasks(tasks);
        }

        // 5. 设置返回的 planId
        campPlan.setPlanId(plan.getPlanId());

        return campPlan;
    }

    /**
     * 删除整天排课
     * 直接删除 t_camp_plan 表中对应 ID 的记录即可
     * 数据库已有 ON DELETE CASCADE 约束自动清理底层任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCampPlan(Integer planId) {
        // 1. 校验排课是否存在
        CampPlan plan = campPlanMapper.selectById(planId);
        if (plan == null) {
            throw new RuntimeException("未找到指定的排课计划");
        }

        // 2. 直接删除排课计划，CASCADE 约束会自动清理底层任务
        campPlanMapper.deleteByPlanId(planId);
    }

    /**
     * 将 Date 转换为 LocalDate
     */
    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 聚合保存单日排课（主表+任务列表全量刷新）
     * 全删全插策略：
     * 1. 更新 CampPlan 主表信息
     * 2. 物理删除该日所有旧任务
     * 3. 遍历前端任务列表，强制设置 planId 并置空 ID，批量插入新任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDayPlan(CampPlanSaveDayDTO request) {
        Integer planId = request.getId();

        CampPlanDTO planDTO = new CampPlanDTO();
        planDTO.setPlanId(planId);
        planDTO.setCampId(request.getCampId());
        planDTO.setTitle(request.getTitle());
        campPlanMapper.updateCampPlan(planDTO);

        planTaskMapper.deleteTasksByPlanId(planId);

        if (request.getTasks() != null && !request.getTasks().isEmpty()) {
            List<PlanTask> tasksToInsert = new ArrayList<>();
            for (CampPlanSaveDayDTO.CampTask taskDTO : request.getTasks()) {
                PlanTask task = new PlanTask();
                task.setPlanId(planId);
                task.setTaskType(taskDTO.getTaskType());
                task.setTaskName(taskDTO.getTaskName());
                task.setTaskDesc(taskDTO.getTaskDesc());
                task.setTaskUrl(taskDTO.getTaskUrl());
                task.setDuration(taskDTO.getDuration());
                task.setIsRequired(taskDTO.getIsRequired());
                task.setSortOrder(taskDTO.getSortOrder());
                tasksToInsert.add(task);
            }
            planTaskMapper.batchInsertTasks(tasksToInsert);
        }
    }
}
```

---

## 4. application.yml

```yaml
# src/main/resources/application.yml

server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://47.120.31.133:3306/camp_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: DailyChineseCultureCODE123.
    driver-class-name: com.mysql.cj.jdbc.Driver
  servlet:
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB

mybatis:
  # 开启驼峰命名转换 (数据库下划线字段自动映射到 Java 驼峰属性)
  configuration:
    map-underscore-to-camel-case: true
  # Mapper XML 文件位置
  mapper-locations: classpath:mapper/*.xml

# 微信小程序配置
wx:
  appid: wx58b4d74c673f584c
  secret: 1ffaef2d4f60b39e18dea3e19f0c924b

# 文件上传配置
file:
  upload-dir: ./uploads/
  max-size: 524288000
```

---

## 5. DailyChineseCultureApplication.java（启动类）

```java
package com.daily.dailychineseculture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
public class DailyChineseCultureApplication {

    public static void main(String[] args) {
        SpringApplication.run(DailyChineseCultureApplication.class, args);
    }

    // 新增：定义 RestTemplate Bean，解决 AuthController 的依赖问题
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // 新增：跨域配置 Bean
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

---

## 6. WebConfig.java（无 Spring Security，仅拦截器配置）

```java
package com.daily.dailychineseculture.config;

import com.daily.dailychineseculture.interceptor.AuthInterceptor;
import com.daily.dailychineseculture.interceptor.AdminAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

/**
 * Web MVC 配置类 - 用于配置静态资源映射和拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    /**
     * 添加静态资源处理器
     * 将 /uploads/** 请求映射到本地物理路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path);
    }

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册认证拦截器（移动端 C 端用户）
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")  // 默认拦截所有路径
                .excludePathPatterns(
                    // ========== 认证相关：完全公开 ==========
                    "/login",                        // 账号密码登录
                    "/admin/login",                  // 管理端登录（无 /api 前缀场景）
                    "/api/admin/login",              // 管理端登录（带 /api 前缀场景）
                    "/wxLogin",                      // 微信登录
                    "/captcha",                      // 验证码（通用）
                    "/admin/captcha",                // 管理端验证码（无 /api 前缀）
                    "/api/admin/captcha",            // 管理端验证码（带 /api 前缀）
                    "/user/register",                // 用户注册
                    "/user/updateAll",               // 用户信息更新（公开接口）
                    "/updateUserInfo",               // 兼容旧版信息更新

                    // ========== 首页与展示数据：完全公开 ==========
                    "/courses/hot",                  // 热门课程推荐（小程序端首页）
                    "/courses/list",                  // 课程列表分页查询（小程序端课程列表页）
                    "/courses/*/schedule",           // 课程安排目录（营期详情页）
                    "/courses/*/info",               // 营期详情信息（课程详情页顶部信息栏）
                    "/api/admin/camps/options",      // 营期选项列表（PC 端登录页）
                    "/api/admin/camps/hot",          // 热门营期列表
                    "/api/admin/camps/all",          // 全部营期列表

                    // ========== 静态资源与系统页面 ==========
                    "/error",                        // 错误页面
                    "/favicon.ico",                  // 网站图标
                    "/swagger-ui/**",                // Swagger 文档
                    "/v3/api-docs/**",               // OpenAPI 文档
                    "/static/**",                    // 静态资源
                    "/public/**",                    // 公共资源
                    "/webjars/**",                   // WebJars 资源

                    // ========== OPTIONS 预检请求 ==========
                    "//**"                          // 允许跨域 OPTIONS 请求
                );

        // 注册 PC 端后台管理鉴权拦截器
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**")  // 拦截所有后台管理接口
                .excludePathPatterns(
                    "/admin/login",         // 排除管理员登录接口（无 /api 前缀）
                    "/api/admin/login",     // 排除管理员登录接口
                    "/captcha",             // 排除验证码接口（通用）
                    "/admin/captcha",       // 排除验证码接口（无 /api 前缀）
                    "/api/admin/captcha",   // 排除验证码接口（带 /api 前缀）
                    "/api/admin/camps/options",  // 排除营期选项（登录页需要）
                    "/api/admin/camps/hot",      // 排除热门营期（登录页需要）
                    "/api/admin/camps/all"       // 排除全部营期（登录页需要）
                );
    }
}
```

---

## 问题分析

### 路由路径问题
- Controller 的 `@RequestMapping` 是 `/api/admin/camp-plans`（复数）
- 前端请求的是 `/api/admin/camp-plan/save-day`（单数 `camp-plan`）
- 路径不匹配！

### 实际可用的接口
- `PUT /api/admin/camp-plans/save-day` （复数）
- `PUT /api/admin/camp-plans` （复数，这是已有的 saveOrUpdateCampPlan 接口）

### 建议修复方案
前端请求路径改为 `/api/admin/camp-plans/save-day`（复数），或新增一个单数路由的 Controller/接口。