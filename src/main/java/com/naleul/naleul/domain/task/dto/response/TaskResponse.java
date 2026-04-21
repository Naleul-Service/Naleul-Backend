package com.naleul.naleul.domain.task.dto.response;

import com.naleul.naleul.domain.task.entity.Task;
import com.naleul.naleul.domain.task.entity.TaskActual;
import com.naleul.naleul.domain.task.enums.TaskPriority;

import java.time.LocalDate;
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
        boolean defaultSettingStatus,
        List<String> dayNames,
        List<TaskActualResponse> actuals
) {
    public static TaskResponse from(Task task) {
        List<String> days = task.getTaskDayOfWeeks().stream()
                .map(tdow -> tdow.getDayOfWeek().getDayName())
                .toList();

        String goalColorCode = task.getGoalCategory().getUserColor() != null
                ? task.getGoalCategory().getUserColor().getColorCode()
                : null;

        String generalColorCode = task.getGeneralCategory().getColor() != null
                ? task.getGeneralCategory().getColor().getColorCode()
                : null;

        List<TaskActualResponse> actuals = task.getTaskActuals().stream()
                .map(TaskActualResponse::from)
                .toList();

        return new TaskResponse(
                task.getTaskId(),
                task.getTaskName(),
                task.getTaskPriority(),
                task.getGoalCategory().getGoalCategoryId(),
                task.getGoalCategory().getGoalCategoryName(),
                goalColorCode,
                task.getGeneralCategory().getGeneralCategoryId(),
                task.getGeneralCategory().getGeneralCategoryName(),
                generalColorCode,
                task.getPlannedStartAt(),
                task.getPlannedEndAt(),
                task.getPlannedDurationMinutes(),
                task.isDefaultSettingStatus(),
                days,
                actuals
        );
    }

    public record TaskActualResponse(
            Long taskActualId,
            LocalDate actualDate,
            LocalDateTime actualStartAt,
            LocalDateTime actualEndAt,
            Long actualDurationMinutes
    ) {
        public static TaskActualResponse from(TaskActual actual) {
            return new TaskActualResponse(
                    actual.getTaskActualId(),
                    actual.getActualDate(),
                    actual.getActualStartAt(),
                    actual.getActualEndAt(),
                    actual.getActualDurationMinutes()
            );
        }
    }
}