package org.example.hilite.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  USER_NOT_FOUND("USER_NOT_FOUND", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
  DUPLICATE_EMAIL("DUPLICATE_EMAIL", "이미 존재하는 이메일입니다.", HttpStatus.CONFLICT),
  INTERNAL_SERVER_ERROR(
      "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  LOGIN_FAILED("LOGIN_FAILED", "아이디 또는 비밀번호가 잘못되었습니다.", HttpStatus.UNAUTHORIZED);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;

  ErrorCode(String code, String message, HttpStatus httpStatus) {
    this.code = code;
    this.message = message;
    this.httpStatus = httpStatus;
  }

  public String code() {
    return code;
  }

  public String message() {
    return message;
  }

  public HttpStatus httpStatus() {
    return httpStatus;
  }
}
