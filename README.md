# 每日中文文化学习平台项目概述

## 项目简介

每日中文文化学习平台是一个基于Spring Boot开发的在线中文学习管理系统，为用户提供中文课程学习、训练营参与、个人学习进度跟踪、作业管理、志愿者服务等功能。系统采用现代化的前后端分离架构，支持用户注册登录、课程管理、学习记录追踪、营期排课、值班申请等核心功能。

## 技术架构概述

### 核心技术栈
- **后端框架**: Spring Boot 4.0.2
- **编程语言**: Java 21
- **构建工具**: Maven (Wrapper)
- **数据库**: MySQL 8.0
- **持久层框架**: MyBatis
- **安全认证**: JWT (JSON Web Token)
- **API文档**: RESTful API设计
- **数据校验**: Jakarta Validation

### 主要技术组件
1. **Spring Boot生态系统**
   - Spring Web MVC (控制器层)
   - Spring Data (数据访问层)
   - Spring Security基础集成
   - Spring Test (单元测试)

2. **数据库相关**
   - MySQL关系型数据库
   - MyBatis ORM框架
   - Druid数据库连接池

3. **安全与认证**
   - JWT令牌认证机制
   - BCrypt密码加密
   - 统一异常处理（GlobalExceptionHandler）
   - 全局响应封装（ResponseResult）
   - 拦截器权限控制（AuthInterceptor、AdminAuthInterceptor）

4. **开发工具**
   - Lombok (简化Java代码)
   - JUnit 5 (单元测试)
   - Mockito (Mock测试)

## 项目目录结构

```
daily-chinese-studies/
├── src/
│   ├── main/
│   │   ├── java/com/daily/dailychineseculture/
│   │   │   ├── common/                 # 通用组件
│   │   │   │   ├── BusinessException.java         # 自定义业务异常
│   │   │   │   ├── GlobalExceptionHandler.java    # 全局异常处理器
│   │   │   │   ├── ResponseResult.java            # 统一响应结果封装
│   │   │   │   └── Result.java                   # 基础响应实体
│   │   │   ├── config/                 # 配置类
│   │   │   │   └── WebConfig.java                 # Web配置（拦截器注册）
├── controller/             # 控制器层（25个）
│   │   │   │   ├── AdminController.java            # 管理员管理
│   │   │   │   ├── AdminDutyApplicationController.java  # 管理员值班申请
│   │   │   │   ├── AdminTestController.java        # 测试接口
│   │   │   │   ├── AppCourseController.java       # 小程序课程
│   │   │   │   ├── AuthController.java            # 登录认证
│   │   │   │   ├── CampController.java           # 营期管理
│   │   │   │   ├── CampPlanController.java       # 排课计划
│   │   │   │   ├── CampTypeController.java       # 营期类型
│   │   │   │   ├── ClassController.java          # 班级管理
│   │   │   │   ├── CommonController.java         # 通用接口
│   │   │   │   ├── CourseController.java         # 课程管理
│   │   │   │   ├── CourseListController.java     # 课程列表
│   │   │   │   ├── DashboardController.java      # 仪表盘
│   │   │   │   ├── DutyApplicationController.java  # 值班申请（用户端）
│   │   │   │   ├── EnrollmentController.java     # 报名管理
│   │   │   │   ├── GroupChatController.java      # 群聊管理
│   │   │   │   ├── HomeworkController.java       # 作业管理
│   │   │   │   ├── ManagementController.java     # 管理接口
│   │   │   │   ├── UserController.java           # 用户管理
│   │   │   │   ├── UserSearchController.java     # 用户搜索
│   │   │   │   ├── VolunteerController.java      # 志愿者（用户端）
│   │   │   │   └── VolunteerManageController.java  # 志愿者管理（后台）
├── dto/                      # 数据传输对象（66个）
│   │   │   │   ├── CampPlanDTO.java               # 排课计划DTO
│   │   │   │   ├── CampPlanAddDayDTO.java        # 智能追加排课DTO
│   │   │   │   ├── CampDTO.java                  # 营期DTO
│   │   │   │   ├── HomeworkDetailDTO.java         # 作业详情DTO
│   │   │   │   ├── UserInfoDTO.java              # 用户信息DTO
│   │   │   │   └── ...
├── entity/                   # 实体类（12个）
│   │   │   │   ├── Camp.java                      # 营期实体
│   │   │   │   ├── CampPlan.java                  # 排课计划实体
│   │   │   │   ├── CampType.java                  # 营期类型实体
│   │   │   │   ├── Course.java                    # 课程实体
│   │   │   │   ├── DutyApplication.java          # 值班申请实体
│   │   │   │   ├── DutyAssignment.java           # 值班分配实体
│   │   │   │   ├── GroupChat.java                # 群聊实体
│   │   │   │   ├── GroupChatMember.java          # 群聊成员实体
│   │   │   │   ├── Message.java                  # 消息实体
│   │   │   │   ├── PlanTask.java                 # 计划任务实体
│   │   │   │   ├── User.java                     # 用户实体
│   │   │   │   ├── UserDailyRecord.java         # 用户每日记录实体
│   │   │   │   └── ...
│   │   │   ├── event/                   # 事件
│   │   │   │   └── CampProgressUpdateEvent.java  # 营期进度更新事件
│   │   │   ├── interceptor/             # 拦截器
│   │   │   │   ├── AdminAuthInterceptor.java     # 管理员权限拦截器
│   │   │   │   └── AuthInterceptor.java          # 用户认证拦截器
│   │   │   ├── listener/                # 事件监听器
│   │   │   │   └── CampProgressUpdateListener.java  # 营期进度更新监听器
├── mapper/                  # 数据访问层（21个）
│   │   │   │   ├── CampMapper.java               # 营期Mapper
│   │   │   │   ├── CampPlanMapper.java          # 排课计划Mapper
│   │   │   │   ├── CourseMapper.java            # 课程Mapper
│   │   │   │   ├── DutyApplicationMapper.java   # 值班申请Mapper
│   │   │   │   ├── EnrollmentMapper.java        # 报名Mapper
│   │   │   │   ├── HomeworkMapper.java          # 作业Mapper
│   │   │   │   ├── UserMapper.java              # 用户Mapper
│   │   │   │   └── ...
├── service/                 # 服务层接口（19个）
│   │   │   │   ├── CampPlanService.java         # 排课服务接口
│   │   │   │   ├── CampService.java             # 营期服务接口
│   │   │   │   ├── CourseService.java           # 课程服务接口
│   │   │   │   ├── HomeworkService.java         # 作业服务接口
│   │   │   │   ├── UserService.java             # 用户服务接口
│   │   │   │   └── ...
├── service/impl/           # 服务层实现（17个）
│   │   │   │   ├── CampPlanServiceImpl.java    # 排课服务实现
│   │   │   │   ├── CampServiceImpl.java       # 营期服务实现
│   │   │   │   ├── CourseServiceImpl.java     # 课程服务实现
│   │   │   │   ├── HomeworkServiceImpl.java   # 作业服务实现
│   │   │   │   └── ...
│   │   │   ├── util/                    # 工具类
│   │   │   │   └── JwtUtils.java                  # JWT工具类
├── vo/                       # 视图对象（17个）
│   │   │   │   ├── AdminDutyApplicationListItemVO.java
│   │   │   │   ├── AdminDutyApplicationStatsVO.java
│   │   │   │   ├── AdminListItemVO.java
│   │   │   │   └── DutyApplicationVO.java
│   │   │   └── DailyChineseCultureApplication.java  # 应用启动类
│   │   └── resources/
│   │       ├── application.yml                  # 应用配置文件
│   │       └── mapper/                           # MyBatis XML映射文件
│   │           ├── CampPlanMapper.xml
│   │           ├── CampMapper.xml
│   │           ├── CourseMapper.xml
│   │           └── ...
│   └── test/                            # 测试代码
├── doc/                                # 开发文档
├── .qoder/repowiki/                    # Qoder Wiki文档
├── pom.xml                             # Maven配置文件
└── README.md                           # 项目说明文件
```

## 核心功能模块

### 1. 用户认证模块
- 用户注册与登录
- JWT令牌生成与验证
- 密码加密存储
- 用户会话管理
- 多端身份切换（学员/志愿者/管理员）

### 2. 课程管理模块
- 课程信息展示
- 我的课程列表查询
- 课程学习进度跟踪
- 热门课程推荐
- 课程分类管理
- 最近活跃课程

### 3. 营期管理模块
- 营期信息管理（CRUD）
- 营期类型管理
- 营期报名/取消报名
- 营期统计分析
- 营期生命周期管理

### 4. 教务排课模块【★ 2026-04-05 升级】
- 排课时间轴管理
- 一键生成日历框架
- 单日课表聚合保存
- 智能追加排课（前端推算+后端落库）
- 并发防御机制（唯一索引冲突处理）
- 周主题智能继承支持

### 5. 作业管理模块
- 作业详情查询
- 作业层级结构
- 作业评选与管理
- 统计分析

### 6. 志愿者管理模块
- 志愿者服务统计
- 志愿者历史记录
- 志愿者职责管理
- 值班申请与审核

### 7. 值班申请模块
- 用户端值班申请提交
- 管理员审核值班申请
- 值班分配管理
- 值班统计

### 8. 群聊管理模块
- 群聊信息管理
- 群聊成员管理
- 消息管理

### 9. 仪表盘模块
- 管理员数据看板
- 学员端快捷入口
- 数据统计分析

## 数据库设计

### 主要数据表
1. **用户表 (users)** - 存储用户基本信息
2. **营期表 (t_camp)** - 存储营期信息
3. **营期类型表 (t_camp_type)** - 存储营期类型字典
4. **排课计划表 (t_camp_plan)** - 存储每日排课计划
5. **任务表 (t_plan_task)** - 存储每日任务
6. **报名表 (t_enrollment)** - 存储用户报名记录
7. **值班申请表 (t_duty_application)** - 存储值班申请
8. **值班分配表 (t_duty_assignment)** - 存储值班分配
9. **作业表 (t_homework)** - 存储作业信息
10. **群聊表 (t_group_chat)** - 存储群聊信息
11. **群聊成员表 (t_group_chat_member)** - 存储群聊成员
12. **消息表 (t_message)** - 存储消息记录
13. **用户每日记录表 (t_user_daily_record)** - 存储用户每日学习记录
14. **用户任务记录表 (t_user_task_record)** - 存储用户任务完成记录

### 数据库特性
- 使用MySQL 8.0数据库
- **唯一索引约束**：`uk_camp_day`（营期+天数）、`uk_camp_date`（营期+日期）
- 支持事务处理和数据一致性
- 逻辑删除支持（is_deleted字段）

## API接口概览

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/wx-login` - 微信登录

### 营期管理
- `GET /api/admin/camps` - 营期列表
- `GET /api/admin/camps/{id}` - 营期详情
- `POST /api/admin/camps` - 新增营期
- `PUT /api/admin/camps` - 更新营期
- `DELETE /api/admin/camps/{id}` - 删除营期

### 教务排课
- `GET /api/admin/camp-plans` - 获取排课时间轴
- `POST /api/admin/camp-plans/generate` - 一键生成日历
- `POST /api/admin/camp-plans/add-smart-day` - 智能追加排课【新增】
- `PUT /api/admin/camp-plans/save-day` - 聚合保存单日课表

### 作业管理
- `GET /api/admin/homework` - 作业列表
- `GET /api/admin/homework/{id}` - 作业详情

### 志愿者管理
- `GET /api/admin/volunteers` - 志愿者列表
- `GET /api/admin/volunteers/stats` - 志愿者统计

## 项目特点

### 架构优势
- **分层架构**: 严格按照Controller-Service-Mapper三层架构设计
- **前后端分离**: 提供标准RESTful API接口
- **统一响应格式**: 所有API返回统一的ResponseResult格式
- **全局异常处理**: 集中处理系统异常，提供友好的错误信息
- **事件驱动**: 使用Spring Event实现营期进度更新解耦

### 安全特性
- JWT无状态认证机制
- 密码BCrypt加密存储
- 请求参数校验（Jakarta Validation）
- SQL注入防护
- 拦截器权限控制

### 开发规范
- 完整的单元测试覆盖
- 详细的API文档说明
- 规范的代码注释
- 标准的Git版本控制

## 部署与运行

### 环境要求
- JDK 21
- MySQL 8.0数据库
- Maven 3.6+

### 启动步骤
1. 配置数据库连接信息（application.yml）
2. 执行数据库初始化脚本
3. 运行Maven构建命令：`./mvnw clean package`
4. 启动Spring Boot应用：`java -jar target/DailyChineseCulture-0.0.1-SNAPSHOT.jar`

### 测试验证
项目包含完整的测试套件，可验证各功能模块的正确性。

## 项目状态

**当前版本**: v0.0.1-SNAPSHOT
**最后更新**: 2026-04-08

当前项目已完成核心功能开发，包括用户认证、课程管理、营期管理、教务排课、作业管理、志愿者管理、值班申请、群聊管理等主要模块，具备完整的测试用例和文档说明。

### 最近更新 (2026-04-05)
- **智能追加排课接口**：`POST /api/admin/camp-plans/add-smart-day`
  - 前端智能推算完整数据，后端仅负责落库
  - 支持周主题（moduleName、moduleIndex）智能继承
  - 讲师姓名（teacherName）可选字段
- **并发防御机制**
  - 数据库唯一索引：`uk_camp_day`、`uk_camp_date`
  - 全局异常处理：捕获 `DuplicateKeyException`
  - 友好错误提示："该营期的当前天数或日期已被占用"
- **CampPlanDTO 升级**
  - 新增 `moduleName`、`moduleIndex`、`teacherName` 字段
  - 支持前端周主题智能推算功能
