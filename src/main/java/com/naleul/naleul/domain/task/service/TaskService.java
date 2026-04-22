package com.naleul.naleul.domain.task.service;

import com.naleul.naleul.domain.generalCategory.entity.GeneralCategory;
import com.naleul.naleul.domain.generalCategory.repository.GeneralCategoryRepository;
import com.naleul.naleul.domain.goalCategory.entity.GoalCategory;
import com.naleul.naleul.domain.goalCategory.repository.GoalCategoryRepository;
import com.naleul.naleul.domain.task.dto.request.*;
import com.naleul.naleul.domain.task.dto.response.TaskMonthlyResponse;
import com.naleul.naleul.domain.task.dto.response.TaskPageResponse;
import com.naleul.naleul.domain.task.dto.response.TaskResponse;
import com.naleul.naleul.domain.task.dto.response.TaskWeeklyResponse;
import com.naleul.naleul.domain.task.entity.Task;
import com.naleul.naleul.domain.task.entity.TaskActual;
import com.naleul.naleul.domain.task.enums.TaskPriority;
import com.naleul.naleul.domain.task.repository.TaskActualRepository;
import com.naleul.naleul.domain.task.repository.TaskRepository;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.user.repository.UserRepository;
import com.naleul.naleul.global.common.response.ErrorCode;
import com.naleul.naleul.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GoalCategoryRepository goalCategoryRepository;
    private final GeneralCategoryRepository generalCategoryRepository;
    private final TaskActualRepository taskActualRepository;

    // KST = UTC+9
    private static final int KST_OFFSET_HOURS = 9;

    private static final List<String> DAY_ORDER = List.of(
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    );

    // ── 생성 ──────────────────────────────────────────────
    @Transactional
    public TaskResponse createTask(Long userId, TaskCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        GoalCategory goalCategory = goalCategoryRepository.findById(request.goalCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_CATEGORY_NOT_FOUND));

        GeneralCategory generalCategory = generalCategoryRepository.findById(request.generalCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.GENERAL_CATEGORY_NOT_FOUND));

        Task task = Task.builder()
                .taskName(request.taskName())
                .taskPriority(request.taskPriority())
                .user(user)
                .goalCategory(goalCategory)
                .generalCategory(generalCategory)
                .plannedStartAt(request.plannedStartAt())
                .plannedEndAt(request.plannedEndAt())
                .plannedDurationMinutes(calculateMinutes(request.plannedStartAt(), request.plannedEndAt()))
                .defaultSettingStatus(request.defaultSettingStatus())
                .build();

        taskRepository.save(task);
        return TaskResponse.from(task);
    }

    // ── 단건 조회 ──────────────────────────────────────────
    public TaskResponse getTask(Long userId, Long taskId) {
        Task task = taskRepository.findByTaskIdAndUserIdWithDetails(taskId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));
        return TaskResponse.from(task);
    }

    // ── 전체 조회 ──────────────────────────────────────────
    public List<TaskResponse> getTasksByUser(Long userId) {
        return taskRepository.findAllByUserIdWithDetails(userId)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskPageResponse getTasksByTimeRange(Long userId, TaskTimeRangeRequest request, Pageable pageable) {
        if (request.startTime().isAfter(request.endTime())) {
            throw new CustomException(ErrorCode.TASK_INVALID_TIME_RANGE);
        }

        LocalDateTime startDateTime = LocalDateTime.of(request.date(), request.startTime());
        LocalDateTime endDateTime = LocalDateTime.of(request.date(), request.endTime());

        Page<Task> taskPage = taskRepository.findByTimeRange(userId, startDateTime, endDateTime, pageable);
        return TaskPageResponse.from(taskPage.map(TaskResponse::from));
    }

    public TaskPageResponse getDailyTasks(Long userId, TaskDailyRequest request, Pageable pageable) {
        TaskPriority priority = parsePriority(request.priority());

        LocalDateTime kstDayStart = request.date().atStartOfDay().minusHours(9);
        LocalDateTime kstDayEnd = request.date().plusDays(1).atStartOfDay().minusHours(9);

        Page<Task> taskPage = taskRepository.findDailyTasks(
                userId,
                kstDayStart,
                kstDayEnd,
                request.goalCategoryId(),
                request.generalCategoryId(),
                priority,
                pageable
        );

        return TaskPageResponse.from(taskPage.map(TaskResponse::from));
    }

    public TaskWeeklyResponse getWeeklyTasks(Long userId, TaskWeeklyRequest request) {
        if (request.startDate() != null && request.endDate() != null) {
            if (request.startDate().isAfter(request.endDate())) {
                throw new CustomException(ErrorCode.TASK_INVALID_DATE_RANGE);
            }
            if (request.startDate().plusDays(7).isBefore(request.endDate())) {
                throw new CustomException(ErrorCode.TASK_INVALID_WEEK_RANGE);
            }
        }

        TaskPriority priority = parsePriority(request.priority());

        // KST 자정 넘어가는 일정 커버 — 전날/다음날(UTC 기준)도 함께 조회
        List<Task> tasks = taskRepository.findWeeklyTasksWithoutPage(
                userId,
                request.startDate() != null ? request.startDate().minusDays(1) : null,
                request.endDate() != null ? request.endDate().plusDays(1) : null,
                request.goalCategoryId(),
                request.generalCategoryId(),
                priority
        );

        Map<String, LocalDate> dayToDate = new LinkedHashMap<>();
        if (request.startDate() != null) {
            for (int i = 0; i < DAY_ORDER.size(); i++) {
                dayToDate.put(DAY_ORDER.get(i), request.startDate().plusDays(i));
            }
        }

        Map<String, List<TaskResponse>> tasksByDay = new LinkedHashMap<>();
        DAY_ORDER.forEach(day -> {
            LocalDate dayDate = dayToDate.get(day);
            List<TaskResponse> dayTasks = tasks.stream()
                    .filter(task -> matchesDay(task, dayDate))
                    .map(TaskResponse::from)
                    .toList();
            tasksByDay.put(day, dayTasks);
        });

        return TaskWeeklyResponse.from(tasksByDay);
    }

    public TaskMonthlyResponse getMonthlyTasks(Long userId, TaskMonthlyRequest request) {
        List<Task> tasks = taskRepository.findMonthlyTasksWithoutPage(
                userId,
                request.year(),
                request.month()
        );

        LocalDate firstDay = LocalDate.of(request.year(), request.month(), 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        Map<String, List<TaskResponse>> tasksByDate = new LinkedHashMap<>();
        firstDay.datesUntil(lastDay.plusDays(1))
                .forEach(date -> tasksByDate.put(date.toString(), new ArrayList<>()));

        tasks.forEach(task -> {
            if (task.getPlannedStartAt() == null) return;

            LocalDate startDate = task.getPlannedStartAt().plusHours(KST_OFFSET_HOURS).toLocalDate();
            LocalDate endDate = task.getPlannedEndAt() != null
                    ? task.getPlannedEndAt().plusHours(KST_OFFSET_HOURS).toLocalDate()
                    : startDate;

            putIfPresent(tasksByDate, startDate, TaskResponse.from(task));
            if (!endDate.equals(startDate)) {
                putIfPresent(tasksByDate, endDate, TaskResponse.from(task));
            }
        });

        return TaskMonthlyResponse.from(tasksByDate);
    }

    // ── 실제 시간 기록 ──────────────────────────────────────
    @Transactional
    public TaskResponse recordActual(Long userId, Long taskId, TaskUpdateActualRequest request) {
        Task task = taskRepository.findByTaskIdAndUserIdWithDetails(taskId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

        TaskActual actual = taskActualRepository
                .findByTaskTaskId(taskId)
                .orElse(TaskActual.builder().task(task).build());

        actual.update(request.actualStartAt(), request.actualEndAt());
        taskActualRepository.save(actual);

        return TaskResponse.from(
                taskRepository.findByTaskIdAndUserIdWithDetails(taskId, userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND))
        );
    }

    // ── 수정 ──────────────────────────────────────────────
    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskUpdateRequest request) {
        Task task = taskRepository.findByTaskIdAndUserIdWithDetails(taskId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

        GoalCategory goalCategory = goalCategoryRepository.findById(request.goalCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_CATEGORY_NOT_FOUND));

        GeneralCategory generalCategory = generalCategoryRepository.findById(request.generalCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.GENERAL_CATEGORY_NOT_FOUND));

        task.update(
                request.taskName(),
                request.taskPriority(),
                goalCategory,
                generalCategory,
                request.plannedStartAt(),
                request.plannedEndAt(),
                request.defaultSettingStatus()
        );

        return TaskResponse.from(task);
    }

    // ── 삭제 ──────────────────────────────────────────────
    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        Task task = taskRepository.findByTaskIdAndUserIdWithDetails(taskId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));
        taskRepository.delete(task);
    }

    // ── 내부 유틸 ──────────────────────────────────────────

    /**
     * UTC로 저장된 시간을 KST(+9)로 변환해서 날짜 일치 여부 확인
     */
    private boolean matchesDay(Task task, LocalDate dayDate) {
        if (dayDate == null || task.getPlannedStartAt() == null) return false;
        LocalDate startDate = task.getPlannedStartAt().plusHours(KST_OFFSET_HOURS).toLocalDate();
        LocalDate endDate = task.getPlannedEndAt() != null
                ? task.getPlannedEndAt().plusHours(KST_OFFSET_HOURS).toLocalDate()
                : startDate;
        return startDate.equals(dayDate) || endDate.equals(dayDate);
    }

    private void putIfPresent(Map<String, List<TaskResponse>> map, LocalDate date, TaskResponse response) {
        String key = date.toString();
        if (map.containsKey(key)) {
            map.get(key).add(response);
        }
    }

    private TaskPriority parsePriority(String priority) {
        if (priority == null) return null;
        try {
            return TaskPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_ENUM_VALUE);
        }
    }

    private Long calculateMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return null;
        return java.time.Duration.between(start, end).toMinutes();
    }
}