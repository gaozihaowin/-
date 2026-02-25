# 热门课程推荐API接口文档

## 接口概述
为小程序首页提供热门课程推荐数据接口，通过联表查询展示最新的课程信息。

## 接口详情

### 获取热门课程推荐
- **接口路径**: `GET /courses/hot`
- **功能描述**: 联表查询 t_camp 和 t_camp_type，按照 start_time 倒序排列，取最新的 5 条数据

#### 请求示例
```http
GET http://localhost:8080/courses/hot
```

#### 响应格式
```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "id": 106,
            "tag": "热招",
            "type": "诚意班",
            "term": "第70期",
            "title": "【致良知线上课堂】诚意班",
            "count": "0"
        },
        {
            "id": 104,
            "tag": "热招",
            "type": "良知班",
            "term": "第10期",
            "title": "【致良知线上课堂】良知班",
            "count": "0"
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
| id | Integer | 营期ID |
| tag | String | 营销角标 |
| type | String | 班级类型名称 |
| term | String | 期数（格式化为"第X期"） |
| title | String | 课程标题 |
| count | String | 报名人数（格式化为千分位） |

## 技术实现

### 核心组件

#### 1. 数据传输对象 (DTO)
- **文件**: `CampVO.java`
- **包路径**: `com.daily.dailychineseculture.dto`
- **用途**: 专门用于接口返回的数据格式

#### 2. 数据访问层 (Mapper)
- **文件**: `CampMapper.java`
- **包路径**: `com.daily.dailychineseculture.mapper`
- **核心SQL**:
```sql
SELECT c.camp_id AS id,
       c.tag,
       ct.level_name AS type,
       CONCAT('第', c.term, '期') AS term,
       c.name AS title,
       FORMAT(c.enroll_count, 0) AS count
FROM t_camp c
JOIN t_camp_type ct ON c.type_id = ct.type_id
ORDER BY c.start_time DESC
LIMIT 5
```

#### 3. 服务层 (Service)
- **接口**: `CampService.java`
- **实现**: `CampServiceImpl.java`
- **包路径**: `com.daily.dailychineseculture.service`

#### 4. 控制器层 (Controller)
- **文件**: `CampController.java`
- **包路径**: `com.daily.dailychineseculture.controller`
- **基础路径**: `/courses`

### 关键特性

1. **联表查询**: 通过JOIN操作关联t_camp和t_camp_type表
2. **数据格式化**: 
   - 使用`CONCAT('第', c.term, '期')`格式化期数
   - 使用`FORMAT(c.enroll_count, 0)`为人数组添加千分位逗号
3. **统一响应**: 使用Result类统一封装返回格式
4. **排序规则**: 按开营时间倒序排列，确保最新课程优先展示

## 测试验证

### 单元测试
已创建测试类 `HotCourseRecommendationTest.java`，包含：
- 接口调用测试
- 服务层直接调用测试
- 数据完整性验证

### 运行测试
```bash
./mvnw test -Dtest=HotCourseRecommendationTest
```

## 前端对接说明

### 小程序请求示例
```javascript
uni.request({
    url: 'http://localhost:8080/courses/hot',
    method: 'GET',
    success: function(res) {
        if (res.data.code === 200) {
            const courses = res.data.data;
            // 处理课程数据
            console.log('热门课程:', courses);
        }
    }
});
```

### 数据处理建议
- 按返回顺序展示（已按时间倒序）
- 可根据tag字段显示不同的营销标签样式
- count字段已格式化为千分位，可直接展示

## 部署与运行

### 启动应用
```bash
./mvnw spring-boot:run
```

### 访问接口
应用启动后，可通过以下URL访问：
- 热门课程推荐: `http://localhost:8080/courses/hot`

## 注意事项

1. **数据库依赖**: 确保t_camp和t_camp_type表存在且有关联关系
2. **字段映射**: SQL中已完成数据格式化，Java代码无需额外处理
3. **性能优化**: 查询已限制返回5条记录，避免数据量过大
4. **错误处理**: 接口包含基本的异常处理机制

## 后续扩展建议

1. 添加分页查询支持
2. 增加搜索和筛选功能
3. 实现缓存机制提高性能
4. 添加详细的日志记录