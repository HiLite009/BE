package org.example.hilite.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("member_role")
@Data
public class MemberRole {
  @Id
  private Long id;
  private Long memberId;
  private Long roleId;

  public MemberRole(Long memberId, Long roleId) {
    this.memberId = memberId;
    this.roleId = roleId;
  }

}