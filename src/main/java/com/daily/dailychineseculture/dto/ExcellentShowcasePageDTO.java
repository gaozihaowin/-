package com.daily.dailychineseculture.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExcellentShowcasePageDTO {
    private Long total;
    private List<ExcellentShowcaseDTO> list;
}
