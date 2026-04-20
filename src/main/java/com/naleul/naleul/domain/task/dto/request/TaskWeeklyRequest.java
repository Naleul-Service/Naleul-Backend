package com.naleul.naleul.domain.task.dto.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record TaskWeeklyRequest(

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,   // 예: 2024-01-15 (월요일)

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate      // 예: 2024-01-21 (일요일)
) {}