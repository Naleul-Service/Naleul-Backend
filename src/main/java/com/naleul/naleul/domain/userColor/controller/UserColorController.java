package com.naleul.naleul.domain.userColor.controller;

// domain/userColor/controller/UserColorController.java

import com.naleul.naleul.domain.color.dto.ColorResponse;
import com.naleul.naleul.domain.color.entity.Color;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.user.repository.UserRepository;
import com.naleul.naleul.domain.userColor.dto.request.UserColorAddRequest;
import com.naleul.naleul.domain.userColor.dto.response.UserColorResponse;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserColorResponse>>> getUserColors(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.COLORS_FOUND, userColorService.getUserColors(userId))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addColor(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserColorAddRequest request
    ) {
        userColorService.addColorToUser(userId, request.getColorCode());
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.COLOR_CREATED, null)
        );
    }

    @DeleteMapping("/{userColorId}")
    public ResponseEntity<ApiResponse<Void>> deleteUserColor(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long userColorId
    ) {
        userColorService.deleteUserColor(userId, userColorId);
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.COLOR_DELETED, null)
        );
    }
}