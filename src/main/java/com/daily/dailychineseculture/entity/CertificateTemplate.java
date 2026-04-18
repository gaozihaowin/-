package com.daily.dailychineseculture.entity;

import lombok.Data;
import java.util.Date;

@Data
public class CertificateTemplate {
    private Integer templateId;
    private String name;
    private String imageUrl;
    private String campType;
    private Date createTime;
}