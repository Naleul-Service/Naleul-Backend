package com.naleul.naleul.domain.task.service;

import com.naleul.naleul.domain.dayOfTheWeek.entity.WeekDay;
import com.naleul.naleul.domain.dayOfTheWeek.repository.WeekDayRepository;
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
import com.naleul.naleul.domain.task.entity.TaskDayOfWeek;
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
    private final WeekDayRepository weekDayRepository;
    private final TaskActualRepository taskActualRepository;

    private static final List<String> DAY_ORDER = List.of(
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    );
    private static final List<String> VALID_DAYS = DAY_ORDER;

    // ── 생성 ──────────────────────────────────────────────
    @Transactional
    public TaskResponse createTask(Long userId, TaskCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        GoalCategory goalCategory = goalCategoryRepository.findById(request.goalCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_CATEGORY_NOT_FOUND));

        GeneralCategory generalCategory = generalCategoryRepository.findById(request.generalCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.GENERAL_CATEGORY_NOT_FOUND));

        List<Long> dayOfWeekIdList = request.dayOfWeekIds() != null ? request.dayOfWeekIds() : List.of();
        List<WeekDay> dayOfWeeks = weekDayRepository.findByDayOfWeekIdIn(dayOfWeekIdList);
        if (dayOfWeeks.size() != dayOfWeekIdList.size()) {
            throw new CustomException(ErrorCode.TASK_INVALID_WEEK_DAY_ID);
        }

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

        dayOfWeeks.forEach(day ->
                task.getTaskDayOfWeeks().add(
                        TaskDayOfWeek.builder().task(task).dayOfWeek(day).build()
                )
        );

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
        if (request.dayOfWeek() != null && !VALID_DAYS.contains(request.dayOfWeek().toUpperCase())) {
            throw new CustomException(ErrorCode.TASK_INVALID_DAY_OF_WEEK);
        }

        TaskPriority priority = parsePriority(request.priority());

        Page<Task> taskPage = taskRepository.findDailyTasks(
                userId,
                request.date(),
                request.dayOfWeek() != null ? request.dayOfWeek().toUpperCase() : null,
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
        String dayOfWeek = parseAndValidateDayOfWeek(request.dayOfWeek());

        List<Task> tasks = taskRepository.findWeeklyTasksWithoutPage(
                userId,
                request.startDate(),
                request.endDate(),
                request.goalCategoryId(),
                request.generalCategoryId(),
                priority,
                dayOfWeek
        );

        // startDate 기준으로 각 요일의 실제 날짜 계산
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
                    .filter(task -> matchesDay(task, day, dayDate))
                    .map(task -> TaskResponse.fromWithDateFilter(task, dayDate))
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
            if (task.getPlannedStartAt() != null) {
                LocalDate startDate = task.getPlannedStartAt().toLocalDate();
                LocalDate endDate = task.getPlannedEndAt() != null
                        ? task.getPlannedEndAt().toLocalDate()
                        : startDate;

                // plannedStartAt 날짜로 그룹핑
                String startKey = startDate.toString();
                if (tasksByDate.containsKey(startKey)) {
                    tasksByDate.get(startKey).add(TaskResponse.from(task));
                }

                // 자정 넘어가는 경우 plannedEndAt 날짜에도 추가 (중복 방지)
                if (!endDate.equals(startDate)) {
                    String endKey = endDate.toString();
                    if (tasksByDate.containsKey(endKey)) {
                        tasksByDate.get(endKey).add(TaskResponse.from(task));
                    }
                }
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
                .findByTaskTaskIdAndActualDate(taskId, request.actualDate())
                .orElse(TaskActual.builder()
                        .task(task)
                        .actualDate(request.actualDate())
                        .build());

        actual.update(request.actualStartAt(), request.actualEndAt());
        taskActualRepository.save(actual);

        Task updatedTask = taskRepository.findByTaskIdAndUserIdWithDetails(taskId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

        return TaskResponse.from(updatedTask);
    }

    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskUpdateRequest request) {
        Task task = taskRepository.findByTaskIdAndUserIdWithDetails(taskId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

        GoalCategory goalCategory = goalCategoryRepository.findById(request.goalCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.GOAL_CATEGORY_NOT_FOUND));

        GeneralCategory generalCategory = generalCategoryRepository.findById(request.generalCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.GENERAL_CATEGORY_NOT_FOUND));

        List<Long> dayOfWeekIdList = request.dayOfWeekIds() != null ? request.dayOfWeekIds() : List.of();
        List<WeekDay> weekDays = weekDayRepository.findByDayOfWeekIdIn(dayOfWeekIdList);
        if (weekDays.size() != dayOfWeekIdList.size()) {
            throw new CustomException(ErrorCode.TASK_INVALID_WEEK_DAY_ID);
        }

        task.update(
                request.taskName(),
                request.taskPriority(),
                goalCategory,
                generalCategory,
                request.plannedStartAt(),
                request.plannedEndAt(),
                request.defaultSettingStatus()
        );

        task.getTaskDayOfWeeks().clear();
        taskRepository.flush();

        weekDays.forEach(day ->
                task.getTaskDayOfWeeks().add(
                        TaskDayOfWeek.builder().task(task).dayOfWeek(day).build()
                )
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
     * 태스크가 특정 요일/날짜에 해당하는지 판단
     * - 반복 태스크(defaultSettingStatus=true): 요일 일치 여부
     * - 단일 태스크(defaultSettingStatus=false): plannedStartAt 날짜 일치 여부
     */
    private boolean matchesDay(Task task, String day, LocalDate dayDate) {
        if (!task.isDefaultSettingStatus()) {
            if (dayDate == null || task.getPlannedStartAt() == null) return false;
            LocalDate startDate = task.getPlannedStartAt().toLocalDate();
            LocalDate endDate = task.getPlannedEndAt() != null
                    ? task.getPlannedEndAt().toLocalDate()
                    : startDate;
            // plannedStartAt 날짜 또는 plannedEndAt 날짜가 해당 요일 날짜와 일치하면 포함
            return startDate.equals(dayDate) || endDate.equals(dayDate);
        }
        return task.getTaskDayOfWeeks().stream()
                .anyMatch(tdow -> tdow.getDayOfWeek().getDayName().equals(day));
    }

    private TaskPriority parsePriority(String priority) {
        if (priority == null) return null;
        try {
            return TaskPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_ENUM_VALUE);
        }
    }

    private String parseAndValidateDayOfWeek(String dayOfWeek) {
        if (dayOfWeek == null) return null;
        String upper = dayOfWeek.toUpperCase();
        if (!VALID_DAYS.contains(upper)) {
            throw new CustomException(ErrorCode.TASK_INVALID_DAY_OF_WEEK);
        }
        return upper;
    }

    private Long calculateMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return null;
        return java.time.Duration.between(start, end).toMinutes();
    }
}