package com.naleul.naleul.domain.user.service;

import com.naleul.naleul.domain.user.dto.LoginResponse;
import com.naleul.naleul.domain.user.entity.RefreshToken;
import com.naleul.naleul.domain.user.entity.User;
import com.naleul.naleul.domain.user.repository.RefreshTokenRepository;
import com.naleul.naleul.domain.user.repository.UserRepository;
import com.naleul.naleul.global.jwt.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenReissueService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    // 필터에서 호출 (만료된 accessToken → userId 추출 후 호출)
    @Transactional
    public LoginResponse reissue(Long userId) {
        return reissueByUserId(userId);
    }

    // 컨트롤러에서 호출 (refreshToken 헤더로 직접 요청)
    @Transactional
    public LoginResponse reissue(String refreshToken) {
        // refreshToken 자체 만료 검증
        if (jwtProvider.isExpired(refreshToken)) {
            throw new RuntimeException("Refresh token expired");
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        // DB의 토큰과 일치하는지 검증 (토큰 탈취 방지)
        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (!savedToken.getToken().equals(refreshToken)) {
            throw new RuntimeException("Refresh token mismatch");
        }

        return reissueByUserId(userId);
    }

    // 공통 로직
    private LoginResponse reissueByUserId(Long userId) {
        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.deleteByUserId(userId);
            throw new RuntimeException("Refresh token expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtProvider.generateAccessToken(user.getUserId(), user.getUserRole());
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getUserId());

        savedToken.updateToken(newRefreshToken, LocalDateTime.now().plusDays(14));

        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                user.getUserId(),
                user.getUserName(),
                user.getUserEmail(),
                user.getUserRole()
        );
    }
}