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
    COLORS_FOUND(HttpStatus.FOUND, "전체 색상 조회 성공"),
    COLOR_DELETED(HttpStatus.OK, "색상이 삭제되었습니다."),

    // 목표
    GOAL_CREATED(HttpStatus.CREATED, "목표 카테고리 생성 성공"),
    GOAL_FOUND(HttpStatus.OK, "목표 카테고리 조회 성공"),
    GOAL_UPDATED(HttpStatus.OK, "목표 카테고리 수정 성공"),
    GOAL_DELETED(HttpStatus.OK, "목표 카테고리 삭제 성공"),
    GOAL_COMPLETED(HttpStatus.OK, "목표 카테고리 완료 처리 성공"),
    GOAL_ASSIGNED(HttpStatus.OK, "일반 카테고리 연결 성공"),

    // 일반 카테고리
    GENERAL_CREATED(HttpStatus.CREATED, "일반 카테고리 생성 성공"),
    GENERALS_FOUND(HttpStatus.OK, "일반 카테고리 전체 조회 성공"),
    GENERAL_FOUND(HttpStatus.OK, "일반 카테고리 조회 성공"),
    GENERAL_UPDATED(HttpStatus.OK, "일반 카테고리 수정 성공"),
    GENERAL_DELETED(HttpStatus.OK, "일반 카테고리 삭제 성공"),

    // 할 일 카테고리
    TASK_CREATED(HttpStatus.CREATED, "할 일 생성 성공"),
    TASKS_FOUND(HttpStatus.OK, "할 일 전체 조회 성공"),
    TASK_FOUND(HttpStatus.OK, "할 일 조회 성공"),
    TASK_UPDATED(HttpStatus.OK, "할 일 수정 성공"),
    TASK_COMPLETED(HttpStatus.OK, "할 일 실제 기록 성공"),
    TASK_DELETED(HttpStatus.OK, "할 일 삭제 성공");

    private final HttpStatus status;
    private final String message;
}
