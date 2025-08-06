package org.example.hilite.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberPermissionDto {
  // Member 정보
  private Long memberId;
  private String username;

  // Role 정보
  private Long roleId;
  private String roleName;

  // Permission 정보
  private Long permissionId;
  private String accessPath;
}
