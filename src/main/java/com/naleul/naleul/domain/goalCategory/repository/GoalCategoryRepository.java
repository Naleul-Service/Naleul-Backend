package com.naleul.naleul.domain.goalCategory.repository;

import com.naleul.naleul.domain.goalCategory.dto.response.CompletedGoalCategoryResponse;
import com.naleul.naleul.domain.goalCategory.entity.GoalCategory;
import com.naleul.naleul.domain.goalCategory.enums.GoalCategoryStatus;
import com.naleul.naleul.domain.userColor.entity.UserColor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GoalCategoryRepository extends JpaRepository<GoalCategory, Long> {

    @Query("""
        SELECT goalCategory FROM GoalCategory goalCategory
        LEFT JOIN FETCH goalCategory.userColor
        LEFT JOIN FETCH goalCategory.generalCategories generalCategory
        LEFT JOIN FETCH generalCategory.color
        WHERE goalCategory.goalCategoryId = :goalCategoryId
        AND goalCategory.goalCategoryStatus != 'DELETED'
    """)
    Optional<GoalCategory> findByIdWithAll(@Param("goalCategoryId") Long goalCategoryId);

    @Query("""
        SELECT goalCategory FROM GoalCategory goalCategory
        LEFT JOIN FETCH goalCategory.userColor
        LEFT JOIN FETCH goalCategory.generalCategories generalCategory
        LEFT JOIN FETCH generalCategory.color
        WHERE goalCategory.user.userId = :userId
        AND goalCategory.goalCategoryStatus != 'DELETED'
    """)
    List<GoalCategory> findAllByUserIdWithAll(@Param("userId") Long userId);

    boolean existsByUser_UserIdAndGoalCategoryName(Long userId, String goalCategoryName);

    boolean existsByUser_UserIdAndGoalCategoryNameAndGoalCategoryIdNot(
            Long userId, String goalCategoryName, Long goalCategoryId
    );

    @Query("""
        SELECT g FROM GoalCategory g
        WHERE g.goalCategoryId = :goalCategoryId
        AND g.goalCategoryStatus != 'DELETED'
    """)
    Optional<GoalCategory> findActiveById(@Param("goalCategoryId") Long goalCategoryId);

    // Repository — durationDays 계산 제거, 날짜 두 개로 받기
    @Query(value = """
    SELECT new com.naleul.naleul.domain.goalCategory.dto.response.CompletedGoalCategoryResponse(
        gc.goalCategoryId,
        gc.goalCategoryName,
        gc.achievement,
        SUM(ta.actualDurationMinutes),
        gc.goalCategoryStartDate,
        gc.goalCategoryEndDate,
        COUNT(t.taskId)
    )
    FROM GoalCategory gc
    LEFT JOIN Task t ON t.goalCategory = gc
    LEFT JOIN TaskActual ta ON ta.task = t
    WHERE gc.user.userId = :userId
      AND gc.goalCategoryStatus = :status
    GROUP BY gc.goalCategoryId,
             gc.goalCategoryName,
             gc.achievement,
             gc.goalCategoryStartDate,
             gc.goalCategoryEndDate
    """,
            countQuery = """
    SELECT COUNT(gc.goalCategoryId)
    FROM GoalCategory gc
    WHERE gc.user.userId = :userId
      AND gc.goalCategoryStatus = :status
    """)
    Page<CompletedGoalCategoryResponse> findCompletedByUserId(
            @Param("userId") Long userId,
            @Param("status") GoalCategoryStatus status,
            Pageable pageable
    );

    @Query("""
    SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END
    FROM GoalCategory g
    WHERE g.userColor = :color
    AND g.goalCategoryStatus != :deletedStatus
""")
    boolean existsByUserColorAndNotDeleted(
            @Param("color") UserColor color,
            @Param("deletedStatus") GoalCategoryStatus deletedStatus
    );
}