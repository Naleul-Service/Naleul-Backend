package com.naleul.naleul.domain.goalCategory.dto.request;

import com.naleul.naleul.domain.goalCategory.enums.GoalCategoryStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class GoalCategoryUpdateRequest {
    private Long colorId;
    private String goalCategoryName;
    private GoalCategoryStatus goalCategoryStatus;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate goalCategoryStartDate;
}