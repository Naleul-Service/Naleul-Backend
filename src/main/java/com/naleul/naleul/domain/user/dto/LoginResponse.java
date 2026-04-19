package com.naleul.naleul.domain.user.dto;

import com.naleul.naleul.domain.user.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String userName;
    private String userEmail;
    private UserRole userRole;
}