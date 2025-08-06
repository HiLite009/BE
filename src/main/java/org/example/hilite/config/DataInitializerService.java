package org.example.hilite.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.entity.AccessPage;
import org.example.hilite.entity.Member;
import org.example.hilite.entity.MemberRole;
import org.example.hilite.entity.Role;
import org.example.hilite.entity.RolePagePermission;
import org.example.hilite.repository.AccessPageRepository;
import org.example.hilite.repository.MemberRepository;
import org.example.hilite.repository.MemberRoleRepository;
import org.example.hilite.repository.RolePagePermissionRepository;
import org.example.hilite.repository.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializerService implements ApplicationRunner {

  private final RoleRepository roleRepository;
  private final AccessPageRepository accessPageRepository;
  private final RolePagePermissionRepository rolePagePermissionRepository;
  private final MemberRepository memberRepository;
  private final MemberRoleRepository memberRoleRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    log.info("Starting reactive data initialization...");

    // @Transactional 제거하고 block()으로 동기 처리
    initializeData()
        .doOnSuccess(v -> log.info("✅ 초기 데이터 설정이 완료되었습니다."))
        .doOnError(error -> log.error("❌ 초기 데이터 설정 중 오류 발생: {}", error.getMessage()))
        .block(); // ApplicationRunner에서는 완료를 기다려야 함
  }

  private Mono<Void> initializeData() {
    return createBasicRoles()
        .then(createBasicPages())
        .then(setupBasicPermissions())
        .then(createBasicUsers())
        .then();
  }

  /**
   * 기본 역할 생성
   */
  private Mono<Void> createBasicRoles() {
    log.debug("Creating basic roles...");

    return Flux.just("ROLE_ADMIN", "ROLE_USER", "ROLE_GUEST")
        .flatMap(roleName ->
            roleRepository.existsByName(roleName)
                .flatMap(exists -> {
                  if (!exists) {
                    Role role = new Role();
                    role.setName(roleName);
                    return roleRepository.save(role)
                        .doOnSuccess(saved -> log.debug("Created role: {}", saved.getName()));
                  }
                  return Mono.empty();
                }))
        .then()
        .doOnSuccess(v -> log.debug("✅ Basic roles created"));
  }

  /**
   * 기본 페이지 경로 생성
   */
  private Mono<Void> createBasicPages() {
    log.debug("Creating basic pages...");

    return Flux.just("/admin/**", "/play/**", "/chat/**", "/user/**", "/member/**")
        .flatMap(pagePath ->
            accessPageRepository.existsByPath(pagePath)
                .flatMap(exists -> {
                  if (!exists) {
                    AccessPage page = new AccessPage(pagePath);
                    return accessPageRepository.save(page)
                        .doOnSuccess(saved -> log.debug("Created page: {}", saved.getPath()));
                  }
                  return Mono.empty();
                }))
        .then()
        .doOnSuccess(v -> log.debug("✅ Basic pages created"));
  }

  /**
   * 기본 권한 설정
   */
  private Mono<Void> setupBasicPermissions() {
    log.debug("Setting up basic permissions...");

    return setupAdminPermissions()
        .then(setupUserPermissions())
        .then(setupGuestPermissions())
        .doOnSuccess(v -> log.debug("✅ Basic permissions set up"));
  }

  private Mono<Void> setupAdminPermissions() {
    return roleRepository.findByName("ROLE_ADMIN")
        .flatMap(adminRole ->
            accessPageRepository.findAll()
                .flatMap(page ->
                    createPermissionIfNotExists(adminRole.getId(), page.getId()))
                .then())
        .doOnSuccess(v -> log.debug("Admin permissions set up"));
  }

  private Mono<Void> setupUserPermissions() {
    String[] userPages = {"/user/**", "/play/**", "/member/**"};

    return roleRepository.findByName("ROLE_USER")
        .flatMap(userRole ->
            Flux.fromArray(userPages)
                .flatMap(pagePath ->
                    accessPageRepository.findByPath(pagePath)
                        .flatMap(page ->
                            createPermissionIfNotExists(userRole.getId(), page.getId())))
                .then())
        .doOnSuccess(v -> log.debug("User permissions set up"));
  }

  private Mono<Void> setupGuestPermissions() {
    return roleRepository.findByName("ROLE_GUEST")
        .flatMap(guestRole ->
            accessPageRepository.findByPath("/play/**")
                .flatMap(playPage ->
                    createPermissionIfNotExists(guestRole.getId(), playPage.getId())))
        .doOnSuccess(v -> log.debug("Guest permissions set up"));
  }

  private Mono<Void> createPermissionIfNotExists(Long roleId, Long pageId) {
    return rolePagePermissionRepository.existsByRoleIdAndAccessPageId(roleId, pageId)
        .flatMap(exists -> {
          if (!exists) {
            RolePagePermission permission = new RolePagePermission(roleId, pageId);
            return rolePagePermissionRepository.save(permission).then();
          }
          return Mono.empty();
        });
  }

  /**
   * 기본 사용자 생성
   */
  private Mono<Void> createBasicUsers() {
    log.debug("Creating basic users...");

    return createAdminUser()
        .then(createTestUser())
        .doOnSuccess(v -> log.debug("✅ Basic users created"));
  }

  private Mono<Void> createAdminUser() {
    return memberRepository.existsByUsername("admin")
        .flatMap(exists -> {
          if (!exists) {
            Member admin = new Member();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");

            return memberRepository.save(admin)
                .flatMap(savedAdmin ->
                    roleRepository.findByName("ROLE_ADMIN")
                        .flatMap(adminRole -> {
                          MemberRole memberRole = new MemberRole(savedAdmin.getId(), adminRole.getId());
                          return memberRoleRepository.save(memberRole);
                        }))
                .doOnSuccess(v -> log.info("✅ 관리자 계정이 생성되었습니다. (username: admin, password: admin123)"))
                .then();
          }
          return Mono.empty();
        });
  }

  private Mono<Void> createTestUser() {
    return memberRepository.existsByUsername("user")
        .flatMap(exists -> {
          if (!exists) {
            Member user = new Member();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@example.com");

            return memberRepository.save(user)
                .flatMap(savedUser ->
                    roleRepository.findByName("ROLE_USER")
                        .flatMap(userRole -> {
                          MemberRole memberRole = new MemberRole(savedUser.getId(), userRole.getId());
                          return memberRoleRepository.save(memberRole);
                        }))
                .doOnSuccess(v -> log.info("✅ 일반 사용자 계정이 생성되었습니다. (username: user, password: user123)"))
                .then();
          }
          return Mono.empty();
        });
  }
}