package com.naleul.naleul.domain.user.service;

import com.naleul.naleul.domain.user.dto.response.UserResponse;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.user.repository.UserRepository;
import com.naleul.naleul.global.exception.CustomException;
import com.naleul.naleul.global.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 조회만 하는 메서드엔 readOnly 붙이는 게 실무 관례
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)  // User 엔티티 → UserResponse DTO 변환
                .toList();
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }
}