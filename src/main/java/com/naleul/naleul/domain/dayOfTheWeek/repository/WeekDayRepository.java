package com.naleul.naleul.domain.dayOfTheWeek.repository;

import com.naleul.naleul.domain.dayOfTheWeek.entity.WeekDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeekDayRepository extends JpaRepository<WeekDay, Long> {

    List<WeekDay> findByDayOfWeekIdIn(List<Long> ids);
}