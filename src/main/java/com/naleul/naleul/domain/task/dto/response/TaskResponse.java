package com.naleul.naleul.domain.task.dto.response;

import com.naleul.naleul.domain.task.entity.Task;
import com.naleul.naleul.domain.task.entity.TaskActual;
import com.naleul.naleul.domain.task.enums.TaskPriority;

import java.time.LocalDateTime;

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
        TaskActualResponse actual
) {
    public static TaskResponse from(Task task) {
        String goalColorCode = task.getGoalCategory().getUserColor() != null
                ? task.getGoalCategory().getUserColor().getColorCode()
                : null;

        String generalColorCode = task.getGeneralCategory().getColor() != null
                ? task.getGeneralCategory().getColor().getColorCode()
                : null;

        TaskActualResponse actual = task.getTaskActual() != null
                ? TaskActualResponse.from(task.getTaskActual())
                : null;

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
                actual
        );
    }

    public record TaskActualResponse(
            Long taskActualId,
            LocalDateTime actualStartAt,
            LocalDateTime actualEndAt,
            Long actualDurationMinutes
    ) {
        public static TaskActualResponse from(TaskActual actual) {
            return new TaskActualResponse(
                    actual.getTaskActualId(),
                    actual.getActualStartAt(),
                    actual.getActualEndAt(),
                    actual.getActualDurationMinutes()
            );
        }
    }
}