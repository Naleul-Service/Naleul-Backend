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
@Table(name = "task_actual")  // uniqueConstraints 제거
public class TaskActual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskActualId;

    @OneToOne(fetch = FetchType.LAZY)  // ManyToOne → OneToOne
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    private Task task;

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