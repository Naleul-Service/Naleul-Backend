package com.naleul.naleul.domain.goalCategory.dto.request;

import com.naleul.naleul.domain.goalCategory.enums.GoalCategoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@Getter
@NoArgsConstructor
public class GoalCategoryCreateRequest {

    @NotNull(message = "colorId는 필수입니다.")
    private Long colorId;

    @NotBlank(message = "목표 카테고리 이름은 필수입니다.")
    private String goalCategoryName;

    @NotNull(message = "목표 카테고리 상태는 필수입니다.")
    private GoalCategoryStatus goalCategoryStatus;  // Enum 외 값이면 자동으로 400 에러

    @NotNull(message = "시작 날짜는 필수입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  // "2024-01-15" 형식 외 값이면 400 에러
    private LocalDate goalCategoryStartDate;
}