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
        Long generalCategoryId,
        String generalCategoryName,
        LocalDateTime plannedStartAt,
        LocalDateTime plannedEndAt,
        Long plannedDurationMinutes,
        LocalDateTime actualStartAt,
        LocalDateTime actualEndAt,
        Long actualDurationMinutes,
        boolean defaultSettingStatus,
        List<String> dayNames // ["MON", "WED", "FRI"]
) {
    public static TaskResponse from(Task task) {
        List<String> days = task.getTaskDayOfWeeks().stream()
                .map(tdow -> tdow.getDayOfWeek().getDayName())
                .toList();

        return new TaskResponse(
                task.getTaskId(),
                task.getTaskName(),
                task.getTaskPriority(),
                task.getGoalCategory().getGoalCategoryId(),
                task.getGoalCategory().getGoalCategoryName(),
                task.getGeneralCategory().getGeneralCategoryId(),
                task.getGeneralCategory().getGeneralCategoryName(),
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