package com.naleul.naleul.domain.userColor.service;

// domain/color/service/UserColorService.java

import com.naleul.naleul.domain.color.dto.ColorResponse;
import com.naleul.naleul.domain.color.entity.Color;
import com.naleul.naleul.domain.color.repository.ColorRepository;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.userColor.entity.UserColor;
import com.naleul.naleul.domain.userColor.repository.UserColorRepository;
import com.naleul.naleul.global.common.response.ErrorCode;
import com.naleul.naleul.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserColorService {

    private final UserColorRepository userColorRepository;
    private final ColorRepository colorRepository;

    // 기본 색상 ID 목록 (data.sql과 일치해야 함)
    private static final List<Long> DEFAULT_COLOR_IDS =
            List.of(1L,2L,3L,4L,5L,6L,7L,8L,9L,10L,
                    11L,12L,13L,14L,15L,16L,17L,18L,19L,20L,21L);

    @Transactional
    public void assignDefaultColors(User user) {
        List<Color> defaultColors = colorRepository.findAllById(DEFAULT_COLOR_IDS);

        List<UserColor> userColors = defaultColors.stream()
                .map(color -> UserColor.builder()
                        .user(user)
                        .color(color)
                        .isDefault(true)
                        .build())
                .toList();

        userColorRepository.saveAll(userColors);
    }

    // 유저의 색상 목록 조회
    @Transactional(readOnly = true)
    public List<ColorResponse> getUserColors(User user) {
        return userColorRepository.findByUser(user)
                .stream()
                .map(userColor -> ColorResponse.from(userColor.getColor()))
                .toList();
    }

    // 유저가 색상 추가
    @Transactional
    public void addColorToUser(User user, Long colorId) {
        Color color = colorRepository.findById(colorId)
                .orElseThrow(() -> new CustomException(ErrorCode.COLOR_NOT_FOUND));

        if (userColorRepository.existsByUserAndColor(user, color)) {
            throw new CustomException(ErrorCode.COLOR_ALREADY_EXISTS); // 중복 방지
        }

        userColorRepository.save(UserColor.builder()
                .user(user)
                .color(color)
                .isDefault(false)
                .build());
    }

    // 유저의 색상 삭제
    @Transactional
    public void deleteUserColor(User user, Long userColorId) {
        UserColor userColor = userColorRepository.findById(userColorId)
                .orElseThrow(() -> new CustomException(ErrorCode.COLOR_NOT_FOUND));

        if (userColor.isDefault()) {
            throw new CustomException(ErrorCode.DEFAULT_COLOR_CANNOT_BE_DELETED);
        }

        userColorRepository.delete(userColor);
    }
}