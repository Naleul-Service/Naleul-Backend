package com.naleul.naleul.domain.task.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskUpdateActualRequest(

        @NotNull(message = "실제 수행 날짜는 필수입니다.")
        LocalDate actualDate,  // 추가

        LocalDateTime actualStartAt,
        LocalDateTime actualEndAt
) {}