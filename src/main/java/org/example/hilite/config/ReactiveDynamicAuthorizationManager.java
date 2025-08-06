package org.example.hilite.config;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.service.DynamicPermissionService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReactiveDynamicAuthorizationManager {

  private final DynamicPermissionService dynamicPermissionService;

  /**
   * 경로와 사용자 역할을 기반으로 권한 확인
   */
  public Mono<Boolean> hasPermission(String requestPath, Collection<String> userRoles) {
    log.debug("Checking reactive permission for path: {} with roles: {}", requestPath, userRoles);

    if (userRoles == null || userRoles.isEmpty()) {
      log.debug("No roles provided, denying access");
      return Mono.just(false);
    }

    return dynamicPermissionService.hasPermission(requestPath, userRoles)
        .doOnNext(hasAccess ->
            log.debug("Reactive permission check result for path {}: {}", requestPath, hasAccess))
        .onErrorReturn(false); // 에러 발생 시 접근 거부
  }

  /**
   * 관리자 권한 확인
   */
  public Mono<Boolean> hasAdminPermission(Collection<String> userRoles) {
    return Mono.just(userRoles.contains("ROLE_ADMIN"))
        .doOnNext(isAdmin ->
            log.debug("Admin permission check result: {}", isAdmin));
  }

  /**
   * 사용자 권한 확인
   */
  public Mono<Boolean> hasUserPermission(Collection<String> userRoles) {
    return Mono.just(userRoles.contains("ROLE_USER") || userRoles.contains("ROLE_ADMIN"))
        .doOnNext(hasUserAccess ->
            log.debug("User permission check result: {}", hasUserAccess));
  }

  /**
   * 본인 리소스 접근 권한 확인
   */
  public Mono<Boolean> canAccessOwnResource(String authenticatedUsername, String targetUsername) {
    boolean canAccess = authenticatedUsername != null && authenticatedUsername.equals(targetUsername);

    log.debug("Own resource access check: {} accessing {}: {}",
        authenticatedUsername, targetUsername, canAccess);

    return Mono.just(canAccess);
  }

  /**
   * 복합 권한 확인 (관리자이거나 본인)
   */
  public Mono<Boolean> canAccessUserResource(
      Collection<String> userRoles,
      String authenticatedUsername,
      String targetUsername) {

    return hasAdminPermission(userRoles)
        .flatMap(isAdmin -> {
          if (isAdmin) {
            return Mono.just(true);
          }
          return canAccessOwnResource(authenticatedUsername, targetUsername);
        })
        .doOnNext(canAccess ->
            log.debug("User resource access check result: {}", canAccess));
  }
}