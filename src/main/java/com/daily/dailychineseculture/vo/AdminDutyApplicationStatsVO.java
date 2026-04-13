package com.daily.dailychineseculture.vo;

import lombok.Data;

/**
 * 审批数据统计VO
 */
@Data
public class AdminDutyApplicationStatsVO {

    /**
     * 总申请数
     */
    private Integer total;

    /**
     * 待审核数
     */
    private Integer pending;

    /**
     * 已通过数
     */
    private Integer passed;

    /**
     * 未通过数
     */
    private Integer rejected;
}
