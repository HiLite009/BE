package org.example.hilite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.dto.reqeust.RolePagePermissionRequestDto;
import org.example.hilite.dto.response.RolePagePermissionResponseDto;
import org.example.hilite.entity.AccessPage;
import org.example.hilite.entity.Role;
import org.example.hilite.entity.RolePagePermission;
import org.example.hilite.repository.AccessPageRepository;
import org.example.hilite.repository.RolePagePermissionRepository;
import org.example.hilite.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RolePagePermissionService {

  private final RolePagePermissionRepository rolePagePermissionRepository;
  private final RoleRepository roleRepository;
  private final AccessPageRepository accessPageRepository;

  /**
   * 권한 생성
   */
  @Transactional
  public Mono<RolePagePermissionResponseDto> createPermission(
      RolePagePermissionRequestDto requestDto) {
    log.debug("Creating permission for role {} and page {}", requestDto.roleId(),
        requestDto.pageId());

    // 역할 조회
    return roleRepository.findById(requestDto.roleId())
        .switchIfEmpty(
            Mono.error(new IllegalArgumentException("존재하지 않는 역할입니다: " + requestDto.roleId())))
        .flatMap(role ->
            // 페이지 조회
            accessPageRepository.findById(requestDto.pageId())
                .switchIfEmpty(Mono.error(
                    new IllegalArgumentException("존재하지 않는 페이지입니다: " + requestDto.pageId())))
                .flatMap(accessPage ->
                    // 중복 권한 체크
                    rolePagePermissionRepository.existsByRoleIdAndAccessPageId(
                            requestDto.roleId(), requestDto.pageId())
                        .flatMap(exists -> {
                          if (exists) {
                            return Mono.error(
                                new IllegalArgumentException("이미 존재하는 권한입니다."));
                          }

                          // 권한 생성
                          RolePagePermission permission = new RolePagePermission(
                              requestDto.roleId(), requestDto.pageId());

                          return rolePagePermissionRepository.save(permission)
                              .map(saved -> createResponseDto(saved, role, accessPage));
                        })))
        .doOnSuccess(dto ->
            log.info("Successfully created permission: role={}, page={}",
                dto.roleName(), dto.path()))
        .doOnError(error ->
            log.error("Failed to create permission for role {} and page {}: {}",
                requestDto.roleId(), requestDto.pageId(), error.getMessage()));
  }

  /**
   * 권한 삭제
   */
  @Transactional
  public Mono<Void> deletePermission(Long permissionId) {
    log.debug("Deleting permission with id: {}", permissionId);

    return rolePagePermissionRepository.findById(permissionId)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 권한입니다: " + permissionId)))
        .flatMap(permission -> {
          return roleRepository.findById(permission.getRoleId())
              .flatMap(role ->
                  accessPageRepository.findById(permission.getAccessPageId())
                      .flatMap(page -> {
                        log.info("Deleting permission: role={}, page={}",
                            role.getName(), page.getPath());

                        return rolePagePermissionRepository.delete(permission);
                      }));
        })
        .doOnSuccess(v -> log.info("Successfully deleted permission with id: {}", permissionId))
        .doOnError(error -> log.error("Failed to delete permission {}: {}", permissionId,
            error.getMessage()));
  }

  /**
   * 특정 역할의 권한 목록 조회
   */
  public Flux<RolePagePermissionResponseDto> getPermissionsByRole(String roleName) {
    log.debug("Getting permissions for role: {}", roleName);

    return roleRepository.findByName(roleName)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 역할입니다: " + roleName)))
        .flatMapMany(role ->
            rolePagePermissionRepository.findByRoleId(role.getId())
                .flatMap(permission ->
                    accessPageRepository.findById(permission.getAccessPageId())
                        .map(page -> createResponseDto(permission, role, page))))
        .doOnNext(dto -> log.debug("Retrieved permission: {}", dto.path()))
        .doOnError(error -> log.error("Failed to get permissions for role {}: {}", roleName,
            error.getMessage()));
  }

  /**
   * 모든 권한 조회
   */
  public Flux<RolePagePermissionResponseDto> getAllPermissions() {
    log.debug("Getting all permissions");

    return rolePagePermissionRepository.findAll()
        .flatMap(permission ->
            roleRepository.findById(permission.getRoleId())
                .flatMap(role ->
                    accessPageRepository.findById(permission.getAccessPageId())
                        .map(page -> createResponseDto(permission, role, page))))
        .doOnNext(dto -> log.debug("Retrieved permission: role={}, page={}",
            dto.roleName(), dto.path()))
        .doOnError(error -> log.error("Failed to get all permissions: {}", error.getMessage()));
  }

  /**
   * 특정 페이지의 권한 목록 조회
   */
  public Flux<RolePagePermissionResponseDto> getPermissionsByPage(String pagePath) {
    log.debug("Getting permissions for page: {}", pagePath);

    return accessPageRepository.findByPath(pagePath)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 페이지입니다: " + pagePath)))
        .flatMapMany(page ->
            rolePagePermissionRepository.findByAccessPageId(page.getId())
                .flatMap(permission ->
                    roleRepository.findById(permission.getRoleId())
                        .map(role -> createResponseDto(permission, role, page))))
        .doOnNext(dto -> log.debug("Retrieved permission: {}", dto.roleName()))
        .doOnError(error -> log.error("Failed to get permissions for page {}: {}", pagePath,
            error.getMessage()));
  }

  /**
   * 역할별 권한 일괄 설정
   */
  @Transactional
  public Mono<Void> setPermissionsForRole(String roleName, java.util.List<String> pagePaths) {
    log.debug("Setting permissions for role {} with pages: {}", roleName, pagePaths);

    return roleRepository.findByName(roleName)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 역할입니다: " + roleName)))
        .flatMap(role -> {
          // 기존 권한 모두 삭제
          return rolePagePermissionRepository.deleteByRoleId(role.getId())
              .then(Flux.fromIterable(pagePaths)
                  .flatMap(pagePath ->
                      accessPageRepository.findByPath(pagePath)
                          .switchIfEmpty(Mono.error(
                              new IllegalArgumentException("존재하지 않는 페이지입니다: " + pagePath)))
                          .flatMap(page -> {
                            RolePagePermission permission = new RolePagePermission(role.getId(),
                                page.getId());
                            return rolePagePermissionRepository.save(permission);
                          }))
                  .then());
        })
        .doOnSuccess(v -> log.info("Successfully set permissions for role: {}", roleName))
        .doOnError(error -> log.error("Failed to set permissions for role {}: {}", roleName,
            error.getMessage()));
  }

  /**
   * 역할과 페이지 유효성 검사 및 조회 (사용하지 않음)
   */
  // 더 이상 사용하지 않는 메서드 제거

  /**
   * ResponseDto 생성
   */
  private RolePagePermissionResponseDto createResponseDto(
      RolePagePermission permission, Role role, AccessPage page) {
    return new RolePagePermissionResponseDto(
        permission.getId(),
        role.getId(),
        role.getName(),
        page.getId(),
        page.getPath(),
        true // R2DBC에서는 항상 true (권한이 존재한다는 의미)
    );
  }
}