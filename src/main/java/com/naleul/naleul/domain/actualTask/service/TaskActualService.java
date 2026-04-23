package com.naleul.naleul.domain.actualTask.service;

import com.naleul.naleul.domain.actualTask.dto.request.TaskActualCreateRequest;
import com.naleul.naleul.domain.actualTask.dto.request.TaskActualDailyRequest;
import com.naleul.naleul.domain.actualTask.dto.request.TaskUpdateActualRequest;
import com.naleul.naleul.domain.actualTask.dto.response.TaskActualResponse;
import com.naleul.naleul.domain.actualTask.entity.TaskActual;
import com.naleul.naleul.domain.actualTask.repository.TaskActualRepository;
import com.naleul.naleul.domain.generalCategory.entity.GeneralCategory;
import com.naleul.naleul.domain.generalCategory.repository.GeneralCategoryRepository;
import com.naleul.naleul.domain.goalCategory.entity.GoalCategory;
import com.naleul.naleul.domain.goalCategory.repository.GoalCategoryRepository;
import com.naleul.naleul.domain.task.dto.response.TaskResponse;
import com.naleul.naleul.domain.task.entity.Task;
import com.naleul.naleul.domain.task.repository.TaskRepository;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.user.repository.UserRepository;
import com.naleul.naleul.global.common.response.ErrorCode;
import com.naleul.naleul.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskActualService {

    private final TaskActualRepository taskActualRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GoalCategoryRepository goalCategoryRepository;
    private final GeneralCategoryRepository generalCategoryRepository;

    private static final int KST_OFFSET_HOURS = 9;

    // ── 독립 생성 (TimeTable 직접 입력) ───────────────────

    @Transactional
    public TaskActualResponse createActual(Long userId, TaskActualCreateRequest request) {
        User user = findUser(userId);
        GoalCategory goalCategory = findGoalCategory(request.goalCategoryId());
        GeneralCategory generalCategory = findGeneralCategory(request.generalCategoryId());

        Task task = null;
        if (request.taskId() != null) {
            task = taskRepository.findByTaskIdAndUserIdWithDetails(request.taskId(), userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));
        }

        TaskActual actual = TaskActual.builder()
                .task(task)
                .user(user)
                .goalCategory(goalCategory)
                .generalCategory(generalCategory)
                .taskName(request.taskName())
                .actualStartAt(request.actualStartAt())
                .actualEndAt(request.actualEndAt())
                .actualDurationMinutes(calculateMinutes(request.actualStartAt(), request.actualEndAt()))
                .build();

        return TaskActualResponse.from(taskActualRepository.save(actual));
    }

    // ── Task 기반 upsert (PATCH /{taskId}/actual) ─────────

    @Transactional
    public TaskResponse recordActual(Long userId, Long taskId, TaskUpdateActualRequest request) {
        Task task = taskRepository.findByTaskIdAndUserIdWithDetails(taskId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

        TaskActual actual = taskActualRepository
                .findFirstByTaskTaskId(taskId)
                .orElse(TaskActual.builder()
                        .task(task)
                        .user(task.getUser())
                        .goalCategory(task.getGoalCategory())
                        .generalCategory(task.getGeneralCategory())
                        .taskName(task.getTaskName())
                        .build());

        actual.update(request.actualStartAt(), request.actualEndAt());
        taskActualRepository.save(actual);

        return TaskResponse.from(task);
    }

    // ── 일간 조회 (TimeTable 렌더용) ──────────────────────

    public List<TaskActualResponse> getDailyActuals(Long userId, TaskActualDailyRequest request) {
        LocalDateTime kstDayStart = toKstDayStart(request.date());
        LocalDateTime kstDayEnd = toKstDayEnd(request.date());

        return taskActualRepository.findDailyActuals(
                        userId,
                        kstDayStart,
                        kstDayEnd,
                        request.goalCategoryId(),
                        request.generalCategoryId()
                )
                .stream()
                .map(TaskActualResponse::from)
                .toList();
    }

    // ── 조회 헬퍼 ─────────────────────────────────────────

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private GoalCategory findGoalCategory(Long goalCategoryId) {
        return goalCategoryRepository.findById(goalCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_CATEGORY_NOT_FOUND));
    }

    private GeneralCategory findGeneralCategory(Long generalCategoryId) {
        return generalCategoryRepository.findById(generalCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.GENERAL_CATEGORY_NOT_FOUND));
    }

    // ── 날짜/시간 유틸 ────────────────────────────────────

    private LocalDateTime toKstDayStart(LocalDate date) {
        return date.atStartOfDay().minusHours(KST_OFFSET_HOURS);
    }

    private LocalDateTime toKstDayEnd(LocalDate date) {
        return date.plusDays(1).atStartOfDay().minusHours(KST_OFFSET_HOURS);
    }

    private Long calculateMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return null;
        return Duration.between(start, end).toMinutes();
    }
}