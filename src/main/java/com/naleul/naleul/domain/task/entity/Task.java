package com.naleul.naleul.domain.task.entity;

import com.naleul.naleul.domain.actualTask.entity.TaskActual;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_category_id", nullable = false)
    private GoalCategory goalCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "general_category_id", nullable = false)
    private GeneralCategory generalCategory;

    private LocalDateTime plannedStartAt;
    private LocalDateTime plannedEndAt;
    private Long plannedDurationMinutes;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean defaultSettingStatus;

    @Builder.Default
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskActual> taskActuals = new ArrayList<>();

    // ── 도메인 메서드 ──────────────────────────────────────

    public void update(String name, TaskPriority priority,
                       GoalCategory goalCategory, GeneralCategory generalCategory,
                       LocalDateTime plannedStartAt, LocalDateTime plannedEndAt,
                       boolean defaultSettingStatus) {
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
}