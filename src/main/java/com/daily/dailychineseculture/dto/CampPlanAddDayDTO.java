package com.daily.dailychineseculture.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 智能追加排课请求 DTO
 * 用于前端智能推算后的完整排课数据落库
 */
@Data
public class CampPlanAddDayDTO {

    /**
     * 营期 ID
     * 必填
     */
    @NotNull(message = "营期 ID 不能为空")
    private Integer campId;

    /**
     * 第几天
     * 必填
     */
    @NotNull(message = "第几天不能为空")
    private Integer dayIndex;

    /**
     * 具体日期
     * 必填
     */
    @NotNull(message = "排课日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planDate;

    /**
     * 模块名称
     * 必填
     */
    @NotBlank(message = "模块名称不能为空")
    private String moduleName;

    /**
     * 模块索引（第几周）
     * 必填
     */
    @NotNull(message = "模块索引不能为空")
    private Integer moduleIndex;

    /**
     * 讲师姓名
     * 可选
     */
    private String teacherName;

    /**
     * 导读主题
     * 必填
     */
    @NotBlank(message = "导读主题不能为空")
    private String title;
}
