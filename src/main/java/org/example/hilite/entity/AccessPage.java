package org.example.hilite.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("access_page")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessPage {

  @Id
  private Long id;

  @Column("path")
  private String path;

  public AccessPage(String path) {
    this.path = path;
  }
}