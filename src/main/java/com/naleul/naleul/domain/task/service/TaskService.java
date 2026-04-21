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
import com.naleul.naleul.domain.task.entity.TaskDayOfWeek;
import com.naleul.naleul.domain.task.enums.TaskPriority;
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
@Transactional(readOnly = true) // 기본은 읽기 전용 → 성능 최적화
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GoalCategoryRepository goalCategoryRepository;
    private final GeneralCategoryRepository generalCategoryRepository;
    private final WeekDayRepository weekDayRepository;

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

        // Task 생성 (실제 시간은 null)
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

        // 중간 테이블 엔티티 생성 및 연결
        dayOfWeeks.forEach(day ->
                task.getTaskDayOfWeeks().add(
                        TaskDayOfWeek.builder().task(task).dayOfWeek(day).build()
                )
        );

        taskRepository.save(task); // cascade로 TaskDayOfWeek도 함께 저장됨
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

        // LocalDate + LocalTime → LocalDateTime 으로 합치기
        LocalDateTime startDateTime = LocalDateTime.of(request.date(), request.startTime());
        LocalDateTime endDateTime = LocalDateTime.of(request.date(), request.endTime());

        Page<Task> taskPage = taskRepository.findByTimeRange(
                userId,
                startDateTime,
                endDateTime,
                pageable
        );

        return TaskPageResponse.from(taskPage.map(TaskResponse::from));
    }

    public TaskPageResponse getDailyTasks(Long userId, TaskDailyRequest request, Pageable pageable) {

        // dayOfWeek 값 검증
        if (request.dayOfWeek() != null) {
            List<String> valid = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
            if (!valid.contains(request.dayOfWeek().toUpperCase())) {
                throw new CustomException(ErrorCode.TASK_INVALID_DAY_OF_WEEK);
            }
        }

        // priority String → enum 변환 (잘못된 값이면 에러)
        TaskPriority priority = null;
        if (request.priority() != null) {
            try {
                priority = TaskPriority.valueOf(request.priority().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.INVALID_ENUM_VALUE);
            }
        }

        Page<Task> taskPage = taskRepository.findDailyTasks(
                userId,
                request.date(),
                request.dayOfWeek() != null ? request.dayOfWeek().toUpperCase() : null,
                request.goalCategoryId(),    // 추가
                request.generalCategoryId(), // 추가
                priority,                    // 추가 (변환된 enum)
                pageable
        );

        Page<TaskResponse> responsePage = taskPage.map(TaskResponse::from);
        return TaskPageResponse.from(responsePage);
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

        List<Task> tasks = taskRepository.findWeeklyTasksWithoutPage(
                userId,
                request.startDate(),
                request.endDate()
        );

        // 요일 순서 고정 (MON → SUN)
        List<String> dayOrder = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

        // 요일별로 그룹핑 (task 하나가 여러 요일에 걸쳐 있을 수 있어서 flatMap)
        Map<String, List<TaskResponse>> tasksByDay = new LinkedHashMap<>(); // 순서 유지

        dayOrder.forEach(day -> {
            List<TaskResponse> dayTasks = tasks.stream()
                    .filter(task -> task.getTaskDayOfWeeks().stream()
                            .anyMatch(tdow -> tdow.getDayOfWeek().getDayName().equals(day)))
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

        // 해당 월의 첫날 ~ 마지막날 구하기
        LocalDate firstDay = LocalDate.of(request.year(), request.month(), 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth()); // 28, 29, 30, 31 자동 계산

        // 날짜별로 그룹핑 (순서 유지)
        Map<String, List<TaskResponse>> tasksByDate = new LinkedHashMap<>();

        // 해당 월의 모든 날짜를 키로 먼저 세팅 (task 없는 날도 빈 배열로)
        firstDay.datesUntil(lastDay.plusDays(1))  // 첫날부터 마지막날까지 순회
                .forEach(date -> tasksByDate.put(date.toString(), new ArrayList<>()));

        // task를 날짜별로 분류
        tasks.forEach(task -> {
            if (task.getPlannedStartAt() != null) {
                String dateKey = task.getPlannedStartAt().toLocalDate().toString(); // "2024-01-15"
                if (tasksByDate.containsKey(dateKey)) {
                    tasksByDate.get(dateKey).add(TaskResponse.from(task));
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

        task.recordActual(request.actualStartAt(), request.actualEndAt());
        return TaskResponse.from(task);
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

        // 기본 정보 수정
        task.update(
                request.taskName(),
                request.taskPriority(),
                goalCategory,
                generalCategory,
                request.plannedStartAt(),
                request.plannedEndAt(),
                request.defaultSettingStatus()
        );

        // 요일 교체 (기존 전부 삭제 후 새로 추가)
        // orphanRemoval = true 이기 때문에 clear()하면 DB에서도 삭제됨
        task.getTaskDayOfWeeks().clear();
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
        // cascade + orphanRemoval로 TaskDayOfWeek도 자동 삭제
    }

    // ── 내부 유틸 ──────────────────────────────────────────
    private Long calculateMinutes(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        if (start == null || end == null) return null;
        return java.time.Duration.between(start, end).toMinutes();
    }
}