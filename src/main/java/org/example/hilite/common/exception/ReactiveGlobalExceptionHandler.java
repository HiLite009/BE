package org.example.hilite.common.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
public class ReactiveGlobalExceptionHandler {

  /**
   * WebFlux 유효성 검사 예외 처리
   */
  @ExceptionHandler(WebExchangeBindException.class)
  public Mono<ResponseEntity<ApiErrorResponse>> handleValidationException(
      WebExchangeBindException ex, ServerWebExchange exchange) {

    log.warn("Validation error on path {}: {}", exchange.getRequest().getPath(), ex.getMessage());

    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

    ApiErrorResponse errorResponse = new ApiErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        "VALIDATION_ERROR",
        "입력값이 유효하지 않습니다.",
        exchange.getRequest().getPath().value(),
        fieldErrors
    );

    return Mono.just(ResponseEntity.badRequest().body(errorResponse));
  }

  /**
   * 커스텀 예외 처리
   */
  @ExceptionHandler(CustomException.class)
  public Mono<ResponseEntity<ApiErrorResponse>> handleCustomException(
      CustomException ex, ServerWebExchange exchange) {

    log.warn("Custom exception on path {}: {} - {}",
        exchange.getRequest().getPath(), ex.getErrorCode().code(), ex.getMessage());

    ApiErrorResponse response = new ApiErrorResponse(
        ex.getErrorCode().httpStatus().value(),
        ex.getErrorCode().httpStatus().getReasonPhrase(),
        ex.getErrorCode().code(),
        ex.getErrorCode().message(),
        exchange.getRequest().getPath().value(),
        Map.of()
    );

    return Mono.just(ResponseEntity.status(ex.getErrorCode().httpStatus()).body(response));
  }

  /**
   * 인증 예외 처리
   */
  @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
  public Mono<ResponseEntity<ApiErrorResponse>> handleAuthenticationException(
      Exception ex, ServerWebExchange exchange) {

    // 내부 로그에는 구체적인 정보 기록 (디버깅용)
    if (ex instanceof BadCredentialsException) {
      log.warn("Authentication failed - URI: {}, Message: {}",
          exchange.getRequest().getPath(), ex.getMessage());
    } else {
      log.warn("Authentication failed - URI: {}, Exception: {}",
          exchange.getRequest().getPath(), ex.getClass().getSimpleName());
    }

    // 클라이언트에는 동일한 메시지 반환 (보안 강화)
    ApiErrorResponse response = new ApiErrorResponse(
        HttpStatus.UNAUTHORIZED.value(),
        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
        "AUTHENTICATION_FAILED",
        "아이디 또는 비밀번호가 잘못되었습니다.", // 통일된 메시지
        exchange.getRequest().getPath().value(),
        Map.of()
    );

    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
  }

  /**
   * 접근 거부 예외 처리
   */
  @ExceptionHandler(AccessDeniedException.class)
  public Mono<ResponseEntity<ApiErrorResponse>> handleAccessDeniedException(
      AccessDeniedException ex, ServerWebExchange exchange) {

    log.warn("Access denied on path {}: {}", exchange.getRequest().getPath(), ex.getMessage());

    ApiErrorResponse response = new ApiErrorResponse(
        HttpStatus.FORBIDDEN.value(),
        HttpStatus.FORBIDDEN.getReasonPhrase(),
        "ACCESS_DENIED",
        "접근 권한이 없습니다.",
        exchange.getRequest().getPath().value(),
        Map.of()
    );

    return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(response));
  }

  /**
   * IllegalArgumentException 처리
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public Mono<ResponseEntity<ApiErrorResponse>> handleIllegalArgumentException(
      IllegalArgumentException ex, ServerWebExchange exchange) {

    log.warn("Illegal argument on path {}: {}", exchange.getRequest().getPath(), ex.getMessage());

    ApiErrorResponse response = new ApiErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        "ILLEGAL_ARGUMENT",
        ex.getMessage(),
        exchange.getRequest().getPath().value(),
        Map.of()
    );

    return Mono.just(ResponseEntity.badRequest().body(response));
  }


  /**
   * WebFlux DataBufferLimitException 처리 (파일 업로드 등)
   */
  @ExceptionHandler(org.springframework.core.io.buffer.DataBufferLimitException.class)
  public Mono<ResponseEntity<ApiErrorResponse>> handleDataBufferLimitException(
      org.springframework.core.io.buffer.DataBufferLimitException ex, ServerWebExchange exchange) {

    log.warn("Data buffer limit exceeded on path {}: {}",
        exchange.getRequest().getPath(), ex.getMessage());

    ApiErrorResponse response = new ApiErrorResponse(
        HttpStatus.PAYLOAD_TOO_LARGE.value(),
        HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase(),
        "PAYLOAD_TOO_LARGE",
        "요청 데이터가 너무 큽니다.",
        exchange.getRequest().getPath().value(),
        Map.of()
    );

    return Mono.just(ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response));
  }
}

