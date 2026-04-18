package com.naleul.naleul.domain.user.dto.response;

import com.naleul.naleul.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String nickname;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getUserId())
                .email(user.getUserEmail())
                .nickname(user.getUserName())
                .build();
    }

}
