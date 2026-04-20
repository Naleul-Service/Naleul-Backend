package com.naleul.naleul.domain.generalCategory.entity;

import com.naleul.naleul.domain.color.entity.Color;
import com.naleul.naleul.domain.goalCategory.entity.GoalCategory;
import com.naleul.naleul.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "general_category")
public class GeneralCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long generalCategoryId;

    @Column(nullable = false)
    private String generalCategoryName;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isDefault; // ETC 여부 판별용

    // FK - User (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // FK - GoalCategory (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_category_id", nullable = false)
    private GoalCategory goalCategory;

    // FK - Color (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id", unique = true)
    private Color color;

    // ✅ 정적 팩토리 메서드 - 일반 생성
    public static GeneralCategory create(String name, User user, GoalCategory goalCategory, Color color) {
        GeneralCategory generalCategory = new GeneralCategory();
        generalCategory.generalCategoryName = name;
        generalCategory.user = user;
        generalCategory.goalCategory = goalCategory;
        generalCategory.color = color;
        generalCategory.isDefault = false;
        return generalCategory;
    }

    // ✅ 정적 팩토리 메서드 - ETC 기본 생성
    public static GeneralCategory createDefault(User user, GoalCategory etcGoalCategory) {
        GeneralCategory generalCategory = new GeneralCategory();
        generalCategory.generalCategoryName = "ETC";
        generalCategory.user = user;
        generalCategory.goalCategory = etcGoalCategory;
        generalCategory.isDefault = true;
        return generalCategory;
    }

    // ✅ 수정 메서드 (setter 대신 도메인 메서드)
    public void update(String name, GoalCategory goalCategory, Color color) {
        this.generalCategoryName = name;
        this.goalCategory = goalCategory;
        this.color = color;
    }

    public void assignGoalCategory(GoalCategory goalCategory) {
        this.goalCategory = goalCategory;
    }
}

