package com.daily.dailychineseculture.dto;

import lombok.Data;

@Data
public class HandoverReq {
    private Long oldUserId;
    private Long newUserId;
}