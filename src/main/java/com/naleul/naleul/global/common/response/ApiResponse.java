package com.naleul.naleul.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 JSON에서 제외
public class ApiResponse<T> {

    private final boolean success;
    private final int status;
    private final String message;
    private final T data;

    @Builder
    private ApiResponse(boolean success, int status, String message, T data) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // ✅ 성공 응답 (data 있음)
    public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(successCode.getStatus())
                .message(successCode.getMessage())
                .data(data)
                .build();
    }

    // ✅ 성공 응답 (data 없음 - 삭제 같은 경우)
    public static <T> ApiResponse<T> success(SuccessCode successCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(successCode.getStatus())
                .message(successCode.getMessage())
                .build();
    }

    // ❌ 실패 응답
    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(errorCode.getStatus())
                .message(errorCode.getMessage())
                .build();
    }
}