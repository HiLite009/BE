package org.example.hilite.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.entity.RolePagePermission;
import org.example.hilite.repository.RolePagePermissionRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicPermissionService {

  private final RolePagePermissionRepository rolePagePermissionRepository;

  public boolean hasPermission(String requestPath, Collection<String> userRoles) {
    List<String> roleNames = new ArrayList<>(userRoles);

    log.debug("Checking permission for path: {} with roles: {}", requestPath, roleNames);

    // 정확한 경로 매칭 먼저 확인
    List<RolePagePermission> exactMatch =
        rolePagePermissionRepository.findByRoleNamesAndPath(roleNames, requestPath);

    if (!exactMatch.isEmpty()) {
      log.debug("Exact path match found for: {}", requestPath);
      return true;
    }

    // 패턴 매칭 (/** 형태)
    List<RolePagePermission> allPermissions =
        rolePagePermissionRepository.findByRoleNames(roleNames);

    boolean hasAccess =
        allPermissions.stream()
            .anyMatch(permission -> pathMatches(permission.getAccessPage().getPath(), requestPath));

    log.debug("Pattern matching result for path {}: {}", requestPath, hasAccess);
    return hasAccess;
  }

  private boolean pathMatches(String pattern, String path) {
    // 간단한 패턴 매칭 (Ant 스타일)
    if (pattern.endsWith("/**")) {
      String prefix = pattern.substring(0, pattern.length() - 3);
      return path.startsWith(prefix);
    } else if (pattern.endsWith("/*")) {
      String prefix = pattern.substring(0, pattern.length() - 2);
      return path.startsWith(prefix) && !path.substring(prefix.length()).contains("/");
    }
    return pattern.equals(path);
  }
}
