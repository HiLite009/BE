package org.example.hilite.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User {
  @Id @GeneratedValue private Long id;

  private String username;
  private String password;
  private String email;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private Set<UserRole> userRoles = new HashSet<>();

  public void addRole(Role role) {
    UserRole userRole = new UserRole(this, role);
    userRoles.add(userRole);
  }
}
