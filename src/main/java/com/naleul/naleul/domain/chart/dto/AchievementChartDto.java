package com.naleul.naleul.domain.chart.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AchievementChartDto {
    private Long totalCount;
    private Long achievedCount;
    private Long unachievedCount;
    private Double achievementRate; // 0~100, 소수점 1자리
}