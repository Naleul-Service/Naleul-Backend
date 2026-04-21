package com.naleul.naleul.domain.task.dto.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record TaskDailyRequest(

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,          // 예: 2024-01-15

        String dayOfWeek,         // 예: MON, TUE, WED ...

        Long goalCategoryId,    // 추가
        Long generalCategoryId, // 추가
        String priority         // 추가
) {}