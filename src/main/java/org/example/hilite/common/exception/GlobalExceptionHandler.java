package org.example.hilite.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> fieldErrors = new HashMap<>();

    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

    ApiErrorResponse errorResponse =
        new ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "VALIDATION_ERROR",
            "입력값이 유효하지 않습니다.",
            request.getRequestURI(),
            fieldErrors);

    return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(errorResponse);
  }

  @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
  public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
      Exception ex, HttpServletRequest request) {

    // 내부 로그에는 구체적인 정보 기록 (디버깅용)
    if (ex instanceof BadCredentialsException) {
      log.warn(
          "Authentication failed - URI: {}, Message: {}", request.getRequestURI(), ex.getMessage());
    } else {
      log.warn(
          "Authentication failed - URI: {}, Exception: {}",
          request.getRequestURI(),
          ex.getClass().getSimpleName());
    }

    // 클라이언트에는 동일한 메시지 반환 (보안 강화)
    ApiErrorResponse response =
        new ApiErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            "AUTHENTICATION_FAILED",
            "아이디 또는 비밀번호가 잘못되었습니다.", // 통일된 메시지
            request.getRequestURI(),
            Map.of());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException ex, HttpServletRequest request) {
    ApiErrorResponse response =
        new ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "ILLEGAL_ARGUMENT",
            ex.getMessage(),
            request.getRequestURI(),
            Map.of());
    return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGeneralException(
      Exception ex, HttpServletRequest request) {
    ApiErrorResponse response =
        new ApiErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "INTERNAL_SERVER_ERROR",
            "예기치 못한 오류가 발생했습니다.",
            request.getRequestURI(),
            Map.of());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(MediaType.APPLICATION_JSON)
        .body(response);
  }
}
