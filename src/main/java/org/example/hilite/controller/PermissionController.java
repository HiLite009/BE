package org.example.hilite.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "권한 관리", description = "동적 권한 관리 API")
public class PermissionController {

  private final RolePagePermissionService rolePagePermissionService;

  @PostMapping
  @Operation(summary = "권한 추가", description = "역할에 페이지 접근 권한을 추가합니다.")
  public ResponseEntity<RolePagePermissionResponseDto> addPermission(
      @RequestBody RolePagePermissionRequestDto request) {
    RolePagePermissionResponseDto response = rolePagePermissionService.createPermission(request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{permissionId}")
  @Operation(summary = "권한 삭제", description = "특정 권한을 삭제합니다.")
  public ResponseEntity<Void> removePermission(@PathVariable Long permissionId) {
    rolePagePermissionService.deletePermission(permissionId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(summary = "모든 권한 조회", description = "모든 권한을 조회합니다.")
  public ResponseEntity<List<RolePagePermissionResponseDto>> getAllPermissions() {
    List<RolePagePermissionResponseDto> permissions = rolePagePermissionService.getAllPermissions();
    return ResponseEntity.ok(permissions);
  }

  @GetMapping("/by-role")
  @Operation(summary = "역할별 권한 조회", description = "특정 역할의 권한을 조회합니다.")
  public ResponseEntity<List<RolePagePermissionResponseDto>> getPermissionsByRole(
      @RequestParam String roleName) {
    List<RolePagePermissionResponseDto> permissions =
        rolePagePermissionService.getPermissionsByRole(roleName);
    return ResponseEntity.ok(permissions);
  }
}