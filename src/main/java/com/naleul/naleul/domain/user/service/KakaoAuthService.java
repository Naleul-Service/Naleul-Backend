package com.naleul.naleul.domain.user.service;

import com.naleul.naleul.domain.user.dto.KakaoTokenResponse;
import com.naleul.naleul.domain.user.dto.KakaoUserInfo;
import com.naleul.naleul.domain.user.dto.LoginResponse;
import com.naleul.naleul.domain.user.entity.RefreshToken;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.user.enums.UserRole;
import com.naleul.naleul.domain.user.repository.RefreshTokenRepository;
import com.naleul.naleul.domain.user.repository.UserRepository;
import com.naleul.naleul.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Transactional
    public LoginResponse kakaoLogin(String code) {
        // 1. 인가코드로 카카오 액세스 토큰 받기
        KakaoTokenResponse tokenResponse = getKakaoToken(code);

        // 2. 액세스 토큰으로 카카오 유저 정보 받기
        KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(tokenResponse.getAccessToken());

        // 3. DB에 유저 저장 또는 조회 (없으면 회원가입, 있으면 로그인)
        User user = saveOrGetUser(kakaoUserInfo);

        // 4. refreshToken DB 저장(있으면 업데이트, 없으면 새로 저장)
        saveOrUpdateRefreshToken(user.getUserId(), tokenResponse);

        // 4. JWT 발급
        String jwtToken = jwtProvider.generateToken(user.getUserId(), user.getUserRole());

        return new LoginResponse(jwtToken, user.getUserId(), user.getUserName(), user.getUserEmail(), user.getUserRole());
    }

    private KakaoTokenResponse getKakaoToken(String code) {
        return WebClient.create("https://kauth.kakao.com")
                .post()
                .uri("/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .bodyValue("grant_type=authorization_code"
                        + "&client_id=" + clientId
                        + "&redirect_uri=" + redirectUri
                        + "&code=" + code
                        + "&client_secret=" + clientSecret)
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .block();
    }

    private KakaoUserInfo getKakaoUserInfo(String accessToken) {
        return WebClient.create("https://kapi.kakao.com")
                .get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfo.class)
                .block();
    }

    private User saveOrGetUser(KakaoUserInfo kakaoUserInfo) {
        return userRepository.findByUserEmail(kakaoUserInfo.getEmail())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .userName(kakaoUserInfo.getNickname())
                                .userEmail(kakaoUserInfo.getEmail())
                                .userRole(UserRole.FREE)
                                .build()
                ));
    }

    private void saveOrUpdateRefreshToken(Long userId, KakaoTokenResponse tokenResponse) {
        // 만료 시각 계산 (카카오가 초 단위로 줌)
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(tokenResponse.getRefreshTokenExpiresIn());

        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        // 이미 있으면 → 토큰 값만 업데이트
                        existing -> existing.updateToken(tokenResponse.getRefreshToken(), expiresAt),
                        // 없으면 → 새로 저장
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .userId(userId)
                                        .token(tokenResponse.getRefreshToken())
                                        .expiresAt(expiresAt)
                                        .build()
                        )
                );
    }

    public KakaoTokenResponse reissueKakaoToken(String refreshToken) {
        return WebClient.create("https://kauth.kakao.com")
                .post()
                .uri("/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .bodyValue("grant_type=refresh_token"
                        + "&client_id=" + clientId
                        + "&refresh_token=" + refreshToken
                        + "&client_secret=" + clientSecret)
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .block();
    }

}