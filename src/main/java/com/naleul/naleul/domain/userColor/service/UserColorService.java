package com.naleul.naleul.domain.userColor.service;

// domain/color/service/UserColorService.java

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
    private final UserRepository userRepository;

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public class DefaultColors {
        public static final List<String> CODES = List.of(
                "#FF0000", "#FF5733", "#FF8C00",
                "#FFD700", "#ADFF2F", "#00FF00",
                "#00FA9A", "#00FFFF", "#1E90FF",
                "#0000FF", "#8A2BE2", "#FF00FF",
                "#FF69B4", "#A52A2A", "#808080",
                "#000000", "#FFFFFF", "#F5F5DC",
                "#40E0D0", "#FF7F50", "#6495ED"
        );
    }

    public void createDefaultColors(User user) {
        List<UserColor> defaultColors = DefaultColors.CODES.stream()
                .map(code -> UserColor.builder()
                        .user(user)
                        .colorCode(code)
                        .isDefault(true)
                        .build())
                .toList();
        userColorRepository.saveAll(defaultColors);
    }

    @Transactional(readOnly = true)
    public List<UserColorResponse> getUserColors(Long userId) {
        return userColorRepository.findByUser_UserId(userId)
                .stream()
                .map(UserColorResponse::from)
                .toList();
    }

    @Transactional
    public void addColorToUser(Long userId, String colorCode) {
        if (userColorRepository.existsByUser_UserIdAndColorCode(userId, colorCode)) {
            throw new CustomException(ErrorCode.COLOR_ALREADY_EXISTS);
        }

        User user = getUser(userId);

        userColorRepository.save(UserColor.builder()
                .user(user)
                .colorCode(colorCode)
                .isDefault(false)
                .build());
    }

    @Transactional
    public void deleteUserColor(Long userId, Long userColorId) {
        UserColor userColor = userColorRepository
                .findByUserColorIdAndUser_UserId(userColorId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.COLOR_NOT_FOUND));

        if (userColor.isDefault()) {
            throw new CustomException(ErrorCode.DEFAULT_COLOR_CANNOT_BE_DELETED);
        }

        userColorRepository.delete(userColor);
    }
}