package com.naleul.naleul.domain.task.entity;

import com.naleul.naleul.domain.dayOfTheWeek.entity.WeekDay;
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
@Table(
        name = "task_day_of_week",
        uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "day_of_week_id"}) // 동일 요일 중복 방지
)
public class TaskDayOfWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_of_week_id", nullable = false)
    private WeekDay dayOfWeek;
}