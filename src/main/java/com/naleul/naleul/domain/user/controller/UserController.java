package com.naleul.naleul.domain.user.controller;

import com.naleul.naleul.domain.user.dto.response.UserResponse;
import com.naleul.naleul.domain.user.service.UserService;
import com.naleul.naleul.global.common.response.ApiResponse;
import com.naleul.naleul.global.common.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.USERS_FOUND, userService.getAllUsers()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.USER_FOUND, userService.getUserById(userId)));
    }
}
