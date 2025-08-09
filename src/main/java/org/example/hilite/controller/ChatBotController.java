package org.example.hilite.controller;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.dto.reqeust.ChatRequestDto;
import org.example.hilite.dto.reqeust.ChatResponseDto;
import org.example.hilite.service.ChatBotService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatBotController {

  private final ChatBotService chatBotService;

  /** 일반 채팅 API - 즉시 응답 */
  @PostMapping("/chat")
  public ResponseEntity<ChatResponseDto> chat(@RequestBody ChatRequestDto request) {
    log.info("Received chat request: {}", request.getMessage());

    try {
      ChatResponseDto response = chatBotService.processChat(request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error processing chat request", e);
      ChatResponseDto errorResponse =
          ChatResponseDto.builder()
              .message(request.getMessage())
              .botResponse("죄송합니다. 처리 중 오류가 발생했습니다.")
              .sessionId(request.getSessionId())
              .timestamp(LocalDateTime.now())
              .status("ERROR")
              .build();
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }

  /** 스트리밍 채팅 API - SSE를 통한 실시간 응답 */
  @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamChat(@RequestBody ChatRequestDto request) {
    SseEmitter emitter = new SseEmitter(30000L); // 30초 타임아웃
    
    // SecurityContext를 현재 스레드에서 복사
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // 비동기 처리
    CompletableFuture.runAsync(
        () -> {
          try {
            // 새로운 SecurityContext 생성하고 Authentication 설정
            SecurityContext newContext = new SecurityContextImpl();
            newContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(newContext);
            
            log.info("User in async thread: {}", authentication != null ? authentication.getName() : "null");
            chatBotService.processStreamingChat(request, emitter);
          } catch (Exception e) {
            log.error("Error in streaming chat", e);
            try {
              emitter.completeWithError(e);
            } catch (Exception emitterEx) {
              log.error("Error completing emitter with error", emitterEx);
            }
          } finally {
            // SecurityContext 정리
            SecurityContextHolder.clearContext();
          }
        });

    return emitter;
  }

  /** 헬스 체크 API */
  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("ChatBot service is running");
  }
}
