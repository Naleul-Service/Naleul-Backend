package com.naleul.naleul.domain.actualTask.dto.response;

import com.naleul.naleul.domain.actualTask.entity.TaskActual;

import java.time.LocalDateTime;

// TaskActualResponse.java
public record TaskActualResponse(
        Long taskActualId,
        Long taskId,              // nullable
        String taskName,
        Long goalCategoryId,
        String goalCategoryName,
        String goalCategoryColorCode,
        Long generalCategoryId,
        String generalCategoryName,
        String generalCategoryColorCode,
        LocalDateTime actualStartAt,
        LocalDateTime actualEndAt,
        Long actualDurationMinutes
) {
    public static TaskActualResponse from(TaskActual actual) {
        String goalColorCode = actual.getGoalCategory().getUserColor() != null
                ? actual.getGoalCategory().getUserColor().getColorCode()
                : null;
        String generalColorCode = actual.getGeneralCategory().getColor() != null
                ? actual.getGeneralCategory().getColor().getColorCode()
                : null;

        return new TaskActualResponse(
                actual.getTaskActualId(),
                actual.getTask() != null ? actual.getTask().getTaskId() : null,
                actual.getTaskName(),
                actual.getGoalCategory().getGoalCategoryId(),
                actual.getGoalCategory().getGoalCategoryName(),
                goalColorCode,
                actual.getGeneralCategory().getGeneralCategoryId(),
                actual.getGeneralCategory().getGeneralCategoryName(),
                generalColorCode,
                actual.getActualStartAt(),
                actual.getActualEndAt(),
                actual.getActualDurationMinutes()
        );
    }
}