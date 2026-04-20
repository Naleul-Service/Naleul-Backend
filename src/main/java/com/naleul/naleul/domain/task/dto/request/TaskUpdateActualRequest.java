package com.naleul.naleul.domain.task.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskUpdateActualRequest(

        @NotNull(message = "실제 시작 시간은 필수입니다.")
        LocalDateTime actualStartAt,

        @NotNull(message = "실제 종료 시간은 필수입니다.")
        LocalDateTime actualEndAt
) {}