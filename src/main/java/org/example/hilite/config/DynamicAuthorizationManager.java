package org.example.hilite.config;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.service.DynamicPermissionService;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicAuthorizationManager
    implements AuthorizationManager<RequestAuthorizationContext> {

  private final DynamicPermissionService dynamicPermissionService;

  @Override
  public AuthorizationDecision check(
      Supplier<Authentication> authentication, RequestAuthorizationContext context) {

    Authentication auth = authentication.get();

    // 인증되지 않은 사용자
    if (auth == null || !auth.isAuthenticated()) {
      log.debug("Authentication is null or not authenticated");
      return new AuthorizationDecision(false);
    }

    String requestPath = context.getRequest().getRequestURI();

    // 사용자의 권한 목록 추출 (ROLE_ 프리픽스 제거)
    Collection<String> userRoles =
        auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

    log.info(
        "Checking authorization for user: {} with roles: {} for path: {}",
        auth.getName(),
        userRoles,
        requestPath);

    boolean hasPermission = dynamicPermissionService.hasPermission(requestPath, userRoles);

    log.info("Authorization decision for {}: {}", requestPath, hasPermission);
    return new AuthorizationDecision(hasPermission);
  }
}
