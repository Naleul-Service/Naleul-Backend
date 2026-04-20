package com.naleul.naleul.domain.goalCategory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class GoalCategoryCompleteRequest {

    @NotNull(message = "종료 날짜는 필수입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate goalCategoryEndDate;

    private String achievement;
}