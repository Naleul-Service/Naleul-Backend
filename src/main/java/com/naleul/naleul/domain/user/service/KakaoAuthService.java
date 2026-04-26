package com.naleul.naleul.domain.user.service;

import com.naleul.naleul.domain.generalCategory.service.GeneralCategoryService;
import com.naleul.naleul.domain.goalCategory.entity.GoalCategory;
import com.naleul.naleul.domain.goalCategory.service.GoalCategoryService;
import com.naleul.naleul.domain.user.dto.KakaoTokenResponse;
import com.naleul.naleul.domain.user.dto.KakaoUserInfo;
import com.naleul.naleul.domain.user.dto.LoginResponse;
import com.naleul.naleul.domain.user.entity.RefreshToken;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.user.enums.UserRole;
import com.naleul.naleul.domain.user.repository.RefreshTokenRepository;
import com.naleul.naleul.domain.user.repository.UserRepository;
import com.naleul.naleul.domain.userColor.service.UserColorService;
import com.naleul.naleul.global.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Slf4j
@Service
public class KakaoAuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final GoalCategoryService goalCategoryService;
    private final GeneralCategoryService generalCategoryService;
    private final UserColorService userColorService;
    private final String clientId;
    private final String clientSecret;
    private final String defaultRedirectUri;

    // ✅ @RequiredArgsConstructor 제거 — @Value는 생성자 파라미터에 붙여야 final 필드에 주입 가능
    public KakaoAuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtProvider jwtProvider,
            GoalCategoryService goalCategoryService,
            GeneralCategoryService generalCategoryService,
            UserColorService userColorService,
            @Value("${kakao.client-id}") String clientId,
            @Value("${kakao.client-secret}") String clientSecret,
            @Value("${kakao.redirect-uri}") String defaultRedirectUri
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProvider = jwtProvider;
        this.goalCategoryService = goalCategoryService;
        this.generalCategoryService = generalCategoryService;
        this.userColorService = userColorService;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.defaultRedirectUri = defaultRedirectUri;
    }

    @Transactional
    public LoginResponse kakaoLogin(String code, String redirectUri) {
        // ✅ redirectUri가 null이면 설정값 사용
        String uri = (redirectUri != null) ? redirectUri : defaultRedirectUri;

        // 1. 인가코드로 카카오 액세스 토큰 받기
        KakaoTokenResponse tokenResponse = getKakaoToken(code, uri);

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
        log.info("=== 카카오 토큰 요청 ===");
        log.info("redirectUri: {}", redirectUri);
        log.info("clientId: {}", clientId);

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
                .orElseGet(() -> {
                    User newUser = userRepository.save(
                            User.builder()
                                    .userName(kakaoUserInfo.getNickname())
                                    .userEmail(kakaoUserInfo.getEmail())
                                    .userRole(UserRole.FREE)
                                    .build()
                    );

                    GoalCategory etcGoalCategory = goalCategoryService.createDefaultEtcCategory(newUser);
                    generalCategoryService.createDefaultCategory(newUser, etcGoalCategory);
                    userColorService.createDefaultColors(newUser);

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