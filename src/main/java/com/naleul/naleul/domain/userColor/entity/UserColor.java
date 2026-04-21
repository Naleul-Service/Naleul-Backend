package com.naleul.naleul.domain.userColor.entity;

// domain/color/entity/UserColor.java

import com.naleul.naleul.domain.color.entity.Color;
import com.naleul.naleul.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_color")
public class UserColor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userColorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id", nullable = false)
    private Color color;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDefault = false;  // 기본 제공 색상 여부
}