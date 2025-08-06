package org.example.hilite.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePagePermissionDto {
  // RolePagePermission 필드들
  private Long permissionId;
  private Long roleId;
  private Long accessPageId;

  // Role 필드들
  private String roleName;

  // AccessPage 필드들
  private String accessPath;
}