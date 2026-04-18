package com.daily.dailychineseculture.controller;

import com.daily.dailychineseculture.common.ResponseResult;
import com.daily.dailychineseculture.entity.Certificate;
import com.daily.dailychineseculture.service.CertificateService;
import com.daily.dailychineseculture.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/certificate")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/my-list")
    public ResponseResult<List<Certificate>> getMyCertificates(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseResult.error("请先登录");
        }
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(token);
        List<Certificate> certList = certificateService.getMyCertificates(userId);
        return ResponseResult.success(certList);
    }

    @GetMapping("/user")
    public ResponseResult<List<Certificate>> getUserCertificates(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseResult.error("请先登录");
        }
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(token);
        List<Certificate> certList = certificateService.getMyCertificates(userId);
        return ResponseResult.success(certList);
    }

    @GetMapping("/detail")
    public ResponseResult<Certificate> getCertificateDetail(@RequestParam Integer certId) {
        Certificate cert = certificateService.getCertificateById(certId);
        if (cert == null) {
            return ResponseResult.error("证书不存在");
        }
        return ResponseResult.success(cert);
    }

    @PostMapping("/upload-image")
    public ResponseResult<String> uploadCertificateImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("certId") Integer certId,
            HttpServletRequest request) {

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

        String uploadDir = "/uploads/certificates";
        Path targetPath = Paths.get(uploadDir);
        if (!Files.exists(targetPath)) {
            try {
                Files.createDirectories(targetPath);
            } catch (IOException e) {
                return ResponseResult.error(500, "目录创建失败：" + e.getMessage());
            }
        }

        String newFileName = "cert_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path destPath = targetPath.resolve(newFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return ResponseResult.error(500, "文件保存失败：" + e.getMessage());
        }

        String imageUrl = "/uploads/certificates/" + newFileName;
        boolean success = certificateService.uploadCertificateImage(certId, imageUrl);

        if (success) {
            return ResponseResult.success("证书图片上传成功", imageUrl);
        } else {
            return ResponseResult.error("证书不存在，请先颁发证书");
        }
    }

    @GetMapping("/list-by-camp")
    public ResponseResult<List<Certificate>> getCertificatesByCampId(@RequestParam Integer campId) {
        List<Certificate> certificates = certificateService.getCertificatesByCampId(campId);
        return ResponseResult.success(certificates);
    }

    @PostMapping("/issue")
    public ResponseResult<Integer> issueCertificatesForCamp(
            @RequestParam Integer campId,
            @RequestParam(required = false) Integer templateId) {
        int count = certificateService.issueCertificatesForCamp(campId, templateId);
        return ResponseResult.success("已为" + count + "名结业学员颁发证书", count);
    }

    @PostMapping("/generate-images")
    public ResponseResult<Integer> generateCertificateImages(@RequestParam Integer campId) {
        int count = certificateService.generateCertificateImages(campId);
        return ResponseResult.success("已生成" + count + "张证书图片", count);
    }
}