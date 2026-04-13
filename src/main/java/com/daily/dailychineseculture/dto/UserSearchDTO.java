package com.daily.dailychineseculture.dto;

import lombok.Data;

@Data
public class UserSearchDTO {
    private Long userId;
    private String account;
    private String nickname;
    private String avatar;
}