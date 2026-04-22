package com.naleul.naleul.domain.goalCategory.dto.response;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record CompletedGoalCategoryResponse(
        Long goalCategoryId,
        String goalCategoryName,
        String achievement,
        Long totalActualMinutes,
        Long durationDays,
        Long taskCount
) {
    // record는 canonical constructor(6개 파라미터)가 자동 생성됨
    // JPQL new()용 — startDate/endDate 7개 파라미터 생성자 별도 추가
    public CompletedGoalCategoryResponse(
            Long goalCategoryId,
            String goalCategoryName,
            String achievement,
            Long totalActualMinutes,
            LocalDate startDate,
            LocalDate endDate,
            Long taskCount
    ) {
        // record의 canonical constructor를 this()로 명시 호출
        this(
                goalCategoryId,
                goalCategoryName,
                achievement,
                totalActualMinutes == null ? 0L : totalActualMinutes,
                (startDate != null && endDate != null)
                        ? ChronoUnit.DAYS.between(startDate, endDate)
                        : 0L,
                taskCount == null ? 0L : taskCount
        );
    }
}