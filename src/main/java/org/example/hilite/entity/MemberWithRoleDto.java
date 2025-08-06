package org.example.hilite.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberWithRoleDto {
  // Member 필드들
  private Long memberId;
  private String username;
  private String password;
  private String email;

  // Role 필드들
  private Long roleId;
  private String roleName;
}