package com.naleul.naleul.domain.chart.repository;

import com.naleul.naleul.domain.chart.repository.projection.CategoryStatProjection;
import com.naleul.naleul.domain.chart.repository.projection.GoalGeneralStatProjection;
import com.naleul.naleul.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChartRepository extends JpaRepository<Task, Long> {

    // 7-1-1: goalCategory별 실제 소요 시간 합계
    @Query("""
        SELECT t.goalCategory.goalCategoryId AS categoryId,
               g.goalCategoryName            AS categoryName,
               gc.colorCode                   AS colorHex,
               COALESCE(SUM(ta.actualDurationMinutes), 0) AS totalMinutes
        FROM Task t
        JOIN t.goalCategory g
        LEFT JOIN g.userColor gc
        LEFT JOIN t.taskActuals ta
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
        LEFT JOIN t.taskActuals ta
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
        LEFT JOIN t.taskActuals ta
        WHERE t.user.userId = :userId
          AND ta.actualEndAt IS NOT NULL
        GROUP BY t.generalCategory.generalCategoryId, ge.generalCategoryName, gc.colorCode
    """)
    List<CategoryStatProjection> findGeneralCategoryStats(@Param("userId") Long userId);
}