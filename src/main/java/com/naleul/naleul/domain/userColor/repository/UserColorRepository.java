package com.naleul.naleul.domain.userColor.repository;


import com.naleul.naleul.domain.color.entity.Color;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.userColor.entity.UserColor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserColorRepository extends JpaRepository<UserColor, Long> {

    List<UserColor> findByUser(User user);

    boolean existsByUserAndColor(User user, Color color);
}