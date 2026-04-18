package com.daily.dailychineseculture.service.impl;

import com.daily.dailychineseculture.entity.Certificate;
import com.daily.dailychineseculture.entity.CertificateTemplate;
import com.daily.dailychineseculture.mapper.CertificateMapper;
import com.daily.dailychineseculture.mapper.CertificateTemplateMapper;
import com.daily.dailychineseculture.service.CertificateImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class CertificateImageServiceImpl implements CertificateImageService {

    @Autowired
    private CertificateMapper certificateMapper;

    @Autowired
    private CertificateTemplateMapper templateMapper;

    @Value("${upload.path:/uploads}")
    private String uploadPath;

    @Override
    public String generateCertificateImage(Certificate certificate) {
        if (certificate == null || certificate.getTemplateId() == null) {
            return null;
        }

        CertificateTemplate template = templateMapper.selectById(certificate.getTemplateId());
        if (template == null || template.getImageUrl() == null) {
            return null;
        }

        String templatePath = template.getImageUrl();
        if (templatePath.startsWith("/")) {
            templatePath = uploadPath + templatePath;
        }

        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            return null;
        }

        try {
            BufferedImage templateImage = ImageIO.read(templateFile);
            int width = templateImage.getWidth();
            int height = templateImage.getHeight();

            Graphics2D g2d = templateImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
            String dateStr = sdf.format(certificate.getIssueTime() != null ? certificate.getIssueTime() : new Date());

            int baseY = height * 3 / 4;

            Font nameFont = new Font("思源宋体", Font.BOLD, Math.max(width / 20, 30));
            Font infoFont = new Font("思源宋体", Font.PLAIN, Math.max(width / 30, 20));
            Font dateFont = new Font("思源宋体", Font.PLAIN, Math.max(width / 35, 18));

            g2d.setColor(Color.BLACK);

            if (certificate.getStudentName() != null) {
                g2d.setFont(nameFont);
                String name = certificate.getStudentName();
                FontMetrics fm = g2d.getFontMetrics();
                int nameX = (width - fm.stringWidth(name)) / 2;
                g2d.drawString(name, nameX, baseY);
            }

            if (certificate.getClassName() != null) {
                g2d.setFont(infoFont);
                String classInfo = "「" + certificate.getClassName() + "」学员";
                FontMetrics fm = g2d.getFontMetrics();
                int infoX = (width - fm.stringWidth(classInfo)) / 2;
                g2d.drawString(classInfo, infoX, baseY + 60);
            }

            g2d.setFont(dateFont);
            String certNum = "证书编号：" + (certificate.getNumber() != null ? certificate.getNumber() : "");
            FontMetrics fm = g2d.getFontMetrics();
            int numX = (width - fm.stringWidth(certNum)) / 2;
            g2d.drawString(certNum, numX, baseY + 100);

            g2d.setFont(dateFont);
            String dateText = "颁发日期：" + dateStr;
            fm = g2d.getFontMetrics();
            int dateX = (width - fm.stringWidth(dateText)) / 2;
            g2d.drawString(dateText, dateX, baseY + 130);

            g2d.dispose();

            String outputDir = uploadPath + "/uploads/certificates/generated";
            Path outputPath = Paths.get(outputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }

            String outputFileName = "cert_" + certificate.getCertId() + "_" + System.currentTimeMillis() + ".jpg";
            String outputPathStr = outputDir + "/" + outputFileName;
            File outputFile = new File(outputPathStr);

            ImageIO.write(templateImage, "jpg", outputFile);

            return "/uploads/certificates/generated/" + outputFileName;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int batchGenerateImages(Integer campId) {
        List<Certificate> certificates = certificateMapper.selectByCampId(campId);
        int count = 0;

        for (Certificate cert : certificates) {
            if (cert.getIsGenerated() != null && cert.getIsGenerated() == 1) {
                continue;
            }
            if (cert.getTemplateId() == null) {
                continue;
            }

            String imageUrl = generateCertificateImage(cert);
            if (imageUrl != null) {
                cert.setImageUrl(imageUrl);
                cert.setIsGenerated(1);
                certificateMapper.updateImageUrl(cert);
                count++;
            }
        }

        return count;
    }
}