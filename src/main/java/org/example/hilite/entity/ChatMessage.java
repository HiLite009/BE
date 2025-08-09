package org.example.hilite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.hilite.common.base.BaseEntity;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatMessage extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false)
  private String sessionId;

  @Column(name = "content", columnDefinition = "TEXT")
  private String content;

  @Column(name = "sender")
  @Enumerated(EnumType.STRING)
  private MessageSender sender;

  @Column(name = "username")
  private String username;

  public enum MessageSender {
    USER, BOT
  }
}
