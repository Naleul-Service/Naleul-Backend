package com.naleul.naleul.domain.user.service;

import com.naleul.naleul.domain.user.dto.KakaoTokenResponse;
import com.naleul.naleul.domain.user.entity.RefreshToken;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.user.repository.RefreshTokenRepository;
import com.naleul.naleul.domain.user.repository.UserRepository;
import com.naleul.naleul.global.jwt.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final KakaoAuthService kakaoAuthService;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    /**
     * JWT 만료 시 자동으로 호출되는 메서드
     * refreshToken → 카카오 새 accessToken → 우리 서버 새 JWT 반환
     */
    @Transactional
    public String reissueJwt(Long userId) {
        // 1. DB에서 refreshToken 조회
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("REFRESH_TOKEN_NOT_FOUND"));

        // 2. refreshToken 만료 확인
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.deleteByUserId(userId);
            throw new RuntimeException("REFRESH_TOKEN_EXPIRED");
        }

        // 3. 카카오에 새 accessToken 요청
        KakaoTokenResponse kakaoResponse = kakaoAuthService.reissueKakaoToken(refreshToken.getToken());

        // 4. 카카오가 새 refreshToken도 줬다면 DB 업데이트
        //    (카카오 정책: refreshToken 만료 1개월 전부터 갱신 시 새 refreshToken도 같이 줌)
        if (kakaoResponse.getRefreshToken() != null) {
            LocalDateTime newExpiresAt = LocalDateTime.now()
                    .plusSeconds(kakaoResponse.getRefreshTokenExpiresIn());
            refreshToken.updateToken(kakaoResponse.getRefreshToken(), newExpiresAt);
            log.info("RefreshToken도 갱신됨 - userId: {}", userId);
        }

        // 5. 우리 서버 JWT 새로 발급
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        String newJwt = jwtProvider.generateToken(user.getUserId(), user.getUserRole());
        log.info("JWT 자동 갱신 완료 - userId: {}", userId);

        return newJwt;
    }
}