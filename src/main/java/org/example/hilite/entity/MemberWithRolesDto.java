package org.example.hilite.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberWithRolesDto {
  private Long id;
  private String username;
  private String password;
  private String email;
  private java.util.List<String> roleNames;

  // R2DBC Row Mapper용 생성자 (GROUP_CONCAT 결과 처리)
  public MemberWithRolesDto(Long id, String username, String password, String email, String roleNamesStr) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.email = email;
    this.roleNames = roleNamesStr != null && !roleNamesStr.isEmpty()
        ? java.util.Arrays.asList(roleNamesStr.split(","))
        : java.util.Collections.emptyList();
  }
}