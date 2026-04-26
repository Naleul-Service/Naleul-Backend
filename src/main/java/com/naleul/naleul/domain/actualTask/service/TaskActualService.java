package com.naleul.naleul.domain.actualTask.service;

import com.naleul.naleul.domain.actualTask.dto.request.*;
import com.naleul.naleul.domain.actualTask.dto.response.TaskActualResponse;
import com.naleul.naleul.domain.actualTask.dto.response.TaskActualWeeklyResponse;
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

import java.time.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Transactional
    public TaskActualResponse updateActual(Long userId, Long taskActualId, TaskActualUpdateRequest request) {
        TaskActual actual = taskActualRepository.findByTaskActualIdAndUserId(taskActualId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACTUAL_TASK_NOT_FOUND));

        GoalCategory goalCategory = findGoalCategory(request.goalCategoryId());
        GeneralCategory generalCategory = findGeneralCategory(request.generalCategoryId());

        actual.updateAll(
                request.taskName(),
                goalCategory,
                generalCategory,
                request.actualStartAt(),
                request.actualEndAt()
        );

        return TaskActualResponse.from(actual);
    }

    @Transactional
    public void deleteActual(Long userId, Long taskActualId) {
        TaskActual actual = taskActualRepository.findByTaskActualIdAndUserId(taskActualId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACTUAL_TASK_NOT_FOUND));
        taskActualRepository.delete(actual);
    }

    // TaskActualService.java — 메서드 추가

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Map<DayOfWeek, String> DAY_KEY_MAP = Map.of(
            DayOfWeek.MONDAY,    "MONDAY",
            DayOfWeek.TUESDAY,   "TUESDAY",
            DayOfWeek.WEDNESDAY, "WEDNESDAY",
            DayOfWeek.THURSDAY,  "THURSDAY",
            DayOfWeek.FRIDAY,    "FRIDAY",
            DayOfWeek.SATURDAY,  "SATURDAY",
            DayOfWeek.SUNDAY,    "SUNDAY"
    );

    public TaskActualWeeklyResponse getWeeklyActuals(Long userId, TaskActualWeeklyRequest request) {
        // KST 기준 주 시작/끝을 UTC로 변환
        LocalDateTime kstWeekStart = request.startDate().atStartOfDay().minusHours(KST_OFFSET_HOURS);
        LocalDateTime kstWeekEnd   = request.endDate().plusDays(1).atStartOfDay().minusHours(KST_OFFSET_HOURS);

        List<TaskActual> actuals = taskActualRepository.findWeeklyActuals(
                userId,
                kstWeekStart,
                kstWeekEnd,
                request.goalCategoryId(),
                request.generalCategoryId()
        );

        // 요일별 초기화 (순서 보장)
        Map<String, List<TaskActualResponse>> actualsByDay = new LinkedHashMap<>();
        for (String day : List.of("MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY")) {
            actualsByDay.put(day, new ArrayList<>());
        }

        // KST 기준 요일로 분류
        // TaskActualService.java — getWeeklyActuals 분류 로직 수정

        for (TaskActual actual : actuals) {
            LocalDateTime kstStart = actual.getActualStartAt().plusHours(KST_OFFSET_HOURS);
            LocalDateTime kstEnd   = actual.getActualEndAt().plusHours(KST_OFFSET_HOURS);

            LocalDate startDate = kstStart.toLocalDate();
            LocalDate endDate   = kstEnd.toLocalDate();

            // startDate ~ endDate 사이의 모든 날짜 버킷에 넣기
            // (자정 정각에 끝나는 경우 endDate는 포함하지 않음)
            boolean endIsExact = kstEnd.toLocalTime().equals(LocalTime.MIDNIGHT);
            LocalDate effectiveEndDate = endIsExact ? endDate.minusDays(1) : endDate;

            LocalDate cursor = startDate;
            while (!cursor.isAfter(effectiveEndDate)) {
                // 주 범위 안에 있는 날짜만
                if (!cursor.isBefore(request.startDate()) && !cursor.isAfter(request.endDate())) {
                    String dayKey = DAY_KEY_MAP.get(cursor.getDayOfWeek());
                    actualsByDay.get(dayKey).add(TaskActualResponse.from(actual));
                }
                cursor = cursor.plusDays(1);
            }
        }

        return TaskActualWeeklyResponse.from(actualsByDay);
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
        // 2026-04-25 00:00 - 9h = 2026-04-24 15:00 (UTC)
    }

    private LocalDateTime toKstDayEnd(LocalDate date) {
        return date.plusDays(1).atStartOfDay().minusHours(KST_OFFSET_HOURS);
        // 2026-04-26 00:00 - 9h = 2026-04-25 15:00 (UTC)
    }

    private Long calculateMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return null;
        return Duration.between(start, end).toMinutes();
    }
}