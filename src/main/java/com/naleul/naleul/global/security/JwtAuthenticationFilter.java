package com.naleul.naleul.global.security;

import com.naleul.naleul.domain.user.service.TokenService;
import com.naleul.naleul.global.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// filter/JwtAuthenticationFilter.java
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Authorization 헤더 없으면 그냥 통과 (로그인 안 한 요청)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.replace("Bearer ", "");

        try {
            if (jwtProvider.isValid(token)) {
                // ✅ 정상 토큰 → SecurityContext에 인증 정보 등록 후 통과
                setAuthentication(token);

            } else if (jwtProvider.isExpired(token)) {
                // ✅ 만료된 토큰 → 자동으로 refreshToken으로 갱신
                log.info("JWT 만료 감지 - 자동 갱신 시도");

                Long userId = jwtProvider.extractUserIdIgnoreExpiration(token);
                String newToken = tokenService.reissueJwt(userId);

                // 새 토큰을 응답 헤더에 담아서 클라이언트에 전달
                // 클라이언트는 이 헤더를 받아서 저장해야 함
                response.setHeader("Authorization", "Bearer " + newToken);
                response.setHeader("Access-Control-Expose-Headers", "Authorization");

                // 새 토큰으로 인증 처리
                setAuthenticationByUserId(userId);

            } else {
                // 위조된 토큰
                log.warn("유효하지 않은 JWT - 요청 차단");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

        } catch (RuntimeException e) {
            log.error("토큰 처리 실패: {}", e.getMessage());

            if (e.getMessage().contains("EXPIRED") || e.getMessage().contains("NOT_FOUND")) {
                // refreshToken도 만료 or 없음 → 재로그인 유도
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"RELOGIN_REQUIRED\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token) {
        Long userId = jwtProvider.getUserId(token);
        setAuthenticationByUserId(userId);
    }

    private void setAuthenticationByUserId(Long userId) {
        // JwtProvider에서 role도 추출할 수 있다면 authorities에 넣어도 됨
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}