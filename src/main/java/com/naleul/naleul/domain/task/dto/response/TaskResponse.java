package com.naleul.naleul.domain.task.dto.response;


import com.naleul.naleul.domain.task.entity.Task;
import com.naleul.naleul.domain.task.enums.TaskPriority;

import java.time.LocalDateTime;
import java.util.List;

public record TaskResponse(
        Long taskId,
        String taskName,
        TaskPriority taskPriority,
        Long goalCategoryId,
        String goalCategoryName,
        String goalCategoryColorCode,
        Long generalCategoryId,
        String generalCategoryName,
        String generalCategoryColorCode,
        LocalDateTime plannedStartAt,
        LocalDateTime plannedEndAt,
        Long plannedDurationMinutes,
        LocalDateTime actualStartAt,
        LocalDateTime actualEndAt,
        Long actualDurationMinutes,
        boolean defaultSettingStatus,
        List<String> dayNames // ["MONDAY", "WED", "FRI"]
) {
    public static TaskResponse from(Task task) {
        List<String> days = task.getTaskDayOfWeeks().stream()
                .map(tdow -> tdow.getDayOfWeek().getDayName())
                .toList();

        // color가 null일 수 있어서 null-safe하게 처리
        String goalColorCode = task.getGoalCategory().getColor() != null
                ? task.getGoalCategory().getColor().getColorCode()
                : null;

        String generalColorCode = task.getGeneralCategory().getColor() != null
                ? task.getGeneralCategory().getColor().getColorCode()
                : null;

        return new TaskResponse(
                task.getTaskId(),
                task.getTaskName(),
                task.getTaskPriority(),
                task.getGoalCategory().getGoalCategoryId(),
                task.getGoalCategory().getGoalCategoryName(),
                goalColorCode,           // 추가
                task.getGeneralCategory().getGeneralCategoryId(),
                task.getGeneralCategory().getGeneralCategoryName(),
                generalColorCode,
                task.getPlannedStartAt(),
                task.getPlannedEndAt(),
                task.getPlannedDurationMinutes(),
                task.getActualStartAt(),
                task.getActualEndAt(),
                task.getActualDurationMinutes(),
                task.isDefaultSettingStatus(),
                days
        );
    }
}