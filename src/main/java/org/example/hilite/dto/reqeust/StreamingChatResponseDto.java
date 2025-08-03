package org.example.hilite.dto.reqeust;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamingChatResponseDto {
  private String chunk;
  private String sessionId;
  private boolean isComplete;
  private String type; // "text", "complete", "error"
}