package com.naleul.naleul.domain.actualTask.repository;

import com.naleul.naleul.domain.actualTask.entity.TaskActual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskActualRepository extends JpaRepository<TaskActual, Long> {

    Optional<TaskActual> findByTaskTaskId(Long taskId);

    // TaskActualRepository
    // "Task 1개당 Actual 1개" 제약을 명시적으로 표현
    Optional<TaskActual> findFirstByTaskTaskId(Long taskId);

    // 추가
    // TaskActualRepository — findDailyActuals 쿼리만 수정
    @Query("""
    SELECT ta FROM TaskActual ta
    JOIN FETCH ta.goalCategory g
    JOIN FETCH ta.generalCategory ge
    WHERE ta.user.userId = :userId
      AND ta.actualStartAt >= :kstDayStart
      AND ta.actualStartAt <  :kstDayEnd
      AND (:goalCategoryId IS NULL OR ta.goalCategory.goalCategoryId = :goalCategoryId)
      AND (:generalCategoryId IS NULL OR ta.generalCategory.generalCategoryId = :generalCategoryId)
    ORDER BY ta.actualStartAt ASC
    """)
    List<TaskActual> findDailyActuals(
            @Param("userId") Long userId,
            @Param("kstDayStart") LocalDateTime kstDayStart,
            @Param("kstDayEnd") LocalDateTime kstDayEnd,
            @Param("goalCategoryId") Long goalCategoryId,
            @Param("generalCategoryId") Long generalCategoryId
    );

    @Query("""
    SELECT ta FROM TaskActual ta
    WHERE ta.taskActualId = :taskActualId
      AND ta.user.userId = :userId
    """)
    Optional<TaskActual> findByTaskActualIdAndUserId(
            @Param("taskActualId") Long taskActualId,
            @Param("userId") Long userId
    );

    // TaskActualRepository.java
    @Query("""
    SELECT ta FROM TaskActual ta
    JOIN FETCH ta.goalCategory g
    JOIN FETCH ta.generalCategory ge
    WHERE ta.user.userId = :userId
      AND ta.actualStartAt >= :kstDayStart
      AND ta.actualStartAt <  :kstDayEnd
      AND (:goalCategoryId IS NULL OR ta.goalCategory.goalCategoryId = :goalCategoryId)
      AND (:generalCategoryId IS NULL OR ta.generalCategory.generalCategoryId = :generalCategoryId)
    ORDER BY ta.actualStartAt ASC
    """)
    List<TaskActual> findWeeklyActuals(
            @Param("userId") Long userId,
            @Param("kstWeekStart") LocalDateTime kstWeekStart,
            @Param("kstWeekEnd") LocalDateTime kstWeekEnd,
            @Param("goalCategoryId") Long goalCategoryId,
            @Param("generalCategoryId") Long generalCategoryId
    );
}