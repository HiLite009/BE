package org.example.hilite.common.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String code,
    String message,
    String path,
    Map<String, String> fieldErrors) {
  public ApiErrorResponse(
      int status,
      String error,
      String code,
      String message,
      String path,
      Map<String, String> fieldErrors) {
    this(LocalDateTime.now(), status, error, code, message, path, fieldErrors);
  }
}
