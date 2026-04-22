// retrospective/entity/Retrospective.java
package com.naleul.naleul.domain.retrospective.entity;

import com.naleul.naleul.domain.generalCategory.entity.GeneralCategory;
import com.naleul.naleul.domain.goalCategory.entity.GoalCategory;
import com.naleul.naleul.domain.retrospective.enums.ReviewType;
import com.naleul.naleul.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "retrospective")
public class Retrospective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long retrospectiveId;

    // FK - User (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // FK - GoalCategory (N:1, nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_category_id")
    private GoalCategory goalCategory;

    // FK - GeneralCategory (N:1, nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "general_category_id")
    private GeneralCategory generalCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewType reviewType;

    // 일간 기준 날짜 (주간/월간도 이 날짜 기준으로 범위 계산)
    @Column(nullable = false)
    private LocalDate reviewDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted;

    // ── 도메인 메서드 ──────────────────────────────────────

    public void update(String content, GoalCategory goalCategory, GeneralCategory generalCategory) {
        this.content = content;
        this.goalCategory = goalCategory;
        this.generalCategory = generalCategory;
    }

    public void delete() {
        this.isDeleted = true;
    }
}