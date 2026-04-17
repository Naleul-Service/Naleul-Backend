package com.naleul.naleul.domain.user.controller;

import com.naleul.naleul.domain.user.dto.LoginResponse;
import com.naleul.naleul.domain.user.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoAuthService kakaoAuthService;

    @GetMapping("/kakao/callback")
    public ResponseEntity<LoginResponse> kakaoCallback(@RequestParam String code) {
        LoginResponse response = kakaoAuthService.kakaoLogin(code);
        return ResponseEntity.ok(response);
    }
}