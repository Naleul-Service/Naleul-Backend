package com.naleul.naleul.domain.color.repository;

import com.naleul.naleul.domain.color.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColorRepository extends JpaRepository<Color, Long> {

    // assignDefaultColors에서 사용
    List<Color> findAllBySystemColor(boolean systemColor);

    // addColorToUser 중복 방지에서 사용
    boolean existsByColorCode(String colorCode);
}