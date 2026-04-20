package com.naleul.naleul.domain.dayOfTheWeek.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "day_of_week")
public class WeekDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dayOfWeekId;

    @Column(nullable = false, unique = true, length = 3)
    private String dayName; // "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"

    @Column(nullable = false)
    private int dayOrder; // 1=MON ~ 7=SUN (정렬용)
}
