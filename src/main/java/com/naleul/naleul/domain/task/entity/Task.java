package com.naleul.naleul.domain.task.entity;


import com.naleul.naleul.domain.generalCategory.entity.GeneralCategory;
import com.naleul.naleul.domain.goalCategory.entity.GoalCategory;
import com.naleul.naleul.domain.task.enums.TaskPriority;
import com.naleul.naleul.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @Column(nullable = false)
    private String taskName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority taskPriority;

    // FK - User (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // FK - GoalCategory (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_category_id", nullable = false)
    private GoalCategory goalCategory;

    // FK - GeneralCategory (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "general_category_id", nullable = false)
    private GeneralCategory generalCategory;

    // 계획 시간
    private LocalDateTime plannedStartAt;
    private LocalDateTime plannedEndAt;
    private Long plannedDurationMinutes; // 분 단위

    private LocalDateTime actualStartAt;
    private LocalDateTime actualEndAt;
    private Long actualDurationMinutes;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskActual> taskActuals = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean defaultSettingStatus;

    // 중간 테이블과의 연관관계 (cascade: Task 삭제 시 TaskDayOfWeek도 같이 삭제)
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskDayOfWeek> taskDayOfWeeks = new ArrayList<>();

    // ── 도메인 메서드 ──────────────────────────────────────

    // 실제 시작/종료 기록
    public void recordActual(LocalDateTime startAt, LocalDateTime endAt) {
        this.actualStartAt = startAt;
        this.actualEndAt = endAt;
        if (startAt != null && endAt != null) {
            this.actualDurationMinutes = java.time.Duration.between(startAt, endAt).toMinutes();
        }
    }

    // 기본 정보 수정
    public void update(String name, TaskPriority priority,
                       GoalCategory goalCategory, GeneralCategory generalCategory,
                       LocalDateTime plannedStartAt, LocalDateTime plannedEndAt, boolean defaultSettingStatus) {
        this.taskName = name;
        this.taskPriority = priority;
        this.goalCategory = goalCategory;
        this.generalCategory = generalCategory;
        this.plannedStartAt = plannedStartAt;
        this.plannedEndAt = plannedEndAt;
        if (plannedStartAt != null && plannedEndAt != null) {
            this.plannedDurationMinutes = java.time.Duration.between(plannedStartAt, plannedEndAt).toMinutes();
        }
        this.defaultSettingStatus = defaultSettingStatus;
    }

    // Task 엔티티 내부 유틸 메서드
    private Long calculateMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return null;
        return java.time.Duration.between(start, end).toMinutes();
    }
}
