package com.naleul.naleul.domain.task.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

// com/naleul/naleul/domain/task/entity/TaskActual.java
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "task_actual",
        uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "actual_date"}) // 날짜당 1개
)
public class TaskActual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskActualId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false)
    private LocalDate actualDate; // 어느 날짜의 완료인지

    private LocalDateTime actualStartAt;
    private LocalDateTime actualEndAt;
    private Long actualDurationMinutes;

    public void update(LocalDateTime startAt, LocalDateTime endAt) {
        this.actualStartAt = startAt;
        this.actualEndAt = endAt;
        if (startAt != null && endAt != null) {
            this.actualDurationMinutes = Duration.between(startAt, endAt).toMinutes();
        }
    }
}