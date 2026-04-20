package com.naleul.naleul.global.exception;

import com.naleul.naleul.global.common.response.ApiResponse;
import com.naleul.naleul.global.common.response.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 비즈니스 에러 (1번: Color 없음, 2번: 이름 중복 등)
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.fail(e.getErrorCode()));
    }

    // 5~8번: @Valid 검증 실패 (NotNull, NotBlank 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException e) {

        // 여러 필드 에러 중 첫 번째 메시지만 반환
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse(ErrorCode.INVALID_INPUT_VALUE.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE, message));
    }

    // 3번: Enum 외 값, 4번: 날짜 형식 오류
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {

        ErrorCode errorCode;

        if (e.getMessage() != null && e.getMessage().contains("GoalCategoryStatus")) {
            errorCode = ErrorCode.INVALID_ENUM_VALUE;
        } else if (e.getMessage() != null && e.getMessage().contains("LocalDate")) {
            errorCode = ErrorCode.INVALID_DATE_FORMAT;
        } else {
            errorCode = ErrorCode.INVALID_INPUT_VALUE;
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(errorCode));
    }

    // 예상치 못한 서버 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}