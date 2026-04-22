// retrospective/repository/RetrospectiveRepository.java
package com.naleul.naleul.domain.retrospective.repository;

import com.naleul.naleul.domain.retrospective.entity.Retrospective;
import com.naleul.naleul.domain.retrospective.enums.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface RetrospectiveRepository extends JpaRepository<Retrospective, Long> {

    // 단건 조회 (연관관계 fetch join)
    @Query("""
        SELECT r FROM Retrospective r
        LEFT JOIN FETCH r.goalCategory
        LEFT JOIN FETCH r.generalCategory
        WHERE r.retrospectiveId = :id
        AND r.isDeleted = false
    """)
    Optional<Retrospective> findActiveById(@Param("id") Long id);

    // 전체 조회 (필터: 타입, 날짜 범위, goalCategory, generalCategory)
    // 모든 필터는 null이면 무시
    @Query("""
        SELECT r FROM Retrospective r
        LEFT JOIN FETCH r.goalCategory
        LEFT JOIN FETCH r.generalCategory
        WHERE r.user.userId = :userId
        AND r.isDeleted = false
        AND (:reviewType IS NULL OR r.reviewType = :reviewType)
        AND (:startDate IS NULL OR r.reviewDate >= :startDate)
        AND (:endDate IS NULL OR r.reviewDate <= :endDate)
        AND (:goalCategoryId IS NULL OR r.goalCategory.goalCategoryId = :goalCategoryId)
        AND (:generalCategoryId IS NULL OR r.generalCategory.generalCategoryId = :generalCategoryId)
    """)
    Page<Retrospective> findAllByFilter(
            @Param("userId") Long userId,
            @Param("reviewType") ReviewType reviewType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("goalCategoryId") Long goalCategoryId,
            @Param("generalCategoryId") Long generalCategoryId,
            Pageable pageable
    );

    @Query("""
    SELECT r FROM Retrospective r
    LEFT JOIN FETCH r.goalCategory
    LEFT JOIN FETCH r.generalCategory
    WHERE r.retrospectiveId = :id
    AND r.user.userId = :userId
    AND r.isDeleted = false
""")
    Optional<Retrospective> findActiveByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );
}