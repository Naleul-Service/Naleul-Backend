package com.naleul.naleul.domain.color.repository;

import com.naleul.naleul.domain.color.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColorRepository extends JpaRepository<Color, Long> {
}
