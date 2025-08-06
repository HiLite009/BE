package org.example.hilite.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("role_page_permission")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePagePermission {

  @Id
  private Long id;

  @Column("role_id")
  private Long roleId;

  @Column("access_page_id")
  private Long accessPageId;

  public RolePagePermission(Long roleId, Long accessPageId) {
    this.roleId = roleId;
    this.accessPageId = accessPageId;
  }
}