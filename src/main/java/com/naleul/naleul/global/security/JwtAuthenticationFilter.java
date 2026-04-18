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
// @Component 제거됨: SecurityConfig에서 new로 생성하여 관리
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 화이트리스트 경로 체크
        if (isWhiteList(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.replace("Bearer ", "");

        try {
            if (jwtProvider.isValid(token)) {
                setAuthentication(token);
            } else if (jwtProvider.isExpired(token)) {
                handleExpiredToken(token, response);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception e) {
            log.error("JWT 처리 에러: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhiteList(String path) {
        return path.startsWith("/api/auth/") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/api/health") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/swagger-ui.html");
    }

    private void handleExpiredToken(String token, HttpServletResponse response) {
        log.info("만료 토큰 재발급 시도");
        Long userId = jwtProvider.extractUserIdIgnoreExpiration(token);
        String newToken = tokenService.reissueJwt(userId);

        response.setHeader("Authorization", "Bearer " + newToken);
        response.setHeader("Access-Control-Expose-Headers", "Authorization");
        setAuthenticationByUserId(userId);
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