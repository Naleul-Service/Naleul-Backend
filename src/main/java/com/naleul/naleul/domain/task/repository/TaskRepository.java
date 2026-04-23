package com.naleul.naleul.domain.task.repository;

import com.naleul.naleul.domain.chart.repository.projection.AchievementStatProjection;
import com.naleul.naleul.domain.chart.repository.projection.CategoryStatProjection;
import com.naleul.naleul.domain.chart.repository.projection.GoalGeneralStatProjection;
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
AND t.plannedStartAt < :kstDayEnd
AND t.plannedEndAt > :kstDayStart
AND (:goalCategoryId IS NULL OR t.goalCategory.goalCategoryId = :goalCategoryId)
AND (:generalCategoryId IS NULL OR t.generalCategory.generalCategoryId = :generalCategoryId)
AND (:priority IS NULL OR t.taskPriority = :priority)
ORDER BY t.plannedStartAt ASC
"""
    )
    List<Task> findDailyTasks(
            @Param("userId") Long userId,
            @Param("kstDayStart") LocalDateTime kstDayStart,
            @Param("kstDayEnd") LocalDateTime kstDayEnd,
            @Param("goalCategoryId") Long goalCategoryId,
            @Param("generalCategoryId") Long generalCategoryId,
            @Param("priority") TaskPriority priority
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

    // TaskRepository.java 기존 코드 맨 아래 } 전에 추가

// ── 차트 집계 ──────────────────────────────────────

    // 7-1-1: goalCategory별 실제 소요 시간 합계
    @Query("""
    SELECT t.goalCategory.goalCategoryId AS categoryId,
           g.goalCategoryName            AS categoryName,
           gc.colorCode                   AS colorHex,
           COALESCE(SUM(ta.actualDurationMinutes), 0) AS totalMinutes
    FROM Task t
    JOIN t.goalCategory g
    LEFT JOIN g.userColor gc
    LEFT JOIN t.taskActual ta
    WHERE t.user.userId = :userId
      AND ta.actualEndAt IS NOT NULL
    GROUP BY t.goalCategory.goalCategoryId, g.goalCategoryName, gc.colorCode
""")
    List<CategoryStatProjection> findGoalCategoryStats(@Param("userId") Long userId);

    // 7-2-1: goalCategory + generalCategory별 실제 소요 시간 합계
    @Query("""
    SELECT t.goalCategory.goalCategoryId       AS goalCategoryId,
           g.goalCategoryName                  AS goalCategoryName,
           gc2.colorCode                        AS goalColorHex,
           t.generalCategory.generalCategoryId AS generalCategoryId,
           ge.generalCategoryName              AS generalCategoryName,
           gc1.colorCode                        AS generalColorHex,
           COALESCE(SUM(ta.actualDurationMinutes), 0) AS totalMinutes
    FROM Task t
    JOIN t.goalCategory g
    LEFT JOIN g.userColor gc2
    JOIN t.generalCategory ge
    LEFT JOIN ge.color gc1
    LEFT JOIN t.taskActual ta
    WHERE t.user.userId = :userId
      AND ta.actualEndAt IS NOT NULL
    GROUP BY t.goalCategory.goalCategoryId, g.goalCategoryName, gc2.colorCode,
             t.generalCategory.generalCategoryId, ge.generalCategoryName, gc1.colorCode
""")
    List<GoalGeneralStatProjection> findGoalGeneralCategoryStats(@Param("userId") Long userId);

    // 7-3-1: generalCategory별 실제 소요 시간 합계
    @Query("""
    SELECT t.generalCategory.generalCategoryId AS categoryId,
           ge.generalCategoryName              AS categoryName,
           gc.colorCode                         AS colorHex,
           COALESCE(SUM(ta.actualDurationMinutes), 0) AS totalMinutes
    FROM Task t
    JOIN t.generalCategory ge
    LEFT JOIN ge.color gc
    LEFT JOIN t.taskActual ta
    WHERE t.user.userId = :userId
      AND ta.actualEndAt IS NOT NULL
    GROUP BY t.generalCategory.generalCategoryId, ge.generalCategoryName, gc.colorCode
""")
    List<CategoryStatProjection> findGeneralCategoryStats(@Param("userId") Long userId);

    @Query(value = """
    SELECT
        COUNT(t.task_id) AS totalCount,
        SUM(CASE
            WHEN ta.actual_start_at IS NOT NULL
             AND ta.actual_end_at IS NOT NULL
             AND t.planned_start_at IS NOT NULL
             AND t.planned_end_at IS NOT NULL
             AND (
                 TIMESTAMPDIFF(SECOND,
                     GREATEST(t.planned_start_at, ta.actual_start_at),
                     LEAST(t.planned_end_at, ta.actual_end_at)
                 ) * 1.0
                 / NULLIF(TIMESTAMPDIFF(SECOND, t.planned_start_at, t.planned_end_at), 0)
             ) >= 0.5
            THEN 1 ELSE 0
        END) AS achievedCount
    FROM task t
    LEFT JOIN task_actual ta ON ta.task_id = t.task_id
    WHERE t.user_id = :userId
""", nativeQuery = true)
    AchievementStatProjection findAchievementStats(@Param("userId") Long userId);
}