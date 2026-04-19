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

    public void assignGoalCategory(GoalCategory goalCategory) {
        this.goalCategory = goalCategory;
    }
}

