package com.naleul.naleul.domain.chart.service;

import com.naleul.naleul.domain.chart.dto.ChartResponseDto;
import com.naleul.naleul.domain.chart.dto.ChartSliceDto;
import com.naleul.naleul.domain.chart.dto.GoalCategoryChartDto;
import com.naleul.naleul.domain.chart.repository.projection.CategoryStatProjection;
import com.naleul.naleul.domain.chart.repository.projection.GoalGeneralStatProjection;
import com.naleul.naleul.domain.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartService {

    private final TaskRepository taskRepository;

    // 7-1-1: 전체 카테고리 차트
    public ChartResponseDto getGoalCategoryChart(Long userId) {
        List<CategoryStatProjection> stats = taskRepository.findGoalCategoryStats(userId);
        return buildChartResponse(stats);
    }

    // 7-2-1: 목표 카테고리 차트
    public List<GoalCategoryChartDto> getGoalDetailChart(Long userId) {
        List<GoalGeneralStatProjection> stats = taskRepository.findGoalGeneralCategoryStats(userId);

        // goalCategoryId 기준으로 그룹핑
        Map<Long, List<GoalGeneralStatProjection>> grouped = stats.stream()
                .collect(Collectors.groupingBy(GoalGeneralStatProjection::getGoalCategoryId));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<GoalGeneralStatProjection> group = entry.getValue();
                    GoalGeneralStatProjection first = group.get(0);

                    long goalTotal = group.stream()
                            .mapToLong(GoalGeneralStatProjection::getTotalMinutes)
                            .sum();

                    List<ChartSliceDto> slices = group.stream()
                            .map(p -> ChartSliceDto.builder()
                                    .id(p.getGeneralCategoryId())
                                    .name(p.getGeneralCategoryName())
                                    .colorHex(p.getGeneralColorHex())
                                    .totalMinutes(p.getTotalMinutes())
                                    .percentage(calcPercentage(p.getTotalMinutes(), goalTotal))
                                    .build())
                            .toList();

                    return GoalCategoryChartDto.builder()
                            .goalCategoryId(first.getGoalCategoryId())
                            .goalCategoryName(first.getGoalCategoryName())
                            .colorHex(first.getGoalColorHex())
                            .totalMinutes(goalTotal)
                            .generalCategories(slices)
                            .build();
                })
                .toList();
    }

    // 7-3-1: 일반 카테고리 차트
    public ChartResponseDto getGeneralCategoryChart(Long userId) {
        List<CategoryStatProjection> stats = taskRepository.findGeneralCategoryStats(userId);
        return buildChartResponse(stats);
    }

    // ── private 헬퍼 ──────────────────────────────────────

    private ChartResponseDto buildChartResponse(List<CategoryStatProjection> stats) {
        long total = stats.stream().mapToLong(CategoryStatProjection::getTotalMinutes).sum();

        List<ChartSliceDto> slices = stats.stream()
                .map(p -> ChartSliceDto.builder()
                        .id(p.getCategoryId())
                        .name(p.getCategoryName())
                        .colorHex(p.getColorHex())
                        .totalMinutes(p.getTotalMinutes())
                        .percentage(calcPercentage(p.getTotalMinutes(), total))
                        .build())
                .toList();

        return ChartResponseDto.builder()
                .totalMinutes(total)
                .slices(slices)
                .build();
    }

    private double calcPercentage(long part, long total) {
        if (total == 0) return 0.0;
        return Math.round((double) part / total * 1000.0) / 10.0; // 소수점 1자리
    }
}