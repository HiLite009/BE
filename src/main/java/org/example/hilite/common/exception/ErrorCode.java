package org.example.hilite.common.exception;

public enum ErrorCode {
  USER_NOT_FOUND("USER_NOT_FOUND", "존재하지 않는 사용자입니다."),
  DUPLICATE_EMAIL("DUPLICATE_EMAIL", "이미 존재하는 이메일입니다."),
  INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");

  private final String code;
  private final String message;

  ErrorCode(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String code() {
    return code;
  }

  public String message() {
    return message;
  }
}
