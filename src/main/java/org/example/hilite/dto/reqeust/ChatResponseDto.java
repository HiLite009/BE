package org.example.hilite.dto.reqeust;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponseDto {
  private String message;
  private String botResponse;
  private String sessionId;
  private LocalDateTime timestamp;
  private String status;
}
