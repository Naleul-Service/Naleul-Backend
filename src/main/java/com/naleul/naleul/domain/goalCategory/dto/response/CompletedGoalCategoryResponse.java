package com.naleul.naleul.domain.goalCategory.dto.response;

import com.naleul.naleul.domain.generalCategory.dto.response.GeneralCategoryResponse;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record CompletedGoalCategoryResponse(
        Long goalCategoryId,
        String goalCategoryName,
        String achievement,
        Long totalActualMinutes,
        Long durationDays,
        Long taskCount,
        List<GeneralCategoryResponse> generalCategories,
        String colorCode,
        LocalDate startDate,
        LocalDate endDate
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
        this(
                goalCategoryId,
                goalCategoryName,
                achievement,
                totalActualMinutes == null ? 0L : totalActualMinutes,
                (startDate != null && endDate != null)
                        ? ChronoUnit.DAYS.between(startDate, endDate)
                        : 0L,
                taskCount == null ? 0L : taskCount,
                List.of(),
                null,
                startDate,
                endDate
        );
    }
}