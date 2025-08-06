package org.example.hilite.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.common.exception.ApiErrorResponse;
import org.example.hilite.dto.reqeust.RolePagePermissionRequestDto;
import org.example.hilite.dto.response.RolePagePermissionResponseDto;
import org.example.hilite.service.RolePagePermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Slf4j
@Tag(name = "권한 관리", description = "동적 권한 관리 API")
public class PermissionController {

  private final RolePagePermissionService rolePagePermissionService;

  /**
   * 권한 추가
   */
  @PostMapping
  @Operation(summary = "권한 추가", description = "역할에 페이지 접근 권한을 추가합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "권한 추가 성공",
          content = {
              @Content(schema = @Schema(implementation = RolePagePermissionResponseDto.class))}),
      @ApiResponse(responseCode = "409", description = "이미 존재하는 권한",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))}),
      @ApiResponse(responseCode = "404", description = "역할 또는 페이지를 찾을 수 없음",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
  })
  public Mono<ResponseEntity<RolePagePermissionResponseDto>> addPermission(
      @RequestBody RolePagePermissionRequestDto request,
      ServerWebExchange exchange) {

    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(java.security.Principal::getName)
        .flatMap(adminUsername -> {
          log.debug("Admin {} adding permission: roleId={}, pageId={}",
              adminUsername, request.roleId(), request.pageId());

          return rolePagePermissionService.createPermission(request)
              .map(ResponseEntity::ok);
        })
        .doOnSuccess(response ->
            log.info("Successfully added permission: roleId={}, pageId={}",
                request.roleId(), request.pageId()))
        .doOnError(error ->
            log.error("Error adding permission: {}", error.getMessage()));
  }

  /**
   * 권한 삭제
   */
  @DeleteMapping("/{permissionId}")
  @Operation(summary = "권한 삭제", description = "특정 권한을 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "권한 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "권한을 찾을 수 없음",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
  })
  public Mono<ResponseEntity<Void>> removePermission(
      @PathVariable Long permissionId,
      ServerWebExchange exchange) {

    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(java.security.Principal::getName)
        .flatMap(adminUsername -> {
          log.debug("Admin {} removing permission with id: {}", adminUsername, permissionId);

          return rolePagePermissionService.deletePermission(permissionId)
              .then(Mono.just(ResponseEntity.noContent().<Void>build()));
        })
        .doOnSuccess(response ->
            log.info("Successfully removed permission with id: {}", permissionId))
        .doOnError(error ->
            log.error("Error removing permission {}: {}", permissionId, error.getMessage()));
  }

  /**
   * 모든 권한 조회
   */
  @GetMapping
  @Operation(summary = "모든 권한 조회", description = "모든 권한을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "권한 목록 조회 성공")
  })
  public Flux<RolePagePermissionResponseDto> getAllPermissions(ServerWebExchange exchange) {
    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(java.security.Principal::getName)
        .flatMapMany(adminUsername -> {
          log.debug("Admin {} requesting all permissions", adminUsername);

          return rolePagePermissionService.getAllPermissions()
              .doOnNext(permission ->
                  log.debug("Retrieved permission: role={}, page={}",
                      permission.roleName(), permission.path()));
        })
        .doOnError(error ->
            log.error("Error getting all permissions: {}", error.getMessage()));
  }

  /**
   * 역할별 권한 조회
   */
  @GetMapping("/by-role")
  @Operation(summary = "역할별 권한 조회", description = "특정 역할의 권한을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "역할별 권한 조회 성공"),
      @ApiResponse(responseCode = "404", description = "역할을 찾을 수 없음",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
  })
  public Flux<RolePagePermissionResponseDto> getPermissionsByRole(
      @RequestParam String roleName,
      ServerWebExchange exchange) {

    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(java.security.Principal::getName)
        .flatMapMany(adminUsername -> {
          log.debug("Admin {} requesting permissions for role: {}", adminUsername, roleName);

          return rolePagePermissionService.getPermissionsByRole(roleName)
              .doOnNext(permission ->
                  log.debug("Retrieved permission for role {}: {}", roleName, permission.path()));
        })
        .doOnError(error ->
            log.error("Error getting permissions for role {}: {}", roleName, error.getMessage()));
  }

  /**
   * 페이지별 권한 조회
   */
  @GetMapping("/by-page")
  @Operation(summary = "페이지별 권한 조회", description = "특정 페이지의 권한을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "페이지별 권한 조회 성공"),
      @ApiResponse(responseCode = "404", description = "페이지를 찾을 수 없음",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
  })
  public Flux<RolePagePermissionResponseDto> getPermissionsByPage(
      @RequestParam String pagePath,
      ServerWebExchange exchange) {

    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(java.security.Principal::getName)
        .flatMapMany(adminUsername -> {
          log.debug("Admin {} requesting permissions for page: {}", adminUsername, pagePath);

          return rolePagePermissionService.getPermissionsByPage(pagePath)
              .doOnNext(permission ->
                  log.debug("Retrieved permission for page {}: {}", pagePath,
                      permission.roleName()));
        })
        .doOnError(error ->
            log.error("Error getting permissions for page {}: {}", pagePath, error.getMessage()));
  }

  /**
   * 역할별 권한 일괄 설정
   */
  @PostMapping("/batch-set")
  @Operation(summary = "역할별 권한 일괄 설정", description = "특정 역할의 모든 권한을 일괄 설정합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "권한 일괄 설정 성공"),
      @ApiResponse(responseCode = "404", description = "역할 또는 페이지를 찾을 수 없음",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
  })
  public Mono<ResponseEntity<String>> setPermissionsForRole(
      @RequestParam String roleName,
      @RequestBody java.util.List<String> pagePaths,
      ServerWebExchange exchange) {

    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(java.security.Principal::getName)
        .flatMap(adminUsername -> {
          log.debug("Admin {} setting permissions for role {} with pages: {}",
              adminUsername, roleName, pagePaths);

          return rolePagePermissionService.setPermissionsForRole(roleName, pagePaths)
              .then(Mono.just(ResponseEntity.ok("권한이 성공적으로 설정되었습니다.")));
        })
        .doOnSuccess(response ->
            log.info("Successfully set permissions for role: {}", roleName))
        .doOnError(error ->
            log.error("Error setting permissions for role {}: {}", roleName, error.getMessage()));
  }
}