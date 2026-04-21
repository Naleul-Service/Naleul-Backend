package com.naleul.naleul.domain.userColor.controller;

// domain/userColor/controller/UserColorController.java

import com.naleul.naleul.domain.color.dto.ColorResponse;
import com.naleul.naleul.domain.color.entity.Color;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.user.repository.UserRepository;
import com.naleul.naleul.domain.userColor.dto.request.UserColorAddRequest;
import com.naleul.naleul.domain.userColor.service.UserColorService;
import com.naleul.naleul.global.common.response.ApiResponse;
import com.naleul.naleul.global.common.response.ErrorCode;
import com.naleul.naleul.global.common.response.SuccessCode;
import com.naleul.naleul.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-colors")
public class UserColorController {

    private final UserColorService userColorService;
    private final UserRepository userRepository;

    // 유저 색상 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<ColorResponse>>> getUserColors(
            @AuthenticationPrincipal Long userId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.COLORS_FOUND, userColorService.getUserColors(user)));
    }

    // 유저 색상 추가
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addColor(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserColorAddRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        userColorService.addColorToUser(user, request.getColorCode()); // colorId → colorCode

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.COLOR_CREATED, null));
    }

    // 유저 색상 삭제
    @DeleteMapping("/{userColorId}")
    public ResponseEntity<ApiResponse<Void>> deleteUserColor(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long userColorId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        userColorService.deleteUserColor(user, userColorId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.COLOR_DELETED, null));
    }
}