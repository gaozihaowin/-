package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 通用文件上传控制器
 */
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.max-size:5242880}")
    private long maxSize;

    /**
     * 上传图片接口
     * 
     * @param file 上传的文件
     * @param request HTTP 请求对象（用于获取用户信息）
     * @return 包含图片访问 URL 的响应结果
     */
    @PostMapping("/upload")
    public ResponseResult<String> uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // 0. 获取当前登录用户 ID（由拦截器设置）
        Long userId = (Long) request.getAttribute("userId");
        System.out.println("用户上传头像，userId: " + userId);
        
        // 1. 参数校验
        if (file == null || file.isEmpty()) {
            return ResponseResult.error(400, "上传文件不能为空");
        }

        // 2. 获取原始文件名和后缀
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return ResponseResult.error(400, "文件名无效");
        }

        // 3. 提取文件后缀并转换为小写
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        
        // 4. 校验文件类型（只允许 jpg/png/jpeg）
        if (!"jpg".equals(extension) && !"png".equals(extension) && !"jpeg".equals(extension)) {
            return ResponseResult.error(400, "不支持的文件类型，仅支持 jpg、png、jpeg 格式");
        }

        // 5. 校验文件大小
        if (file.getSize() > maxSize) {
            return ResponseResult.error(400, "文件大小超过限制（最大 5MB）");
        }

        try {
            // 6. 创建上传目录（如果不存在）
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    return ResponseResult.error(500, "创建上传目录失败");
                }
            }

            // 7. 生成新的文件名（UUID + 时间戳 + 原后缀）
            String timestamp = String.valueOf(System.currentTimeMillis());
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String newFileName = timestamp + "_avatar_" + uuid + "." + extension;

            // 8. 构建完整的文件路径
            Path filePath = Paths.get(uploadDir + newFileName);

            // 9. 保存文件到本地硬盘
            file.transferTo(filePath);

            // 10. 构建完整的 HTTP 访问路径
            String accessUrl = "http://localhost:8080/uploads/" + newFileName;

            // 11. 返回成功响应
            return ResponseResult.success("上传成功", accessUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseResult.error(500, "文件上传失败：" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error(500, "服务器内部错误：" + e.getMessage());
        }
    }
}
