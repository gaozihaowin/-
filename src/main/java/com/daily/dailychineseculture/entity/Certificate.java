package com.daily.dailychineseculture.entity;

import lombok.Data;
import java.util.Date;

/**
 * 证书实体类（适配 t_certificate 表）
 */
@Data
public class Certificate {
    /**
     * 证书ID（主键 cert_id）
     */
    private Integer certId;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 证书类型/名称（对应 type 字段）
     */
    private String type;

    /**
     * 证书编号（对应 number 字段）
     */
    private String number;

    /**
     * 证书图片地址（对应 image_url 字段）
     */
    private String imageUrl;

    /**
     * 颁发时间（对应 issue_time 字段）
     */
    private Date issueTime;

    /**
     * 奖项类型（优秀结业奖等）
     */
    private String awardType;

    /**
     * 营期ID
     */
    private Integer campId;

    /**
     * 班级ID
     */
    private Integer classId;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 学员姓名
     */
    private String studentName;

    /**
     * 模板ID
     */
    private Integer templateId;

    /**
     * 是否已生成证书图片
     */
    private Integer isGenerated;
}