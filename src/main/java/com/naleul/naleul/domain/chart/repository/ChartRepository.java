package com.naleul.naleul.domain.chart.repository;

import com.naleul.naleul.domain.chart.repository.projection.AchievementStatProjection;
import com.naleul.naleul.domain.chart.repository.projection.CategoryStatProjection;
import com.naleul.naleul.domain.chart.repository.projection.GoalGeneralStatProjection;
import com.naleul.naleul.domain.actualTask.entity.TaskActual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChartRepository extends JpaRepository<TaskActual, Long> {

    // 7-1-1: goalCategory별 실제 소요 시간 합계 (독립 TaskActual 포함)
    @Query("""
        SELECT ta.goalCategory.goalCategoryId AS categoryId,
               g.goalCategoryName             AS categoryName,
               gc.colorCode                   AS colorHex,
               COALESCE(SUM(ta.actualDurationMinutes), 0) AS totalMinutes
        FROM TaskActual ta
        JOIN ta.goalCategory g
        LEFT JOIN g.userColor gc
        WHERE ta.user.userId = :userId
          AND ta.actualEndAt IS NOT NULL
        GROUP BY ta.goalCategory.goalCategoryId, g.goalCategoryName, gc.colorCode
    """)
    List<CategoryStatProjection> findGoalCategoryStats(@Param("userId") Long userId);

    // 7-2-1: goalCategory + generalCategory별 실제 소요 시간 합계 (독립 TaskActual 포함)
    @Query("""
        SELECT ta.goalCategory.goalCategoryId       AS goalCategoryId,
               g.goalCategoryName                   AS goalCategoryName,
               gc2.colorCode                        AS goalColorHex,
               ta.generalCategory.generalCategoryId AS generalCategoryId,
               ge.generalCategoryName               AS generalCategoryName,
               gc1.colorCode                        AS generalColorHex,
               COALESCE(SUM(ta.actualDurationMinutes), 0) AS totalMinutes
        FROM TaskActual ta
        JOIN ta.goalCategory g
        LEFT JOIN g.userColor gc2
        JOIN ta.generalCategory ge
        LEFT JOIN ge.color gc1
        WHERE ta.user.userId = :userId
          AND ta.actualEndAt IS NOT NULL
        GROUP BY ta.goalCategory.goalCategoryId, g.goalCategoryName, gc2.colorCode,
                 ta.generalCategory.generalCategoryId, ge.generalCategoryName, gc1.colorCode
    """)
    List<GoalGeneralStatProjection> findGoalGeneralCategoryStats(@Param("userId") Long userId);

    // 7-3-1: generalCategory별 실제 소요 시간 합계 (독립 TaskActual 포함)
    @Query("""
        SELECT ta.generalCategory.generalCategoryId AS categoryId,
               ge.generalCategoryName               AS categoryName,
               gc.colorCode                         AS colorHex,
               COALESCE(SUM(ta.actualDurationMinutes), 0) AS totalMinutes
        FROM TaskActual ta
        JOIN ta.generalCategory ge
        LEFT JOIN ge.color gc
        WHERE ta.user.userId = :userId
          AND ta.actualEndAt IS NOT NULL
        GROUP BY ta.generalCategory.generalCategoryId, ge.generalCategoryName, gc.colorCode
    """)
    List<CategoryStatProjection> findGeneralCategoryStats(@Param("userId") Long userId);
}