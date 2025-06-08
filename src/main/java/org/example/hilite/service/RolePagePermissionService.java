package org.example.hilite.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class RolePagePermissionService {

  private final RolePagePermissionRepository rolePagePermissionRepository;
  private final RoleRepository roleRepository;
  private final AccessPageRepository accessPageRepository;

  @Transactional
  public RolePagePermissionResponseDto createPermission(RolePagePermissionRequestDto requestDto) {
    // 역할과 페이지 조회
    Role role =
        roleRepository
            .findById(requestDto.roleId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 역할입니다."));

    AccessPage accessPage =
        accessPageRepository
            .findById(requestDto.pageId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 페이지입니다."));

    // 중복 권한 체크
    rolePagePermissionRepository
        .findByRoleIdAndPageId(requestDto.roleId(), requestDto.pageId())
        .ifPresent(
            existing -> {
              throw new IllegalArgumentException("이미 존재하는 권한입니다.");
            });

    // 권한 생성
    RolePagePermission permission = new RolePagePermission();
    permission.setRole(role);
    permission.setAccessPage(accessPage);

    RolePagePermission saved = rolePagePermissionRepository.save(permission);

    return new RolePagePermissionResponseDto(
        saved.getId(),
        role.getId(),
        role.getName(),
        accessPage.getId(),
        accessPage.getPath(),
        true);
  }

  @Transactional
  public void deletePermission(Long permissionId) {
    RolePagePermission permission =
        rolePagePermissionRepository
            .findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 권한입니다."));

    rolePagePermissionRepository.delete(permission);
  }

  @Transactional(readOnly = true)
  public List<RolePagePermissionResponseDto> getPermissionsByRole(String roleName) {
    return rolePagePermissionRepository.findByRoleNameWithDetails(roleName).stream()
        .map(this::toResponseDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<RolePagePermissionResponseDto> getAllPermissions() {
    return rolePagePermissionRepository.findAll().stream().map(this::toResponseDto).toList();
  }

  private RolePagePermissionResponseDto toResponseDto(RolePagePermission permission) {
    return new RolePagePermissionResponseDto(
        permission.getId(),
        permission.getRole().getId(),
        permission.getRole().getName(),
        permission.getAccessPage().getId(),
        permission.getAccessPage().getPath(),
        true);
  }
}
