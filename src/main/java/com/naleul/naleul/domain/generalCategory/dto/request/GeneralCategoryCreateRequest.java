package com.naleul.naleul.domain.generalCategory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class GeneralCategoryCreateRequest {

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    private String generalCategoryName;

    @NotNull(message = "목표 카테고리 ID는 필수입니다.")
    private Long goalCategoryId;

    private Long colorId; // 색상은 선택
}