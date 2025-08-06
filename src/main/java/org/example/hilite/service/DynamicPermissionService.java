package org.example.hilite.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.entity.AccessPage;
import org.example.hilite.repository.AccessPageRepository;
import org.example.hilite.repository.RolePagePermissionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicPermissionService {

  private final RolePagePermissionRepository rolePagePermissionRepository;
  private final AccessPageRepository accessPageRepository;

  /**
   * 요청 경로와 사용자 역할로 권한 확인 (리액티브 방식)
   */
  public Mono<Boolean> hasPermission(String requestPath, Collection<String> userRoles) {
    List<String> roleNames = new ArrayList<>(userRoles);

    log.debug("Checking permission for path: {} with roles: {}", requestPath, roleNames);

    if (roleNames.isEmpty()) {
      log.debug("No roles provided, denying access");
      return Mono.just(false);
    }

    // 정확한 경로 매칭 먼저 확인
    return checkExactPathMatch(requestPath, roleNames)
        .flatMap(hasExactMatch -> {
          if (hasExactMatch) {
            log.debug("Exact path match found for: {}", requestPath);
            return Mono.just(true);
          }

          // 패턴 매칭 확인
          return checkPatternMatch(requestPath, roleNames);
        })
        .doOnNext(hasAccess ->
            log.debug("Permission check result for path {}: {}", requestPath, hasAccess))
        .onErrorReturn(false);
  }

  /**
   * 정확한 경로 매칭 확인
   */
  private Mono<Boolean> checkExactPathMatch(String requestPath, List<String> roleNames) {
    return rolePagePermissionRepository.findByRoleNamesAndPath(roleNames, requestPath)
        .hasElements()
        .doOnNext(hasMatch -> {
          if (hasMatch) {
            log.debug("Found exact path match for: {}", requestPath);
          }
        });
  }

  /**
   * 패턴 매칭 확인 (/** 형태)
   */
  private Mono<Boolean> checkPatternMatch(String requestPath, List<String> roleNames) {
    return rolePagePermissionRepository.findByRoleNames(roleNames)
        .flatMap(permission ->
            accessPageRepository.findById(permission.getAccessPageId())
                .map(AccessPage::getPath)
                .filter(pattern -> pathMatches(pattern, requestPath))
                .hasElement())
        .any(Boolean::booleanValue)
        .doOnNext(hasMatch -> {
          if (hasMatch) {
            log.debug("Found pattern match for: {}", requestPath);
          }
        });
  }

  /**
   * 경로 패턴 매칭 로직
   */
  private boolean pathMatches(String pattern, String path) {
    // 간단한 패턴 매칭 (Ant 스타일)
    if (pattern.endsWith("/**")) {
      String prefix = pattern.substring(0, pattern.length() - 3);
      boolean matches = path.startsWith(prefix);
      log.debug("Pattern {} matches path {}: {}", pattern, path, matches);
      return matches;
    } else if (pattern.endsWith("/*")) {
      String prefix = pattern.substring(0, pattern.length() - 2);
      boolean matches = path.startsWith(prefix) && !path.substring(prefix.length()).contains("/");
      log.debug("Pattern {} matches path {}: {}", pattern, path, matches);
      return matches;
    }

    boolean matches = pattern.equals(path);
    log.debug("Exact pattern {} matches path {}: {}", pattern, path, matches);
    return matches;
  }

  /**
   * 최적화된 권한 체크 (단일 쿼리 사용)
   */
  public Mono<Boolean> hasPermissionOptimized(String requestPath, Collection<String> userRoles) {
    List<String> roleNames = new ArrayList<>(userRoles);

    if (roleNames.isEmpty()) {
      return Mono.just(false);
    }

    // 와일드카드 패턴 생성
    String pathPattern = requestPath + "%";

    return rolePagePermissionRepository.hasPermissionForPath(roleNames, requestPath, pathPattern)
        .doOnNext(hasAccess ->
            log.debug("Optimized permission check for path {}: {}", requestPath, hasAccess))
        .onErrorReturn(false);
  }
}