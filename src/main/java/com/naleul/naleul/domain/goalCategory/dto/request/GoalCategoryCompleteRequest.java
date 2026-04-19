package com.naleul.naleul.domain.goalCategory.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class GoalCategoryCompleteRequest {
    private LocalDate goalCategoryEndDate;
    private String achievement;
}
