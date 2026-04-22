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

    @Query("""
    SELECT t FROM Task t
    JOIN FETCH t.goalCategory
    JOIN FETCH t.generalCategory
    WHERE t.user.userId = :userId
    """)
    List<Task> findAllByUserIdWithDetails(@Param("userId") Long userId);

    @Query("""
    SELECT t FROM Task t
    JOIN FETCH t.goalCategory
    JOIN FETCH t.generalCategory
    WHERE t.taskId = :taskId AND t.user.userId = :userId
    """)
    Optional<Task> findByTaskIdAndUserIdWithDetails(
            @Param("taskId") Long taskId,
            @Param("userId") Long userId
    );

    @Query(
            value = """
    SELECT t FROM Task t
    JOIN FETCH t.goalCategory
    JOIN FETCH t.generalCategory
    WHERE t.user.userId = :userId
    AND (
        CAST(t.plannedStartAt AS date) = :date
        OR CAST(t.plannedStartAt AS date) = :prevDate
        OR CAST(t.plannedEndAt AS date) = :date
    )
    AND (:goalCategoryId IS NULL OR t.goalCategory.goalCategoryId = :goalCategoryId)
    AND (:generalCategoryId IS NULL OR t.generalCategory.generalCategoryId = :generalCategoryId)
    AND (:priority IS NULL OR t.taskPriority = :priority)
    ORDER BY t.plannedStartAt ASC
    """,
            countQuery = """
    SELECT COUNT(t) FROM Task t
    WHERE t.user.userId = :userId
    AND (
        CAST(t.plannedStartAt AS date) = :date
        OR CAST(t.plannedStartAt AS date) = :prevDate
        OR CAST(t.plannedEndAt AS date) = :date
    )
    AND (:goalCategoryId IS NULL OR t.goalCategory.goalCategoryId = :goalCategoryId)
    AND (:generalCategoryId IS NULL OR t.generalCategory.generalCategoryId = :generalCategoryId)
    AND (:priority IS NULL OR t.taskPriority = :priority)
    """
    )
    Page<Task> findDailyTasks(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("prevDate") LocalDate prevDate,
            @Param("goalCategoryId") Long goalCategoryId,
            @Param("generalCategoryId") Long generalCategoryId,
            @Param("priority") TaskPriority priority,
            Pageable pageable
    );

    @Query("""
    SELECT t FROM Task t
    JOIN FETCH t.goalCategory
    JOIN FETCH t.generalCategory
    WHERE t.user.userId = :userId
    AND (:startDate IS NULL OR CAST(t.plannedStartAt AS date) >= :startDate)
    AND (:endDate IS NULL OR CAST(t.plannedStartAt AS date) <= :endDate)
    AND (:goalCategoryId IS NULL OR t.goalCategory.goalCategoryId = :goalCategoryId)
    AND (:generalCategoryId IS NULL OR t.generalCategory.generalCategoryId = :generalCategoryId)
    AND (:priority IS NULL OR t.taskPriority = :priority)
    ORDER BY t.plannedStartAt ASC
    """)
    List<Task> findWeeklyTasksWithoutPage(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("goalCategoryId") Long goalCategoryId,
            @Param("generalCategoryId") Long generalCategoryId,
            @Param("priority") TaskPriority priority
    );

    @Query("""
    SELECT t FROM Task t
    JOIN FETCH t.goalCategory
    JOIN FETCH t.generalCategory
    WHERE t.user.userId = :userId
    AND (
        (YEAR(t.plannedStartAt) = :year AND MONTH(t.plannedStartAt) = :month)
        OR
        (t.plannedEndAt IS NOT NULL AND YEAR(t.plannedEndAt) = :year AND MONTH(t.plannedEndAt) = :month)
    )
    ORDER BY t.plannedStartAt ASC
    """)
    List<Task> findMonthlyTasksWithoutPage(
            @Param("userId") Long userId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );

    @Query("""
    SELECT t FROM Task t
    JOIN FETCH t.goalCategory
    JOIN FETCH t.generalCategory
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
}