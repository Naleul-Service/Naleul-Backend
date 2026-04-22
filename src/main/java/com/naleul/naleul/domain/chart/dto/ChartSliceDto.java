package com.naleul.naleul.domain.chart.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChartSliceDto {
    private Long id;
    private String name;
    private String colorHex;        // UserColor에서 가져올 색상
    private Long totalMinutes;      // 실제 소요 시간 합계
    private Double percentage;      // 비율 (0~100)
}