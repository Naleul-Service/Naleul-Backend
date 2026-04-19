package com.naleul.naleul.domain.goalCategory.dto.request;

import com.naleul.naleul.domain.goalCategory.enums.GoalCategoryStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class GoalCategoryUpdateRequest {
    private Long colorId;
    private String goalCategoryName;
    private GoalCategoryStatus goalCategoryStatus;
    private LocalDate goalCategoryStartDate;
}