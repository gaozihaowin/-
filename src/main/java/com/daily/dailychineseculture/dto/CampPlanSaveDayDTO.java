package com.daily.dailychineseculture.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "单日排课聚合保存请求")
public class CampPlanSaveDayDTO {

    @NotNull(message = "计划ID不能为空")
    @Schema(description = "计划ID", example = "1")
    private Integer id;

    @NotNull(message = "营期ID不能为空")
    @Schema(description = "营期ID", example = "101")
    private Integer campId;

    @Schema(description = "第几天", example = "1")
    private Integer dayNum;

    @NotBlank(message = "标题不能为空")
    @Schema(description = "单日标题", example = "第1天：心学导论与立志")
    private String title;

    @Schema(description = "任务列表")
    private List<CampTask> tasks;

    @Data
    @Schema(description = "任务信息")
    public static class CampTask {

        @Schema(description = "任务ID（新增时为null）", example = "10")
        private Integer taskId;

        @NotBlank(message = "任务类型不能为空")
        @Schema(description = "任务类型", example = "READ", allowableValues = {"READ", "VIDEO", "HOMEWORK", "EXTRA"})
        private String taskType;

        @NotBlank(message = "任务名称不能为空")
        @Schema(description = "任务名称", example = "原典精读：经典篇目")
        private String taskName;

        @Schema(description = "任务说明", example = "请认真阅读以下经典篇目")
        private String taskDesc;

        @Schema(description = "资源链接", example = "http://localhost:8080/uploads/videos/xxx.mp4")
        private String taskUrl;

        @Schema(description = "建议时长（分钟）", example = "30")
        private Integer duration;

        @Schema(description = "是否必修（1必修，0选修）", example = "1")
        private Integer isRequired;

        @Schema(description = "排序序号", example = "1")
        private Integer sortOrder;
    }
}