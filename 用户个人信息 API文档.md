# 用户个人信息 API 接口文档

## 接口概述

为小程序个人中心页面提供用户基本信息及统计指标数据，包含地区、职业、注册年数和学时等统计信息。

---

## 技术实现

### 1. 接口路径

**GET `/user/info`**

### 2. 请求参数

#### 请求头 (Header)
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | JWT Token，格式：`Bearer {token}` |

#### 请求体 (Body)
无

### 3. 返回数据结构

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "userId": "2026000001",
    "account": "student01",
    "nickname": "微信昵称或默认昵称",
    "avatar": "https://...",
    "currentIdentity": "学员端",
    "statsList": [
      { "label": "地区", "value": "北京" },
      { "label": "职业", "value": "IT 工程师" },
      { "label": "年数", "value": "0" },
      { "label": "学时", "value": "12h" }
    ]
  }
}
```

### 4. 字段说明

#### 4.1 用户基本信息

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| userId | String | 用户 ID | "2026000001" |
| account | String | 账号 | "student01" |
| nickname | String | 昵称（优先显示微信昵称，若无则显示账号） | "张三" |
| avatar | String | 头像 URL（若为空则显示默认头像） | "https://..." |
| currentIdentity | String | 当前身份（学员端/志愿者端） | "学员端" |

#### 4.2 统计指标列表 (statsList)

| 索引 | label | value 说明 | 数据来源 | 示例 |
|------|-------|------------|----------|------|
| 0 | 地区 | 用户地域字段，若为空返回"-" | t_user.region | "北京" |
| 1 | 职业 | 用户职业字段，若为空返回"-" | t_user.profession | "IT 工程师" |
| 2 | 年数 | 注册时间至今的年数（向下取整） | 计算 createTime 至今 | "0" |
| 3 | 学时 | 作业提交次数 × 2，拼接"h" | t_homework 表统计 | "12h" |

---

## 业务逻辑详解

### 1. Token 解析与鉴权

- **拦截器配置**: `/user/info` 接口需要登录认证
- **Token 解析**: 从 `Authorization` 请求头中提取 JWT Token
- **用户 ID 获取**: 解析 Token 中的 `userId` 字段
- **异常处理**: 
  - Token 无效 → 返回 `"无效的 Token"`
  - 用户不存在 → 返回 `"用户不存在"`

### 2. 数据查询流程

```
1. Controller 层 (AuthController.getUserInfo)
   ↓ 解析 Token 获取 userId
   ↓ 调用 Service.getUserProfile(userId)
   
2. Service 层 (UserService.getUserProfile)
   ↓ 查询 t_user 表获取基本信息
   ↓ 判断是否为志愿者身份
   ↓ 统计作业提交次数
   ↓ 计算注册年数
   ↓ 组装 UserProfileDTO
   
3. Mapper 层 (UserMapper)
   ↓ selectById(userId) - 查询用户信息
   ↓ countUserHomework(userId) - 统计作业次数
   ↓ countVolunteerAssignments(userId) - 判断志愿者身份
```

### 3. 核心算法

#### 3.1 年数计算

```java
// 计算从注册时间到现在的年数（向下取整）
long calculateYearsSinceRegistration(Date createTime) {
    if (createTime == null) {
        return 0;
    }
    
    long now = System.currentTimeMillis();
    long registrationTime = createTime.getTime();
    
    // 计算毫秒差值，转换为年（向下取整）
    long millisPerYear = 365L * 24 * 60 * 60 * 1000;
    return (now - registrationTime) / millisPerYear;
}
```

#### 3.2 学时计算

```java
// 学时 = 作业提交次数 × 2
Integer homeworkCount = userMapper.countUserHomework(userId);
int studyHours = (homeworkCount != null ? homeworkCount : 0) * 2;
// 返回值格式："12h"
hoursItem.setValue(studyHours + "h");
```

#### 3.3 空值处理

| 字段 | 空值处理逻辑 |
|------|--------------|
| region | `user.getRegion() != null && !user.getRegion().isEmpty() ? user.getRegion() : "-"` |
| profession | `user.getProfession() != null && !user.getProfession().isEmpty() ? user.getProfession() : "-"` |
| nickname | `user.getNickname() != null ? user.getNickname() : user.getAccount()` |
| avatar | `user.getAvatar() != null && !user.getAvatar().isEmpty() ? user.getAvatar() : "https://img.icons8.com/color/96/person-male.png"` |

---

## 代码文件清单

### 1. DTO 类

#### UserStatsItem.java
```java
/**
 * 用户个人信息统计项
 */
@Data
public class UserStatsItem {
    private String label;  // 统计项标签
    private String value;  // 统计项的值
}
```

#### UserProfileDTO.java
```java
/**
 * 用户个人信息响应 DTO
 */
@Data
public class UserProfileDTO {
    private String userId;           // 用户 ID
    private String account;          // 账号
    private String nickname;         // 昵称
    private String avatar;           // 头像 URL
    private String currentIdentity;  // 当前身份
    private List<UserStatsItem> statsList;  // 统计指标列表
}
```

### 2. Controller 层

#### AuthController.java
```java
/**
 * 获取用户信息接口
 */
@GetMapping("/user/info")
public Result<UserProfileDTO> getUserInfo(@RequestHeader("Authorization") String token) {
    // 1. 解析 Token 获取用户 ID
    Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
    
    // 2. 调用 Service 获取用户个人信息
    UserProfileDTO userProfile = userService.getUserProfile(userId);
    
    // 3. 返回成功响应
    return Result.success(userProfile);
}
```

### 3. Service 层

#### UserService.java
```java
/**
 * 获取用户个人信息（包含统计指标）
 */
public UserProfileDTO getUserProfile(Long userId) {
    // 1. 查询用户基本信息
    User user = userMapper.selectById(userId);
    
    // 2. 创建返回对象并设置基本信息
    UserProfileDTO profileDTO = new UserProfileDTO();
    profileDTO.setUserId(user.getUserId().toString());
    profileDTO.setAccount(user.getAccount());
    profileDTO.setNickname(...);
    profileDTO.setAvatar(...);
    
    // 3. 判断当前身份
    boolean isVolunteer = isVolunteer(userId);
    profileDTO.setCurrentIdentity(isVolunteer ? "志愿者端" : "学员端");
    
    // 4. 组装统计指标列表
    List<UserStatsItem> statsList = new ArrayList<>();
    
    // 4.1 地区
    statsList.add(new UserStatsItem("地区", ...));
    
    // 4.2 职业
    statsList.add(new UserStatsItem("职业", ...));
    
    // 4.3 年数
    long years = calculateYearsSinceRegistration(user.getCreateTime());
    statsList.add(new UserStatsItem("年数", String.valueOf(years)));
    
    // 4.4 学时
    Integer homeworkCount = userMapper.countUserHomework(userId);
    int studyHours = homeworkCount * 2;
    statsList.add(new UserStatsItem("学时", studyHours + "h"));
    
    profileDTO.setStatsList(statsList);
    return profileDTO;
}
```

### 4. Mapper 层

#### UserMapper.java
```java
/**
 * 查询用户统计信息（作业提交次数）
 */
@Select("SELECT COUNT(*) AS homeworkCount FROM t_homework WHERE user_id = #{userId}")
Integer countUserHomework(Long userId);
```

---

## 测试用例

### 单元测试类：UserProfileApiTest.java

```java
@SpringBootTest
public class UserProfileApiTest {
    
    @Test
    public void testGetUserProfile() {
        // 生成测试 Token
        Long testUserId = 2026000001L;
        String token = jwtUtils.generateToken(testUserId, "student01");
        
        // 调用接口
        Result<UserProfileDTO> result = authController.getUserInfo("Bearer " + token);
        
        // 验证响应
        assertEquals(200, result.getCode().intValue());
        
        // 验证数据完整性
        UserProfileDTO profile = result.getData();
        assertNotNull(profile.getUserId());
        assertNotNull(profile.getStatsList());
        assertEquals(4, profile.getStatsList().size());
    }
}
```

### 运行测试

```bash
./mvnw test -Dtest=UserProfileApiTest
```

---

## 常见错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 401 | Token 无效或已过期 | 检查 Token 是否正确，或重新登录获取新 Token |
| 404 | 用户不存在 | 检查 userId 是否正确 |
| 500 | 服务器内部错误 | 查看后端日志排查具体原因 |

---

## 性能优化建议

1. **缓存策略**: 考虑使用 Redis 缓存用户基本信息，减少数据库查询
2. **SQL 优化**: `t_homework` 表的 `user_id` 字段应建立索引
3. **批量查询**: 如需同时展示多个用户信息，建议使用批量查询接口

---

## 前端调用示例

### uni-app 调用示例

```javascript
// 获取用户个人信息
async function getUserProfile() {
  try {
    const token = uni.getStorageSync('token');
    
    const response = await uni.request({
      url: 'http://localhost:8080/user/info',
      method: 'GET',
      header: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    if (response.data.code === 200) {
      const profile = response.data.data;
      
      console.log('用户 ID:', profile.userId);
      console.log('昵称:', profile.nickname);
      console.log('当前身份:', profile.currentIdentity);
      
      // 遍历统计指标
      profile.statsList.forEach(stat => {
        console.log(`${stat.label}: ${stat.value}`);
      });
      
      return profile;
    } else {
      console.error('获取用户信息失败:', response.data.msg);
      return null;
    }
  } catch (error) {
    console.error('请求异常:', error);
    return null;
  }
}
```

---

## 开发时间线

- **需求分析**: 明确统计指标的计算逻辑和返回格式
- **DTO 设计**: 创建 `UserStatsItem` 和 `UserProfileDTO`
- **Mapper 开发**: 新增 `countUserHomework` 方法
- **Service 开发**: 实现 `getUserProfile` 核心业务逻辑
- **Controller 开发**: 更新 `/user/info` 接口
- **测试验证**: 编写单元测试并运行
- **文档编写**: 生成完整 API文档

---

## 总结

本次开发完成了以下核心功能：

✅ **基本信息展示**: userId、account、nickname、avatar  
✅ **身份识别**: 自动判断学员端/志愿者端  
✅ **统计指标组装**: 地区、职业、年数、学时（4 个维度）  
✅ **空值兜底处理**: 所有字段都有默认值处理  
✅ **Token 鉴权**: 基于 JWT 的安全认证  
✅ **异常处理**: 完善的错误捕获和日志记录  

接口已准备就绪，可供前端调用！ 🎉
