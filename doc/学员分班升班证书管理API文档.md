# 学员分班、升班、证书管理及小程序端 API 文档

## 一、Web端分班管理

### 1.1 获取待分班学员列表

**接口地址**: `GET /api/admin/class/unassigned-students`

**接口描述**: 获取指定营期下未分配班级的学员列表

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| campId | Integer | 是 | 营期ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "获取待分班学员失败: xxx",
  "data": [
    {
      "user_id": 2026000001,
      "account": "user001",
      "nickname": "张三",
      "region": "北京市",
      "gender": 1,
      "phone": "13800138000",
      "class_id": null
    }
  ]
}
```

**字段说明**:
| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | Long | 用户ID |
| account | String | 账户名 |
| nickname | String | 昵称/姓名 |
| region | String | 地域（格式：直辖市为`北京市`，其他为`浙江省 杭州市`） |
| gender | Integer | 性别: 0未知, 1男, 2女 |
| phone | String | 手机号 |
| class_id | Integer | 班级ID（null表示未分班）|

---

### 1.2 获取班级列表

**接口地址**: `GET /api/admin/class/camp-classes`

**接口描述**: 获取指定营期下的所有班级列表

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| campId | Integer | 是 | 营期ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "classId": 1,
      "name": "第1班",
      "campName": "诚意班69期",
      "campId": 10
    }
  ]
}
```

---

### 1.3 执行分班（按班级数量）

**接口地址**: `POST /api/admin/class/assign-by-count`

**接口描述**: 按照设定的班级数量，对待分班学员进行自动分班。采用**地域优先、平均分配**原则。

**地域格式规范**:
- 格式：`省 + [空格] + 市`，例如 `北京市`、`浙江省 杭州市`、`广东省 深圳市`
- 直辖市（北京市、天津市、上海市、重庆市）不加"市"后缀
- 其他省市：省和市之间有空格，如 `浙江省 杭州市`
- 前端通过省/市两级联动选择器统一格式，确保数据一致性
- 地域数据来自小程序端 `china-area.js`，全国省市标准格式

**分班算法说明**:
1. **地域优先原则**：同一地区的学员优先分配到同一班级
2. **平均分配原则**：在各地区学员数不均衡时，按班级数量平均分配
3. **算法流程**：
   - 按地区对学员进行分组
   - 同一地区学员优先进入同一班级
   - 各班学员数尽量保持均衡

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "campId": 10,
  "classCount": 5
}
```

**请求字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| campId | Integer | 是 | 营期ID |
| classCount | Integer | 是 | 班级数量 |

**响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "campId": 10,
    "totalStudents": 50,
    "classCount": 5,
    "avgPerClass": 10,
    "classes": [
      {
        "classId": 1,
        "className": "第1班",
        "studentCount": 10,
        "students": [
          {
            "userId": 2026000001,
            "nickname": "张三",
            "region": "北京市",
            "gender": 1
          }
        ]
      }
    ]
  }
}
```

**业务逻辑**:
1. 先重置该营期所有学员的班级ID（清空历史分班）
2. 删除该营期所有班级
3. 获取待分班学员列表
4. 按平均分配原则将学员分配到各班级
5. 班级名称格式："第X班"

---

### 1.4 获取营期下所有学员

**接口地址**: `GET /api/admin/class/students`

**接口描述**: 获取指定营期下的所有学员列表（不分是否已分班）

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| campId | Integer | 是 | 营期ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "user_id": 2026000001,
      "account": "user001",
      "nickname": "张三",
      "region": "北京市",
      "gender": 1,
      "phone": "13800138000",
      "class_id": 1,
      "class_name": "第1班"
    }
  ]
}
```

---

### 1.5 移动学员到其他班级

**接口地址**: `POST /api/admin/class/move-student`

**接口描述**: 将学员从当前班级移动到其他班级，或退出到待分班状态

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "userId": 2026000001,
  "campId": 10,
  "newClassId": 3
}
```

**请求字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 学员用户ID |
| campId | Integer | 是 | 营期ID |
| newClassId | Integer | 否 | 目标班级ID，null表示退出到待分班状态 |

**响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

### 1.6 获取学员详情

**接口地址**: `GET /api/admin/class/student-detail`

**接口描述**: 获取指定学员在营期中的详细信息

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户ID |
| campId | Integer | 是 | 营期ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 2026000001,
    "account": "user001",
    "nickname": "张三",
    "region": "北京市",
    "birthday": "1990-01-01",
    "gender": 1,
    "profession": "教师",
    "phone": "13800138000",
    "classId": 1,
    "className": "第1班"
  }
}
```

---

### 1.7 重置分班

**接口地址**: `POST /api/admin/class/reset-assignment`

**接口描述**: 重置指定营期的所有分班信息，所有学员恢复为待分班状态，班级清空

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| campId | Integer | 是 | 营期ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 二、结业升班管理

### 2.1 检查升班资格

**接口地址**: `GET /api/admin/promotion/check`

**接口描述**: 检查学员是否符合升班条件

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户ID |
| campId | Integer | 是 | 营期ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "userId": 2026000001,
    "currentCampId": 10,
    "currentCampName": "诚意班69期",
    "currentTypeId": 1,
    "currentTypeName": "诚意班",
    "nextTypeId": 2,
    "nextTypeName": "正心班",
    "eligible": true,
    "reason": "符合升班条件",
    "progress": 100,
    "totalPlans": 35,
    "submittedHomework": 35,
    "missedConsecutive": 0,
    "lateSubmissions": 2,
    "missedSubmissions": 1,
    "availableCamps": [
      {
        "campId": 20,
        "name": "正心班70期",
        "term": "70"
      }
    ]
  }
}
```

**字段说明**:
| 字段 | 类型 | 说明 |
|------|------|------|
| eligible | Boolean | 是否符合升班条件 |
| reason | String | 不符合条件的原因 |
| progress | Integer | 学习进度百分比 |
| totalPlans | Integer | 总功课计划数 |
| submittedHomework | Integer | 已提交功课数 |
| missedConsecutive | Integer | 连续未提交天数 |
| lateSubmissions | Integer | 不准时交功课次数（超过18:30） |
| missedSubmissions | Integer | 完全未交功课次数 |
| availableCamps | Array | 可升入的下一营期列表 |

---

### 2.2 执行升班

**接口地址**: `POST /api/admin/promotion/promote`

**接口描述**: 将符合条件的学员升入下一营期

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "userId": 2026000001,
  "currentCampId": 10,
  "targetCampId": 20
}
```

**请求字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户ID |
| currentCampId | Integer | 是 | 当前营期ID |
| targetCampId | Integer | 是 | 目标营期ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "升班成功",
  "data": null
}
```

**业务逻辑**:
1. 标记学员在当前营期为已完成
2. 为学员颁发当前营期的结业证书
3. 在目标营期创建新的报名记录

---

### 2.3 批量检查结业

**接口地址**: `POST /api/admin/promotion/batch-check`

**接口描述**: 批量检查营期下所有学员的结业资格，对符合条件的学员自动标记结业并颁发证书

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| campId | Integer | 是 | 营期ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "批量检查完成，共标记5名学员完成营期",
  "data": 5
}
```

---

### 2.4 批量升班（保留原班结构）

**接口地址**: `POST /api/admin/promotion/batch-promote-with-class`

**接口描述**: 批量将当前营期符合升班条件的学员升班到目标营期，同时尽量保留原班级结构。原班学员会优先分配到目标营期对应的班级中。

**业务逻辑**:
1. 获取当前营期所有未结业的学员
2. 检查每位学员的升班资格
3. 获取目标营期的班级信息
4. 按原班级顺序将学员分配到目标营期的对应班级（如果有对应班级）
5. 如果原班级学员超过目标营期班级数量，则循环分配
6. 为每位学员标记当前营期结业、颁发证书，并在目标营期创建报名记录（带班级分配）

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| currentCampId | Integer | 是 | 当前营期ID |
| targetCampId | Integer | 是 | 目标营期ID |

**请求示例**:
```json
{
  "currentCampId": 1,
  "targetCampId": 2
}
```

**响应格式**:
```json
{
  "code": 200,
  "message": "批量升班完成，共转移15名学员到目标营期并保留班级结构",
  "data": 15
}
```

**响应字段说明**:
| 字段 | 类型 | 说明 |
|------|------|------|
| data | Integer | 成功升班的学员数量 |

**前置条件**:
- 目标营期必须已存在班级（否则返回0）
- 学员必须符合升班条件（学习进度100% + 功课提交规则）

**分配规则**:
- 有原班级的学员：按原班级序号取模目标班级数量，分配到对应班级
- 无原班级的学员：分配到当前人数最少的班级

---

## 三、结业证书管理

### 3.1 获取我的证书列表

**接口地址**: `GET /api/certificate/my-list`

**接口描述**: 获取当前登录用户的结业证书列表

**请求头**:
```
Authorization: Bearer <token>
```

**响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "certId": 1,
      "userId": 2026000001,
      "type": "诚意班",
      "number": "CERT-1712345678900-ABCD1234",
      "imageUrl": "/uploads/certificates/cert_xxx.png",
      "issueTime": "2026-04-14T10:00:00",
      "campId": 10,
      "classId": 1,
      "className": "第1班",
      "studentName": "张三"
    }
  ]
}
```

**字段说明**:
| 字段 | 类型 | 说明 |
|------|------|------|
| certId | Integer | 证书ID |
| userId | Long | 用户ID |
| type | String | 证书类型/营期名称 |
| number | String | 证书编号 |
| imageUrl | String | 证书图片URL |
| issueTime | Date | 颁发时间 |
| campId | Integer | 营期ID |
| classId | Integer | 班级ID |
| className | String | 班级名称 |
| studentName | String | 学员姓名 |

---

### 3.2 获取用户证书（管理员用）

**接口地址**: `GET /api/certificate/user`

**接口描述**: 获取指定用户的证书列表（档案管理员使用）

**请求头**:
```
Authorization: Bearer <token>
```

**响应格式**: 同 3.1

---

### 3.3 获取证书详情

**接口地址**: `GET /api/certificate/detail`

**接口描述**: 根据证书ID获取证书详细信息（档案管理员使用）

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| certId | Integer | 是 | 证书ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "certId": 1,
    "userId": 2026000001,
    "type": "诚意班",
    "number": "CERT-1712345678900-ABCD1234",
    "imageUrl": "/uploads/certificates/cert_xxx.png",
    "issueTime": "2026-04-14T10:00:00",
    "campId": 10,
    "classId": 1,
    "className": "第1班",
    "studentName": "张三"
  }
}
```

---

### 3.4 上传证书图片（手动）

**接口地址**: `POST /api/certificate/upload-image`

**接口描述**: 档案管理员手动上传单个学员的证书图片

**请求格式**: `multipart/form-data`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 证书图片文件（jpg、jpeg、png格式，最大10MB） |
| certId | Integer | 是 | 证书ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "证书图片上传成功",
  "data": "/uploads/certificates/cert_1712345678900_xxx.png"
}
```

---

### 3.5 获取营期证书列表

**接口地址**: `GET /api/certificate/list-by-camp`

**接口描述**: 获取指定营期下所有学员的证书列表

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| campId | Integer | 是 | 营期ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "certId": 1,
      "userId": 2026000001,
      "type": "诚意班",
      "number": "CERT-1712345678900-ABCD1234",
      "imageUrl": null,
      "issueTime": "2026-04-14T10:00:00",
      "campId": 10,
      "classId": 1,
      "className": "第1班",
      "studentName": "张三",
      "templateId": 1,
      "isGenerated": 0
    }
  ]
}
```

---

### 3.6 颁发证书（批量）

**接口地址**: `POST /api/certificate/issue`

**接口描述**: 为营期下所有已结业的学员批量颁发证书

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| campId | Integer | 是 | 营期ID |
| templateId | Integer | 否 | 证书模板ID（不传则使用默认模板） |

**响应格式**:
```json
{
  "code": 200,
  "message": "已为5名结业学员颁发证书",
  "data": 5
}
```

---

### 3.7 生成证书图片（批量）

**接口地址**: `POST /api/certificate/generate-images`

**接口描述**: 使用模板为营期下所有已颁发证书的学员自动生成证书图片。图片合成的学员信息包括：学员姓名、班级名称、证书编号、颁发日期

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| campId | Integer | 是 | 营期ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "已生成5张证书图片",
  "data": 5
}
```

---

## 四、证书模板管理（档案管理员）

### 4.1 获取模板列表

**接口地址**: `GET /api/admin/certificate-template/list`

**接口描述**: 获取所有证书模板列表

**响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "templateId": 1,
      "name": "诚意班结业证书模板",
      "imageUrl": "/uploads/certificates/templates/chengyi_template.png",
      "campType": "诚意班",
      "createTime": "2026-04-14T10:00:00"
    }
  ]
}
```

---

### 4.2 按营期类型获取模板

**接口地址**: `GET /api/admin/certificate-template/list-by-type`

**接口描述**: 根据营期类型获取对应的证书模板

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| campType | String | 是 | 营期类型（如：诚意班、正心班） |

**响应格式**: 同 4.1

---

### 4.3 上传证书模板

**接口地址**: `POST /api/admin/certificate-template/upload`

**接口描述**: 档案管理员上传证书模板图片

**请求格式**: `multipart/form-data`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 模板图片文件（jpg、jpeg、png格式，最大10MB） |
| name | String | 是 | 模板名称 |
| campType | String | 否 | 适用营期类型（如：诚意班） |

**响应格式**:
```json
{
  "code": 200,
  "message": "模板上传成功",
  "data": "/uploads/certificates/templates/template_xxx.png"
}
```

---

### 4.4 删除证书模板

**接口地址**: `POST /api/admin/certificate-template/delete`

**接口描述**: 删除指定的证书模板

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| templateId | Integer | 是 | 模板ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 五、小程序端报名

### 5.1 报名营期

**接口地址**: `POST /camp/enroll`

**接口描述**: 小程序用户报名参加营期

**请求头**:
```
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "campId": 10
}
```

**请求字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| campId | Integer | 是 | 营期ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "报名成功",
  "data": null
}
```

**业务规则**:
1. 诚意班可以直接报名
2. 其他营期需持有上一营期的结业证书才能报名
3. 已报名过的营期不可重复报名
4. 已结束的营期不可报名

**错误响应示例**:
```json
{
  "code": 400,
  "message": "报名正心班需要先获得诚意班的结业证书",
  "data": null
}
```

---

### 5.2 检查报名状态

**接口地址**: `GET /enrollment/check`

**接口描述**: 检查当前用户是否已报名指定营期

**请求头**:
```
Authorization: Bearer <token>
```

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| campId | Integer | 是 | 营期ID |

**响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

---

## 六、用户信息管理

### 6.1 更新用户信息

**接口地址**: `POST /user/update`

**接口描述**: 小程序用户完善或修改个人信息

**请求头**:
```
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "nickname": "张三",
  "phone": "13800138000",
  "region": "北京市",
  "birthday": "1990-01-01",
  "profession": "教师",
  "gender": 1
}
```

**请求字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | String | 否 | 昵称/姓名 |
| phone | String | 否 | 手机号 |
| region | String | 否 | 地域（格式：直辖市为`北京市`，其他为`浙江省 杭州市`） |
| birthday | String | 否 | 生日（格式：YYYY-MM-DD） |
| profession | String | 否 | 职业 |
| gender | Integer | 否 | 性别: 0未知, 1男, 2女 |

**响应格式**:
```json
{
  "code": 200,
  "message": "信息保存成功",
  "data": null
}
```

---

### 5.2 获取当前用户信息

**接口地址**: `GET /user/me`

**接口描述**: 获取当前登录用户的详细信息

**请求头**:
```
Authorization: Bearer <token>
```

**响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 2026000001,
    "account": "user001",
    "nickname": "张三",
    "phone": "13800138000",
    "region": "北京市",
    "birthday": "1990-01-01",
    "profession": "教师",
    "gender": 1,
    "avatar": "http://example.com/avatar.jpg"
  }
}
```

---

## 六、营期规则说明

### 6.1 诚意班升班规则（5周/35天）

| 条件 | 阈值 | 说明 |
|------|------|------|
| 学习进度 | 100% | 必须完成全部学习进度 |
| 不准时交功课 | ≤3次 | 提交时间晚于18:30 |
| 不交功课 | ≤2次 | 当天完全未提交 |

**判断逻辑**:
- 不准时交：当天24:00前未提交，或提交时间晚于18:30
- 不交功课：当天完全没有提交记录

### 6.2 其他营期升班规则

暂时沿用"连续3天未提交功课"判断逻辑，后续可配置。

### 6.3 营期报名前置条件

| 营期 | 前置条件 |
|------|---------|
| 诚意班 | 无，可直接报名 |
| 正心班 | 需持有诚意班结业证书 |
| 格物班 | 需持有正心班结业证书 |
| 致知班 | 需持有格物班结业证书 |
| 笃行班 | 需持有致知班结业证书 |
| 印证班 | 需持有笃行班结业证书 |
| 良知班 | 需持有印证班结业证书 |

---

## 七、数据表结构

### 7.1 t_certificate（证书表）

```sql
CREATE TABLE t_certificate (
    cert_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) COMMENT '证书类型/营期名称',
    number VARCHAR(50) COMMENT '证书编号',
    image_url VARCHAR(255) COMMENT '证书图片',
    issue_time DATETIME COMMENT '发放时间',
    FOREIGN KEY (user_id) REFERENCES t_user(user_id)
);
```

### 7.2 t_camp_enrollment（学员档案表）

```sql
CREATE TABLE t_camp_enrollment (
    enroll_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    camp_id INT NOT NULL,
    class_id INT,
    is_completed TINYINT DEFAULT 0,
    UNIQUE KEY uk_user_camp (user_id, camp_id),
    FOREIGN KEY (user_id) REFERENCES t_user(user_id),
    FOREIGN KEY (camp_id) REFERENCES t_camp(camp_id)
);
```

---

## 八、错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误或业务校验失败 |
| 401 | 未登录或登录已过期 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 常见错误消息

| 错误消息 | 说明 |
|----------|------|
| 营期ID无效 | campId为空或小于等于0 |
| 班级数量必须大于0 | classCount为空或小于等于0 |
| 暂无待分班学员 | 该营期下没有未分班的学员 |
| 您已报名过该营期，请勿重复操作 | 重复报名 |
| 当前营期已结束，不可报名 | 营期结束时间已过 |
| 报名正心班需要先获得诚意班的结业证书 | 前置证书条件不满足 |
| 学习进度未达100% | 学习进度未完成 |
| 不准时交功课次数超过3次，不符合升班条件 | 诚意班不准时次数超限 |
| 未交功课次数超过2次，不符合升班条件 | 诚意班不交次数超限 |
| 连续3天未提交功课，不符合升班条件 | 其他营期连续缺勤超限 |
