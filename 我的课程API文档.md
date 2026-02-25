# 我的课程API接口文档

## 接口概述
为小程序"我的课程"页面提供数据接口，支持按不同状态分类展示用户的课程信息。

## 接口详情

### 获取我的课程列表
- **接口路径**: `GET /courses`
- **功能描述**: 根据用户ID和标签类型查询用户的课程列表

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| tabType | Integer | 是 | 标签类型：1-正在学习, 2-历史课程, 3-已结业 |

#### 请求示例
```http
GET http://localhost:8080/courses?userId=1000001&tabType=1
```

#### 响应格式
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 101,
      "status": "ing",
      "statusText": "学习中",
      "type": "诚意班",
      "term": "第69期",
      "title": "【致良知线上课堂】诚意班",
      "updateDate": "2025-11-25",
      "progress": 45
    }
  ]
}
```

#### 响应字段说明
| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应状态码，200表示成功 |
| msg | String | 响应消息 |
| data | Array | 课程数据列表 |

#### 课程对象字段说明
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 课程ID（营期ID） |
| status | String | 状态编码：ing-学习中, hist-已结营, done-已结业 |
| statusText | String | 状态文本描述 |
| type | String | 班级类型名称 |
| term | String | 期数（格式化为"第X期"） |
| title | String | 课程标题 |
| updateDate | String | 更新日期（格式化为yyyy-MM-dd） |
| progress | Integer | 学习进度（0-100） |

## 业务逻辑说明

### 状态判定规则
- **done/已结业**: `e.is_completed = 1`
- **hist/已结营**: `c.status = 2` 且 `e.is_completed = 0`
- **ing/学习中**: 其余情况

### 过滤条件
- **tabType=1 (正在学习)**: `e.is_completed = 0 AND c.status = 1`
- **tabType=2 (历史课程)**: `e.is_completed = 0 AND c.status = 2`
- **tabType=3 (已结业)**: `e.is_completed = 1`

### 数据格式化
- **term**: 使用 `CONCAT('第', c.term, '期')` 拼接
- **updateDate**: 使用 `DATE_FORMAT(e.create_time, '%Y-%m-%d')` 格式化
- **status/statusText**: 使用 `CASE WHEN` 逻辑判定

## 技术实现

### 核心组件

#### 1. 数据传输对象 (DTO)
- **文件**: `MyCourseVO.java`
- **包路径**: `com.daily.dailychineseculture.dto`

#### 2. 数据访问层 (Mapper)
- **文件**: `MyCourseMapper.java`
- **包路径**: `com.daily.dailychineseculture.mapper`
- **核心技术**: MyBatis动态SQL + CASE WHEN条件判断

#### 3. 服务层 (Service)
- **接口**: `CourseService.java`
- **实现**: `CourseServiceImpl.java`
- **包路径**: `com.daily.dailychineseculture.service`

#### 4. 控制器层 (Controller)
- **文件**: `CourseController.java`
- **包路径**: `com.daily.dailychineseculture.controller`

## 测试验证

### 单元测试
已创建测试类 `MyCourseApiTest.java`，包含：
- 正在学习课程查询测试
- 历史课程查询测试
- 已结业课程查询测试
- 参数校验测试

### 运行测试
```bash
./mvnw test -Dtest=MyCourseApiTest
```

## 前端对接说明

### 小程序请求示例
```javascript
// 获取正在学习的课程
uni.request({
    url: 'http://localhost:8080/courses',
    method: 'GET',
    data: {
        userId: 1000001,
        tabType: 1
    },
    success: function(res) {
        if (res.data.code === 200) {
            const courses = res.data.data;
            // 处理课程数据
            console.log('我的课程:', courses);
        }
    }
});
```

### 数据处理建议
- 根据status字段显示不同状态样式
- progress字段可用于进度条展示
- updateDate字段可用于时间展示
- 按返回顺序展示（已按创建时间倒序）

## 错误处理

### 常见错误响应
```json
{
  "code": 500,
  "msg": "参数错误: 用户ID和标签类型不能为空",
  "data": null
}
```

### 错误类型
- 参数为空错误
- 参数范围错误（tabType必须为1、2、3）
- 数据库查询异常

## 部署与运行

### 启动应用
```bash
./mvnw spring-boot:run
```

### 访问接口
应用启动后，可通过以下URL访问：
- 我的课程: `http://localhost:8080/courses?userId={userId}&tabType={tabType}`

## 注意事项

1. **数据库依赖**: 需要t_camp_enrollment、t_camp、t_camp_type三张表
2. **SQL优化**: 所有数据格式化在SQL层完成，Java层无需额外处理
3. **性能考虑**: 查询已按创建时间倒序，便于前端展示
4. **安全建议**: 生产环境应添加用户身份验证机制

## 后续扩展建议

1. 添加分页查询支持
2. 增加课程详情接口
3. 实现课程搜索功能
4. 添加学习记录统计接口