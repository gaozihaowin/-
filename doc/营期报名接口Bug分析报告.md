# 营期报名接口 Bug 分析报告

## 一、报错定位

**报错信息**：`{"code":400,"data":null,"message":"当前营期不可报名","timestamp":...}`

**抛出位置**：`CampServiceImpl.enrollCamp()` 第 222 行

---

## 二、抛出异常的具体方法

**文件路径**：`src/main/java/com/daily/dailychineseculture/service/impl/CampServiceImpl.java`

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void enrollCamp(Long userId, Integer campId) {
    if (userId == null) {
        throw new IllegalArgumentException("用户未登录");
    }
    if (campId == null) {
        throw new IllegalArgumentException("campId 不能为空");
    }

    Camp camp = campMapper.selectCampForEnroll(campId);
    if (camp == null) {
        throw new IllegalArgumentException("营期不存在");
    }
    if (camp.getStatus() == null || camp.getStatus() == 2) {
        throw new IllegalArgumentException("当前营期不可报名");
    }

    Integer count = campEnrollmentMapper.countByUserIdAndCampId(userId, campId);
    if (count != null && count > 0) {
        throw new IllegalArgumentException("您已报名过该营期，请勿重复操作");
    }

    try {
        int inserted = campEnrollmentMapper.insertEnrollment(userId, campId);
        if (inserted <= 0) {
            throw new RuntimeException("报名失败");
        }
    } catch (DuplicateKeyException e) {
        throw new IllegalArgumentException("您已报名过该营期，请勿重复操作");
    }

    int updated = campMapper.incrementEnrollCount(campId);
    if (updated <= 0) {
        throw new RuntimeException("更新报名人数失败");
    }
}
```

---

## 三、接口入口 Controller

**文件路径**：`src/main/java/com/daily/dailychineseculture/controller/CampController.java`

```java
@PostMapping("/enroll")
public ResponseResult<Void> enrollCamp(@RequestBody CampEnrollDTO dto, HttpServletRequest request) {
    try {
        if (dto == null || dto.getCampId() == null) {
            return ResponseResult.error(400, "campId 不能为空");
        }
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseResult.error(401, "未登录或登录已过期");
        }
        campService.enrollCamp(userId, dto.getCampId());
        return ResponseResult.success("报名成功", null);
    } catch (IllegalArgumentException e) {
        return ResponseResult.error(400, e.getMessage());
    } catch (Exception e) {
        return ResponseResult.error("报名失败：" + e.getMessage());
    }
}
```

---

## 四、请求参数 DTO

**文件路径**：`src/main/java/com/daily/dailychineseculture/dto/CampEnrollDTO.java`

```java
package com.daily.dailychineseculture.dto;

import lombok.Data;

@Data
public class CampEnrollDTO {
    private Integer campId;
}
```

**说明**：DTO 仅包含一个字段 `campId`。

---

## 五、营期实体类

**文件路径**：`src/main/java/com/daily/dailychineseculture/entity/Camp.java`

```java
package com.daily.dailychineseculture.entity;

import lombok.Data;
import org.apache.ibatis.type.Alias;
import java.util.Date;

@Data
@Alias("Camp")
public class Camp {
    private Integer campId;      // 营期ID
    private Integer typeId;       // 营期类型 ID
    private Integer term;         // 期数
    private String name;          // 营期名称
    private String intro;         // 营期介绍
    private Date startTime;       // 开营时间
    private Date endTime;         // 结营时间
    private Integer status;       // 状态：0 未开始，1 进行中，2 已结束
    private String tag;           // 标签
    private Integer enrollCount;  // 报名人数
}
```

---

## 六、SQL 查询（核心 Bug 在此）

**文件路径**：`src/main/resources/mapper/CampMapper.xml`

```xml
<select id="selectCampForEnroll" resultType="com.daily.dailychineseculture.entity.Camp">
    SELECT camp_id, start_time, end_time
    FROM t_camp
    WHERE camp_id = #{campId}
</select>
```

**问题**：`SELECT` 语句中**没有查询 `status` 字段**，只查了 `camp_id, start_time, end_time`。

---

## 七、⚠️ Bug 根因分析

### 7.1 问题链路

```
enrollCamp() 调用
    → campMapper.selectCampForEnroll(campId)
        → SQL: SELECT camp_id, start_time, end_time  ← 注意：没有查 status！
            → 返回 Camp 对象，status 字段为 null

然后校验：
    → if (camp.getStatus() == null || camp.getStatus() == 2)
        → camp.getStatus() == null  → 条件成立！
            → 抛出 "当前营期不可报名"
```

### 7.2 为什么 selectCampForEnroll 不查 status？

在早期版本中，`status` 是物理字段（数据库有 `c.status` 列）。但后来重构为**实时计算**：

```xml
<!-- 列表查询中，status 是通过 CASE WHEN 实时计算的 -->
CASE
    WHEN NOW() < c.start_time THEN 0
    WHEN NOW() > c.end_time THEN 2
    ELSE 1
END AS status
```

然而 `selectCampForEnroll` 做**单条查询**时，SQL 中并没有加入这个 CASE WHEN 计算，导致 `status` 永远为 `null`。

### 7.3 status 字段的含义（营期状态）

| status 值 | 含义 | 是否可报名 |
|-----------|------|-----------|
| 0 | 未开始（待开课） | ✅ 可以报名 |
| 1 | 进行中 | ✅ 可以报名 |
| 2 | 已结束 | ❌ 不可报名 |

**当前 Bug**：`status == null` 也会触发"不可报名"，但实际上 `null` 应该被视为"可报名"（即 status=0 待开课状态）。

---

## 八、修复建议

### 方案一：修复 SQL 查询（推荐）

在 `selectCampForEnroll` 的 SELECT 中加入 CASE WHEN 计算 status：

```xml
<select id="selectCampForEnroll" resultType="com.daily.dailychineseculture.entity.Camp">
    SELECT
        camp_id,
        start_time,
        end_time,
        CASE
            WHEN NOW() &lt; start_time THEN 0
            WHEN NOW() > end_time THEN 2
            ELSE 1
        END AS status
    FROM t_camp
    WHERE camp_id = #{campId}
</select>
```

### 方案二：修改校验逻辑（兼容旧数据）

如果数据库中 `status` 仍为物理字段，则只需在 SQL 中加入 `status` 列即可，不需要 CASE WHEN。

---

## 九、修复后的校验逻辑预期行为

| camp.startTime | camp.endTime | 计算后 status | 能否报名 |
|----------------|--------------|---------------|---------|
| 未来时间 | 未来时间 | 0（待开课） | ✅ 可以 |
| 过去时间 | 未来时间 | 1（进行中） | ✅ 可以 |
| 过去时间 | 过去时间 | 2（已结束） | ❌ 不可报名 |
| 数据库物理 status=0 | - | 0 | ✅ 可以 |
| 数据库物理 status=1 | - | 1 | ✅ 可以 |
| 数据库物理 status=2 | - | 2 | ❌ 不可报名 |