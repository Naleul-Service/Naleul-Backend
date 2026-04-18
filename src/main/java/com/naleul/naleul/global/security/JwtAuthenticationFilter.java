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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. 화이트리스트 경로는 토큰 검사 없이 바로 통과
        if (path.startsWith("/api/auth/") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/api/health") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/swagger-ui.html")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // 2. 헤더가 없거나 Bearer 형식이 아니면 다음 필터로 (SecurityConfig가 차단 여부 결정)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.replace("Bearer ", "");

        try {
            if (jwtProvider.isValid(token)) {
                setAuthentication(token);
            } else if (jwtProvider.isExpired(token)) {
                log.info("JWT 만료 감지 - 자동 갱신 시도: {}", path);
                Long userId = jwtProvider.extractUserIdIgnoreExpiration(token);
                String newToken = tokenService.reissueJwt(userId);

                response.setHeader("Authorization", "Bearer " + newToken);
                response.setHeader("Access-Control-Expose-Headers", "Authorization");
                setAuthenticationByUserId(userId);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception e) {
            log.error("토큰 처리 중 에러 발생: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token) {
        Long userId = jwtProvider.getUserId(token);
        setAuthenticationByUserId(userId);
    }

    private void setAuthenticationByUserId(Long userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}