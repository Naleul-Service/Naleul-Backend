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

    @Column(nullable = false, unique = true, length = 10)
    private String dayName; //'MONDAY'-1, 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'

    @Column(nullable = false)
    private int dayOrder; // 1=MON ~ 7=SUN (정렬용)
}
