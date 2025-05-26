package org.example.hilite.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.hilite.entity.Role;
import org.example.hilite.repository.RoleRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer {

  private final RoleRepository roleRepository;

  @PostConstruct
  public void init() {
    if (roleRepository.count() == 0) {
      Role roleUser = new Role();
      roleUser.setName("ROLE_USER");
      roleRepository.save(roleUser);
    }
  }
}