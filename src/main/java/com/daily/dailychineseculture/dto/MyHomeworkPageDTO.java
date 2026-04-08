package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.List;

@Data
public class MyHomeworkPageDTO {
    private Long total;
    private List<MyHomeworkDTO> list;
}
