package com.naleul.naleul.domain.user.service;

import com.naleul.naleul.domain.goalCategory.service.GoalCategoryService;
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
    private final GoalCategoryService goalCategoryService;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Transactional
    public LoginResponse kakaoLogin(String code, String redirectUri) {  // redirectUri 파라미터로 받음
        // 1. 인가코드로 카카오 액세스 토큰 받기
        KakaoTokenResponse tokenResponse = getKakaoToken(code, redirectUri);

        // 2. 액세스 토큰으로 카카오 유저 정보 받기
        KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(tokenResponse.getAccessToken());

        // 3. DB에 유저 저장 또는 조회 (없으면 회원가입, 있으면 로그인)
        User user = saveOrGetUser(kakaoUserInfo);

        // 4. JWT 발급
        String accessToken = jwtProvider.generateAccessToken(user.getUserId(), user.getUserRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());

        saveOrUpdateRefreshToken(user.getUserId(), refreshToken);

        return new LoginResponse(accessToken, refreshToken, user.getUserId(), user.getUserName(), user.getUserEmail(), user.getUserRole());
    }

    private KakaoTokenResponse getKakaoToken(String code, String redirectUri) {
        return WebClient.create("https://kauth.kakao.com")
                .post()
                .uri("/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .bodyValue("grant_type=authorization_code"
                        + "&client_id=" + clientId
                        + "&redirect_uri=" + redirectUri  // 동적으로 받은 값 사용
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
                .orElseGet(() -> {
                    User newUser = userRepository.save(
                            User.builder()
                                    .userName(kakaoUserInfo.getNickname())
                                    .userEmail(kakaoUserInfo.getEmail())
                                    .userRole(UserRole.FREE)
                                    .build()
                    );

                    // 신규 유저에게만 ETC 목표 자동 생성
                    goalCategoryService.createDefaultEtcCategory(newUser);

                    return newUser;
                });
    }

    private void saveOrUpdateRefreshToken(Long userId, String refreshToken) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(14);

        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        existing -> existing.updateToken(refreshToken, expiresAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .userId(userId)
                                        .token(refreshToken)
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