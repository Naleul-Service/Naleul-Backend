package com.naleul.naleul.domain.chart.controller;

import com.naleul.naleul.domain.chart.dto.AchievementChartDto;
import com.naleul.naleul.domain.chart.dto.ChartResponseDto;
import com.naleul.naleul.domain.chart.dto.GoalCategoryChartDto;
import com.naleul.naleul.domain.chart.service.ChartService;
import com.naleul.naleul.global.common.response.ApiResponse;
import com.naleul.naleul.global.common.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/charts")
@RequiredArgsConstructor
public class ChartController {

    private final ChartService chartService;

    // 7-1-1: 전체 goalCategory 차트
    @GetMapping("/goal-categories")
    public ResponseEntity<ApiResponse<ChartResponseDto>> getGoalCategoryChart(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.CHART_FOUND, chartService.getGoalCategoryChart(userId))
        );
    }

    // 7-2-1: goalCategory별 generalCategory 차트
    @GetMapping("/goal-categories/detail")
    public ResponseEntity<ApiResponse<List<GoalCategoryChartDto>>> getGoalDetailChart(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.CHART_FOUND, chartService.getGoalDetailChart(userId))
        );
    }

    // 7-3-1: 전체 generalCategory 차트
    @GetMapping("/general-categories")
    public ResponseEntity<ApiResponse<ChartResponseDto>> getGeneralCategoryChart(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.CHART_FOUND, chartService.getGeneralCategoryChart(userId))
        );
    }

    // 계획 달성률 차트
    @GetMapping("/achievement")
    public ResponseEntity<ApiResponse<AchievementChartDto>> getAchievementChart(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.CHART_FOUND, chartService.getAchievementChart(userId))
        );
    }
}