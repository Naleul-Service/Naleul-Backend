package com.naleul.naleul.global.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {
    // 공통
    OK(HttpStatus.OK, "요청이 성공했습니다."),
    CREATED(HttpStatus.CREATED, "생성이 완료되었습니다."),

    // 유저
    USERS_FOUND(HttpStatus.OK, "전체 유저 조회 성공"),
    USER_FOUND(HttpStatus.OK, "유저 조회 성공"),
    USER_CREATED(HttpStatus.CREATED, "회원가입 성공"),
    LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),

    // 컬러
    COLOR_CREATED(HttpStatus.CREATED, "색상 저장 성공"),
    COLOR_FOUND(HttpStatus.FOUND, "색상 조회 성공"),
    COLORS_FOUND(HttpStatus.FOUND, "전체 색상 조회 성공");

    private final HttpStatus status;
    private final String message;
}
