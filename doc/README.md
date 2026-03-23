# 每日中文文化学习平台项目概述

## 项目简介

每日中文文化学习平台是一个基于Spring Boot开发的在线中文学习管理系统，为用户提供中文课程学习、训练营参与、个人学习进度跟踪等功能。系统采用现代化的前后端分离架构，支持用户注册登录、课程管理、学习记录追踪等核心功能。

## 技术架构概述

### 核心技术栈
- **后端框架**: Spring Boot 2.7.0
- **编程语言**: Java 8+
- **构建工具**: Maven
- **数据库**: MySQL 8.0
- **持久层框架**: MyBatis Plus
- **安全认证**: JWT (JSON Web Token)
- **API文档**: RESTful API设计

### 主要技术组件
1. **Spring Boot生态系统**
   - Spring Web MVC (控制器层)
   - Spring Data (数据访问层)
   - Spring Security基础集成
   - Spring Test (单元测试)

2. **数据库相关**
   - MySQL关系型数据库
   - MyBatis Plus ORM框架
   - Druid数据库连接池
   - 数据库序列生成器

3. **安全与认证**
   - JWT令牌认证机制
   - BCrypt密码加密
   - 统一异常处理
   - 全局响应封装

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
│   │   │   │   ├── GlobalExceptionHandler.java    # 全局异常处理器
│   │   │   │   ├── ResponseResult.java            # 统一响应结果封装
│   │   │   │   └── Result.java                    # 基础响应实体
│   │   │   ├── controller/             # 控制器层
│   │   │   │   ├── AuthController.java           # 认证授权控制器
│   │   │   │   ├── CampController.java           # 训练营控制器
│   │   │   │   ├── CourseController.java         # 课程控制器
│   │   │   │   └── UserController.java           # 用户控制器
│   │   │   ├── dto/                    # 数据传输对象
│   │   │   │   ├── CampVO.java                   # 训练营视图对象
│   │   │   │   ├── LoginRequest.java             # 登录请求DTO
│   │   │   │   ├── LoginResult.java              # 登录响应DTO
│   │   │   │   └── MyCourseVO.java               # 我的课程视图对象
│   │   │   ├── entity/                 # 实体类
│   │   │   │   ├── Camp.java                     # 训练营实体
│   │   │   │   └── User.java                     # 用户实体
│   │   │   ├── mapper/                 # 数据访问层
│   │   │   │   ├── CampMapper.java               # 训练营Mapper
│   │   │   │   ├── MyCourseMapper.java           # 我的课程Mapper
│   │   │   │   ├── UserMapper.java               # 用户Mapper
│   │   │   │   └── UserSeqMapper.java            # 用户序列Mapper
│   │   │   ├── service/                # 服务层
│   │   │   │   ├── impl/                         # 服务实现类
│   │   │   │   │   ├── CampServiceImpl.java      # 训练营服务实现
│   │   │   │   │   └── CourseServiceImpl.java    # 课程服务实现
│   │   │   │   ├── CampService.java              # 训练营服务接口
│   │   │   │   ├── CourseService.java            # 课程服务接口
│   │   │   │   ├── IdGeneratorService.java       # ID生成器服务
│   │   │   │   └── UserService.java              # 用户服务接口
│   │   │   ├── util/                   # 工具类
│   │   │   │   └── JwtUtils.java                 # JWT工具类
│   │   │   └── DailyChineseCultureApplication.java  # 应用启动类
│   │   └── resources/
│   │       └── application.yml                   # 应用配置文件
│   └── test/                           # 测试代码
│       └── java/com/daily/dailychineseculture/
│           ├── ApiTestClient.java                # API测试客户端
│           ├── DatabaseStructureTest.java        # 数据库结构测试
│           ├── HotCourseRecommendationTest.java  # 热门课程推荐测试
│           ├── IdGeneratorServiceTest.java       # ID生成器测试
│           ├── LoginFunctionTest.java            # 登录功能测试
│           ├── MyCourseApiTest.java              # 我的课程API测试
│           ├── SimpleDatabaseTest.java           # 简单数据库测试
│           └── DailyChineseCultureApplicationTests.java  # 应用测试类
├── pom.xml                                     # Maven配置文件
├── API接口文档.md                              # API接口文档
├── 我的课程API文档.md                          # 我的课程API文档
├── 热门课程推荐API文档.md                      # 热门课程推荐API文档
├── 数据库代码.txt                              # 数据库建表语句
├── 开发环境准备指南.md                          # 开发环境搭建指南
├── 项目开发状态报告.md                          # 项目开发进度报告
├── 项目运行测试指南.md                          # 项目运行测试指南
├── 环境检查报告.md                              # 环境检查报告
└── 热门课程API开发完成报告.md                  # 热门课程开发报告
```

## 核心功能模块

### 1. 用户认证模块
- 用户注册与登录
- JWT令牌生成与验证
- 密码加密存储
- 用户会话管理

### 2. 课程管理模块
- 课程信息展示
- 我的课程列表查询
- 课程学习进度跟踪
- 热门课程推荐

### 3. 训练营模块
- 训练营信息管理
- 训练营参与报名
- 训练营进度追踪

### 4. 学习记录模块
- 学习历史记录
- 学习时长统计
- 课程完成状态

## 数据库设计

### 主要数据表
1. **用户表 (users)** - 存储用户基本信息
2. **训练营表 (camps)** - 存储训练营信息
3. **用户序列表 (user_seq)** - 用户ID生成序列
4. **课程相关表** - 课程信息及学习记录表

### 数据库特性
- 使用MySQL 8.0数据库
- 采用自定义序列生成器确保ID唯一性
- 支持事务处理和数据一致性

## 项目特点

### 架构优势
- **分层架构**: 严格按照Controller-Service-Mapper三层架构设计
- **前后端分离**: 提供标准RESTful API接口
- **统一响应格式**: 所有API返回统一的ResponseResult格式
- **全局异常处理**: 集中处理系统异常，提供友好的错误信息

### 安全特性
- JWT无状态认证机制
- 密码BCrypt加密存储
- 请求参数校验
- SQL注入防护

### 开发规范
- 完整的单元测试覆盖
- 详细的API文档说明
- 规范的代码注释
- 标准的Git版本控制

## 部署与运行

### 环境要求
- JDK 8或更高版本
- MySQL 8.0数据库
- Maven 3.6+

### 启动步骤
1. 配置数据库连接信息
2. 执行数据库初始化脚本
3. 运行Maven构建命令
4. 启动Spring Boot应用

### 测试验证
项目包含完整的测试套件，可验证各功能模块的正确性，包括：
- 数据库连接测试
- API接口功能测试
- 业务逻辑测试
- 性能压力测试

## 项目状态

当前项目已完成核心功能开发，包括用户认证、课程管理、训练营管理等主要模块，具备完整的测试用例和文档说明，可以作为生产环境部署使用。