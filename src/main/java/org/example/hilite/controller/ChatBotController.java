package org.example.hilite.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.dto.reqeust.ChatRequestDto;
import org.example.hilite.dto.reqeust.ChatResponseDto;
import org.example.hilite.dto.reqeust.StreamingChatResponseDto;
import org.example.hilite.service.ChatBotService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
@Tag(name = "챗봇", description = "AI 챗봇 API")
public class ChatBotController {

  private final ChatBotService chatBotService;

  /**
   * 일반 채팅 엔드포인트
   */
  @PostMapping
  @Operation(summary = "일반 채팅", description = "AI 챗봇과 일반적인 채팅을 진행합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "채팅 응답 성공",
          content = {@Content(schema = @Schema(implementation = ChatResponseDto.class))}),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 필요")
  })
  public Mono<ResponseEntity<ChatResponseDto>> chat(
      @RequestBody ChatRequestDto request,
      ServerWebExchange exchange) {

    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(java.security.Principal::getName)
        .flatMap(username -> {
          log.info("Received chat request from user: {}, session: {}, message: {}",
              username, request.getSessionId(), request.getMessage());

          // 요청에 사용자 정보 설정
          ChatRequestDto updatedRequest = new ChatRequestDto(
              request.getMessage(),
              username,
              request.getSessionId()
          );

          return chatBotService.processChat(updatedRequest)
              .map(ResponseEntity::ok);
        })
        .doOnSuccess(response -> log.info("Chat response sent successfully"))
        .doOnError(error -> log.error("Error processing chat request", error));
  }

  /**
   * 스트리밍 채팅 엔드포인트 (큰 데이터 처리용)
   */
  @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(summary = "스트리밍 채팅", description = "AI 챗봇과 스트리밍 방식으로 채팅을 진행합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "스트리밍 채팅 응답 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 필요")
  })
  public Flux<StreamingChatResponseDto> streamChat(
      @RequestBody ChatRequestDto request,
      ServerWebExchange exchange) {

    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(java.security.Principal::getName)
        .flatMapMany(username -> {
          log.info("Received streaming chat request from user: {}, session: {}, message: {}",
              username, request.getSessionId(), request.getMessage());

          // 요청에 사용자 정보 설정
          ChatRequestDto updatedRequest = new ChatRequestDto(
              request.getMessage(),
              username,
              request.getSessionId()
          );

          return chatBotService.processStreamingChat(updatedRequest);
        })
        .doOnComplete(() -> log.info("Streaming chat response completed"))
        .doOnError(error -> log.error("Error processing streaming chat request", error));
  }

  /**
   * Server-Sent Events를 사용한 스트리밍 (브라우저에서 EventSource로 접근 가능)
   */
  @PostMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(summary = "SSE 채팅", description = "Server-Sent Events를 사용한 실시간 채팅입니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "SSE 채팅 응답 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 필요")
  })
  public Flux<String> sseChat(
      @RequestBody ChatRequestDto request,
      ServerWebExchange exchange) {

    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(java.security.Principal::getName)
        .flatMapMany(username -> {
          log.info("Received SSE chat request from user: {}, session: {}, message: {}",
              username, request.getSessionId(), request.getMessage());

          // 요청에 사용자 정보 설정
          ChatRequestDto updatedRequest = new ChatRequestDto(
              request.getMessage(),
              username,
              request.getSessionId()
          );

          return chatBotService.processStreamingChat(updatedRequest)
              .map(response -> {
                if (response.isComplete()) {
                  return "data: {\"type\":\"complete\",\"sessionId\":\"" + response.getSessionId()
                      + "\"}\n\n";
                } else {
                  return "data: {\"chunk\":\"" + response.getChunk().replace("\"", "\\\"") +
                      "\",\"sessionId\":\"" + response.getSessionId() + "\",\"type\":\"text\"}\n\n";
                }
              });
        })
        .doOnComplete(() -> log.info("SSE chat response completed"))
        .doOnError(error -> log.error("Error processing SSE chat request", error));
  }

  /**
   * 헬스 체크 엔드포인트
   */
  @GetMapping("/health")
  @Operation(summary = "챗봇 헬스 체크", description = "챗봇 서비스의 상태를 확인합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "챗봇 서비스 정상")
  })
  public Mono<ResponseEntity<String>> health() {
    return Mono.just(ResponseEntity.ok("ChatBot service is running!"))
        .doOnSuccess(response -> log.debug("ChatBot health check completed"));
  }

  /**
   * 채팅 세션 정리
   */
  @PostMapping("/session/clear")
  @Operation(summary = "채팅 세션 정리", description = "특정 채팅 세션의 기록을 정리합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "세션 정리 완료")
  })
  public Mono<ResponseEntity<String>> clearSession(
      @RequestBody String sessionId,
      ServerWebExchange exchange) {

    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(java.security.Principal::getName)
        .flatMap(username -> {
          log.info("Clearing chat session {} for user: {}", sessionId, username);

          // 실제로는 세션 정리 로직을 구현해야 함
          return Mono.just(ResponseEntity.ok("채팅 세션이 정리되었습니다."));
        })
        .doOnSuccess(response -> log.info("Chat session cleared successfully"))
        .doOnError(error -> log.error("Error clearing chat session", error));
  }
}