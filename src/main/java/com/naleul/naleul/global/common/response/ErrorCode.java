package com.naleul.naleul.global.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Goal Category
    GOAL_CATEGORY_NAME_DUPLICATED(HttpStatus.BAD_REQUEST, "이미 존재하는 목표 카테고리 이름입니다."),
    GOAL_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 목표 카테고리입니다."),
    GOAL_CATEGORY_DEFAULT_CANNOT_MODIFY(HttpStatus.BAD_REQUEST, "기본 카테고리(기타)는 수정할 수 없습니다."),
    GOAL_CATEGORY_DEFAULT_CANNOT_DELETE(HttpStatus.BAD_REQUEST, "기본 카테고리(기타)는 삭제할 수 없습니다."),

    // General Category
    GENERAL_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 일반 카테고리입니다."),
    GENERAL_CATEGORY_DEFAULT_CANNOT_MODIFY(HttpStatus.BAD_REQUEST, "기본 카테고리(ETC)는 수정할 수 없습니다."),
    GENERAL_CATEGORY_DEFAULT_CANNOT_DELETE(HttpStatus.BAD_REQUEST, "기본 카테고리(ETC)는 삭제할 수 없습니다."),

    // Task
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 태스크입니다."),
    TASK_INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "시작 시간이 종료 시간보다 늦을 수 없습니다."),
    TASK_INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "시작일이 종료일보다 늦을 수 없습니다."),
    TASK_INVALID_WEEK_RANGE(HttpStatus.BAD_REQUEST, "조회 범위는 최대 7일입니다."),
    TASK_INVALID_DAY_OF_WEEK(HttpStatus.BAD_REQUEST, "올바르지 않은 요일 값입니다. (MON~SUN)"),
    TASK_INVALID_WEEK_DAY_ID(HttpStatus.BAD_REQUEST, "존재하지 않는 요일 ID가 포함되어 있습니다."),

    // Color
    COLOR_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 색상입니다."),

    // Validation
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INVALID_ENUM_VALUE(HttpStatus.BAD_REQUEST, "올바르지 않은 상태값입니다."),
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"),

    //토큰
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰이 없습니다."),

    // 공통
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 유저
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");

    private final HttpStatus status;
    private final String message;
}