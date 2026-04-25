package com.naleul.naleul.domain.chart.service;

import com.naleul.naleul.domain.chart.dto.AchievementChartDto;
import com.naleul.naleul.domain.chart.dto.ChartResponseDto;
import com.naleul.naleul.domain.chart.dto.ChartSliceDto;
import com.naleul.naleul.domain.chart.dto.GoalCategoryChartDto;
import com.naleul.naleul.domain.chart.repository.ChartRepository;
import com.naleul.naleul.domain.chart.repository.projection.AchievementStatProjection;
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

    private final ChartRepository chartRepository;  // TaskRepository → ChartRepository
    private final TaskRepository taskRepository;    // 달성률은 그대로 유지

    public ChartResponseDto getGoalCategoryChart(Long userId) {
        List<CategoryStatProjection> stats = chartRepository.findGoalCategoryStats(userId);
        return buildChartResponse(stats);
    }

    public List<GoalCategoryChartDto> getGoalDetailChart(Long userId) {
        List<GoalGeneralStatProjection> stats = chartRepository.findGoalGeneralCategoryStats(userId);
        // 나머지 로직 동일
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

    public ChartResponseDto getGeneralCategoryChart(Long userId) {
        List<CategoryStatProjection> stats = chartRepository.findGeneralCategoryStats(userId);
        return buildChartResponse(stats);
    }

    // 달성률 — TaskRepository 그대로 유지
    public AchievementChartDto getAchievementChart(Long userId) {
        AchievementStatProjection stat = taskRepository.findAchievementStats(userId);

        long total      = stat.getTotalCount()    != null ? stat.getTotalCount()    : 0L;
        long achieved   = stat.getAchievedCount() != null ? stat.getAchievedCount() : 0L;
        long unachieved = total - achieved;
        double rate     = total == 0 ? 0.0 : Math.round((double) achieved / total * 1000.0) / 10.0;

        return AchievementChartDto.builder()
                .totalCount(total)
                .achievedCount(achieved)
                .unachievedCount(unachieved)
                .achievementRate(rate)
                .build();
    }

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
        return Math.round((double) part / total * 1000.0) / 10.0;
    }
}