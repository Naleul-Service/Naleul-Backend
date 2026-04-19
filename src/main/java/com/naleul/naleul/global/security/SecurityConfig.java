package com.naleul.naleul.global.security;

import com.naleul.naleul.domain.user.service.TokenReissueService;
import com.naleul.naleul.global.jwt.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    private final TokenReissueService tokenReissueService;

    // @RequiredArgsConstructor 제거하고 생성자 직접 작성
    public SecurityConfig(JwtProvider jwtProvider, TokenReissueService tokenReissueService) {
        this.jwtProvider = jwtProvider;
        this.tokenReissueService = tokenReissueService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(login -> login.disable()) // 기본 로그인 페이지 비활성화
                .httpBasic(basic -> basic.disable()) // 기본 HTTP 인증 비활성화
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",           // 로그인, 회원가입
                                "/swagger-ui/**",            // 스웨거
                                "/v3/api-docs/**",           // 스웨거
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated()        // 나머지는 토큰 필요
                )
                // 필터를 직접 생성하여 등록 (SecurityConfig 설정을 확실히 태움)
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider, tokenReissueService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // www가 붙은 도메인과 안 붙은 도메인 모두 허용하는 것이 안전합니다.
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://naleul.vercel.app",
                "https://naleul.com",
                "https://www.naleul.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}