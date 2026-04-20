package com.naleul.naleul.domain.task.service;


import com.naleul.naleul.domain.dayOfTheWeek.entity.WeekDay;
import com.naleul.naleul.domain.dayOfTheWeek.repository.WeekDayRepository;
import com.naleul.naleul.domain.generalCategory.entity.GeneralCategory;
import com.naleul.naleul.domain.generalCategory.repository.GeneralCategoryRepository;
import com.naleul.naleul.domain.goalCategory.entity.GoalCategory;
import com.naleul.naleul.domain.goalCategory.repository.GoalCategoryRepository;
import com.naleul.naleul.domain.task.dto.request.*;
import com.naleul.naleul.domain.task.dto.response.TaskPageResponse;
import com.naleul.naleul.domain.task.dto.response.TaskResponse;
import com.naleul.naleul.domain.task.entity.Task;
import com.naleul.naleul.domain.task.entity.TaskDayOfWeek;
import com.naleul.naleul.domain.task.repository.TaskRepository;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        GoalCategory goalCategory = goalCategoryRepository.findById(request.goalCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표 카테고리입니다."));

        GeneralCategory generalCategory = generalCategoryRepository.findById(request.generalCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일반 카테고리입니다."));

        List<WeekDay> dayOfWeeks = weekDayRepository.findByDayOfWeekIdIn(request.dayOfWeekIds());
        if (dayOfWeeks.size() != request.dayOfWeekIds().size()) {
            throw new IllegalArgumentException("존재하지 않는 요일 ID가 포함되어 있습니다.");
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
                .orElseThrow(() -> new IllegalArgumentException("태스크를 찾을 수 없습니다."));
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
            throw new IllegalArgumentException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
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

        // dayOfWeek 값 검증 (MON~SUN 외 값 들어오면 에러)
        if (request.dayOfWeek() != null) {
            List<String> valid = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");
            if (!valid.contains(request.dayOfWeek().toUpperCase())) {
                throw new IllegalArgumentException("올바르지 않은 요일 값입니다. (MON~SUN)");
            }
        }

        Page<Task> taskPage = taskRepository.findDailyTasks(
                userId,
                request.date(),
                request.dayOfWeek() != null ? request.dayOfWeek().toUpperCase() : null,
                pageable
        );

        Page<TaskResponse> responsePage = taskPage.map(TaskResponse::from);
        return TaskPageResponse.from(responsePage);
    }

    public TaskPageResponse getWeeklyTasks(Long userId, TaskWeeklyRequest request, Pageable pageable) {

        // startDate가 endDate보다 늦으면 에러
        if (request.startDate() != null && request.endDate() != null) {
            if (request.startDate().isAfter(request.endDate())) {
                throw new IllegalArgumentException("시작일이 종료일보다 늦을 수 없습니다.");
            }

            // 7일 초과 범위 방지 (선택사항 - 필요 없으면 지워도 됩니다)
            if (request.startDate().plusDays(7).isBefore(request.endDate())) {
                throw new IllegalArgumentException("조회 범위는 최대 7일입니다.");
            }
        }

        Page<Task> taskPage = taskRepository.findWeeklyTasks(
                userId,
                request.startDate(),
                request.endDate(),
                pageable
        );

        return TaskPageResponse.from(taskPage.map(TaskResponse::from));
    }

    public TaskPageResponse getMonthlyTasks(Long userId, TaskMonthlyRequest request, Pageable pageable) {

        Page<Task> taskPage = taskRepository.findMonthlyTasks(
                userId,
                request.year(),
                request.month(),
                pageable
        );

        return TaskPageResponse.from(taskPage.map(TaskResponse::from));
    }

    // ── 실제 시간 기록 ──────────────────────────────────────
    @Transactional
    public TaskResponse recordActual(Long userId, Long taskId, TaskUpdateActualRequest request) {
        Task task = taskRepository.findByTaskIdAndUserIdWithDetails(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("태스크를 찾을 수 없습니다."));

        task.recordActual(request.actualStartAt(), request.actualEndAt());
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskUpdateRequest request) {

        Task task = taskRepository.findByTaskIdAndUserIdWithDetails(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("태스크를 찾을 수 없습니다."));

        GoalCategory goalCategory = goalCategoryRepository.findById(request.goalCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표 카테고리입니다."));

        GeneralCategory generalCategory = generalCategoryRepository.findById(request.generalCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일반 카테고리입니다."));

        List<WeekDay> weekDays = weekDayRepository.findByDayOfWeekIdIn(request.dayOfWeekIds());
        if (weekDays.size() != request.dayOfWeekIds().size()) {
            throw new IllegalArgumentException("존재하지 않는 요일 ID가 포함되어 있습니다.");
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
                .orElseThrow(() -> new IllegalArgumentException("태스크를 찾을 수 없습니다."));
        taskRepository.delete(task);
        // cascade + orphanRemoval로 TaskDayOfWeek도 자동 삭제
    }

    // ── 내부 유틸 ──────────────────────────────────────────
    private Long calculateMinutes(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        if (start == null || end == null) return null;
        return java.time.Duration.between(start, end).toMinutes();
    }
}