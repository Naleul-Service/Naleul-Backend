package com.naleul.naleul.domain.task.repository;

import com.naleul.naleul.domain.task.entity.TaskActual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskActualRepository extends JpaRepository<TaskActual, Long> {

    Optional<TaskActual> findByTaskTaskId(Long taskId);
}