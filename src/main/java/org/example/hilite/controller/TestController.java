package org.example.hilite.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@Tag(name = "테스트", description = "시스템 테스트 API")
public class TestController {

  /**
   * 기본 테스트 엔드포인트
   */
  @GetMapping("/test")
  @Operation(summary = "시스템 테스트", description = "시스템이 정상적으로 작동하는지 테스트합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "시스템 정상 작동")
  })
  public Mono<ResponseEntity<String>> test() {
    log.info("Test endpoint accessed");
    log.debug("Debug log test");

    return Mono.fromCallable(() -> {
          log.info("Hello World!");
          log.debug("Hello World!");
          return ResponseEntity.ok("Hello World!");
        })
        .doOnSuccess(response -> log.debug("Test endpoint response sent successfully"))
        .doOnError(error -> log.error("Error in test endpoint: {}", error.getMessage()));
  }

  /**
   * 헬스 체크 엔드포인트
   */
  @GetMapping("/health")
  @Operation(summary = "헬스 체크", description = "애플리케이션 상태를 확인합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "애플리케이션 정상")
  })
  public Mono<ResponseEntity<java.util.Map<String, Object>>> health() {
    log.debug("Health check endpoint accessed");

    return Mono.fromCallable(() -> {
          java.util.Map<String, Object> health = new java.util.HashMap<>();
          health.put("status", "UP");
          health.put("timestamp", java.time.LocalDateTime.now());
          health.put("application", "Hilite Reactive API");
          health.put("version", "1.0.0");

          return ResponseEntity.ok(health);
        })
        .doOnSuccess(response -> log.debug("Health check completed successfully"))
        .doOnError(error -> log.error("Error in health check: {}", error.getMessage()));
  }

  /**
   * 현재 시간 조회
   */
  @GetMapping("/time")
  @Operation(summary = "현재 시간", description = "서버의 현재 시간을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "현재 시간 조회 성공")
  })
  public Mono<ResponseEntity<java.util.Map<String, Object>>> getCurrentTime() {
    log.debug("Current time endpoint accessed");

    return Mono.fromCallable(() -> {
          java.time.LocalDateTime now = java.time.LocalDateTime.now();
          java.util.Map<String, Object> timeInfo = new java.util.HashMap<>();

          timeInfo.put("localDateTime", now);
          timeInfo.put("timestamp", System.currentTimeMillis());
          timeInfo.put("timezone", java.time.ZoneId.systemDefault().toString());
          timeInfo.put("iso8601", now.atZone(java.time.ZoneId.systemDefault()).toString());

          return ResponseEntity.ok(timeInfo);
        })
        .doOnSuccess(response -> log.debug("Current time response sent"))
        .doOnError(error -> log.error("Error getting current time: {}", error.getMessage()));
  }
}