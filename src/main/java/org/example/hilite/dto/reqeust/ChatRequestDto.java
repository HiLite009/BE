package org.example.hilite.dto.reqeust;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDto {
  private String message;
  private String userId;
  private String sessionId;
}
