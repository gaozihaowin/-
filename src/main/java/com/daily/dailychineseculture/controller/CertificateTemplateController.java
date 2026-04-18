package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.entity.CertificateTemplate;
import com.daily.dailychineseculture.service.CertificateTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/certificate-template")
public class CertificateTemplateController {

    @Autowired
    private CertificateTemplateService templateService;

    @GetMapping("/list")
    public ResponseResult<List<CertificateTemplate>> getAllTemplates() {
        List<CertificateTemplate> templates = templateService.getAllTemplates();
        return ResponseResult.success(templates);
    }

    @GetMapping("/list-by-type")
    public ResponseResult<List<CertificateTemplate>> getTemplatesByCampType(@RequestParam String campType) {
        List<CertificateTemplate> templates = templateService.getTemplatesByCampType(campType);
        return ResponseResult.success(templates);
    }

    @GetMapping("/detail")
    public ResponseResult<CertificateTemplate> getTemplateById(@RequestParam Integer templateId) {
        CertificateTemplate template = templateService.getTemplateById(templateId);
        if (template == null) {
            return ResponseResult.error("模板不存在");
        }
        return ResponseResult.success(template);
    }

    @PostMapping("/upload")
    public ResponseResult<String> uploadTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("campType") String campType) {

        if (file == null || file.isEmpty()) {
            return ResponseResult.error(400, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return ResponseResult.error(400, "文件名无效");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!extension.matches("jpg|jpeg|png")) {
            return ResponseResult.error(400, "仅支持 jpg、jpeg、png 格式");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseResult.error(400, "文件大小不能超过10MB");
        }

        String uploadDir = "/uploads/certificates/templates";
        Path targetPath = Paths.get(uploadDir);
        if (!Files.exists(targetPath)) {
            try {
                Files.createDirectories(targetPath);
            } catch (IOException e) {
                return ResponseResult.error(500, "目录创建失败：" + e.getMessage());
            }
        }

        String newFileName = "template_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path destPath = targetPath.resolve(newFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return ResponseResult.error(500, "文件保存失败：" + e.getMessage());
        }

        String imageUrl = "/uploads/certificates/templates/" + newFileName;
        int rows = templateService.addTemplate(name, imageUrl, campType);

        if (rows > 0) {
            return ResponseResult.success("模板上传成功", imageUrl);
        } else {
            return ResponseResult.error("模板保存失败");
        }
    }

    @PostMapping("/delete")
    public ResponseResult<Void> deleteTemplate(@RequestParam Integer templateId) {
        boolean success = templateService.deleteTemplate(templateId);
        if (success) {
            return ResponseResult.success("删除成功", null);
        } else {
            return ResponseResult.error("删除失败");
        }
    }
}