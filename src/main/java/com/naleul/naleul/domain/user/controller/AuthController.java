package com.naleul.naleul.domain.user.controller;

import com.naleul.naleul.domain.user.dto.LoginResponse;
import com.naleul.naleul.domain.user.service.KakaoAuthService;
import com.naleul.naleul.domain.user.service.TokenReissueService;
import com.naleul.naleul.global.common.response.ApiResponse;
import com.naleul.naleul.global.common.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoAuthService kakaoAuthService;
    private final TokenReissueService tokenReissueService;

    @GetMapping("/kakao/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoCallback(@RequestParam String code) {
        LoginResponse response = kakaoAuthService.kakaoLogin(code);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.LOGIN_SUCCESS, response));
    }

    @GetMapping("/token-reissue")
    public ResponseEntity<ApiResponse<LoginResponse>> reissueToken(
            @RequestHeader("Authorization-Refresh") String authHeader  // "Bearer {refreshToken}"
    ) {
        // "Bearer " 제거
        String refreshToken = authHeader.substring(7);
        LoginResponse response = tokenReissueService.reissue(refreshToken);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.LOGIN_SUCCESS, response));
    }
}