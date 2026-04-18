package com.naleul.naleul.global.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {
    // 공통
    OK(200, "요청이 성공했습니다."),
    CREATED(201, "생성이 완료되었습니다."),

    // 유저
    USERS_FOUND(200, "전체 유저 조회 성공"),
    USER_FOUND(200, "유저 조회 성공"),
    USER_CREATED(201, "회원가입 성공"),
    LOGIN_SUCCESS(200, "로그인 성공");

    private final int status;
    private final String message;
}
