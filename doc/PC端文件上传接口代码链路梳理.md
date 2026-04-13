# PC 端文件上传 — 后端代码链路梳理

> 整理目的：支撑"课件/资源上传"功能升级，排查"文档类型被错误限制为只能上传图片"的 Bug。
> 整理时间：2026-04-11

---

## 1. 接口路由总览

| 端 | 接口路径 | 说明 |
|----|---------|------|
| PC + 小程序共用 | `POST /common/upload` | 无 `/api` 前缀 |
| PC + 小程序共用 | `POST /api/common/upload` | 带 `/api` 前缀 |

> ⚠️ **重要发现**：当前 `CommonController` 使用 `@RequestMapping({"/common", "/api/common"})` 同时注册了两个路径，**两者的业务逻辑完全相同**，PC 和小程序调用任意一个均可。

---

## 2. Controller 层

**文件：** `CommonController.java`

```java
@RestController
@RequestMapping({"/common", "/api/common"})   // ← 同时支持两个路径
public class CommonController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.max-size:524288000}")
    private long maxSize;

    private static final List<String> ALLOWED_AVATAR_EXTENSIONS = Arrays.asList("jpg", "png", "jpeg");
    private static final List<String> ALLOWED_VIDEO_EXTENSIONS = Arrays.asList("mp4", "mov", "avi");

    @PostMapping("/upload")
    public ResponseResult<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "avatar") String type,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        if (file == null || file.isEmpty()) {
            return ResponseResult.error(400, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return ResponseResult.error(400, "文件名无效");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        String subDir;
        List<String> allowedExtensions;

        if ("avatar".equals(type)) {
            subDir = "images";
            allowedExtensions = ALLOWED_AVATAR_EXTENSIONS;
        } else if ("video".equals(type)) {
            subDir = "videos";
            allowedExtensions = ALLOWED_VIDEO_EXTENSIONS;
        } else {
            return ResponseResult.error(400, "不支持的上传类型，仅支持 avatar 和 video");
        }

        // ⚠️ 核心校验点：文件后缀白名单
        if (!allowedExtensions.contains(extension)) {
            String allowed = String.join(", ", allowedExtensions);
            return ResponseResult.error(400, "不支持的文件类型，仅支持 " + allowed + " 格式");
        }

        // ⚠️ 文件大小校验
        if (file.getSize() > maxSize) {
            return ResponseResult.error(400, "文件大小超过限制（最大 500MB）");
        }

        // 文件写入磁盘
        String newFileName = System.currentTimeMillis() + "_" + type + "_"
                + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path destPath = targetPath.resolve(newFileName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return ResponseResult.error(500, "文件落盘失败：" + e.getMessage());
        }

        String accessUrl = "http://localhost:8080/uploads/" + finalSubDir + "/" + newFileName;
        return ResponseResult.success("上传成功", accessUrl);
    }
}
```

---

## 3. 文件校验逻辑（Bug 定位）

### 3.1 当前白名单定义

| type 参数值 | 允许的后缀 | 存入目录 |
|------------|-----------|---------|
| `avatar`（默认） | `jpg`, `png`, `jpeg` | `images/` |
| `video` | `mp4`, `mov`, `avi` | `videos/` |

### 3.2 ⚠️ Bug 分析：文档类型没有独立通道

**当前逻辑：**
- 只有 `avatar` 和 `video` 两种 `type`
- `avatar` 类型只认 `jpg/png/jpeg`，**不支持 doc/docx/pdf 等文档格式**
- 如果前端传 `type=document` 或其他值，直接返回：`"不支持的上传类型，仅支持 avatar 和 video"`

**触发场景（推测）：**
前端"课件中心"上传文档时，可能：
1. `type` 传了 `"doc"` / `"document"` / `"file"` 等值 → 被 `else` 分支拦截，返回不支持的类型
2. 或者前端没传 `type`，默认为 `"avatar"` → 以图片白名单校验 doc 文件 → 返回不支持的文件类型

**根因总结：**

```
前端传 type=document
        ↓
CommonController 判断：type 既不是 avatar 也不是 video
        ↓
return ResponseResult.error(400, "不支持的上传类型，仅支持 avatar 和 video")
```

---

## 4. Service 层

**结论：当前无独立 Service 层**，文件处理逻辑全在 `CommonController` 中直接实现，包括：
- 目录创建
- 文件名生成（时间戳 + UUID）
- 物理写入（`Files.copy`）
- 访问 URL 拼接

如需复用文件上传逻辑（如在 `CourseMaterialController` 的 `addMaterial` 中调用），建议将这部分抽取为 `FileUploadService`。

---

## 5. 配置文件

**文件：** `application.yml`

```yaml
file:
  upload-dir: ./uploads/
  max-size: 524288000   # 500MB（字节）
```

---

## 6. 静态资源映射

**文件：** `WebConfig.java`

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path);
    }
}
```

**映射关系：** `/uploads/**` → 本地 `./uploads/` 物理目录

---

## 7. 全局异常处理

**文件：** `GlobalExceptionHandler.java`（当前与上传校验无直接关联）

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseResult<String> handleBusinessException(BusinessException e) {
        return ResponseResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseResult<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return ResponseResult.error(HttpStatus.BAD_REQUEST.value(), message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseResult<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ResponseResult.error(HttpStatus.BAD_REQUEST.value(), "请求参数格式错误，请检查数据提交格式");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseResult<String> handleDuplicateKeyException(DuplicateKeyException e) {
        return ResponseResult.error(409, "该营期的当前天数或日期已被占用，请刷新页面获取最新排课进度！");
    }

    @ExceptionHandler(Exception.class)
    public ResponseResult<String> handleException(Exception e) {
        e.printStackTrace();
        return ResponseResult.error("系统内部错误: " + e.getMessage());
    }
}
```

> ⚠️ 注意：`CommonController.uploadFile` 的所有校验都是**主动判断后返回 `ResponseResult.error`**，不走异常机制，因此不受 `GlobalExceptionHandler` 管辖。

---

## 8. Bug 修复建议

### 方案一：在现有接口中新增 `document` 类型（最小改动）

```java
// 在 CommonController 中新增：
private static final List<String> ALLOWED_DOCUMENT_EXTENSIONS =
    Arrays.asList("doc", "docx", "pdf", "txt", "ppt", "pptx", "xls", "xlsx");

// 在 uploadFile 方法的 if-else 链中新增分支：
} else if ("document".equals(type)) {
    subDir = "documents";
    allowedExtensions = ALLOWED_DOCUMENT_EXTENSIONS;
}
```

### 方案二：新增通用 `file` 类型（全类型放行，推荐）

```java
// 新增通用文件上传分支，不做后缀限制
} else if ("file".equals(type)) {
    subDir = "files";
    // 不限制后缀，由业务层自行判断
}
```

### 方案三：抽取 Service，彻底分离上传逻辑

将文件上传逻辑从 Controller 抽取为独立 `FileUploadService`，供 `CommonController` 和 `CourseMaterialController` 等多处复用，业务决定允许的文件类型列表。

---

## 9. 联调注意事项

### ⚠️ 接口路径
- PC 端前端应调用 `POST /api/common/upload`
- 小程序端调用 `POST /common/upload` 或 `POST /api/common/upload`（两者等价）

### ⚠️ 请求格式
```
POST /api/common/upload
Content-Type: multipart/form-data

file: <文件二进制>
type: avatar | video | document  ← 前端需正确传此参数
```

### ⚠️ 响应示例
```json
{
  "code": 200,
  "message": "上传成功",
  "data": "http://localhost:8080/uploads/videos/1744358400000_video_abc123def.mp4"
}
```

### ⚠️ 当前不支持的场景
- `type` 为空时默认为 `avatar`，即图片上传通道
- 不支持 `doc/docx/pdf` 等文档格式（除非修改代码）
- 返回 URL 为 `localhost:8080`，生产环境需替换为实际域名或 OSS 地址
