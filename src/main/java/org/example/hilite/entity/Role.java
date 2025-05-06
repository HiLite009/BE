package org.example.hilite.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Role {
  @Id @GeneratedValue private Long id;

  private String name;

  @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
  private Set<UserRole> userRoles = new HashSet<>();

  @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
  private List<AccessPage> accessPages = new ArrayList<>();
}
