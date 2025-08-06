package org.example.hilite.entity;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.hilite.common.base.BaseEntity;
import org.springframework.data.annotation.Id;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

  @Id
  private Long id;
  private String username;
  private String password;
  private String email;

  private Set<MemberRole> memberRoles = new HashSet<>();

}