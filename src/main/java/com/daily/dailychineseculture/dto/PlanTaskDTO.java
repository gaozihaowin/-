package com.daily.dailychineseculture.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "任务信息")
public class PlanTaskDTO {

    @Schema(description = "任务ID（新增时为null）", example = "1001")
    private Integer taskId;

    @NotBlank(message = "任务类型不能为空")
    @Pattern(regexp = "^(READ|VIDEO|HOMEWORK|EXTRA)$", message = "任务类型必须是 READ, VIDEO, HOMEWORK, EXTRA 之一")
    @Schema(description = "任务类型", example = "VIDEO", allowableValues = {"READ", "VIDEO", "HOMEWORK", "EXTRA"})
    private String taskType;

    @NotBlank(message = "任务名称不能为空")
    @Schema(description = "任务名称", example = "阳明心学导读视频")
    private String taskName;

    @Schema(description = "任务说明", example = "请认真观看以下视频，理解阳明心学的核心理念")
    private String taskDesc;

    @Schema(description = "资源链接", example = "http://localhost:8080/uploads/video.mp4")
    private String taskUrl;

    @Schema(description = "建议时长（分钟）", example = "30")
    private Integer duration;

    @Schema(description = "是否必修（1必修，0选修）", example = "1")
    private Integer isRequired;

    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;
}