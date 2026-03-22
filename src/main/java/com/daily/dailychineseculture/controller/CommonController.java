package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/common", "/api/common"})
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
        System.out.println("用户上传文件，userId: " + userId + ", type: " + type);

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

        if (!allowedExtensions.contains(extension)) {
            String allowed = String.join(", ", allowedExtensions);
            return ResponseResult.error(400, "不支持的文件类型，仅支持 " + allowed + " 格式");
        }

        if (file.getSize() > maxSize) {
            return ResponseResult.error(400, "文件大小超过限制（最大 500MB）");
        }

        String finalSubDir = subDir;
        Path targetPath = Paths.get(uploadDir, finalSubDir);

        if (!Files.exists(targetPath)) {
            try {
                Files.createDirectories(targetPath);
            } catch (IOException e) {
                return ResponseResult.error(500, "目录创建失败：" + e.getMessage());
            }
        }

        String newFileName = System.currentTimeMillis() + "_" + type + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
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