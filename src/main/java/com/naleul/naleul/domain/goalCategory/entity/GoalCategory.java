package com.naleul.naleul.domain.goalCategory.entity;

import com.naleul.naleul.domain.generalCategory.entity.GeneralCategory;
import com.naleul.naleul.domain.goalCategory.enums.GoalCategoryStatus;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.userColor.entity.UserColor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "goal_category")
public class GoalCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalCategoryId;

    // FK - User (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // FK - Color (1:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id")
    private UserColor userColor;

    @Column(nullable = false)
    private String goalCategoryName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalCategoryStatus goalCategoryStatus;

    private LocalDate goalCategoryStartDate;
    private LocalDate goalCategoryEndDate;

    private String achievement;

    // 양방향 필요하면 추가 (선택)
    @OneToMany(mappedBy = "goalCategory", fetch = FetchType.LAZY)
    private List<GeneralCategory> generalCategories = new ArrayList<>();

    public void complete(LocalDate endDate, String achievement) {
        this.goalCategoryEndDate = endDate;
        this.achievement = achievement;
        this.goalCategoryStatus = GoalCategoryStatus.COMPLETED;
    }

    public void delete() {
        this.goalCategoryStatus = GoalCategoryStatus.DELETED;
        this.userColor = null;
    }

    public void update(String name, GoalCategoryStatus status, LocalDate startDate) {
        this.goalCategoryName = name;
        this.goalCategoryStatus = status;
        this.goalCategoryStartDate = startDate;
    }

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isDefault; // 기타(ETC) 여부 판별용

    public void updateColor(UserColor color) {
        this.userColor = color;
    }
}
