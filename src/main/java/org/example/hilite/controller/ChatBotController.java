package org.example.hilite.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.config.CustomUserDetails;
import org.example.hilite.dto.reqeust.ChatRequestDto;
import org.example.hilite.dto.reqeust.ChatResponseDto;
import org.example.hilite.dto.reqeust.StreamingChatResponseDto;
import org.example.hilite.service.ChatBotService;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatBotController {

  private final ChatBotService chatBotService;

  /**
   * 일반 채팅 엔드포인트 POST /api/chat
   */
  @PostMapping
  public Mono<ChatResponseDto> chat(
      @RequestBody ChatRequestDto request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    log.info("Received chat request from user: {}, session: {}",
        userDetails.getUsername(), request.getSessionId());

    return chatBotService.processChat(request)
        .doOnSuccess(response -> log.info("Chat response sent successfully"))
        .doOnError(error -> log.error("Error processing chat request", error));
  }

  /**
   * 스트리밍 채팅 엔드포인트 (큰 데이터 처리용) POST /api/chat/stream
   */
  @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<StreamingChatResponseDto> streamChat(
      @RequestBody ChatRequestDto request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    log.info("Received streaming chat request from user: {}, session: {}",
        userDetails.getUsername(), request.getSessionId());

    return chatBotService.processStreamingChat(request)
        .doOnComplete(() -> log.info("Streaming chat response completed"))
        .doOnError(error -> log.error("Error processing streaming chat request", error));
  }

  /**
   * Server-Sent Events를 사용한 스트리밍 (브라우저에서 EventSource로 접근 가능) POST /api/chat/sse
   */
  @PostMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> sseChat(
      @RequestBody ChatRequestDto request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    log.info("Received SSE chat request from user: {}, session: {}",
        userDetails.getUsername(), request.getSessionId());

    return chatBotService.processStreamingChat(request)
        .map(response -> {
          if (response.isComplete()) {
            return "data: {\"type\":\"complete\",\"sessionId\":\"" + response.getSessionId()
                + "\"}\n\n";
          } else {
            return "data: {\"chunk\":\"" + response.getChunk().replace("\"", "\\\"") +
                "\",\"sessionId\":\"" + response.getSessionId() + "\",\"type\":\"text\"}\n\n";
          }
        })
        .doOnComplete(() -> log.info("SSE chat response completed"))
        .doOnError(error -> log.error("Error processing SSE chat request", error));
  }

  /**
   * 헬스 체크 엔드포인트 GET /api/chat/health
   */
  @GetMapping("/health")
  public Mono<String> health() {
    return Mono.just("ChatBot service is running!");
  }
}
