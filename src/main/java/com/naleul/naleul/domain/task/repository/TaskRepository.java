package com.naleul.naleul.domain.task.repository;

import com.naleul.naleul.domain.task.entity.Task;
import com.naleul.naleul.domain.task.enums.TaskPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // 사용자의 전체 Task 조회 (N+1 방지용 fetch join)
    @Query("""
        SELECT t FROM Task t
        JOIN FETCH t.goalCategory
        JOIN FETCH t.generalCategory
        JOIN FETCH t.taskDayOfWeeks tdow
        JOIN FETCH tdow.dayOfWeek
        WHERE t.user.userId = :userId
    """)
    List<Task> findAllByUserIdWithDetails(@Param("userId") Long userId);

    // 단건 조회 (본인 소유 확인 포함)
    @Query("""
        SELECT t FROM Task t
        JOIN FETCH t.goalCategory
        JOIN FETCH t.generalCategory
        JOIN FETCH t.taskDayOfWeeks tdow
        JOIN FETCH tdow.dayOfWeek
        WHERE t.taskId = :taskId AND t.user.userId = :userId
    """)
    Optional<Task> findByTaskIdAndUserIdWithDetails(
            @Param("taskId") Long taskId,
            @Param("userId") Long userId
    );

    @Query(
            value = """
        SELECT DISTINCT t FROM Task t
        JOIN FETCH t.goalCategory
        JOIN FETCH t.generalCategory
        JOIN FETCH t.taskDayOfWeeks tdow
        JOIN FETCH tdow.dayOfWeek
        WHERE t.user.userId = :userId
        AND (
            (t.defaultSettingStatus = false AND CAST(t.plannedStartAt AS date) = :date)
            OR
            (t.defaultSettingStatus = true AND (:dayOfWeek IS NULL OR tdow.dayOfWeek.dayName = :dayOfWeek))
        )
        AND (:goalCategoryId IS NULL OR t.goalCategory.goalCategoryId = :goalCategoryId)
        AND (:generalCategoryId IS NULL OR t.generalCategory.generalCategoryId = :generalCategoryId)
        AND (:priority IS NULL OR t.taskPriority = :priority)
        ORDER BY t.plannedStartAt ASC
    """,
            countQuery = """
        SELECT COUNT(DISTINCT t) FROM Task t
        JOIN t.taskDayOfWeeks tdow
        JOIN tdow.dayOfWeek
        WHERE t.user.userId = :userId
        AND (
            (t.defaultSettingStatus = false AND CAST(t.plannedStartAt AS date) = :date)
            OR
            (t.defaultSettingStatus = true AND (:dayOfWeek IS NULL OR tdow.dayOfWeek.dayName = :dayOfWeek))
        )
        AND (:goalCategoryId IS NULL OR t.goalCategory.goalCategoryId = :goalCategoryId)
        AND (:generalCategoryId IS NULL OR t.generalCategory.generalCategoryId = :generalCategoryId)
        AND (:priority IS NULL OR t.taskPriority = :priority)
    """
    )
    Page<Task> findDailyTasks(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("goalCategoryId") Long goalCategoryId,
            @Param("generalCategoryId") Long generalCategoryId,
            @Param("priority") TaskPriority priority,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT t FROM Task t
        JOIN FETCH t.goalCategory
        JOIN FETCH t.generalCategory
        JOIN FETCH t.taskDayOfWeeks tdow
        JOIN FETCH tdow.dayOfWeek
        WHERE t.user.userId = :userId
        AND (:startDate IS NULL OR CAST(t.plannedStartAt AS date) >= :startDate)
        AND (:endDate IS NULL OR CAST(t.plannedStartAt AS date) <= :endDate)
        ORDER BY t.plannedStartAt ASC
    """)
    Page<Task> findWeeklyTasks(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT t FROM Task t
        JOIN FETCH t.goalCategory
        JOIN FETCH t.generalCategory
        JOIN FETCH t.taskDayOfWeeks tdow
        JOIN FETCH tdow.dayOfWeek
        WHERE t.user.userId = :userId
        AND YEAR(t.plannedStartAt) = :year
        AND MONTH(t.plannedStartAt) = :month
        ORDER BY t.plannedStartAt ASC
    """)
    Page<Task> findMonthlyTasks(
            @Param("userId") Long userId,
            @Param("year") Integer year,
            @Param("month") Integer month,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT t FROM Task t
        JOIN FETCH t.goalCategory
        JOIN FETCH t.generalCategory
        JOIN FETCH t.taskDayOfWeeks tdow
        JOIN FETCH tdow.dayOfWeek
        WHERE t.user.userId = :userId
        AND t.plannedStartAt < :endDateTime
        AND t.plannedEndAt > :startDateTime
        ORDER BY t.plannedStartAt ASC
    """)
    Page<Task> findByTimeRange(
            @Param("userId") Long userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT t FROM Task t
        JOIN FETCH t.goalCategory
        JOIN FETCH t.generalCategory
        JOIN FETCH t.taskDayOfWeeks tdow
        JOIN FETCH tdow.dayOfWeek
        WHERE t.user.userId = :userId
        AND (:startDate IS NULL OR CAST(t.plannedStartAt AS date) >= :startDate)
        AND (:endDate IS NULL OR CAST(t.plannedStartAt AS date) <= :endDate)
        ORDER BY t.plannedStartAt ASC
    """)
    List<Task> findWeeklyTasksWithoutPage(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT DISTINCT t FROM Task t
        JOIN FETCH t.goalCategory
        JOIN FETCH t.generalCategory
        JOIN FETCH t.taskDayOfWeeks tdow
        JOIN FETCH tdow.dayOfWeek
        WHERE t.user.userId = :userId
        AND YEAR(t.plannedStartAt) = :year
        AND MONTH(t.plannedStartAt) = :month
        ORDER BY t.plannedStartAt ASC
    """)
    List<Task> findMonthlyTasksWithoutPage(
            @Param("userId") Long userId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );
}