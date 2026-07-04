package io.security.redall.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 모든 컨트롤러의 예외 처리
 * DTO 검증 실패(@Valid)와 비즈니스 예외(중복 등)를 구분해 응답
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid 형식 거증 실패 -> 400, 필드별 메시지
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e){
        Map<String, String> errors = new HashMap<>();

        for(FieldError error: e.getBindingResult().getFieldErrors()){
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(Map.of(
                "message", "입력값이 올바르지 않습니다.",
                "errors", errors
        ));
    }

    // 중복 등 비즈니스 규칙 위반 -> 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    // 서버 상태 등 문제 (기본 권한 누락 등) -> 500
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", e.getMessage()));
    }

}
