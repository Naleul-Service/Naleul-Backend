package com.naleul.naleul.domain.goalCategory.repository;

import com.naleul.naleul.domain.goalCategory.entity.GoalCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GoalCategoryRepository extends JpaRepository<GoalCategory, Long> {
    @Query("""
        SELECT goalCategory FROM GoalCategory goalCategory
        LEFT JOIN FETCH goalCategory.color
        LEFT JOIN FETCH goalCategory.generalCategories generalCategory
        LEFT JOIN FETCH generalCategory.color
        WHERE goalCategory.goalCategoryId = :goalCategoryId
    """)
    Optional<GoalCategory> findByIdWithAll(@Param("goalCategoryId") Long goalCategoryId);

    // ✅ 유저별 전체 조회 (fetch join)
    @Query("""
        SELECT goalCategory FROM GoalCategory goalCategory
        LEFT JOIN FETCH goalCategory.color
        LEFT JOIN FETCH goalCategory.generalCategories generalCategory
        LEFT JOIN FETCH generalCategory.color
        WHERE goalCategory.user.userId = :userId
    """)
    List<GoalCategory> findAllByUserIdWithAll(@Param("userId") Long userId);

    // 2번: 동일 유저의 동일 카테고리명 중복 체크
    boolean existsByUser_UserIdAndGoalCategoryName(Long userId, String goalCategoryName);

    // 자기 자신 제외한 중복 체크
    boolean existsByUser_UserIdAndGoalCategoryNameAndGoalCategoryIdNot(
            Long userId,
            String goalCategoryName,
            Long goalCategoryId
    );
}
