package org.example.hilite.config;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.entity.AccessPage;
import org.example.hilite.entity.Member;
import org.example.hilite.entity.Role;
import org.example.hilite.entity.RolePagePermission;
import org.example.hilite.repository.AccessPageRepository;
import org.example.hilite.repository.MemberRepository;
import org.example.hilite.repository.RolePagePermissionRepository;
import org.example.hilite.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

  private final RoleRepository roleRepository;
  private final AccessPageRepository accessPageRepository;
  private final RolePagePermissionRepository rolePagePermissionRepository;
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  @PostConstruct
  @Transactional
  public void init() {
    // 기본 역할 생성
    Role adminRole = createRoleIfNotExists("ROLE_ADMIN");
    Role userRole = createRoleIfNotExists("ROLE_USER");
    Role guestRole = createRoleIfNotExists("ROLE_GUEST");

    // 기본 페이지 경로 생성
    AccessPage adminPages = createPageIfNotExists("/api/admin/**");
    AccessPage playPages = createPageIfNotExists("/play/**");
    AccessPage userPages = createPageIfNotExists("/api/user/**");

    // 기본 권한 설정
    createPermissionIfNotExists(adminRole, adminPages);
    createPermissionIfNotExists(adminRole, playPages);
    createPermissionIfNotExists(adminRole, userPages);

    createPermissionIfNotExists(userRole, userPages);
    createPermissionIfNotExists(userRole, playPages);

    createPermissionIfNotExists(guestRole, playPages);

    // 관리자 계정 생성
    createAdminIfNotExists(adminRole);

    log.info("초기 데이터 설정이 완료되었습니다.");
  }

  private Role createRoleIfNotExists(String roleName) {
    return roleRepository
        .findByName(roleName)
        .orElseGet(
            () -> {
              Role role = new Role();
              role.setName(roleName);
              return roleRepository.save(role);
            });
  }

  private AccessPage createPageIfNotExists(String path) {
    return accessPageRepository
        .findByPath(path)
        .orElseGet(() -> accessPageRepository.save(new AccessPage(path)));
  }

  private void createPermissionIfNotExists(Role role, AccessPage page) {
    boolean exists =
        rolePagePermissionRepository.findByRoleIdAndPageId(role.getId(), page.getId()).isPresent();

    if (!exists) {
      RolePagePermission permission = new RolePagePermission();
      permission.setRole(role);
      permission.setAccessPage(page);
      rolePagePermissionRepository.save(permission);
    }
  }

  private void createAdminIfNotExists(Role adminRole) {
    if (!memberRepository.existsByUsername("admin")) {
      Member admin = new Member();
      admin.setUsername("admin");
      admin.setPassword(passwordEncoder.encode("admin123"));
      admin.setEmail("admin@example.com");
      admin.addRole(adminRole);
      memberRepository.save(admin);
      log.info("관리자 계정이 생성되었습니다. (username: admin, password: admin123)");
    }
  }
}
