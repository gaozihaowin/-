# 仪表盘快捷入口 API文档

## 接口概览

为后台管理系统仪表盘提供动态快捷入口功能，根据当前登录用户的角色返回对应的快捷操作网格。

---

## 1. 获取仪表盘快捷入口列表

### 接口信息
- **请求路径**: `GET /api/admin/dashboard/shortcuts`
- **请求方法**: GET
- **Content-Type**: application/json
- **认证方式**: 需要 Token 认证

### 请求头
```http
Authorization: Bearer <token>
```

### 响应结果
**成功响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": "shortcut_plan",
      "title": "教务排课工作台",
      "icon": "icon-calendar",
      "route": "/camp-plan",
      "bgColor": "#fdfbf7",
      "sortOrder": 1
    },
    {
      "id": "shortcut_type",
      "title": "课程体系大纲",
      "icon": "icon-books",
      "route": "/camp-type",
      "bgColor": "#fdfbf7",
      "sortOrder": 2
    },
    {
      "id": "shortcut_material",
      "title": "课件资源中台",
      "icon": "icon-video-library",
      "route": "/materials",
      "bgColor": "#fdfbf7",
      "sortOrder": 3
    },
    {
      "id": "shortcut_analytics",
      "title": "营期学情雷达",
      "icon": "icon-radar-chart",
      "route": "/analytics",
      "bgColor": "#fdfbf7",
      "sortOrder": 4
    }
  ],
  "timestamp": 1709900000000
}
```

### 字段说明

#### ShortcutDTO 字段
| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | String | 快捷入口唯一标识 | "shortcut_plan" |
| title | String | 显示标题 | "教务排课工作台" |
| icon | String | 图标类名（前端使用） | "icon-calendar" |
| route | String | 前端路由路径 | "/camp-plan" |
| bgColor | String | 背景颜色（十六进制） | "#fdfbf7" |
| sortOrder | Integer | 排序序号（数字越小越靠前） | 1 |

---

## 业务逻辑说明

### 当前实现（硬编码方式）

现阶段采用硬编码方式返回固定列表，专为 **COURSE_ADMIN（课程管理员）** 角色设计。

**返回的 4 个快捷入口**:
1. **教务排课工作台** (`/camp-plan`)
   - 用于快速进入排课管理界面
   - 图标：`icon-calendar`
   
2. **课程体系大纲** (`/camp-type`)
   - 用于快速进入课程体系分类管理
   - 图标：`icon-books`
   
3. **课件资源中台** (`/materials`)
   - 用于快速进入课件资源管理
   - 图标：`icon-video-library`
   
4. **营期学情雷达** (`/analytics`)
   - 用于快速进入数据分析界面
   - 图标：`icon-radar-chart`

### 未来扩展（TODO）

后续可从以下方式实现动态配置：
1. **数据库存储**: 在数据库中建立快捷入口配置表
2. **配置文件**: 通过 `application.yml` 配置不同角色的快捷入口
3. **权限系统**: 根据用户权限动态过滤可访问的快捷入口

---

## 代码实现

### DTO 类：ShortcutDTO.java
```java
package com.daily.dailychineseculture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表盘快捷入口 DTO
 * 用于返回根据角色动态生成的快捷操作网格
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortcutDTO {
    
    /**
     * 快捷入口 ID（唯一标识）
     */
    private String id;
    
    /**
     * 显示标题
     */
    private String title;
    
    /**
     * 图标类名
     */
    private String icon;
    
    /**
     * 前端路由路径
     */
    private String route;
    
    /**
     * 背景颜色
     */
    private String bgColor;
    
    /**
     * 排序序号（数字越小越靠前）
     */
    private Integer sortOrder;
}
```

**技术亮点**:
- ✅ 使用 `@Builder` 模式，方便构建对象
- ✅ 使用 `@Data`、`@NoArgsConstructor`、`@AllArgsConstructor` 简化代码
- ✅ 符合 Java 21 简洁编程风格

### Controller 层：DashboardController.java
```java
package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.dto.ShortcutDTO;
import com.daily.dailychineseculture.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘控制器
 * 提供仪表相关的 RESTful API
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * 获取仪表盘快捷入口列表
     * GET /api/admin/dashboard/shortcuts
     * 
     * @return 统一响应结果，包含快捷入口列表
     */
    @GetMapping("/shortcuts")
    public ResponseResult<List<ShortcutDTO>> getShortcuts() {
        List<ShortcutDTO> shortcuts = dashboardService.getShortcuts();
        return ResponseResult.success("操作成功", shortcuts);
    }
}
```

**技术亮点**:
- ✅ 使用 `@RequiredArgsConstructor` 实现构造器注入
- ✅ 遵循 RESTful 规范，使用 GET 方法
- ✅ 统一响应格式 `ResponseResult`

### Service 层：DashboardServiceImpl.java
```java
package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.dto.ShortcutDTO;
import com.daily.dailychineseculture.service.DashboardService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 仪表盘服务实现类
 * 当前阶段使用硬编码方式返回固定列表
 */
@Service
public class DashboardServiceImpl implements DashboardService {
    
    @Override
    public List<ShortcutDTO> getShortcuts() {
        // 硬编码返回 COURSE_ADMIN 角色的快捷入口列表
        // TODO: 未来可从数据库或配置中动态加载
        return List.of(
            ShortcutDTO.builder()
                .id("shortcut_plan")
                .title("教务排课工作台")
                .icon("icon-calendar")
                .route("/camp-plan")
                .bgColor("#fdfbf7")
                .sortOrder(1)
                .build(),
            
            ShortcutDTO.builder()
                .id("shortcut_type")
                .title("课程体系大纲")
                .icon("icon-books")
                .route("/camp-type")
                .bgColor("#fdfbf7")
                .sortOrder(2)
                .build(),
            
            ShortcutDTO.builder()
                .id("shortcut_material")
                .title("课件资源中台")
                .icon("icon-video-library")
                .route("/materials")
                .bgColor("#fdfbf7")
                .sortOrder(3)
                .build(),
            
            ShortcutDTO.builder()
                .id("shortcut_analytics")
                .title("营期学情雷达")
                .icon("icon-radar-chart")
                .route("/analytics")
                .bgColor("#fdfbf7")
                .sortOrder(4)
                .build()
        );
    }
}
```

**技术亮点**:
- ✅ 使用 Java 21 的 `List.of()` 创建不可变列表
- ✅ 使用 Builder 模式构建对象，代码清晰优雅
- ✅ 添加 TODO 注释，明确未来优化方向

---

## 测试用例

### 测试 1：获取快捷入口列表
```bash
curl -X GET http://localhost:8080/api/admin/dashboard/shortcuts \
  -H "Authorization: Bearer <your_token>"
```

**预期响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": "shortcut_plan",
      "title": "教务排课工作台",
      "icon": "icon-calendar",
      "route": "/camp-plan",
      "bgColor": "#fdfbf7",
      "sortOrder": 1
    },
    ... (其他 3 个快捷入口)
  ]
}
```

### 测试 2：验证数据完整性
```javascript
// 前端 JavaScript 测试示例
async function testShortcuts() {
  const response = await fetch('/api/admin/dashboard/shortcuts', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const { code, msg, data } = await response.json();
  
  // 验证响应码
  console.assert(code === 200, '响应码应为 200');
  
  // 验证数据数量
  console.assert(data.length === 4, '应返回 4 个快捷入口');
  
  // 验证第一个快捷入口
  console.assert(data[0].id === 'shortcut_plan', '第一个应为教务排课工作台');
  console.assert(data[0].sortOrder === 1, '排序应为 1');
  
  // 验证所有必填字段
  data.forEach(shortcut => {
    console.assert(shortcut.id, 'id 不能为空');
    console.assert(shortcut.title, 'title 不能为空');
    console.assert(shortcut.icon, 'icon 不能为空');
    console.assert(shortcut.route, 'route 不能为空');
    console.assert(shortcut.bgColor, 'bgColor 不能为空');
    console.assert(shortcut.sortOrder, 'sortOrder 不能为空');
  });
}
```

---

## 相关文件列表

| 文件路径 | 说明 | 行数 |
|---------|------|------|
| `dto/ShortcutDTO.java` | 快捷入口 DTO | 48 |
| `service/DashboardService.java` | 仪表盘服务接口 | 20 |
| `service/impl/DashboardServiceImpl.java` | 仪表盘服务实现 | 59 |
| `controller/DashboardController.java` | 仪表盘控制器 | 36 |

---

## 前端集成指南

### React/Vue 组件示例
```jsx
// React 示例
function DashboardShortcuts() {
  const [shortcuts, setShortcuts] = useState([]);
  
  useEffect(() => {
    fetch('/api/admin/dashboard/shortcuts', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    .then(res => res.json())
    .then(({ code, data }) => {
      if (code === 200) {
        setShortcuts(data);
      }
    });
  }, []);
  
  return (
    <div className="dashboard-grid">
      {shortcuts.map(shortcut => (
        <div 
          key={shortcut.id}
          className="shortcut-card"
          style={{ backgroundColor: shortcut.bgColor }}
          onClick={() => navigate(shortcut.route)}
        >
          <i className={shortcut.icon}></i>
          <h3>{shortcut.title}</h3>
        </div>
      ))}
    </div>
  );
}
```

---

## 注意事项

### ⚠️ **重要提醒**

1. **鉴权机制**
   - 当前实现未显式添加 `@AdminAuth` 注解
   - 如需鉴权，请在拦截器白名单中排除此路径，或使用项目已有的 Token 验证机制

2. **硬编码限制**
   - 当前为硬编码实现，所有用户看到相同的快捷入口
   - 未来需根据角色动态返回不同的快捷入口列表

3. **排序规则**
   - `sortOrder` 数字越小，显示越靠前
   - 前端应按 `sortOrder` 升序排列

4. **扩展性**
   - 预留了 `id` 字段，便于后续从数据库查询
   - `bgColor` 支持不同模块使用不同背景色

---

## 版本历史

| 版本 | 日期 | 修改内容 | 作者 |
|------|------|----------|------|
| v1.0 | 2026-03-08 | 初始版本，实现硬编码快捷入口 | AI Assistant |

---

## 总结

✅ **已完成**:
1. 创建 `ShortcutDTO` 数据传输对象
2. 实现 `DashboardService` 业务逻辑层
3. 实现 `DashboardController` 控制器层
4. 返回固定的 4 个快捷入口（COURSE_ADMIN 角色专用）

✅ **代码质量**:
- 使用 Java 21 新特性（`List.of()`、Builder 模式）
- 代码简洁优雅，符合最佳实践
- 添加详细注释和 TODO 标记

🎯 **下一步建议**:
- 前端对接并展示快捷入口网格
- 收集用户反馈，优化快捷入口配置
- 未来实现基于角色的动态快捷入口
