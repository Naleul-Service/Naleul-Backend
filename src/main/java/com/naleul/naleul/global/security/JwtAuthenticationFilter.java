package com.naleul.naleul.global.security;

import com.naleul.naleul.domain.user.dto.LoginResponse;
import com.naleul.naleul.domain.user.service.TokenReissueService;
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
    private final TokenReissueService tokenReissueService;

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
            // 토큰 없으면 401 바로 반환
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"인증이 필요합니다.\",\"status\":\"401\",\"success\":false}");
            return; // filterChain.doFilter 호출 안 함!
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
        return path.startsWith("/api/v1/auth/") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/api/health") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/swagger-ui.html");
    }

    private void handleExpiredToken(String token, HttpServletResponse response) {
        Long userId = jwtProvider.extractUserIdIgnoreExpiration(token);
        LoginResponse newToken = tokenReissueService.reissue(userId);

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