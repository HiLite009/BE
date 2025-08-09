package org.example.hilite.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.dto.reqeust.ChatRequestDto;
import org.example.hilite.dto.reqeust.ChatResponseDto;
import org.example.hilite.dto.reqeust.StreamingChatResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class ChatBotService {

  private final Random random = new Random();

  // 간단한 응답 패턴들
  private final List<String> greetingResponses =
      Arrays.asList("안녕하세요! 무엇을 도와드릴까요?", "반갑습니다! 궁금한 것이 있으시면 언제든 물어보세요.", "안녕하세요! 오늘 기분은 어떠신가요?");

  private final List<String> helpResponses =
      Arrays.asList(
          "도움이 필요하시군요! 구체적으로 어떤 부분이 궁금하신가요?",
          "무엇을 도와드릴까요? 자세히 말씀해 주세요.",
          "도움을 드리고 싶습니다. 어떤 문제가 있으신지 알려주세요.");

  private final List<String> defaultResponses =
      Arrays.asList(
          "흥미로운 질문이네요! 좀 더 자세히 설명해 주실 수 있나요?",
          "그런 관점으로 생각해본 적이 없었네요. 더 말씀해 주세요.",
          "좋은 포인트입니다! 다른 궁금한 점도 있으신가요?",
          "네, 이해했습니다. 다른 도움이 필요한 것이 있을까요?");

  /** 일반 채팅 처리 */
  public ChatResponseDto processChat(ChatRequestDto request) {
    log.info("Processing chat for message: {}", request.getMessage());

    String response = generateResponse(request.getMessage());

    return ChatResponseDto.builder()
        .message(request.getMessage())
        .botResponse(response)
        .sessionId(request.getSessionId())
        .timestamp(LocalDateTime.now())
        .status("SUCCESS")
        .build();
  }

  /** 스트리밍 채팅 처리 - SSE를 통한 실시간 응답 전송 */
  public void processStreamingChat(ChatRequestDto request, SseEmitter emitter) {
    try {
      log.info("Processing streaming chat for message: {}", request.getMessage());

      // AI 서버 응답 시뮬레이션 - 긴 응답 생성
      String fullResponse = generateLongResponse(request.getMessage());

      // 응답을 작은 청크로 분할
      String[] chunks = splitIntoChunks(fullResponse, 20);

      // 각 청크를 순차적으로 전송
      for (int i = 0; i < chunks.length; i++) {
        // AI 서버로부터 데이터 수신 시뮬레이션을 위한 딜레이
        Thread.sleep(200);

        StreamingChatResponseDto chunkResponse =
            StreamingChatResponseDto.builder()
                .chunk(chunks[i])
                .sessionId(request.getSessionId())
                .isComplete(i == chunks.length - 1)
                .type(i == chunks.length - 1 ? "complete" : "text")
                .build();

        // SSE 이벤트 전송
        emitter.send(SseEmitter.event().name("message").data(chunkResponse));

        log.debug("Sent chunk {}/{} for session: {}", i + 1, chunks.length, request.getSessionId());
      }

      // 스트리밍 완료
      emitter.complete();
      log.info("Completed streaming chat for session: {}", request.getSessionId());

    } catch (InterruptedException e) {
      log.error("Streaming interrupted", e);
      Thread.currentThread().interrupt();
      sendErrorAndComplete(emitter, request.getSessionId(), "스트리밍이 중단되었습니다.");
    } catch (Exception e) {
      log.error("Error in streaming chat", e);
      sendErrorAndComplete(emitter, request.getSessionId(), "스트리밍 중 오류가 발생했습니다.");
    }
  }

  /** 에러 발생 시 에러 메시지 전송 후 연결 종료 */
  private void sendErrorAndComplete(SseEmitter emitter, String sessionId, String errorMessage) {
    try {
      StreamingChatResponseDto errorResponse =
          StreamingChatResponseDto.builder()
              .chunk(errorMessage)
              .sessionId(sessionId)
              .isComplete(true)
              .type("error")
              .build();

      emitter.send(SseEmitter.event().name("error").data(errorResponse));

      emitter.completeWithError(new RuntimeException(errorMessage));
    } catch (Exception ex) {
      log.error("Failed to send error message", ex);
      emitter.completeWithError(ex);
    }
  }

  /** 간단한 응답 생성 로직 */
  private String generateResponse(String message) {
    String lowerMessage = message.toLowerCase();

    if (lowerMessage.contains("안녕")
        || lowerMessage.contains("hello")
        || lowerMessage.contains("hi")) {
      return getRandomResponse(greetingResponses);
    } else if (lowerMessage.contains("도움")
        || lowerMessage.contains("help")
        || lowerMessage.contains("문제")) {
      return getRandomResponse(helpResponses);
    } else {
      return getRandomResponse(defaultResponses);
    }
  }

  /** 긴 응답 생성 (스트리밍용) */
  private String generateLongResponse(String message) {

    String response =
        "사용자님의 질문 '"
            + message
            + "'에 대해 상세히 답변드리겠습니다. "

            // 더미 긴 응답 생성
            + "이것은 스트리밍 응답의 예시입니다. "
            + "실제 AI 챗봇에서는 이 부분에서 대용량의 데이터나 복잡한 분석 결과를 "
            + "점진적으로 전송할 수 있습니다. "
            + "WebFlux를 사용하면 이러한 스트리밍 응답을 매우 효율적으로 처리할 수 있으며, "
            + "사용자는 전체 응답이 완료되기를 기다릴 필요 없이 "
            + "실시간으로 응답을 받아볼 수 있습니다. "
            + "이는 특히 ChatGPT와 같은 AI 서비스에서 많이 사용되는 패턴입니다. "
            + "추가적인 질문이 있으시면 언제든 말씀해 주세요!";

    return response;
  }

  /** 문자열을 지정된 크기의 청크로 분할 */
  private String[] splitIntoChunks(String text, int chunkSize) {
    int length = text.length();
    int chunks = (length + chunkSize - 1) / chunkSize;
    String[] result = new String[chunks];

    for (int i = 0; i < chunks; i++) {
      int start = i * chunkSize;
      int end = Math.min(start + chunkSize, length);
      result[i] = text.substring(start, end);
    }

    return result;
  }

  /** 랜덤 응답 선택 */
  private String getRandomResponse(List<String> responses) {
    return responses.get(random.nextInt(responses.size()));
  }
}
