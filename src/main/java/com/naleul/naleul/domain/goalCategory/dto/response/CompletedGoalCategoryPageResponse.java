package com.naleul.naleul.domain.goalCategory.dto.response;

import org.springframework.data.domain.Page;
import java.util.List;

public record CompletedGoalCategoryPageResponse(
        // 통계
        long totalCompletedCount,       // 총 달성한 목표 개수
        long totalActualMinutes,        // 누적 소요시간 (분)
        double averageDurationDays,     // 평균 진행 기간 (일)

        // 페이지 정보
        List<CompletedGoalCategoryResponse> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static CompletedGoalCategoryPageResponse from(Page<CompletedGoalCategoryResponse> page) {
        List<CompletedGoalCategoryResponse> content = page.getContent();

        long totalCompletedCount = page.getTotalElements();

        long totalActualMinutes = content.stream()
                .mapToLong(CompletedGoalCategoryResponse::totalActualMinutes)
                .sum();

        double averageDurationDays = content.stream()
                .mapToLong(CompletedGoalCategoryResponse::durationDays)
                .average()
                .orElse(0.0);

        return new CompletedGoalCategoryPageResponse(
                totalCompletedCount,
                totalActualMinutes,
                averageDurationDays,
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}