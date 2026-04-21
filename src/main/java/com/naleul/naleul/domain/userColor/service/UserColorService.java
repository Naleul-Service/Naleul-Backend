package com.naleul.naleul.domain.userColor.service;

// domain/color/service/UserColorService.java

import com.naleul.naleul.domain.color.dto.ColorResponse;
import com.naleul.naleul.domain.color.entity.Color;
import com.naleul.naleul.domain.color.repository.ColorRepository;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.user.repository.UserRepository;
import com.naleul.naleul.domain.userColor.dto.response.UserColorResponse;
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
@Transactional(readOnly = true)
public class UserColorService {

    private final UserColorRepository userColorRepository;
    private final ColorRepository colorRepository;
    private final UserRepository userRepository;

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void assignDefaultColors(Long userId) {
        User user = getUser(userId);
        List<Color> defaultColors = colorRepository.findAllBySystemColor(true);

        List<UserColor> userColors = defaultColors.stream()
                .map(color -> UserColor.builder()
                        .user(user)
                        .color(color)
                        .defaultColor(true)
                        .build())
                .toList();

        userColorRepository.saveAll(userColors);
    }

    public List<UserColorResponse> getUserColors(Long userId) {
        return userColorRepository.findByUser_UserId(userId)
                .stream()
                .map(UserColorResponse::from)
                .toList();
    }

    @Transactional
    public void addColorToUser(Long userId, String colorCode) {
        if (userColorRepository.existsByUser_UserIdAndColor_ColorCode(userId, colorCode)) {
            throw new CustomException(ErrorCode.COLOR_ALREADY_EXISTS);
        }

        User user = getUser(userId);

        Color color = colorRepository.save(
                Color.builder()
                        .colorCode(colorCode)
                        .build()
        );

        userColorRepository.save(UserColor.builder()
                .user(user)
                .color(color)
                .defaultColor(false)
                .build());
    }


    @Transactional
    public void deleteUserColor(Long userId, Long userColorId) {
        UserColor userColor = userColorRepository
                .findByUserColorIdAndUser_UserId(userColorId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.COLOR_NOT_FOUND));

        if (userColor.isDefaultColor()) {
            throw new CustomException(ErrorCode.DEFAULT_COLOR_CANNOT_BE_DELETED);
        }

        userColorRepository.delete(userColor);
    }
}