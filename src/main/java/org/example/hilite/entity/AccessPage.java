package org.example.hilite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class AccessPage {

  @Id @GeneratedValue
  private Long id;

  @Column(nullable = false, unique = true)
  private String path;

  public AccessPage(String path) {
    this.path = path;
  }
}
